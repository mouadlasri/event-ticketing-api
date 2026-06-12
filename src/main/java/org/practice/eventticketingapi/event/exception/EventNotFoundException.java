package org.practice.eventticketingapi.event.exception;

import org.practice.eventticketingapi.exception.ResourceNotFoundException;

import java.util.UUID;

public class EventNotFoundException extends ResourceNotFoundException {
    public EventNotFoundException(UUID eventId) {
        super("Event not found with id: " + eventId);
    }
}
