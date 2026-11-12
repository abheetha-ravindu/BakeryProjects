package com.example.bakeryshop.Admin.controller;

import com.example.bakeryshop.model.BookingStatus;
import com.example.bakeryshop.model.Customer;
import com.example.bakeryshop.model.CustomCakeBooking;
import com.example.bakeryshop.model.Delivery;
import com.example.bakeryshop.model.User;
import com.example.bakeryshop.model.order.Order;
import com.example.bakeryshop.model.order.OrderStatus;
import com.example.bakeryshop.services.BookingService;
import com.example.bakeryshop.services.DeliveryService;
import com.example.bakeryshop.services.UserService;
import com.example.bakeryshop.services.order.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Controller
// Handles Admin Web controller logic.
public class AdminWebController {
    private final UserService userService;
    private final DeliveryService deliveryService;
    private final OrderService orderService;
    private final BookingService bookingService;

    public AdminWebController(UserService userService, DeliveryService deliveryService,
                              OrderService orderService, BookingService bookingService) {
        this.userService = userService;
        this.deliveryService = deliveryService;
        this.orderService = orderService;
        this.bookingService = bookingService;
    }

    @GetMapping({"/addItem", "/adminItems"})
    public String addItem() {
        return "redirect:/adminpages/adminitem.html";
    }

    @GetMapping({"/adminDashboard", "/adminindex.html"})
    public String adminDashboard() {
        return "redirect:/adminpages/adminindex.html";
    }

    @GetMapping({"/customOrders", "/custome_order", "/custome_orders", "/custome_orders.html"})
    public String getCustomOrders() {
        return "redirect:/adminpages/custome_orders.html";
    }

    @GetMapping({"/bakeryOrders", "/admin-orders.html"})
    public String getBakeryOrders() {
        return "redirect:/adminpages/admin-orders.html";
    }

    @GetMapping({"/adminPayments", "/admin-payments.html"})
    public String getAdminPayments() {
        return "redirect:/adminpages/admin-payments.html";
    }

    @GetMapping("/adminInvoices")
    public String getAdminInvoices() {
        return "redirect:/adminpages/allinvoicess.html";
    }

    @GetMapping("/adminRefunds")
    public String getAdminRefunds() {
        return "redirect:/adminpages/admin-refund-management.html";
    }

    @GetMapping({"/users", "/userManagement"})
    public String getUsers() {
        return "redirect:/adminpages/userManage.html";
    }

    @GetMapping("/admin-refund-management")
    public String getRefundManagement() {
        return "redirect:/adminpages/admin-refund-management.html";
    }

    @GetMapping("/api/admin/users")
    @ResponseBody
    public List<CustomerSummary> getCustomerUsers() {
        return userService.findAllCustomers().stream()
                .map(customer -> new CustomerSummary(
                        customer.getId(),
                        customer.getUsername(),
                        customer.getEmail(),
                        customer.getPhoneNumber()))
                .toList();
    }

    @GetMapping("/api/admin/deliveries")
    @ResponseBody
    public List<DeliverySummary> getDeliveries() {
        List<DeliverySummary> summaries = new ArrayList<>();
        deliveryService.findAll().stream()
                .filter(delivery -> !isCancelled(delivery.getStatus()))
                .map(this::toDeliverySummary)
                .forEach(summaries::add);

        List<Order> orders = orderService.getAll(null);
        Map<Long, List<Order>> linkedOrders = new HashMap<>();
        for (Order order : orders) {
            Long bookingId = referencedBookingId(order.getNote());
            if (bookingId != null) {
                linkedOrders.computeIfAbsent(bookingId, ignored -> new ArrayList<>()).add(order);
            }
        }

        Set<Long> processedOrderIds = new HashSet<>();
        for (CustomCakeBooking booking : bookingService.getAllBookings()) {
            List<Order> relatedOrders = linkedOrders.getOrDefault(booking.getBookingId(), List.of());
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                relatedOrders.stream()
                        .map(Order::getId)
                        .filter(id -> id != null)
                        .forEach(processedOrderIds::add);
                continue;
            }

            String status = bookingDeliveryStatus(booking.getStatus());
            for (Order order : relatedOrders) {
                status = laterStatus(status, orderDeliveryStatus(order.getStatus()));
                if (order.getId() != null) {
                    processedOrderIds.add(order.getId());
                }
            }

            summaries.add(toBookingSummary(booking, relatedOrders, status));
        }

