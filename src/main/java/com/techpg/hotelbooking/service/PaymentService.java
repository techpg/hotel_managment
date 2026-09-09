package com.techpg.hotelbooking.service;

import com.techpg.hotelbooking.domain.Booking;
import com.techpg.hotelbooking.domain.BookingStatus;
import com.techpg.hotelbooking.repository.BookingRepository;
import com.techpg.hotelbooking.strategy.PaymentGateway;
import com.techpg.hotelbooking.strategy.PaymentMethod;
import com.techpg.hotelbooking.strategy.PaymentResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PaymentService {
    private final BookingRepository bookingRepository;
    private final PaymentGateway paymentGateway;
    private final InventoryService inventoryService;

    private final Map<UUID, ReentrantLock> paymentLocks = new ConcurrentHashMap<>();
    private final Map<UUID, PaymentResult> completedPayments = new ConcurrentHashMap<>();

    public PaymentService(BookingRepository bookingRepository,
                          PaymentGateway paymentGateway,
                          InventoryService inventoryService) {
        this.bookingRepository = bookingRepository;
        this.paymentGateway = paymentGateway;
        this.inventoryService = inventoryService;
    }

    public PaymentResult pay(UUID bookingId, PaymentMethod method) {
        ReentrantLock lock = paymentLocks.computeIfAbsent(bookingId, id -> new ReentrantLock());
        lock.lock();
        try {
            PaymentResult previousResult = completedPayments.get(bookingId);
            if (previousResult != null) {
                return previousResult;
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            if (booking.status() != BookingStatus.PENDING_PAYMENT) {
                throw new IllegalStateException("Booking is not payable from " + booking.status());
            }

            PaymentResult result = paymentGateway.charge(bookingId, booking.amount(), method);
            if (result.successful()) {
                booking.markConfirmed();
            } else {
                booking.markPaymentFailed();
                inventoryService.release(booking.roomTypeId(), booking.id());
            }
            bookingRepository.save(booking);
            completedPayments.put(bookingId, result);
            return result;
        } finally {
            lock.unlock();
        }
    }
}
