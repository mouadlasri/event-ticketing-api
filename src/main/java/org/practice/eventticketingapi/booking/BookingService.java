package org.practice.eventticketingapi.booking;

import org.practice.eventticketingapi.booking.dto.BookingResponse;
import org.practice.eventticketingapi.booking.exception.BookingAlreadyExistsException;
import org.practice.eventticketingapi.booking.exception.BookingNotFoundException;
import org.practice.eventticketingapi.booking.exception.EventSoldOutException;
import org.practice.eventticketingapi.booking.exception.InvalidBookingStateException;
import org.practice.eventticketingapi.event.Event;
import org.practice.eventticketingapi.event.EventService;
import org.practice.eventticketingapi.user.User;
import org.practice.eventticketingapi.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final EventService eventService;
    private final UserService userService;


    public BookingService(BookingRepository bookingRepository, EventService eventService, UserService userService) {
        this.bookingRepository = bookingRepository;
        this.eventService = eventService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findByIdAndUserIdWithEvent(bookingId, userId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        return toBookingResponse(booking, booking.getEvent());
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookings(UUID userId, Pageable pageable) {
        Page<Booking> bookingPage = bookingRepository.findAllByUserIdWithEvent(userId, pageable);

        Page<BookingResponse> bookingResponsePage = bookingPage.map(booking -> toBookingResponse(booking, booking.getEvent()));

        return bookingResponsePage;
    }

    @Transactional
    public BookingResponse create(UUID eventId, UUID userId) {
        // check if event exists (and published)
        Event event = eventService.getPublishedEventEntityById(eventId);

        // check if user already booked this event or not
        if (bookingRepository.existsConfirmedBookingByUserIdAndEventId(userId, eventId)) {
            throw new BookingAlreadyExistsException("This event was already booked");
        }

        // check if there are available seats in this event
        if (event.getAvailableTickets() < 1) {
            throw new EventSoldOutException(event.getId());
        }

        User user = userService.getUserEntityById(userId);

        Booking booking = new Booking(user, event);

        event.setAvailableTickets(event.getAvailableTickets() - 1);
        Booking newBooking = bookingRepository.save(booking);

        return toBookingResponse(newBooking, event);
    }

    @Transactional
    public BookingResponse cancel(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findByIdAndUserIdWithEvent(bookingId, userId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus().equals(BookingStatus.CANCELLED)) {
            throw new InvalidBookingStateException("You can only cancel active bookings");
        }

        booking.getEvent().setAvailableTickets(booking.getEvent().getAvailableTickets() + 1);
        booking.cancel();

        return toBookingResponse(booking, booking.getEvent());
    }

    private BookingResponse toBookingResponse(Booking booking, Event event) {
        return new BookingResponse(
                booking.getId(),
                event.getId(),
                event.getName(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getCancelledAt()
        );
    }
}
