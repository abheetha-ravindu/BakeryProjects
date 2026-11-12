package com.example.bakeryshop.billing;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PAYMENT")
// Represents the payment invoice domain object.
public class PaymentInvoice extends Invoice {
    @Override
    public double calculateTotal() {
        return getAmount() == null ? 0 : getAmount().doubleValue();
    }
}
