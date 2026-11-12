package com.example.bakeryshop.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SIZE")
// Represents the size domain object.
public class Size extends CakeOption {

    @Override
    public double getExtraPrice() {
        // Polymorphism
        return getPrice() * 1.5;
    }
}