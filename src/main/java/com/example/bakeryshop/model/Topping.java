package com.example.bakeryshop.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DECORATION")
// Represents the topping domain object.
public class Topping extends CakeOption {

    @Override
    public double getExtraPrice() {
        // Polymorphism
        return getPrice() + 300.0;
    }
}