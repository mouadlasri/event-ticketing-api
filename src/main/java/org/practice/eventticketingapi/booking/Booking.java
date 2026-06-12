package org.practice.eventticketingapi.booking;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.practice.eventticketingapi.event.Event;
import org.practice.eventticketingapi.user.User;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    public Booking() {}

    public Booking(User user, Event event) {
        this.user = user;
        this.event = event;
        this.status = BookingStatus.CONFIRMED;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Event getEvent() {
        return event;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = OffsetDateTime.now();
    }
}
