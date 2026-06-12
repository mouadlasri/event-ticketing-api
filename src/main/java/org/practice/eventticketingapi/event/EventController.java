package org.practice.eventticketingapi.event;

import jakarta.validation.Valid;
import org.hibernate.sql.Update;
import org.practice.eventticketingapi.event.dto.CreateEventRequest;
import org.practice.eventticketingapi.event.dto.EventResponse;
import org.practice.eventticketingapi.event.dto.UpdateEventRequest;
import org.practice.eventticketingapi.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<Page<EventResponse>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EventResponse> events = eventService.getAllEvents(pageable);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable UUID eventId) {
        EventResponse eventResponse = eventService.getEventById(eventId);

        return ResponseEntity.ok(eventResponse);
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest createEventRequest,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        EventResponse createdEventResponse = eventService.createEvent(createEventRequest, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdEventResponse);
    }

    @PatchMapping("/{eventId}/publish")
    public ResponseEntity<EventResponse> publishEvent(@PathVariable UUID eventId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        EventResponse publishedEventResponse = eventService.publishEvent(eventId, user.getId());

        return ResponseEntity.ok(publishedEventResponse);
    }

    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<EventResponse> cancelEvent(@PathVariable UUID eventId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        EventResponse canceledEventResponse = eventService.cancelEvent(eventId, user.getId());

        return ResponseEntity.ok(canceledEventResponse);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdateEventRequest updateEventRequest,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        EventResponse updatedEventResponse = eventService.updateEvent(eventId, updateEventRequest, user.getId());

        return ResponseEntity.ok(updatedEventResponse);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        eventService.deleteEvent(eventId, user.getId());

        return ResponseEntity.noContent().build();
    }

}

