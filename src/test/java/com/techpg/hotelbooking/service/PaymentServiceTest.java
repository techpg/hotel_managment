package com.techpg.hotelbooking.service;

import com.techpg.hotelbooking.domain.Booking;
import com.techpg.hotelbooking.domain.BookingStatus;
import com.techpg.hotelbooking.domain.DateRange;
import com.techpg.hotelbooking.repository.BookingRepository;
import com.techpg.hotelbooking.strategy.PaymentGateway;
import com.techpg.hotelbooking.strategy.PaymentMethod;
import com.techpg.hotelbooking.strategy.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private InventoryService inventoryService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(bookingRepository, paymentGateway, inventoryService);
    }

    private Booking pendingBooking() {
        DateRange stay = new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12));
        return new Booking(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                stay, 2, 1, BigDecimal.valueOf(2000));
    }

    @Test
    void successfulPaymentConfirmsBooking() {
        Booking booking = pendingBooking();
        when(bookingRepository.findById(booking.id())).thenReturn(Optional.of(booking));
        when(paymentGateway.charge(booking.id(), booking.amount(), PaymentMethod.CARD))
                .thenReturn(PaymentResult.success("TXN-1"));

        PaymentResult result = paymentService.pay(booking.id(), PaymentMethod.CARD);

        assertThat(result.successful()).isTrue();
        assertThat(booking.status()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository).save(booking);
        verify(inventoryService, never()).release(any(), any());
    }

    @Test
    void failedPaymentMarksBookingFailedAndReleasesInventory() {
        Booking booking = pendingBooking();
        when(bookingRepository.findById(booking.id())).thenReturn(Optional.of(booking));
        when(paymentGateway.charge(booking.id(), booking.amount(), PaymentMethod.CARD))
                .thenReturn(PaymentResult.failure("Declined"));

        PaymentResult result = paymentService.pay(booking.id(), PaymentMethod.CARD);

        assertThat(result.successful()).isFalse();
        assertThat(booking.status()).isEqualTo(BookingStatus.PAYMENT_FAILED);
        verify(inventoryService).release(booking.roomTypeId(), booking.id());
    }

    @Test
    void payThrowsWhenBookingNotFound() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.pay(bookingId, PaymentMethod.CARD))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void payRejectsBookingNotInPendingPaymentState() {
        Booking booking = pendingBooking();
        booking.markConfirmed();
        when(bookingRepository.findById(booking.id())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> paymentService.pay(booking.id(), PaymentMethod.CARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void repeatedPaymentAttemptsAreIdempotentAndDoNotRechargeGateway() {
        Booking booking = pendingBooking();
        when(bookingRepository.findById(booking.id())).thenReturn(Optional.of(booking));
        when(paymentGateway.charge(booking.id(), booking.amount(), PaymentMethod.CARD))
                .thenReturn(PaymentResult.success("TXN-1"));

        PaymentResult first = paymentService.pay(booking.id(), PaymentMethod.CARD);
        PaymentResult second = paymentService.pay(booking.id(), PaymentMethod.CARD);

        assertThat(second).isEqualTo(first);
        verify(paymentGateway, times(1)).charge(booking.id(), booking.amount(), PaymentMethod.CARD);
    }
}
