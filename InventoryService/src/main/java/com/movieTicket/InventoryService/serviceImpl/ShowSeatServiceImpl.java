package com.movieTicket.InventoryService.serviceImpl;
import com.movieTicket.InventoryService.dtos.CreateSeatHoldRequest;
import com.movieTicket.InventoryService.dtos.CreateShowSeatRequest;
import com.movieTicket.InventoryService.dtos.SeatHoldResponse;
import com.movieTicket.InventoryService.dtos.ShowSeatResponse;
import com.movieTicket.InventoryService.entity.SeatHold;
import com.movieTicket.InventoryService.entity.ShowSeat;
import com.movieTicket.InventoryService.enums.SeatStatus;
import com.movieTicket.InventoryService.exceptions.ResourceNotFoundException;
import com.movieTicket.InventoryService.mapper.SeatHoldMapper;
import com.movieTicket.InventoryService.mapper.ShowSeatMapper;
import com.movieTicket.InventoryService.reddis.SeatLock;
import com.movieTicket.InventoryService.repos.ShowSeatRepository;
import com.movieTicket.InventoryService.services.ShowSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
@Service
@RequiredArgsConstructor
@Transactional
public class ShowSeatServiceImpl implements ShowSeatService {

    private final ShowSeatRepository showSeatRepository;
    private final ShowSeatMapper showSeatMapper;
    private final SeatLock redisSeatLock;



    // ============================================================
    // NORMAL CRUD OPERATIONS
    // ============================================================

    @Override
    public ShowSeatResponse createShowSeat(
            CreateShowSeatRequest request) {

        ShowSeat showSeat = showSeatMapper.toEntity(request);

        ShowSeat savedShowSeat =
                showSeatRepository.save(showSeat);

        return showSeatMapper.toResponse(savedShowSeat);
    }


    @Override
    @Transactional(readOnly = true)
    public ShowSeatResponse getShowSeat(Long showSeatId) {

        ShowSeat showSeat =
                showSeatRepository.findById(showSeatId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "ShowSeat not found : " + showSeatId));

        return showSeatMapper.toResponse(showSeat);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ShowSeatResponse> getShowSeatsByShow(Long showId) {

        return showSeatRepository.findByShowId(showId)
                .stream()
                .map(showSeatMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<ShowSeatResponse> getAvailableSeats(Long showId) {

        return showSeatRepository
                .findByShowIdAndStatus(
                        showId,
                        SeatStatus.AVAILABLE)
                .stream()
                .map(showSeatMapper::toResponse)
                .toList();
    }


    // ============================================================
    // ATOMIC CONDITIONAL UPDATE
    // ============================================================
    //
    // Concurrency strategy:
    //     Database-level atomic state transition
    //
    // SQL:
    //
    // UPDATE show_seat
    // SET status = 'HELD'
    // WHERE id = ?
    // AND status = 'AVAILABLE';
    //
    // Important:
    // We DON'T do:
    //
    //     SELECT → check AVAILABLE → UPDATE
    //
    // Instead, PostgreSQL performs the condition + update
    // atomically in a single SQL statement.
    //
    // Return value:
    //     1 → this request successfully acquired the seat
    //     0 → another transaction already changed the seat
    //
    // This is useful for very short state transitions such as:
    //
    //     AVAILABLE → HELD
    //
    // ============================================================

//    @Override
//    @Transactional
//    public boolean holdSeat(Long seatId) {
//
//        int updatedRows =
//                showSeatRepository.holdSeat(seatId);
//
//        // Exactly one row means:
//        // the seat was AVAILABLE and we successfully changed it to HELD.
//        if (updatedRows == 1) {
//            return true;
//        }
//
//        // Zero rows means:
//        // the WHERE condition was not satisfied.
//        // Most commonly, another user already acquired the seat.
//        return false;
//    }

    // before reddis
//@Override
//@Transactional
//public boolean holdSeat(Long showSeatId) {
//    int updatedRows = showSeatRepository.holdSeat(showSeatId);
//
//    // Exactly 1 row means:
//    // The show seat was AVAILABLE and successfully changed to HELD.
//    return updatedRows == 1;
//}

    // reddis
    @Override
    @Transactional
    public boolean holdSeat(Long showSeatId) {
        String token = UUID.randomUUID().toString();

        boolean acquired =
                redisSeatLock.tryLock(showSeatId, token);

        System.out.println(
                Thread.currentThread().getName()
                        + " | token=" + token
                        + " | lock=" + acquired
        );

        if (!acquired) {
            return false;
        }

        try {

            int updatedRows =
                    showSeatRepository.holdSeat(showSeatId);

            System.out.println(
                    Thread.currentThread().getName()
                            + " | token=" + token
                            + " | updatedRows=" + updatedRows
            );

            return updatedRows == 1;

        } finally {

            redisSeatLock.unlock(showSeatId, token);

            System.out.println(
                    Thread.currentThread().getName()
                            + " | token=" + token
                            + " | lock released"
            );
        }
    }
}

//@Service
//@RequiredArgsConstructor
//@Transactional
//public class ShowSeatServiceImpl implements ShowSeatService {
//
//
//    private final ShowSeatRepository showSeatRepository;
//
//    private final ShowSeatMapper showSeatMapper;
//
//    private final CyclicBarrier barrier =
//            new CyclicBarrier(2);
//
//    @Override
//    public ShowSeatResponse createShowSeat(
//            CreateShowSeatRequest request) {
//
//        ShowSeat showSeat = showSeatMapper.toEntity(request);
//
//        ShowSeat savedShowSeat =
//                showSeatRepository.save(showSeat);
//
//        return showSeatMapper.toResponse(savedShowSeat);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public ShowSeatResponse getShowSeat(Long showSeatId) {
//
//        ShowSeat showSeat = showSeatRepository.findById(showSeatId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException(
//                                "ShowSeat not found : " + showSeatId));
//
//        return showSeatMapper.toResponse(showSeat);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ShowSeatResponse> getShowSeatsByShow(Long showId) {
//
//        return showSeatRepository.findByShowId(showId)
//                .stream()
//                .map(showSeatMapper::toResponse)
//                .toList();
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ShowSeatResponse> getAvailableSeats(Long showId) {
//
//        return showSeatRepository
//                .findByShowIdAndStatus(
//                        showId,
//                        SeatStatus.AVAILABLE)
//                .stream()
//                .map(showSeatMapper::toResponse)
//                .toList();
//    }
//    //Atomic Update
//    @Transactional
//    public boolean holdSeat(Long seatId) {
//
//        int updatedRows = showSeatRepository.holdSeat(seatId);
//
//        return updatedRows == 1;
//    }
//
//    @Transactional
//    public void holdSeatTest(Long seatId) throws BrokenBarrierException, InterruptedException {
//
//        ShowSeat seat = showSeatRepository.findById(seatId)
//                .orElseThrow();
//
////        System.out.println(
////                Thread.currentThread().getName()
////                        + " read version = "
////                        + seat.getVersion()
////        );
//
//        seat.setStatus(SeatStatus.HELD);
//
//        barrier.await();
//
//
//        showSeatRepository.save(seat);
//    }
//
//
//
//
//}
