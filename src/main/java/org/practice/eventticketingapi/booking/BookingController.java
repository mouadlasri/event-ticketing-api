package org.practice.eventticketingapi.booking;

import org.practice.eventticketingapi.booking.dto.BookingResponse;
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
@RequestMapping("/api/v1")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/bookings")
    public ResponseEntity<Page<BookingResponse>> getAllBookings(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookingResponse> bookingResponsePage = bookingService.getAllBookings(user.getId(), pageable);

        return ResponseEntity.ok(bookingResponsePage);
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable UUID bookingId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        BookingResponse bookingResponse = bookingService.getBooking(bookingId, user.getId());

        return ResponseEntity.ok(bookingResponse);
    }


    @PostMapping("/events/{eventId}/bookings")
    public ResponseEntity<BookingResponse> createBooking(@PathVariable UUID eventId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        BookingResponse bookingResponse = bookingService.create(eventId, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(bookingResponse);
    }

    @PatchMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID bookingId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        BookingResponse bookingResponse = bookingService.cancel(bookingId, user.getId());

        return ResponseEntity.ok(bookingResponse);
    }
}
