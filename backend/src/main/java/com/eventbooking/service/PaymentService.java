package com.eventbooking.service;

import com.eventbooking.dto.PaymentRequest;
import com.eventbooking.entity.Booking;
import com.eventbooking.entity.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Autowired
    private BookingService bookingService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public Map<String, String> createPaymentIntent(PaymentRequest paymentRequest, User user) {
        try {
            Booking booking = bookingService.getBookingById(paymentRequest.getBookingId(), user);
            
            Map<String, Object> params = new HashMap<>();
            params.put("amount", paymentRequest.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue()); // Convert to cents
            params.put("currency", "usd");
            params.put("payment_method", paymentRequest.getPaymentMethodId());
            params.put("confirmation_method", "manual");
            params.put("confirm", true);
            params.put("return_url", "http://localhost:3000/payment-success");
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("booking_id", booking.getId().toString());
            metadata.put("user_id", user.getId().toString());
            params.put("metadata", metadata);

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Update booking with payment intent ID
            booking.setPaymentIntentId(paymentIntent.getId());
            bookingService.confirmBooking(booking.getId());

            Map<String, String> response = new HashMap<>();
            response.put("client_secret", paymentIntent.getClientSecret());
            response.put("status", paymentIntent.getStatus());
            
            return response;
        } catch (StripeException e) {
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }

    public Map<String, Object> confirmPayment(String paymentIntentId, User user) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", paymentIntent.getStatus());
            response.put("payment_intent", paymentIntent);
            
            if ("succeeded".equals(paymentIntent.getStatus())) {
                // Payment successful, confirm booking
                Booking booking = bookingService.getBookingByPaymentIntentId(paymentIntentId);
                bookingService.confirmBooking(booking.getId());
            }
            
            return response;
        } catch (StripeException e) {
            throw new RuntimeException("Payment confirmation failed: " + e.getMessage());
        }
    }

    public void handleStripeWebhook(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, "your_webhook_secret");
            
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                    if (paymentIntent != null) {
                        handleSuccessfulPayment(paymentIntent);
                    }
                    break;
                case "payment_intent.payment_failed":
                    // Handle failed payment
                    break;
                default:
                    System.out.println("Unhandled event type: " + event.getType());
            }
        } catch (Exception e) {
            throw new RuntimeException("Webhook handling failed: " + e.getMessage());
        }
    }

    private void handleSuccessfulPayment(PaymentIntent paymentIntent) {
        String bookingId = paymentIntent.getMetadata().get("booking_id");
        if (bookingId != null) {
            bookingService.confirmBooking(Long.parseLong(bookingId));
        }
    }
}