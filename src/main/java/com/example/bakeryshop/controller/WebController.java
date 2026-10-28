package com.example.bakeryshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }

    @GetMapping({"/items", "/item"})
    public String items() {
        return "redirect:/item.html";
    }

    @GetMapping("/main")
    public String main() {
        return "redirect:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/login.html";
    }

    @GetMapping("/register")
    public String register() {
        return "redirect:/register.html";
    }

    @GetMapping("/orders")
    public String orders() {
        return "redirect:/orders.html";
    }

    @GetMapping("/payment")
    public String payment() {
        return "redirect:/payment.html";
    }

    @GetMapping("/payments")
    public String payments() {
        return "redirect:/payments.html";
    }

    @GetMapping("/profileupdate")
    public String profileUpdate() {
        return "redirect:/profileupdate.html";
    }

    @GetMapping("/singup")
    public String singup() {
        return "redirect:/singup.html";
    }

    @GetMapping("/loging")
    public String loging() {
        return "redirect:/loging.html";
    }

    @GetMapping({"/custom-cake", "/custome_cake"})
    public String customCake() {
        return "redirect:/custom-cake.html";
    }

    @GetMapping({"/invoice-detail", "/invoice-details"})
    public String invoiceDetails(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "invoiceId", required = false) Long invoiceId) {
        Long requestedId = id != null ? id : invoiceId;
        if (requestedId == null || requestedId < 1) {
            return "redirect:/billing/my-invoices.html";
        }
        return "redirect:/billing/invoice-detail.html?id=" + requestedId;
    }

    @GetMapping({"/my-invoices", "/my-invoice"})
    public String myInvoice() {
        return "redirect:/billing/my-invoices.html";
    }

    @GetMapping("/refund-request-form")
    public String refundRequestForm(@RequestParam(name = "orderId", required = false) Long orderId) {
        if (orderId == null || orderId < 1) {
            return "redirect:/billing/refund-request-form.html";
        }
        return "redirect:/billing/refund-request-form.html?orderId=" + orderId;
    }

}
