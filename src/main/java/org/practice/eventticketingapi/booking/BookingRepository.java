package org.practice.eventticketingapi.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.user.id = :userId
        AND b.event.id = :eventId
        AND b.status = org.practice.eventticketingapi.booking.BookingStatus.CONFIRMED
    """)
    boolean existsConfirmedBookingByUserIdAndEventId(@Param("userId") UUID userId, @Param("eventId") UUID eventId);

    // priviliged/internal lookup (admin endpoint, NOT user-facing)
    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.event WHERE b.id = :bookingId")
    Optional<Booking> findByIdWithUserAndEvent(@Param("bookingId") UUID bookingId);

    // Find all bookings for a user with event loaded, paginated
    @Query(value = "SELECT b FROM Booking b JOIN FETCH b.event WHERE b.user.id = :userId",
            countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId")
    Page<Booking> findAllByUserIdWithEvent(@Param("userId") UUID userId, Pageable pageable);

    // user-facing method to return the booking details of the user sending the request
    @Query("SELECT b FROM Booking b JOIN FETCH b.event WHERE b.id = :bookingId AND b.user.id = :userId")
    Optional<Booking> findByIdAndUserIdWithEvent(@Param("bookingId") UUID bookingId, @Param("userId") UUID userId);

    // used for testing (can ignore for now)
    long countByEvent_IdAndStatus(UUID eventId, BookingStatus status);
}
