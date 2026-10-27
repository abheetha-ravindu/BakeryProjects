package com.example.bakeryshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class WebController {
    @GetMapping("/items")
    public String items() {
        return "redirect:/item.html";
    }
}
