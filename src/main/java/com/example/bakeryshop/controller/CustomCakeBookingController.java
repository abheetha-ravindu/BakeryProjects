package com.example.bakeryshop.controller;

import com.example.bakeryshop.model.CustomCakeBooking;
import com.example.bakeryshop.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CustomCakeBookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/bookings")
    public CustomCakeBooking createBooking(@RequestBody CustomCakeBooking booking) {
        return bookingService.createBooking(booking);
    }

    @GetMapping("/cakeBookings")
    public List<CustomCakeBooking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/bookings/my")
    public List<CustomCakeBooking> getMyBookings() {
        return bookingService.getAllBookings();
    }

    @PostMapping("/admin/updateBookingStatus")
    public CustomCakeBooking updateBookingStatus(@RequestBody Map<String, Object> request) {
        Long bookingId = Long.valueOf(request.get("bookingId").toString());
        String status = (String) request.get("status");
        return bookingService.updateBookingStatus(bookingId, status);
    }

    @DeleteMapping("/cakeBookings/{id}")
    public void deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
    }
}