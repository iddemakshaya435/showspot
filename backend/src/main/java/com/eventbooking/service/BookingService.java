package com.eventbooking.service;

import com.eventbooking.dto.BookingRequest;
import com.eventbooking.entity.Booking;
import com.eventbooking.entity.Event;
import com.eventbooking.entity.User;
import com.eventbooking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private TicketService ticketService;

    @Transactional
    public Booking createBooking(BookingRequest bookingRequest, User user) {
        Event event = eventService.getEventById(bookingRequest.getEventId());
        
        // Check if enough tickets are available
        if (!eventService.isEventAvailable(bookingRequest.getEventId(), bookingRequest.getNumberOfTickets())) {
            throw new RuntimeException("Not enough tickets available");
        }

        // Calculate total amount
        BigDecimal totalAmount = event.getTicketPrice()
                .multiply(BigDecimal.valueOf(bookingRequest.getNumberOfTickets()));

        // Create booking
        String bookingReference = generateBookingReference();
        Booking booking = new Booking(bookingReference, user, event, 
                                    bookingRequest.getNumberOfTickets(), totalAmount);
        
        booking = bookingRepository.save(booking);

        // Update available tickets
        eventService.updateAvailableTickets(bookingRequest.getEventId(), 
                                          bookingRequest.getNumberOfTickets());

        return booking;
    }

    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Booking getBookingById(Long id, User user) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        return booking;
    }

    public Booking getBookingByReference(String reference, User user) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        return booking;
    }

    @Transactional
    public Booking cancelBooking(Long id, User user) {
        Booking booking = getBookingById(id, user);
        
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Cannot cancel booking with status: " + booking.getStatus());
        }
        
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        
        // Return tickets to available pool
        eventService.updateAvailableTickets(booking.getEvent().getId(), 
                                          -booking.getNumberOfTickets());
        
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);
        
        // Generate tickets
        ticketService.generateTicketsForBooking(booking);
        
        return booking;
    }

    public Booking getBookingByPaymentIntentId(String paymentIntentId) {
        return bookingRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Booking not found for payment intent: " + paymentIntentId));
    }

    private String generateBookingReference() {
        return "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}