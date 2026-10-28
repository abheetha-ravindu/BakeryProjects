package com.example.bakeryshop.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DECORATION")
public class Topping extends CakeOption {

    @Override
    public double getExtraPrice() {
        // Polymorphism
        return getPrice() + 300.0;
    }
}