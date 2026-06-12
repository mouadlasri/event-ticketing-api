package org.practice.eventticketingapi.booking.dto;

import org.practice.eventticketingapi.booking.BookingStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public class BookingResponse {
    private UUID id;
    private UUID eventId;
    private String eventName;
    private BookingStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime cancelledAt;

    public BookingResponse(UUID id, UUID eventId, String eventName, BookingStatus status, OffsetDateTime createdAt, OffsetDateTime cancelledAt) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.status = status;
        this.createdAt = createdAt;
        this.cancelledAt = cancelledAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
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
}
