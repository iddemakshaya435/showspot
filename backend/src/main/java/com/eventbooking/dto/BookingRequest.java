package com.eventbooking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class BookingRequest {
    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Number of tickets is required")
    @Positive(message = "Number of tickets must be positive")
    private Integer numberOfTickets;

    // Constructors
    public BookingRequest() {}

    public BookingRequest(Long eventId, Integer numberOfTickets) {
        this.eventId = eventId;
        this.numberOfTickets = numberOfTickets;
    }

    // Getters and Setters
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Integer getNumberOfTickets() { return numberOfTickets; }
    public void setNumberOfTickets(Integer numberOfTickets) { this.numberOfTickets = numberOfTickets; }
}