        for (Order order : orders) {
            if (order.getId() != null && processedOrderIds.contains(order.getId())) {
                continue;
            }
            summaries.add(toOrderSummary(order, orderDeliveryStatus(order.getStatus())));
        }

        return summaries;
    }

    @DeleteMapping("/api/admin/users/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        User user = userService.findById(id);
        if (!(user instanceof Customer)) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record CustomerSummary(Long id, String username, String email, String phoneNumber) {
    }

    private DeliverySummary toDeliverySummary(Delivery delivery) {
        return new DeliverySummary(
                reference("Delivery", delivery.getId()),
                delivery.getCustomer() == null ? null : delivery.getCustomer().getUsername(),
                delivery.getRecipientName(),
                delivery.getAddress(),
                delivery.getStatus());
    }

    private static DeliverySummary toBookingSummary(CustomCakeBooking booking, List<Order> relatedOrders,
                                                     String status) {
        String reference = reference("Custom order", booking.getBookingId());
        for (Order order : relatedOrders) {
            if (order.getId() != null) {
                reference += " / Bakery order #" + order.getId();
            }
        }
        String address = hasText(booking.getDeliveryAddress())
                ? booking.getDeliveryAddress()
                : booking.getPickupOrDelivery();
        return new DeliverySummary(reference, booking.getCustomerName(), booking.getCustomerName(), address, status);
    }

    private static DeliverySummary toOrderSummary(Order order, String status) {
        String name = noteValue(order.getNote(), "Name");
        String customerName = hasText(name) ? name
                : order.getUserId() == null ? null : "Customer #" + order.getUserId();
        return new DeliverySummary(
                reference("Bakery order", order.getId()),
                customerName,
                name,
                noteValue(order.getNote(), "Address"),
                status);
    }

    private static String orderDeliveryStatus(OrderStatus status) {
        if (status == OrderStatus.READY) {
            return "Out for Delivery";
        }
        return status == OrderStatus.DELIVERED ? "Delivered" : "Pending";
    }

    private static String bookingDeliveryStatus(BookingStatus status) {
        if (status == BookingStatus.READY) {
            return "Out for Delivery";
        }
        return status == BookingStatus.COMPLETED ? "Delivered" : "Pending";
    }

    private static String laterStatus(String first, String second) {
        return deliveryStage(second) > deliveryStage(first) ? second : first;
    }

    private static int deliveryStage(String status) {
        if ("delivered".equalsIgnoreCase(status)) {
            return 2;
        }
        if ("out for delivery".equalsIgnoreCase(status)) {
            return 1;
        }
        return 0;
    }

    private static boolean isCancelled(String status) {
        return status != null && "cancelled".equalsIgnoreCase(status.trim());
    }

    private static String noteValue(String note, String key) {
        if (note == null) {
            return null;
        }
        for (String part : note.split("\\|")) {
            String trimmed = part.trim();
            int separator = trimmed.indexOf(':');
            if (separator > 0 && key.equalsIgnoreCase(trimmed.substring(0, separator).trim())) {
                String value = trimmed.substring(separator + 1).trim();
                return hasText(value) ? value : null;
            }
        }
        return null;
    }

    private static Long referencedBookingId(String note) {
        String prefix = "Custom cake booking #";
        if (note == null || !note.trim().regionMatches(true, 0, prefix, 0, prefix.length())) {
            return null;
        }
        String value = note.trim().substring(prefix.length()).trim();
        int end = 0;
        while (end < value.length() && Character.isDigit(value.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return null;
        }
        try {
            return Long.parseLong(value.substring(0, end));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String reference(String type, Long id) {
        return id == null ? type : type + " #" + id;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record DeliverySummary(String id, String customerName, String recipientName, String address, String status) {
    }
}
