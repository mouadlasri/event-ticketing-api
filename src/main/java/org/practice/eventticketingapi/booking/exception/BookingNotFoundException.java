package org.practice.eventticketingapi.booking.exception;

import org.practice.eventticketingapi.exception.ResourceNotFoundException;

import java.util.UUID;

public class BookingNotFoundException extends ResourceNotFoundException {
    public BookingNotFoundException(UUID bookingId) {
        super("Booking not found with id: " + bookingId);
    }
}
