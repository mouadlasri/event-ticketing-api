package org.practice.eventticketingapi.booking.exception;

import org.practice.eventticketingapi.exception.InvalidResourceStateException;

import java.util.UUID;

public class EventSoldOutException extends InvalidResourceStateException {
    public EventSoldOutException(UUID eventId) {
        super("Event fully booked with id " + eventId);
    }
}
