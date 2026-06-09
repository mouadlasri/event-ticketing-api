

**Event Ticketing API**

Design Document

| Author | Mouad Lasri |
| :---- | :---- |
| **Version** | 1.0 |
| **Status** | Draft |
| **Created** | 2026-06-06 |
| **Last Updated** | 2026-06-06 |

# **1. Overview**

## **1.1 Purpose**

A REST API for managing events and ticket bookings. Organizers create and publish events, users book tickets. The core technical challenge is handling concurrent ticket bookings safely (optimistic/pessimistic locking).

## **1.2 Scope**

**In scope**: User registration/login (JWT), event CRUD, ticket booking with concurrency handling, pagination on list endpoints, integration tests.

**Out of scope**: Payment processing, email notifications, role-based access control (organizer vs attendee is determined by ownership, not roles).

## **1.3 Key Decisions & Assumptions**

- Soft deletes on User and Event (deletedAt timestamp). Booking uses status (CONFIRMED/CANCELLED) instead of soft delete.
- All timestamps are UTC.
- `available_tickets` counter on Event instead of `COUNT(*)` from bookings — faster under load, but requires locking to prevent overselling.
- Event status (DRAFT/PUBLISHED/CANCELLED) is organizer-controlled. Derived states (sold out, past event) are computed, not stored.
- One booking = one ticket. A user can only have one confirmed booking per event.
- Auth from day one — userId comes from JWT, never from request body.

# **2. Domain Model**

## **2.1 Entity Relationship Overview**

- A **User** can organize many **Events** (one-to-many).
- A **User** can have many **Bookings** (one-to-many).
- An **Event** can have many **Bookings** (one-to-many).
- **Booking** is the join between User and Event, but is a full domain because it carries its own state and lifecycle.

## **2.2 Entity Definitions**

**User**

| Field | Type | Nullable | Constraints | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | UUID | No | PK | Unique identifier |
| name | String | No | @NotBlank | User display name |
| email | String | No | Unique, @Email, @NotBlank | User email address |
| password | String | No | @NotBlank, Min 8 chars | BCrypt hashed password |
| createdAt | OffsetDateTime | No | @CreationTimestamp | Account creation timestamp |
| updatedAt | OffsetDateTime | Yes | Auto-update | Last modification timestamp |
| deletedAt | OffsetDateTime | Yes | Soft delete | Null if active |

**Event**

| Field | Type | Nullable | Constraints | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | UUID | No | PK | Unique identifier |
| name | String | No | @NotBlank | Event name |
| organizerId | UUID | No | FK → users | Event organizer |
| status | EVENT_STATUS | No | Default DRAFT | Event lifecycle status |
| availableTickets | Integer | No | Min(0) | Remaining bookable tickets |
| totalTickets | Integer | No | Min(1) | Total ticket capacity |
| createdAt | OffsetDateTime | No | @CreationTimestamp | Event creation timestamp |
| updatedAt | OffsetDateTime | Yes | Auto-update | Last modification timestamp |
| deletedAt | OffsetDateTime | Yes | Soft delete | Null if active |

**Booking**

| Field | Type | Nullable | Constraints | Description |
| :---- | :---- | :---- | :---- | :---- |
| id | UUID | No | PK | Unique identifier |
| userId | UUID | No | FK → users | User who booked |
| eventId | UUID | No | FK → events | Event that was booked |
| status | BOOKING_STATUS | No | Default CONFIRMED | Booking lifecycle status |
| createdAt | OffsetDateTime | No | @CreationTimestamp | Booking creation timestamp |
| cancelledAt | OffsetDateTime | Yes | Set when status → CANCELLED | Cancellation timestamp |

## **2.3 Enums**

**EVENT_STATUS:**
- DRAFT — created but not yet published
- PUBLISHED — open for ticket sales
- CANCELLED — event cancelled by organizer

Valid transitions: DRAFT → PUBLISHED, DRAFT → CANCELLED, PUBLISHED → CANCELLED. No transition back from CANCELLED. No transition from PUBLISHED → DRAFT.

**BOOKING_STATUS:**
- CONFIRMED — default on creation
- CANCELLED — user cancelled their booking

