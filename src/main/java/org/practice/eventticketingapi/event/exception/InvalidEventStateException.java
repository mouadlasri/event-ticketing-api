package org.practice.eventticketingapi.event.exception;

import org.practice.eventticketingapi.exception.InvalidResourceStateException;

public class InvalidEventStateException extends InvalidResourceStateException {
    public InvalidEventStateException(String message) {
        super(message);
    }
}
