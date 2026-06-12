package org.practice.eventticketingapi.event;

import org.practice.eventticketingapi.event.dto.CreateEventRequest;
import org.practice.eventticketingapi.event.dto.EventResponse;
import org.practice.eventticketingapi.event.dto.UpdateEventRequest;
import org.practice.eventticketingapi.event.exception.EventForbiddenException;
import org.practice.eventticketingapi.event.exception.EventNotFoundException;
import org.practice.eventticketingapi.event.exception.InvalidEventStateException;
import org.practice.eventticketingapi.user.User;
import org.practice.eventticketingapi.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserService userService;

    public EventService(EventRepository eventRepository, UserService userService) {
        this.eventRepository = eventRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(UUID eventId) {
        Event event = findEventById(eventId, EventStatus.PUBLISHED);

        return toEventResponse(event);
    }

    @Transactional(readOnly = true)
    public Event getPublishedEventEntityById(UUID eventId) {
        return eventRepository.findByIdAndStatusAndDeletedAtIsNull(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    @Transactional
    public Event getPublishedEventEntityByIdForUpdate(UUID eventId) {
        return eventRepository.findByIdAndStatusAndDeletedAtIsNullForUpdate(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    @Transactional
    public Event getEventEntityByIdForUpdate(UUID eventId) {
        return eventRepository.findByIdAndDeletedAtIsNullForUpdate(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        Page<Event> eventPage = eventRepository.findAllByStatusWithOrganizer(EventStatus.PUBLISHED, pageable);

        Page<EventResponse> eventResponsePage = eventPage
                .map(event -> toEventResponse(event));

        return eventResponsePage;
    }

    @Transactional
    public EventResponse createEvent(CreateEventRequest createEventRequest, UUID authUserId) {
        User organizer = userService.getUserEntityById(authUserId);

        Event event = new Event(
                createEventRequest.getName(),
                organizer,
                createEventRequest.getTotalTickets()
        );

        Event created = eventRepository.save(event);

        return toEventResponse(created);
    }

    @Transactional
    public EventResponse updateEvent(UUID eventId, UpdateEventRequest updateEventRequest, UUID authUserId) {
        // check existence of event and event ownership
        Event event = eventRepository.findByIdWithOrganizer(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getOrganizer().getId().equals(authUserId)) {
            throw new EventForbiddenException("You don't own this event");
        }

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new InvalidEventStateException("Only draft events can be updated");
        }

        event.setName(updateEventRequest.getName());
        int newTotalTickets = updateEventRequest.getTotalTickets();
        int newAvailableTickets = event.getAvailableTickets() + (newTotalTickets - event.getTotalTickets());

        if (newAvailableTickets < 0) {
            throw new InvalidEventStateException("Total tickets cannot be less than already booked tickets");
        }
        event.setTotalTickets(newTotalTickets);
        event.setAvailableTickets(newAvailableTickets);
        return toEventResponse(event);
    }

    @Transactional
    public EventResponse publishEvent(UUID eventId, UUID authUserId) {
        // Load event, verify organizer ownership, then apply the status transition
        Event event = eventRepository.findByIdWithOrganizer(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getOrganizer().getId().equals(authUserId)) {
            throw new EventForbiddenException("You don't own this event");
        }

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new InvalidEventStateException("Only draft events can be published");
        }

        event.setStatus(EventStatus.PUBLISHED);

        return toEventResponse(event);
    }

    @Transactional
    public void deleteEvent(UUID eventId, UUID authUserId) {
        Event event = eventRepository.findByIdWithOrganizer(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        // only event organizer can delete the event, and only DRAFT events can be deleted
        if (!event.getOrganizer().getId().equals(authUserId)) {
            throw new EventForbiddenException("You don't own this event");
        }

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new InvalidEventStateException("Only draft events can be deleted");
        }

        event.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Transactional
    public EventResponse cancelEvent(UUID eventId, UUID authUserId) {
        Event event = eventRepository.findByIdWithOrganizer(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        // only event organizer can delete the event, and only DRAFT events can be deleted
        if (!event.getOrganizer().getId().equals(authUserId)) {
            throw new EventForbiddenException("You don't own this event");
        }

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new InvalidEventStateException("Event is already cancelled");
        }

        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.PUBLISHED) {
            throw new InvalidEventStateException("Only draft or published events can be cancelled");
        }

        event.setStatus(EventStatus.CANCELLED);

        return toEventResponse(event);
    }


    private Event findEventById(UUID eventId, EventStatus status) {
        return eventRepository.findByIdAndStatusWithOrganizer(eventId, status)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    private EventResponse toEventResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getOrganizer().getId(),
                event.getOrganizer().getName(),
                event.getAvailableTickets(),
                event.getTotalTickets(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
