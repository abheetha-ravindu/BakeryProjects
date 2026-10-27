package com.example.bakeryshop.controller;

import com.example.bakeryshop.model.AdminUser;
import com.example.bakeryshop.services.DeliveryService;
import com.example.bakeryshop.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final DeliveryService deliveryService;

    public AdminController(UserService userService, DeliveryService deliveryService) {
        this.userService = userService;
        this.deliveryService = deliveryService;
    }

    private AdminUser currentAdmin(HttpSession session) {
        Long adminId = (Long) session.getAttribute("loggedInAdminId");
        return adminId == null ? null : userService.findAdminById(adminId);
    }

    /** Admin profile: "users" tab + "deliveries" tab. */
    @GetMapping("/profile")
    public String adminProfile(@RequestParam(defaultValue = "users") String tab,
                               HttpSession session, Model model) {
        AdminUser admin = currentAdmin(session);
        if (admin == null) {
            return "redirect:/login";
        }
        model.addAttribute("admin", admin);
        model.addAttribute("users", userService.findAllCustomers());
        model.addAttribute("deliveries", deliveryService.findAll());
        model.addAttribute("activeTab", "deliveries".equals(tab) ? "deliveries" : "users");
        return "adminprofile";
    }

    @PostMapping("/delivery/update/{id}")
    public String updateDeliveryStatus(@PathVariable Long id, @RequestParam String status,
                                       HttpSession session) {
        if (currentAdmin(session) == null) {
            return "redirect:/login";
        }
        deliveryService.updateStatus(id, status);
        return "redirect:/admin/profile?tab=deliveries";
    }

    @GetMapping("/delivery/delete/{id}")
    public String deleteDelivery(@PathVariable Long id, HttpSession session) {
        if (currentAdmin(session) == null) {
            return "redirect:/login";
        }
        deliveryService.deleteById(id);
        return "redirect:/admin/profile?tab=deliveries";
    }
}
