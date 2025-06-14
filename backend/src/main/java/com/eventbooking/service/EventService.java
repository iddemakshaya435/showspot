package com.eventbooking.service;

import com.eventbooking.entity.Event;
import com.eventbooking.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public List<Event> getAllUpcomingEvents() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now());
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    public List<Event> searchEvents(String query) {
        return eventRepository.findByNameContainingIgnoreCase(query);
    }

    public Event createEvent(Event event) {
        event.setAvailableTickets(event.getTotalTickets());
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, Event eventDetails) {
        Event event = getEventById(id);
        
        event.setName(eventDetails.getName());
        event.setDescription(eventDetails.getDescription());
        event.setEventDate(eventDetails.getEventDate());
        event.setVenue(eventDetails.getVenue());
        event.setLocation(eventDetails.getLocation());
        event.setTicketPrice(eventDetails.getTicketPrice());
        event.setTotalTickets(eventDetails.getTotalTickets());
        event.setImageUrl(eventDetails.getImageUrl());
        event.setStatus(eventDetails.getStatus());
        event.setUpdatedAt(LocalDateTime.now());
        
        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        Event event = getEventById(id);
        eventRepository.delete(event);
    }

    public boolean isEventAvailable(Long eventId, int requestedTickets) {
        Event event = getEventById(eventId);
        return event.getAvailableTickets() >= requestedTickets;
    }

    public void updateAvailableTickets(Long eventId, int ticketsBooked) {
        Event event = getEventById(eventId);
        event.setAvailableTickets(event.getAvailableTickets() - ticketsBooked);
        eventRepository.save(event);
    }
}