# **3. Data Transfer Objects (DTOs)**

## **3.1 Request DTOs**

**CreateUserRequest**

| Field | Type | Validation | Notes |
| :---- | :---- | :---- | :---- |
| name | String | @NotBlank | User display name |
| email | String | @NotBlank, @Email | Must be unique |
| password | String | @NotBlank, @Size(min=8, max=100) | Raw password, hashed before storage |

**LoginRequest**

| Field | Type | Validation | Notes |
| :---- | :---- | :---- | :---- |
| email | String | @NotBlank, @Email | |
| password | String | @NotBlank | |

**CreateEventRequest**

| Field | Type | Validation | Notes |
| :---- | :---- | :---- | :---- |
| name | String | @NotBlank | Event name |
| totalTickets | Integer | @NotNull, @Min(1) | Total ticket capacity. availableTickets set equal to totalTickets on creation |

**UpdateEventRequest**

| Field | Type | Validation | Notes |
| :---- | :---- | :---- | :---- |
| name | String | @NotBlank | Updated event name |
| totalTickets | Integer | @NotNull, @Min(1) | Updated capacity. Service adjusts availableTickets by the difference |

## **3.2 Response DTOs**

**UserResponse**

| Field | Type | Notes |
| :---- | :---- | :---- |
| id | UUID | |
| name | String | |
| email | String | |
| createdAt | OffsetDateTime | |

**EventResponse**

| Field | Type | Notes |
| :---- | :---- | :---- |
| id | UUID | |
| name | String | |
| organizerId | UUID | |
| organizerName | String | Denormalized for convenience — avoids extra lookup |
| status | EVENT_STATUS | |
| availableTickets | Integer | |
| totalTickets | Integer | |
| createdAt | OffsetDateTime | |
| updatedAt | OffsetDateTime | |

**BookingResponse**

| Field | Type | Notes |
| :---- | :---- | :---- |
| id | UUID | |
| userId | UUID | |
| eventId | UUID | |
| eventName | String | Denormalized for convenience |
| status | BOOKING_STATUS | |
| createdAt | OffsetDateTime | |
| cancelledAt | OffsetDateTime | Null if not cancelled |

**LoginResponse**

| Field | Type | Notes |
| :---- | :---- | :---- |
| token | String | JWT token |

## **3.3 DTO Mapping Notes**

Manual mapping in service layer. Fields intentionally excluded from responses: password, deletedAt.

# **4. Repository Layer**

## **4.1 UserRepository**

| Method Signature | Returns | Description |
| :---- | :---- | :---- |
| findByEmailAndDeletedAtIsNull(String) | Optional\<User\> | Lookup by email, excluding soft-deleted |
| existsByEmailAndDeletedAtIsNull(String) | boolean | Check email uniqueness among active users |

## **4.2 EventRepository**

| Method Signature | Returns | Description |
| :---- | :---- | :---- |
| *(to be determined during implementation)* | | |

## **4.3 BookingRepository**

| Method Signature | Returns | Description |
| :---- | :---- | :---- |
| findAllByUserIdAndDeletedAtIsNull(...) | List\<Booking\> | All bookings for a user, with event data via JOIN FETCH |
| existsByEventIdAndUserIdAndStatus(...) | boolean | Check if user has a confirmed booking for an event |

# **5. Service Layer**

## **5.1 AuthService**

| Method Signature | Returns | Throws | Tx | Description |
| :---- | :---- | :---- | :---- | :---- |
| login(LoginRequest) | LoginResponse | InvalidCredentialsException | readOnly | Verify email/password, return JWT |

## **5.2 UserService**

| Method Signature | Returns | Throws | Tx | Description |
| :---- | :---- | :---- | :---- | :---- |
| createUser(CreateUserRequest) | UserResponse | DuplicateEmailException | write | Validate uniqueness, hash password, save |
| getUserById(UUID) | UserResponse | UserNotFoundException | readOnly | Fetch active user by ID |
| deleteUser(UUID userId, UUID authUserId) | void | UserNotFoundException, UnauthorizedAccessException | write | Soft delete — set deletedAt |
| getUserEntityById(UUID) | User | UserNotFoundException | readOnly | Internal — returns entity for other services |

