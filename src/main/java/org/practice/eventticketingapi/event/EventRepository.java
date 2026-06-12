package org.practice.eventticketingapi.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query(
        value = "SELECT e FROM Event e JOIN FETCH e.organizer WHERE e.status = :status AND e.deletedAt IS NULL",
        countQuery = "SELECT COUNT(e) FROM Event e WHERE e.status = :status AND e.deletedAt IS NULL"
    )
    Page<Event> findAllByStatusWithOrganizer(@Param("status") EventStatus status, Pageable pageable);
    @Query("SELECT e FROM Event e JOIN FETCH e.organizer WHERE e.id = :eventId AND e.deletedAt IS NULL AND e.status = :status")
    Optional<Event> findByIdAndStatusWithOrganizer(@Param("eventId") UUID eventId, @Param("status") EventStatus status);
    @Query("SELECT e FROM Event e JOIN FETCH e.organizer WHERE e.id = :eventId AND e.deletedAt IS NULL")
    Optional<Event> findByIdWithOrganizer(UUID eventId);
}
