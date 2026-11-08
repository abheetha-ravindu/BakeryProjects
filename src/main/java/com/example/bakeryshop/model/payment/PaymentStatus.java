package com.example.bakeryshop.model.payment;

public enum PaymentStatus {
    PAID,
    REFUNDED;

    // paid payment can refund
    public boolean canMoveTo(PaymentStatus next) {
        return this == PAID && next == REFUNDED;
    }
}