package com.example.bakeryshop.Admin.controller;

import com.example.bakeryprojects.model.Customer;
import com.example.bakeryprojects.model.User;
import com.example.bakeryprojects.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
public class AdminWebController {
    private final UserService userService;

    public AdminWebController(UserService userService) {
        this.userService = userService;
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
}
