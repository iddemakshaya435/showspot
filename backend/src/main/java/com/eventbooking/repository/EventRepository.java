package com.eventbooking.repository;

import com.eventbooking.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusAndEventDateAfter(Event.EventStatus status, LocalDateTime date);
    
    @Query("SELECT e FROM Event e WHERE e.status = 'ACTIVE' AND e.eventDate > :currentDate ORDER BY e.eventDate ASC")
    List<Event> findUpcomingEvents(LocalDateTime currentDate);
    
    List<Event> findByNameContainingIgnoreCase(String name);
}