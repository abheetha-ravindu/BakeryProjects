package com.example.bakeryshop.repisitory;

import com.example.bakeryshop.model.CustomCakeBooking;
import org.springframework.data.jpa.repository.JpaRepository;
// Accesses Custom Cake Booking persistence logic.

public interface CustomCakeBookingRepository extends JpaRepository<CustomCakeBooking, Long> {
}