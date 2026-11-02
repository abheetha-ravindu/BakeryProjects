package com.example.bakeryshop.controller;

import com.example.bakeryshop.model.CakeDesign;
import com.example.bakeryshop.services.CakeDesignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cake-designs")
public class CakeDesignController {

    @Autowired
    private CakeDesignService cakeDesignService;

    // Create: Customer saves a new custom design
    @PostMapping
    public Map<String, Object> saveDesign(@RequestBody CakeDesign design) {
        CakeDesign savedDesign = cakeDesignService.saveDesign(design);
        return Map.of("success", true, "designCode", "DES-" + savedDesign.getId());
    }

    // Read: Customer views their saved designs
    @GetMapping
    public List<CakeDesign> getSavedDesigns() {
        return cakeDesignService.getAllDesigns();
    }

    // Update: Customer edits a saved design
    @PutMapping("/{id}")
    public Map<String, Object> updateDesign(@PathVariable Long id, @RequestBody CakeDesign design) {
        cakeDesignService.updateDesign(id, design);
        return Map.of("success", true, "message", "Design updated successfully");
    }

    // Delete: Customer deletes a saved design
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteDesign(@PathVariable Long id) {
        cakeDesignService.deleteDesign(id);
        return Map.of("success", true, "message", "Design deleted successfully");
    }

    // Upload: Handle reference image uploads
    @PostMapping("/upload")
    public Map<String, Object> uploadReferenceImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = "/images/uploads/" + file.getOriginalFilename();
        return Map.of("success", true, "imageUrl", imageUrl);
    }
}