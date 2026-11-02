package com.example.bakeryshop.repisitory;

import com.example.bakeryshop.model.CustomCakeBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomCakeBookingRepository extends JpaRepository<CustomCakeBooking, Long> {
}