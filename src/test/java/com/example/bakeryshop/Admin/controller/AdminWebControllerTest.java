package com.example.bakeryshop.Admin.controller;

import com.example.bakeryshop.model.BookingStatus;
import com.example.bakeryshop.model.CustomCakeBooking;
import com.example.bakeryshop.model.Delivery;
import com.example.bakeryshop.model.order.Order;
import com.example.bakeryshop.model.order.OrderStatus;
import com.example.bakeryshop.services.BookingService;
import com.example.bakeryshop.services.DeliveryService;
import com.example.bakeryshop.services.UserService;
import com.example.bakeryshop.services.order.OrderService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Handles Admin Web  Test controller logic.
class AdminWebControllerTest {

    @Test
    void getDeliveriesAggregatesActiveOrdersAndCoalescesLinkedCustomOrders() {
        UserService userService = mock(UserService.class);
        DeliveryService deliveryService = mock(DeliveryService.class);
        OrderService orderService = mock(OrderService.class);
        BookingService bookingService = mock(BookingService.class);

        Delivery activeDelivery = new Delivery();
        activeDelivery.setId(40L);
        activeDelivery.setStatus("Pending");
        Delivery cancelledDelivery = new Delivery();
        cancelledDelivery.setId(41L);
        cancelledDelivery.setStatus("Cancelled");
        when(deliveryService.findAll()).thenReturn(List.of(activeDelivery, cancelledDelivery));

        Order bakeryOrder = order(1L, 7L, "Name: Ava | Phone: 555-0101 | Address: 12 Main Street",
                OrderStatus.READY);
        Order linkedCustomOrder = order(2L, 7L, "Custom cake booking #20", OrderStatus.DELIVERED);
        Order cancelledCustomOrder = order(3L, 7L, "Custom cake booking #22", OrderStatus.PENDING);
        when(orderService.getAll(null)).thenReturn(List.of(bakeryOrder, linkedCustomOrder, cancelledCustomOrder));

        CustomCakeBooking linkedBooking = booking(20L, "Ava", BookingStatus.PREPARING);
        CustomCakeBooking completedBooking = booking(21L, "Mia", BookingStatus.COMPLETED);
        CustomCakeBooking cancelledBooking = booking(22L, "Lee", BookingStatus.CANCELLED);
        when(bookingService.getAllBookings()).thenReturn(
                List.of(linkedBooking, completedBooking, cancelledBooking));

        AdminWebController controller = new AdminWebController(
                userService, deliveryService, orderService, bookingService);
        List<AdminWebController.DeliverySummary> summaries = controller.getDeliveries();

        assertEquals(4, summaries.size());
        assertEquals("Pending", find(summaries, "Delivery #40").status());
        assertEquals("Out for Delivery", find(summaries, "Bakery order #1").status());
        assertEquals("Ava", find(summaries, "Bakery order #1").recipientName());
        assertEquals("12 Main Street", find(summaries, "Bakery order #1").address());
        assertEquals("Delivered", find(summaries, "Custom order #20 / Bakery order #2").status());
        assertEquals("Delivered", find(summaries, "Custom order #21").status());
        assertNull(findIfPresent(summaries, "Custom order #22"));
        assertNull(findIfPresent(summaries, "Bakery order #2"));
        assertNull(findIfPresent(summaries, "Bakery order #3"));
    }

    private static Order order(Long id, Long userId, String note, OrderStatus status) {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(id);
        when(order.getUserId()).thenReturn(userId);
        when(order.getNote()).thenReturn(note);
        when(order.getStatus()).thenReturn(status);
        return order;
    }

    private static CustomCakeBooking booking(Long id, String customerName, BookingStatus status) {
        CustomCakeBooking booking = new CustomCakeBooking();
        booking.setBookingId(id);
        booking.setCustomerName(customerName);
        booking.setStatus(status);
        return booking;
    }

    private static AdminWebController.DeliverySummary find(
            List<AdminWebController.DeliverySummary> summaries, String id) {
        AdminWebController.DeliverySummary summary = findIfPresent(summaries, id);
        if (summary == null) {
            throw new AssertionError("Expected delivery summary " + id);
        }
        return summary;
    }

    private static AdminWebController.DeliverySummary findIfPresent(
            List<AdminWebController.DeliverySummary> summaries, String id) {
        return summaries.stream()
                .filter(summary -> id.equals(summary.id()))
                .findFirst()
                .orElse(null);
    }
}
