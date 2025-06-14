package com.eventbooking.controller;

import com.eventbooking.dto.BookingRequest;
import com.eventbooking.entity.Booking;
import com.eventbooking.entity.User;
import com.eventbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody BookingRequest bookingRequest,
            @AuthenticationPrincipal User user) {
        Booking booking = bookingService.createBooking(bookingRequest, user);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getUserBookings(@AuthenticationPrincipal User user) {
        List<Booking> bookings = bookingService.getUserBookings(user);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        Booking booking = bookingService.getBookingById(id, user);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<Booking> getBookingByReference(
            @PathVariable String reference,
            @AuthenticationPrincipal User user) {
        Booking booking = bookingService.getBookingByReference(reference, user);
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        Booking booking = bookingService.cancelBooking(id, user);
        return ResponseEntity.ok(booking);
    }
}