Boilerplate setup

curl http://localhost:8080/api/health

# Quick API Examples

## 1. Create property

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

Copy `id` and `roomTypes[0].id` from the response.

## 2. Search

```bash
curl 'http://localhost:8080/api/search?city=Bengaluru&checkIn=2026-10-10&checkOut=2026-10-12&guests=2&minStars=3'
```

## 3. Book

Replace IDs below:

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

## 4. Pay

```bash
curl -X POST http://localhost:8080/api/bookings/BOOKING_UUID/payment \
  -H 'Content-Type: application/json' \
  -d '{"method":"UPI"}'
```

## 5. Cancel

```bash
curl -X POST http://localhost:8080/api/bookings/BOOKING_UUID/cancel
```
