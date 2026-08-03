package com.movieTicket.InventoryService.serviceImpl;
import com.movieTicket.InventoryService.dtos.CreateSeatRequest;
import com.movieTicket.InventoryService.dtos.SeatResponse;
import com.movieTicket.InventoryService.dtos.UpdateSeatRequest;
import com.movieTicket.InventoryService.entity.Seat;
import com.movieTicket.InventoryService.exceptions.ResourceNotFoundException;
import com.movieTicket.InventoryService.mapper.SeatMapper;
import com.movieTicket.InventoryService.repos.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.movieTicket.InventoryService.services.SeatService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatServiceImpl implements SeatService {
    private final SeatRepository seatRepository;
    private final SeatMapper seatMapper;

    @Override
    public SeatResponse createSeat(CreateSeatRequest request) {

        Seat seat = seatMapper.toEntity(request);

        Seat savedSeat = seatRepository.save(seat);

        return seatMapper.toResponse(savedSeat);
    }

    @Override
    @Transactional(readOnly = true)
    public SeatResponse getSeat(Long seatId) {

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Seat not found : " + seatId));

        return seatMapper.toResponse(seat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByScreen(Long screenId) {

        return seatRepository.findByScreenId(screenId)
                .stream()
                .map(seatMapper::toResponse)
                .toList();
    }

    @Override
    public SeatResponse updateSeat(
            Long seatId,
            UpdateSeatRequest request) {

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Seat not found : " + seatId));

        seatMapper.updateFromRequest(request, seat);

        Seat updatedSeat = seatRepository.save(seat);

        return seatMapper.toResponse(updatedSeat);
    }

    @Override
    public void deleteSeat(Long seatId) {

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Seat not found : " + seatId));

        seatRepository.delete(seat);
    }
}
