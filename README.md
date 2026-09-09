# Hotel Booking

A small Spring Boot service for onboarding hotel properties, searching availability, and
booking/paying/cancelling reservations. In-memory storage, no external dependencies required to run.

## How to run

Requires Java 17+.

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`. Health check:

```bash
curl http://localhost:8080/api/health
```

### Run the tests

```bash
./mvnw test
```

### Quick API walkthrough

**1. Onboard a property**

```bash
curl -X POST http://localhost:8080/api/properties \
  -H 'Content-Type: application/json' \
  -d '{
    "ownerName":"Acme Hospitality",
    "name":"Acme Residency",
    "city":"Bengaluru",
    "locality":"Indiranagar",
    "starRating":4,
    "amenities":["wifi","parking"],
    "roomTypes":[
      {
        "name":"Deluxe",
        "capacity":2,
        "totalRooms":2,
        "nightlyPrice":3500,
        "amenities":["wifi","tv"]
      }
    ]
  }'
```

Copy `id` and `roomTypes[0].id` from the response — you'll need them below.

**2. Search**

```bash
curl 'http://localhost:8080/api/search?city=Bengaluru&checkIn=2026-10-10&checkOut=2026-10-12&guests=2&minStars=3'
```

**3. Book**

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H 'Content-Type: application/json' \
  -d '{
    "propertyId":"PROPERTY_UUID",
    "roomTypeId":"ROOM_TYPE_UUID",
    "guestId":"00000000-0000-0000-0000-000000000001",
    "checkIn":"2026-10-10",
    "checkOut":"2026-10-12",
    "guests":2
  }'
```

**4. Pay**

```bash
curl -X POST http://localhost:8080/api/bookings/BOOKING_UUID/payment \
  -H 'Content-Type: application/json' \
  -d '{"method":"UPI"}'
```

**5. Cancel**

```bash
curl -X POST http://localhost:8080/api/bookings/BOOKING_UUID/cancel
```

## Design decisions

- Standard layering: `controller` → `service` → `repository`, `domain` for entities/value objects,
  `strategy` for pluggable bits (pricing, refund policy, payment gateway) behind interfaces.
- Storage is in-memory (`ConcurrentHashMap` repositories) for now, but entities are already
  JPA-annotated and the repository interfaces are the seam — swapping in real Spring Data JPA repos
  later shouldn't touch services or controllers.
- Inventory is tracked separately from bookings: a per-room-type lock plus date-range overlap check
  in `InventoryService`, so two concurrent bookings can't both grab the last room.
- Rooms are reserved at booking time, not at payment time, and released if payment fails — otherwise
  someone could sit on the last room mid-checkout.
- `Booking` is a small state machine (`PENDING_PAYMENT → CONFIRMED/PAYMENT_FAILED → CANCELLED`) so
  the "can this transition happen" logic lives in one place instead of scattered across services.
- Payment is idempotent per booking ID (locked + cached result), so a retried payment request
  doesn't double-charge.
- Stays are half-open (`[checkIn, checkOut)`), so a checkout and another guest's check-in on the
  same day don't count as overlapping.

## Assumptions

- Single currency, no taxes/fees — price is just `nightlyPrice × nights × rooms`.
- No auth — any caller can onboard properties or act on any booking ID they have.
- No real payment provider; `MockPaymentGateway` just succeeds unless the amount is negative.
- Single-instance app — the in-memory locking prevents overbooking on one JVM only.
- Cancellations always refund in full for now (`FullRefundCancellationPolicy`), though it's swappable.

## What I'd do with more time

- Back the repositories with real Spring Data JPA + H2 instead of in-memory maps, and move
  overbooking protection to the database so it works across multiple instances.
- Add auth so guests/owners can only act on their own bookings/properties.
- Add pagination and better filtering to search (it currently loads everything into memory).
- Integrate a real payment gateway with webhook-based confirmation.
- Expire stale `PENDING_PAYMENT` bookings on a schedule instead of holding rooms forever.

## Tests

Unit tests cover the core business logic: booking state transitions (`BookingTest`), availability/
overbooking protection (`InventoryServiceTest`), booking creation and cancellation orchestration
(`BookingServiceTest`), payment processing and idempotency (`PaymentServiceTest`), and pricing math
(`StandardPricingStrategyTest`).

Run them with `./mvnw test`.
