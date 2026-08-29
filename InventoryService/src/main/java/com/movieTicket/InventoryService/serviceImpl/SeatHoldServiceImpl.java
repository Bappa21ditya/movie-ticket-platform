package com.movieTicket.InventoryService.serviceImpl;
import com.movieTicket.InventoryService.dtos.CreateSeatHoldRequest;
import com.movieTicket.InventoryService.dtos.SeatHoldResponse;
import com.movieTicket.InventoryService.entity.SeatHold;
import com.movieTicket.InventoryService.entity.ShowSeat;
import com.movieTicket.InventoryService.enums.HoldStatus;
import com.movieTicket.InventoryService.enums.SeatStatus;
import com.movieTicket.InventoryService.exceptions.ResourceNotFoundException;
import com.movieTicket.InventoryService.exceptions.SeatUnavailableException;
import com.movieTicket.InventoryService.exceptions.ShowSeatNotFoundException;
import com.movieTicket.InventoryService.mapper.SeatHoldMapper;
import com.movieTicket.InventoryService.repos.SeatHoldRepository;
import com.movieTicket.InventoryService.repos.ShowSeatRepository;
import com.movieTicket.InventoryService.services.SeatHoldService;
import com.movieTicket.InventoryService.services.ShowSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatHoldServiceImpl implements SeatHoldService {

    private final SeatHoldRepository seatHoldRepository;
    private final SeatHoldMapper seatHoldMapper;
    private final ShowSeatRepository showSeatRepository;
    private final ShowSeatService showSeatService;


    // ============================================================
    // PESSIMISTIC LOCKING
    // ============================================================
    //
    // Concurrency strategy:
    //     SELECT ... FOR UPDATE
    //
    // The database locks the selected ShowSeat row.
    //
    // Example:
    //
    // User A → SELECT FOR UPDATE → gets row lock
    // User B → SELECT FOR UPDATE → waits
    //
    // Once User A commits:
    //
    // User A → lock released
    // User B → continues
    //
    // Important:
    // The database row is locked only for the duration
    // of the transaction.
    //
    // We should NOT keep this transaction open while calling
    // external systems such as Payment Service.
    //
    // ============================================================

    @Override
    public SeatHoldResponse createHold(
            CreateSeatHoldRequest request) {

        // 1. HOLD THE SEAT
        // ============================================================
        //
        // ShowSeatService is responsible for the actual concurrency
        // control:
        //
        //     Redis lock
        //          ↓
        //     PostgreSQL atomic UPDATE
        //          ↓
        //     AVAILABLE → HELD
        //
        // If Redis is unavailable, ShowSeatService falls back
        // directly to PostgreSQL.
        //
        boolean held =
                showSeatService.holdSeat(
                        request.getShowSeatId());

        // If the atomic UPDATE affected 0 rows,
        // the seat was not AVAILABLE.
        if (!held) {

            throw new SeatUnavailableException(
                    "Seat is not available");
        }


        // ============================================================
        // 2. CREATE SEAT HOLD
        // ============================================================
        //
        // At this point:
        //
        //     ShowSeat = HELD
        //
        // Now create the corresponding SeatHold record.
        //
        // This operation is part of the same database transaction
        // because createHold() is @Transactional.
        //
        SeatHold hold =
                seatHoldMapper.toEntity(request);

        SeatHold savedHold =
                seatHoldRepository.save(hold);


        // ============================================================
        // 3. TRANSACTION RESULT
        // ============================================================
        //
        // If everything succeeds:
        //
        //     COMMIT
        //       ↓
        //     ShowSeat = HELD
        //     SeatHold = CREATED
        //
        // If SeatHold creation fails:
        //
        //     ROLLBACK
        //       ↓
        //     ShowSeat = AVAILABLE
        //     SeatHold = NOT CREATED
        //
        // Redis lock itself is already released by
        // ShowSeatService after the PostgreSQL operation.
        //
        return seatHoldMapper.toResponse(savedHold);
    }


    // ============================================================
    // READ OPERATIONS
    // ============================================================

    @Transactional
    public void releaseHold(Long showSeatId, UUID bookingId) {

        SeatHold hold = seatHoldRepository
                .findByShowSeatIdAndBookingId(showSeatId, bookingId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Seat hold not found"
                        ));

        if (hold.getStatus() != HoldStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Seat hold is not active"
            );
        }

        // Release the inventory seat
        boolean released =
                releaseSeat(showSeatId);

        if (!released) {
            throw new IllegalStateException(
                    "Unable to release seat"
            );
        }

        // Release the hold
        hold.setStatus(HoldStatus.RELEASED);

        seatHoldRepository.save(hold);
    }

    @Override
    @Transactional(readOnly = true)
    public SeatHoldResponse getHold(Long holdId) {

        SeatHold hold =
                seatHoldRepository.findById(holdId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "SeatHold not found : " + holdId));

        return seatHoldMapper.toResponse(hold);
    }


    @Override
    @Transactional(readOnly = true)
    public List<SeatHoldResponse> getHoldsByBooking(
            Long bookingId) {

        return seatHoldRepository
                .findByBookingId(bookingId)
                .stream()
                .map(seatHoldMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<SeatHoldResponse> getHoldsByUser(
            Long userId) {

        return seatHoldRepository
                .findByUserId(userId)
                .stream()
                .map(seatHoldMapper::toResponse)
                .toList();
    }


    @Transactional
    public void confirmSeat(Long showSeatId, UUID bookingId) {

        ShowSeat showSeat = showSeatRepository.findById(showSeatId)
                .orElseThrow(() ->
                        new ShowSeatNotFoundException(showSeatId)
                );

        if (showSeat.getStatus() != SeatStatus.HELD) {
            throw new IllegalStateException("Seat is not held");
        }

        SeatHold seatHold = seatHoldRepository
                .findByShowSeatIdAndBookingId(showSeatId, bookingId)
                .orElseThrow(() ->
                        new IllegalStateException("No hold found for this booking")
                );

        if (seatHold.getStatus() != HoldStatus.ACTIVE) {
            throw new IllegalStateException("Hold is not active");
        }

        if (seatHold.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalStateException("Seat hold has expired");
        }

        showSeat.setStatus(SeatStatus.BOOKED);

        seatHold.setStatus(HoldStatus.CONFIRMED);
    }

    public boolean releaseSeat(Long showSeatId) {

        int updated =
                showSeatRepository.releaseSeat(showSeatId);
        return updated == 1;
    }
}
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class SeatHoldServiceImpl  implements SeatHoldService {
//
//    private final SeatHoldRepository seatHoldRepository;
//    private final SeatHoldMapper seatHoldMapper;
//    private final ShowSeatRepository showSeatRepository;
//
//
//    // pressimstic lock
//    @Override
//    public SeatHoldResponse createHold(
//            CreateSeatHoldRequest request) {
//
////        ShowSeat showSeat = showSeatRepository.findById(request.getShowSeatId())
////                .orElseThrow(() ->
////                        new ResourceNotFoundException(
////                                "Show seat not found: " + request.getShowSeatId()));
//
//        ShowSeat showSeat = showSeatRepository
//                .findByIdForUpdate(request.getShowSeatId())
//                .orElseThrow(() ->
//                        new ResourceNotFoundException(
//                                "Show seat not found"));
//
//        if (showSeat.getStatus() != SeatStatus.AVAILABLE) {
//            throw new SeatUnavailableException(
//                    "Seat is not available");
//        }
//
//        showSeat.setStatus(SeatStatus.HELD);
//
//        showSeatRepository.save(showSeat);
//
//        SeatHold hold = seatHoldMapper.toEntity(request);
//
//        SeatHold savedHold =
//                seatHoldRepository.save(hold);
//
//        return seatHoldMapper.toResponse(savedHold);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public SeatHoldResponse getHold(Long holdId) {
//
//        SeatHold hold = seatHoldRepository.findById(holdId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException(
//                                "SeatHold not found : " + holdId));
//
//        return seatHoldMapper.toResponse(hold);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<SeatHoldResponse> getHoldsByBooking(
//            Long bookingId) {
//
//        return seatHoldRepository.findByBookingId(bookingId)
//                .stream()
//                .map(seatHoldMapper::toResponse)
//                .toList();
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<SeatHoldResponse> getHoldsByUser(
//            Long userId) {
//
//        return seatHoldRepository.findByUserId(userId)
//                .stream()
//                .map(seatHoldMapper::toResponse)
//                .toList();
//    }
//}
