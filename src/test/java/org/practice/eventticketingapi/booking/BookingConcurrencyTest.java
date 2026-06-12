package org.practice.eventticketingapi.booking;

import org.junit.jupiter.api.Test;
import org.practice.eventticketingapi.booking.exception.EventSoldOutException;
import org.practice.eventticketingapi.event.Event;
import org.practice.eventticketingapi.event.EventRepository;
import org.practice.eventticketingapi.event.EventService;
import org.practice.eventticketingapi.event.dto.CreateEventRequest;
import org.practice.eventticketingapi.event.dto.EventResponse;
import org.practice.eventticketingapi.user.UserService;
import org.practice.eventticketingapi.user.dto.CreateUserRequest;
import org.practice.eventticketingapi.user.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Test
    void concurrentBookingsDoNotOversellLastTicket() throws Exception {
        String suffix = UUID.randomUUID().toString();

        UserResponse organizer = userService.createUser(new CreateUserRequest(
                "Organizer",
                "organizer-" + suffix + "@example.com",
                "password123"
        ));
        UserResponse firstUser = userService.createUser(new CreateUserRequest(
                "First User",
                "first-" + suffix + "@example.com",
                "password123"
        ));
        UserResponse secondUser = userService.createUser(new CreateUserRequest(
                "Second User",
                "second-" + suffix + "@example.com",
                "password123"
        ));

        EventResponse createdEvent = eventService.createEvent(
                new CreateEventRequest("One Ticket Event " + suffix, 1),
                organizer.getId()
        );
        EventResponse publishedEvent = eventService.publishEvent(createdEvent.getId(), organizer.getId());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> firstBooking = bookingAttempt(publishedEvent.getId(), firstUser.getId(), ready, start);
        Callable<Boolean> secondBooking = bookingAttempt(publishedEvent.getId(), secondUser.getId(), ready, start);

        try {
            Future<Boolean> firstResult = executor.submit(firstBooking);
            Future<Boolean> secondResult = executor.submit(secondBooking);

            ready.await();
            start.countDown();

            long successCount = Stream.of(firstResult.get(), secondResult.get())
                    .filter(Boolean::booleanValue)
                    .count();

            assertThat(successCount).isEqualTo(1);

            Event event = eventRepository.findById(publishedEvent.getId()).orElseThrow();
            assertThat(event.getAvailableTickets()).isEqualTo(0);

            long confirmedBookings = bookingRepository.countByEvent_IdAndStatus(
                    publishedEvent.getId(),
                    BookingStatus.CONFIRMED
            );
            assertThat(confirmedBookings).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<Boolean> bookingAttempt(
            UUID eventId,
            UUID userId,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        return () -> {
            ready.countDown();
            start.await();

            try {
                bookingService.create(eventId, userId);
                return true;
            } catch (EventSoldOutException exception) {
                return false;
            }
        };
    }
}
