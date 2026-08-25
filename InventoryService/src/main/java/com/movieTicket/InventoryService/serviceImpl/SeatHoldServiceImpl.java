package com.movieTicket.InventoryService.serviceImpl;
import com.movieTicket.InventoryService.dtos.CreateSeatHoldRequest;
import com.movieTicket.InventoryService.dtos.SeatHoldResponse;
import com.movieTicket.InventoryService.entity.SeatHold;
import com.movieTicket.InventoryService.entity.ShowSeat;
import com.movieTicket.InventoryService.enums.SeatStatus;
import com.movieTicket.InventoryService.exceptions.ResourceNotFoundException;
import com.movieTicket.InventoryService.exceptions.SeatUnavailableException;
import com.movieTicket.InventoryService.mapper.SeatHoldMapper;
import com.movieTicket.InventoryService.repos.SeatHoldRepository;
import com.movieTicket.InventoryService.repos.ShowSeatRepository;
import com.movieTicket.InventoryService.services.SeatHoldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatHoldServiceImpl implements SeatHoldService {

    private final SeatHoldRepository seatHoldRepository;
    private final SeatHoldMapper seatHoldMapper;
    private final ShowSeatRepository showSeatRepository;


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

        // SELECT ... FOR UPDATE
        //
        // This acquires a database row lock on the ShowSeat.
        ShowSeat showSeat =
                showSeatRepository
                        .findByIdForUpdate(
                                request.getShowSeatId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Show seat not found"));


        // Because we have the pessimistic lock,
        // another transaction cannot simultaneously acquire
        // this same row using the same locking strategy.
        //
        // Now we safely check the business condition.
        if (showSeat.getStatus() != SeatStatus.AVAILABLE) {

            throw new SeatUnavailableException(
                    "Seat is not available");
        }


        // Change the business state:
        //
        // AVAILABLE → HELD
        showSeat.setStatus(SeatStatus.HELD);


        // Save the changed ShowSeat.
        //
        // The row remains locked until the surrounding
        // @Transactional method commits.
        showSeatRepository.save(showSeat);


        // Create the SeatHold record.
        //
        // This and the ShowSeat update are part of
        // the SAME database transaction.
        SeatHold hold =
                seatHoldMapper.toEntity(request);

        SeatHold savedHold =
                seatHoldRepository.save(hold);


        // If everything succeeds:
        //
        // COMMIT
        //     ↓
        // ShowSeat = HELD
        // SeatHold = created
        // Row lock = released
        //
        // If something fails:
        //
        // ROLLBACK
        //     ↓
        // ShowSeat change is rolled back
        // SeatHold creation is rolled back
        // Row lock is released
        //
        return seatHoldMapper.toResponse(savedHold);
    }


    // ============================================================
    // READ OPERATIONS
    // ============================================================

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
