package com.example.bakeryshop.controller;

import com.example.bakeryshop.model.CakeOption;
import com.example.bakeryshop.services.CakeOptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cake-options")
public class CakeOptionController {

    @Autowired
    private CakeOptionService cakeOptionService;

    // Read: Admin views all cake options
    @GetMapping
    public List<CakeOption> getAllOptions() {
        return cakeOptionService.getAllOptions();
    }

    // Read: Customer views active options for the cake builder
    @GetMapping("/available")
    public List<CakeOption> getAvailableOptions() {
        return cakeOptionService.getAvailableOptions();
    }

    // Create: Admin adds a new cake option
    @PostMapping
    public Map<String, Object> createOption(@RequestBody Map<String, Object> payload) {
        cakeOptionService.createOption(payload);
        return Map.of("success", true, "message", "Cake option added successfully!");
    }

    // Update: Admin edits a cake option
    @PutMapping("/{id}")
    public Map<String, Object> updateOption(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        cakeOptionService.updateOption(id, payload);
        return Map.of("success", true, "message", "Cake option updated successfully!");
    }

    // Delete: Admin removes a cake option
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteOption(@PathVariable Long id) {
        cakeOptionService.deleteOption(id);
        return Map.of("success", true, "message", "Cake option deleted successfully!");
    }
}