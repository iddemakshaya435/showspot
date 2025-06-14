package com.eventbooking.controller;

import com.eventbooking.entity.Ticket;
import com.eventbooking.entity.User;
import com.eventbooking.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<Ticket>> getTicketsByBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal User user) {
        List<Ticket> tickets = ticketService.getTicketsByBooking(bookingId, user);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{ticketNumber}")
    public ResponseEntity<Ticket> getTicketByNumber(
            @PathVariable String ticketNumber,
            @AuthenticationPrincipal User user) {
        Ticket ticket = ticketService.getTicketByNumber(ticketNumber, user);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/{ticketNumber}/download")
    public ResponseEntity<Resource> downloadTicket(
            @PathVariable String ticketNumber,
            @AuthenticationPrincipal User user) {
        Resource resource = ticketService.generateTicketPDF(ticketNumber, user);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"ticket-" + ticketNumber + ".pdf\"")
                .body(resource);
    }

    @PostMapping("/{ticketNumber}/validate")
    public ResponseEntity<String> validateTicket(@PathVariable String ticketNumber) {
        boolean isValid = ticketService.validateTicket(ticketNumber);
        return ResponseEntity.ok(isValid ? "Valid ticket" : "Invalid ticket");
    }

    @PostMapping("/{ticketNumber}/use")
    public ResponseEntity<String> useTicket(@PathVariable String ticketNumber) {
        ticketService.markTicketAsUsed(ticketNumber);
        return ResponseEntity.ok("Ticket marked as used");
    }
}