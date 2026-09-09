# Event Ticketing API

A Spring Boot REST API for creating events and reserving limited ticket inventory. The project focuses on stateless JWT authentication, event lifecycle rules, user-scoped bookings, transactional updates, and preventing overselling during concurrent booking requests.

## Stack

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL 17
- Flyway
- JJWT
- Docker and Docker Compose
- Maven Wrapper

## Domains

- `User`: registered account used for authentication, organizing events, and making bookings
- `Event`: organizer-owned event with a lifecycle and limited ticket inventory
- `Booking`: a user's reservation for one ticket at an event

## Authentication

The API uses stateless JWT bearer authentication:

1. A user registers with a name, email, and password.
2. Passwords are hashed with BCrypt before being stored.
3. A successful login returns a signed JWT whose subject is the user's UUID.
4. The client sends the token with every protected request.
5. A custom Spring Security filter validates the token and places the authenticated user in the `SecurityContext`.

Only the registration and login endpoints are public. All event and booking endpoints require:

```http
Authorization: Bearer <jwt-token>
```

## Business Rules

### Events

- New events start in `DRAFT` status.
- Only the organizer can update, publish, cancel, or delete an event.
- Only draft events can be updated, published, or soft deleted.
- Draft and published events can be cancelled.
- Event listing and detail endpoints expose only active, published events.
- Reducing total ticket capacity below the number of tickets already booked is rejected.

Event lifecycle:

```text
DRAFT -> PUBLISHED -> CANCELLED
  |
  +--------> CANCELLED
```

### Bookings

- A booking represents one ticket and starts in `CONFIRMED` status.
- Users can book only active, published events with available tickets.
- A user cannot hold more than one confirmed booking for the same event.
- Users can view and cancel only their own bookings.
- Cancelling a confirmed booking restores one available ticket.
- A cancelled booking cannot be cancelled again.

## Concurrency Control

Booking and cancellation lock the relevant event row with a pessimistic write lock. The availability check and ticket-count update then occur in the same database transaction, preventing concurrent requests from overselling the final ticket.

The integration test `BookingConcurrencyTest` starts two simultaneous booking attempts for an event with one remaining ticket and verifies that:

- exactly one booking succeeds;
- the available ticket count becomes zero;
- exactly one confirmed booking is stored.

## Database

Flyway owns the database schema, while Hibernate validates that the entity mappings match it. Migrations create:

- `users`
- `events`
- `bookings`
- indexes for common foreign-key lookups
- PostgreSQL triggers that maintain `updated_at` values

Events use soft deletion through `deleted_at`. Booking history is retained and cancelled bookings are represented by status and `cancelled_at`.

## Local Setup

### Prerequisites

- Docker with Docker Compose
- Java 21 and Maven are optional when running the entire application through Docker

Create the local environment file:

```bash
cp .env.example .env
```

Replace the placeholder passwords and JWT secret in `.env`. The JWT secret must contain at least 32 bytes for the configured HMAC signing algorithm.

Build and start the API and PostgreSQL:

```bash
docker compose up --build -d
```

The API is available at `http://localhost:8080`, and PostgreSQL is exposed locally on port `5433`.

View application logs:

```bash
docker compose logs -f app
```

Stop the containers without deleting database data:

```bash
docker compose down
```

To run the application with Maven instead, start only PostgreSQL and provide equivalent datasource and JWT configuration through environment variables or an ignored `application-local.yml` file:

```bash
docker compose up -d db
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## API Endpoints

### Authentication

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/v1/auth/register` | Register a user |
| `POST` | `/api/v1/auth/login` | Authenticate and receive a JWT |

Register request:

```json
{
  "name": "Mouad",
  "email": "mouad@example.com",
  "password": "password123"
}
```

Login request:

```json
{
  "email": "mouad@example.com",
  "password": "password123"
}
```

### Events

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/v1/events` | List published events with pagination |
| `GET` | `/api/v1/events/{eventId}` | Get a published event |
| `POST` | `/api/v1/events` | Create a draft event |
| `PATCH` | `/api/v1/events/{eventId}` | Update a draft event |
| `PATCH` | `/api/v1/events/{eventId}/publish` | Publish a draft event |
| `PATCH` | `/api/v1/events/{eventId}/cancel` | Cancel an event |
| `DELETE` | `/api/v1/events/{eventId}` | Soft delete a draft event |

Create or update event request:

```json
{
  "name": "Spring Backend Meetup",
  "totalTickets": 50
}
```

Pagination is controlled with `page` and `size` query parameters:

```http
GET /api/v1/events?page=0&size=20
```

### Bookings

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/v1/bookings` | List the authenticated user's bookings |
| `GET` | `/api/v1/bookings/{bookingId}` | Get one owned booking |
| `POST` | `/api/v1/events/{eventId}/bookings` | Book one ticket for a published event |
| `PATCH` | `/api/v1/bookings/{bookingId}/cancel` | Cancel an owned booking |

## Error Handling

The API returns structured JSON errors through a global exception handler. Domain failures use appropriate HTTP status codes, including:

- `400 Bad Request` for validation failures
- `401 Unauthorized` for missing, invalid, or incorrect credentials
- `403 Forbidden` for organizer ownership violations
- `404 Not Found` for unavailable users, events, or bookings
- `409 Conflict` for duplicate resources, sold-out events, and invalid state transitions

## Verification

Run the test suite against a configured local PostgreSQL database:

```bash
./mvnw test -Dspring.profiles.active=local
```

The suite includes an application context test and a concurrent-booking integration test.

## Scope

This project intentionally excludes payment processing, seat selection, email delivery, refresh tokens, and administrative role management. Its scope is the backend transaction flow from publishing an event through safely booking and cancelling tickets.
