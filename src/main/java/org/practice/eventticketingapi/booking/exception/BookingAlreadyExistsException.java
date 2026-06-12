package org.practice.eventticketingapi.booking.exception;

import org.practice.eventticketingapi.exception.ResourceAlreadyExistsException;

public class BookingAlreadyExistsException extends ResourceAlreadyExistsException {
    public BookingAlreadyExistsException(String message) {
        super(message);
    }
}
