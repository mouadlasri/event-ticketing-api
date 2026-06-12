package org.practice.eventticketingapi.booking.exception;

import org.practice.eventticketingapi.exception.InvalidResourceStateException;

public class InvalidBookingStateException extends InvalidResourceStateException {
    public InvalidBookingStateException(String message) {
        super(message);
    }
}