## **5.3 EventService**

| Method Signature | Returns | Throws | Tx | Description |
| :---- | :---- | :---- | :---- | :---- |
| createEvent(CreateEventRequest, UUID authUserId) | EventResponse | UserNotFoundException | write | Create event with status DRAFT. Set availableTickets = totalTickets |
| getEventById(UUID) | EventResponse | EventNotFoundException | readOnly | Fetch active event by ID |
| getAllEvents(Pageable) | Page\<EventResponse\> | — | readOnly | Paginated list of active events |
| updateEvent(UUID eventId, UpdateEventRequest, UUID authUserId) | EventResponse | EventNotFoundException, UnauthorizedAccessException, InvalidEventStateException | write | Only organizer can update. Only DRAFT events can be updated. Adjust availableTickets by the totalTickets difference |
| publishEvent(UUID eventId, UUID authUserId) | EventResponse | EventNotFoundException, UnauthorizedAccessException, InvalidEventStatusTransitionException | write | DRAFT → PUBLISHED. Only organizer |
| cancelEvent(UUID eventId, UUID authUserId) | EventResponse | EventNotFoundException, UnauthorizedAccessException, InvalidEventStatusTransitionException | write | DRAFT/PUBLISHED → CANCELLED. Only organizer |
| deleteEvent(UUID eventId, UUID authUserId) | void | EventNotFoundException, UnauthorizedAccessException, EventDeletionNotAllowedException | write | Soft delete. Only organizer. Only DRAFT events can be deleted |
| getEventEntityById(UUID) | Event | EventNotFoundException | readOnly | Internal — returns entity for other services |

## **5.4 BookingService**

| Method Signature | Returns | Throws | Tx | Description |
| :---- | :---- | :---- | :---- | :---- |
| createBooking(UUID eventId, UUID authUserId) | BookingResponse | EventNotFoundException, EventNotPublishedException, EventSoldOutException, DuplicateBookingException | write | Verify event is PUBLISHED, has tickets, user hasn't already booked. Decrement availableTickets. **This is where locking is needed** |
| getBookingById(UUID bookingId) | BookingResponse | BookingNotFoundException | readOnly | Fetch booking by ID |
| getBookingsByUser(UUID userId, Pageable) | Page\<BookingResponse\> | — | readOnly | Paginated list of authenticated user's bookings |
| getBookingsByEvent(UUID eventId, UUID authUserId, Pageable) | Page\<BookingResponse\> | EventNotFoundException, UnauthorizedAccessException | readOnly | Only the event organizer can view an event's bookings |
| cancelBooking(UUID bookingId, UUID authUserId) | void | BookingNotFoundException, UnauthorizedAccessException | write | Only the booking owner can cancel. Set status to CANCELLED, set cancelledAt. Increment availableTickets |

# **6. Controller Layer (API Endpoints)**

## **6.1 AuthController — /api/v1/auth**

| Method | Path | Request Body | Response | Auth |
| :---- | :---- | :---- | :---- | :---- |
| POST | /api/v1/auth/register | CreateUserRequest | UserResponse (201) | Public |
| POST | /api/v1/auth/login | LoginRequest | LoginResponse (200) | Public |

## **6.2 UserController — /api/v1/users**

| Method | Path | Request Body | Response | Auth |
| :---- | :---- | :---- | :---- | :---- |
| GET | /api/v1/users/{id} | — | UserResponse (200) | Protected |
| DELETE | /api/v1/users/{id} | — | 204 No Content | Protected (self only) |

## **6.3 EventController — /api/v1/events**

| Method | Path | Request Body | Response | Auth |
| :---- | :---- | :---- | :---- | :---- |
| POST | /api/v1/events | CreateEventRequest | EventResponse (201) | Protected |
| GET | /api/v1/events/{id} | — | EventResponse (200) | Protected |
| GET | /api/v1/events | — | Page\<EventResponse\> (200) | Protected |
| PATCH | /api/v1/events/{id} | UpdateEventRequest | EventResponse (200) | Protected (organizer only) |
| PATCH | /api/v1/events/{id}/publish | — | EventResponse (200) | Protected (organizer only) |
| PATCH | /api/v1/events/{id}/cancel | — | EventResponse (200) | Protected (organizer only) |
| DELETE | /api/v1/events/{id} | — | 204 No Content | Protected (organizer only) |

