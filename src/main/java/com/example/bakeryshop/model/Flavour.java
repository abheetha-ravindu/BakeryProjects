package com.example.bakeryshop.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FLAVOUR")
// Represents the flavour domain object.
public class Flavour extends CakeOption {

    @Override
    public double getExtraPrice() {
        // Polymorphism
        return getPrice() + 150.0;
    }
}