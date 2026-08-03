package com.movieTicket.InventoryService.repos;

import com.movieTicket.InventoryService.entity.ShowSeat;
import com.movieTicket.InventoryService.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat,Long> {
    List<ShowSeat> findByShowId(Long showId);

    List<ShowSeat> findByShowIdAndStatus(
            Long showId,
            SeatStatus status);
}