## **6.4 BookingController — /api/v1/bookings**

| Method | Path | Request Body | Response | Auth |
| :---- | :---- | :---- | :---- | :---- |
| POST | /api/v1/events/{eventId}/bookings | — | BookingResponse (201) | Protected |
| GET | /api/v1/bookings/{id} | — | BookingResponse (200) | Protected |
| GET | /api/v1/me/bookings | — | Page\<BookingResponse\> (200) | Protected |
| GET | /api/v1/events/{eventId}/bookings | — | Page\<BookingResponse\> (200) | Protected (organizer only) |
| POST | /api/v1/bookings/{id}/cancel | — | 204 No Content | Protected (booking owner only) |

# **7. Exception Handling**

## **7.1 Custom Exceptions**

| Exception | Extends | HTTP Status | When Thrown |
| :---- | :---- | :---- | :---- |
| ResourceNotFoundException | RuntimeException | 404 | Base class for all not-found exceptions |
| UserNotFoundException | ResourceNotFoundException | 404 | User ID does not exist or is soft-deleted |
| EventNotFoundException | ResourceNotFoundException | 404 | Event ID does not exist or is soft-deleted |
| BookingNotFoundException | ResourceNotFoundException | 404 | Booking ID does not exist |
| DuplicateEmailException | RuntimeException | 409 | Email already registered |
| InvalidCredentialsException | RuntimeException | 401 | Login with wrong email/password |
| UnauthorizedAccessException | RuntimeException | 403 | User trying to modify a resource they don't own |
| InvalidEventStatusTransitionException | RuntimeException | 400 | Invalid status change (e.g., CANCELLED → PUBLISHED) |
| EventNotPublishedException | RuntimeException | 400 | Trying to book an event that isn't PUBLISHED |
| EventSoldOutException | RuntimeException | 409 | No available tickets remaining |
| DuplicateBookingException | RuntimeException | 409 | User already has a confirmed booking for this event |
| EventDeletionNotAllowedException | RuntimeException | 400 | Trying to delete an event that isn't in DRAFT status |

## **7.2 Global Error Response Format**

```json
{
  "message": "Event with id 550e8400-... not found",
  "errors": null,
  "timestamp": "2026-06-06T12:00:00Z",
  "path": "/api/v1/events/550e8400-..."
}
```

Handled by `@RestControllerAdvice` GlobalExceptionHandler. Validation errors populate `errors` list with field-level messages.

# **8. Security & Authentication**

- JWT-based stateless authentication (same pattern as Project 3)
- JwtService: generate token (userId as subject), validate, extract claims
- JwtAuthenticationFilter: extract Bearer token, validate, set SecurityContext
- SecurityConfig: CSRF disabled, stateless sessions, public endpoints (register, login), all others authenticated
- Authorization (ownership checks) enforced in the service layer

**Public endpoints**: POST /api/v1/auth/register, POST /api/v1/auth/login

**Protected endpoints**: Everything else

# **9. Cross-Cutting Concerns**

## **9.1 Pagination & Sorting**

Offset-based pagination via Spring Data `Pageable`. Default page size: 20. Applied to: GET /events, GET /me/bookings, GET /events/{id}/bookings.

## **9.2 Validation Strategy**

- DTO-level: `@Valid` + Bean Validation annotations on request DTOs
- Service-level: business rule validation (ownership, status transitions, duplicate checks)
- Database-level: unique constraints, foreign keys, not-null constraints

# **10. Database Migration**

Flyway with versioned SQL files. `ddl-auto=validate`.

- V1__create_users_table.sql
- V2__create_events_table.sql
- V3__create_bookings_table.sql

Additional migrations added as needed during implementation.

# **11. Open Questions**

1. Optimistic vs pessimistic locking for ticket booking — to be decided when implementing BookingService.
2. Should cancelling an event automatically cancel all its bookings?
3. Should updating totalTickets be allowed on PUBLISHED events (e.g., organizer adds more tickets)?
