package com.techpg.hotelbooking.strategy;

public record PaymentResult(boolean successful, String transactionId, String message) {
    public static PaymentResult success(String transactionId) {
        return new PaymentResult(true, transactionId, "Payment successful");
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, message);
    }
}
