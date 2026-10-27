package com.example.bakeryshop.Admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class AdminWebController {

    @GetMapping("/addItem")
    public String addItem() {
        return "redirect:/adminitem";
    }

    @GetMapping({"/custome_order", "/custome_orders"})
    public String getCustomOrders() {
        return "redirect:/custome_orders.html";
    }
}
