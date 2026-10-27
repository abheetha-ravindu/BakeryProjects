package com.example.bakeryshop.controller;

import com.example.bakeryshop.model.Customer;
import com.example.bakeryshop.model.Delivery;
import com.example.bakeryshop.model.User;
import com.example.bakeryshop.services.DeliveryService;
import com.example.bakeryshop.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final UserService userService;

    public DeliveryController(DeliveryService deliveryService, UserService userService) {
        this.deliveryService = deliveryService;
        this.userService = userService;
    }

    // Delivery Status සහ Information පිටුව පෙන්වීම
    @GetMapping({"/status", "/info", ""})
    public String showDeliveryStatus(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", userService.findById(userId));
        model.addAttribute("deliveries", deliveryService.findAll());
        return "deliverystatus";
    }


    @GetMapping("/new")
    public String showDeliveryForm(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId != null) {
            model.addAttribute("user", userService.findById(userId));
        }
        model.addAttribute("delivery", new Delivery());
        return "deliveryform";
    }


    @PostMapping("/new")
    public String createDelivery(@ModelAttribute Delivery delivery, HttpSession session) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.findById(userId);
        if (user instanceof Customer customer) {
            delivery.setCustomer(customer);

            // Profile eke save karapu address eka thama delivery address eka
            if (customer.getAddress() != null && !customer.getAddress().isBlank()) {
                delivery.setAddress(customer.getAddress());
            }
            if (delivery.getRecipientName() == null || delivery.getRecipientName().isBlank()) {
                delivery.setRecipientName(customer.getUsername());
            }
        }
        deliveryService.create(delivery);
        return "redirect:/delivery/status";
    }


    @PostMapping("/update/{id}")
    public String updateDeliveryStatus(@PathVariable Long id, @RequestParam String status) {
        deliveryService.updateStatus(id, status);
        return "redirect:/delivery/status";
    }


    @GetMapping("/delete/{id}")
    public String deleteDelivery(@PathVariable Long id) {
        deliveryService.deleteById(id);
        return "redirect:/delivery/status";
    }
}
