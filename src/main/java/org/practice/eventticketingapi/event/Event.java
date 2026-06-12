package org.practice.eventticketingapi.event;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.practice.eventticketingapi.user.User;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(name = "available_tickets")
    private int availableTickets;

    @Column(name = "total_tickets")
    private int totalTickets;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(name = "created_at")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public Event() {}

    public Event(String name, User organizer, int totalTickets) {
        this.name = name;
        this.organizer = organizer;
        this.status = EventStatus.DRAFT;
        this.availableTickets = totalTickets;
        this.totalTickets = totalTickets;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EventStatus getStatus() {
        return status;
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public User getOrganizer() {
        return organizer;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public void setAvailableTickets(int availableTickets) {
        this.availableTickets = availableTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
