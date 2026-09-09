package com.techpg.hotelbooking.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingTest {

    private Booking newBooking() {
        DateRange stay = new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12));
        return new Booking(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                stay, 2, 1, BigDecimal.valueOf(7000));
    }

    @Test
    void startsAsPendingPayment() {
        assertThat(newBooking().status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void markConfirmedTransitionsFromPendingPayment() {
        Booking booking = newBooking();

        booking.markConfirmed();

        assertThat(booking.status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void markPaymentFailedTransitionsFromPendingPayment() {
        Booking booking = newBooking();

        booking.markPaymentFailed();

        assertThat(booking.status()).isEqualTo(BookingStatus.PAYMENT_FAILED);
    }

    @Test
    void cancelAllowedFromPendingPaymentAndConfirmed() {
        Booking pending = newBooking();
        pending.cancel();
        assertThat(pending.status()).isEqualTo(BookingStatus.CANCELLED);

        Booking confirmed = newBooking();
        confirmed.markConfirmed();
        confirmed.cancel();
        assertThat(confirmed.status()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void rejectsInvalidStateTransitions() {
        Booking confirmed = newBooking();
        confirmed.markConfirmed();
        assertThatThrownBy(confirmed::markConfirmed).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(confirmed::markPaymentFailed).isInstanceOf(IllegalStateException.class);

        Booking cancelled = newBooking();
        cancelled.cancel();
        assertThatThrownBy(cancelled::cancel).isInstanceOf(IllegalStateException.class);
    }
}
