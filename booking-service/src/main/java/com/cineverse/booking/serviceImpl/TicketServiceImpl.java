package com.cineverse.booking.serviceImpl;

import com.cineverse.booking.dto.CreateTicketRequest;
import com.cineverse.booking.dto.TicketResponse;
import com.cineverse.booking.dto.UpdateTicketRequest;
import com.cineverse.booking.entity.Booking;
import com.cineverse.booking.entity.Ticket;
import com.cineverse.booking.enums.TicketStatus;
import com.cineverse.booking.exception.BookingNotFoundException;
import com.cineverse.booking.exception.TicketNotFoundException;
import com.cineverse.booking.repository.BookingRepository;
import com.cineverse.booking.repository.TicketRepository;
import com.cineverse.booking.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;

    @Override
    public TicketResponse createTicket(
            UUID bookingId,
            CreateTicketRequest request) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(bookingId)
                );

        Ticket ticket = Ticket.builder()
                .booking(booking)
                .ticketNumber(request.getTicketNumber())
                .qrCode(request.getQrCode())
                .status(TicketStatus.ISSUED)
                .build();

        return mapToResponse(
                ticketRepository.save(ticket)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicket(UUID ticketId) {

        return mapToResponse(findTicket(ticketId));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketByBooking(
            UUID bookingId) {

        Ticket ticket = ticketRepository
                .findByBookingBookingId(bookingId)
                .orElseThrow(() ->
                        new TicketNotFoundException(
                                "Ticket not found for booking: "
                                        + bookingId
                        )
                );

        return mapToResponse(ticket);
    }

    @Override
    public TicketResponse updateTicket(
            UUID ticketId,
            UpdateTicketRequest request) {

        Ticket ticket = findTicket(ticketId);

        ticket.setQrCode(request.getQrCode());
        ticket.setStatus(request.getStatus());

        return mapToResponse(
                ticketRepository.save(ticket)
        );
    }

    @Override
    public void deleteTicket(UUID ticketId) {

        ticketRepository.delete(findTicket(ticketId));
    }

    private Ticket findTicket(UUID ticketId) {

        return ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new TicketNotFoundException(ticketId)
                );
    }

    private TicketResponse mapToResponse(Ticket ticket) {

        return TicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .bookingId(
                        ticket.getBooking().getBookingId()
                )
                .ticketNumber(ticket.getTicketNumber())
                .issuedAt(ticket.getIssuedAt())
                .qrCode(ticket.getQrCode())
                .status(ticket.getStatus())
                .build();
    }
}
