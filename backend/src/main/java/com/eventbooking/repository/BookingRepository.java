package com.eventbooking.repository;

import com.eventbooking.entity.Booking;
import com.eventbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    Optional<Booking> findByBookingReference(String bookingReference);
    Optional<Booking> findByPaymentIntentId(String paymentIntentId);
}