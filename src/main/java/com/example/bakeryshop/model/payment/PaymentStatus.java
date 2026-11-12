package com.example.bakeryshop.model.payment;


// Represents the payment status domain object.
public enum PaymentStatus {
    PAID,
    REFUNDED;

    // paid payment can refund
    public boolean canMoveTo(PaymentStatus next) {
        return this == PAID && next == REFUNDED;
    }
}