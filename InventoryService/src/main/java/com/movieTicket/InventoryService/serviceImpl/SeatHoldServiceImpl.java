package com.movieTicket.InventoryService.serviceImpl;
import com.movieTicket.InventoryService.dtos.CreateSeatHoldRequest;
import com.movieTicket.InventoryService.dtos.SeatHoldResponse;
import com.movieTicket.InventoryService.entity.SeatHold;
import com.movieTicket.InventoryService.exceptions.ResourceNotFoundException;
import com.movieTicket.InventoryService.mapper.SeatHoldMapper;
import com.movieTicket.InventoryService.repos.SeatHoldRepository;
import com.movieTicket.InventoryService.services.SeatHoldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatHoldServiceImpl  implements SeatHoldService {

    private final SeatHoldRepository seatHoldRepository;
    private final SeatHoldMapper seatHoldMapper;

    @Override
    public SeatHoldResponse createHold(
            CreateSeatHoldRequest request) {

        SeatHold hold = seatHoldMapper.toEntity(request);

        SeatHold savedHold =
                seatHoldRepository.save(hold);

        return seatHoldMapper.toResponse(savedHold);
    }

    @Override
    @Transactional(readOnly = true)
    public SeatHoldResponse getHold(Long holdId) {

        SeatHold hold = seatHoldRepository.findById(holdId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "SeatHold not found : " + holdId));

        return seatHoldMapper.toResponse(hold);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatHoldResponse> getHoldsByBooking(
            Long bookingId) {

        return seatHoldRepository.findByBookingId(bookingId)
                .stream()
                .map(seatHoldMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatHoldResponse> getHoldsByUser(
            Long userId) {

        return seatHoldRepository.findByUserId(userId)
                .stream()
                .map(seatHoldMapper::toResponse)
                .toList();
    }
}
