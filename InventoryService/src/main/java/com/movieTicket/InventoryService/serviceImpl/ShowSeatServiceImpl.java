package com.movieTicket.InventoryService.serviceImpl;
import com.movieTicket.InventoryService.dtos.CreateShowSeatRequest;
import com.movieTicket.InventoryService.dtos.ShowSeatResponse;
import com.movieTicket.InventoryService.entity.ShowSeat;
import com.movieTicket.InventoryService.enums.SeatStatus;
import com.movieTicket.InventoryService.exceptions.ResourceNotFoundException;
import com.movieTicket.InventoryService.mapper.ShowSeatMapper;
import com.movieTicket.InventoryService.repos.ShowSeatRepository;
import com.movieTicket.InventoryService.services.ShowSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ShowSeatServiceImpl implements ShowSeatService {


    private final ShowSeatRepository showSeatRepository;

    private final ShowSeatMapper showSeatMapper;

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

        ShowSeat showSeat = showSeatRepository.findById(showSeatId)
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


}
