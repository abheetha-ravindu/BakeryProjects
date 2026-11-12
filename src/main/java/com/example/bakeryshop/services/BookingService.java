package com.example.bakeryshop.services;

import com.example.bakeryshop.model.CustomCakeBooking;
import java.util.List;

// Provides Booking service logic.
public interface BookingService {
    CustomCakeBooking createBooking(CustomCakeBooking booking);
    List<CustomCakeBooking> getAllBookings();
    CustomCakeBooking getBookingById(Long id);
    CustomCakeBooking updateBookingStatus(Long id, String status);
    void deleteBooking(Long id);
}