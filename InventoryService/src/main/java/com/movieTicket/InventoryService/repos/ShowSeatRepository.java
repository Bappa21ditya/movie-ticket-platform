package com.movieTicket.InventoryService.repos;

import com.movieTicket.InventoryService.entity.ShowSeat;
import com.movieTicket.InventoryService.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT s
       FROM ShowSeat s
       WHERE s.showSeatId = :showSeatId
       """)

    Optional<ShowSeat> findByIdForUpdate(
            @Param("showSeatId") Long showSeatId);

    List<ShowSeat> findByShowId(Long showId);

    List<ShowSeat> findByShowIdAndStatus(
            Long showId,
            SeatStatus status);

    @Modifying
    @Query("""
    UPDATE ShowSeat s
    SET s.status = 'HELD'
    WHERE s.id = :seatId
      AND s.status = 'AVAILABLE'
""")
    int holdSeat(@Param("seatId") Long seatId);
}
