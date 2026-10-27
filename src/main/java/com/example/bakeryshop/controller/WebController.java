package com.example.bakeryshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }

    @GetMapping("/items")
    public String items() {
        return "redirect:/item.html";
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
}
