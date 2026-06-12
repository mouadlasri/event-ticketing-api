package org.practice.eventticketingapi.event.dto;

import org.practice.eventticketingapi.event.EventStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public class EventResponse {
    private UUID id;
    private String name;
    private UUID organizerId;
    private String organizerName;
    private EventStatus status;
    private int availableTickets;
    private int totalTickets;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public EventResponse(UUID id, String name, UUID organizerId, String organizerName, int availableTickets, int totalTickets, EventStatus status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.availableTickets = availableTickets;
        this.totalTickets = totalTickets;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getOrganizerId() {
        return organizerId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public EventStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
