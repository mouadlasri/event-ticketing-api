package org.practice.eventticketingapi.event.exception;

import org.practice.eventticketingapi.exception.ForbiddenException;

public class EventForbiddenException extends ForbiddenException {
    public EventForbiddenException(String message) {
        super(message);
    }
}
