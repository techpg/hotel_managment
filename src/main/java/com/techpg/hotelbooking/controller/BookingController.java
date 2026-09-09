package com.techpg.hotelbooking.controller;

import com.techpg.hotelbooking.domain.Booking;
import com.techpg.hotelbooking.domain.DateRange;
import com.techpg.hotelbooking.dto.BookingRequest;
import com.techpg.hotelbooking.dto.PaymentRequest;
import com.techpg.hotelbooking.service.BookingService;
import com.techpg.hotelbooking.service.PaymentService;
import com.techpg.hotelbooking.strategy.PaymentMethodParser;
import com.techpg.hotelbooking.strategy.PaymentResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final PaymentService paymentService;

    public BookingController(BookingService bookingService, PaymentService paymentService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Booking> book(@Valid @RequestBody BookingRequest request) {
        Booking booking = bookingService.create(
                request.propertyId(), request.roomTypeId(), request.guestId(),
                new DateRange(request.checkIn(), request.checkOut()), request.guests()
        );
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/{id}")
    public Booking get(@PathVariable UUID id) {
        return bookingService.get(id);
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<PaymentResult> pay(
            @PathVariable UUID id, @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(
                paymentService.pay(id, PaymentMethodParser.parse(request.method()))
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable UUID id) {
        BigDecimal refund = bookingService.cancel(id);
        return ResponseEntity.ok(Map.of(
                "bookingId", id,
                "status", bookingService.get(id).status(),
                "refundAmount", refund
        ));
    }
}
