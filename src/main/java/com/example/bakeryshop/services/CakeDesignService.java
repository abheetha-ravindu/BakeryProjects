package com.example.bakeryshop.services;

import com.example.bakeryshop.model.CakeDesign;
import com.example.bakeryshop.repisitory.CakeDesignRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
// Provides Cake Design service logic.
public class CakeDesignService {

    private final CakeDesignRepository repository;

    public CakeDesignService(CakeDesignRepository repository) {
        this.repository = repository;
    }

    public CakeDesign saveDesign(CakeDesign design) {
        return repository.save(design);
    }

    public List<CakeDesign> getAllDesigns() {
        return repository.findAll();
    }

    public CakeDesign updateDesign(Long id, CakeDesign updatedDesign) {
        CakeDesign existing = repository.findById(id).orElse(null);
        if (existing != null) {
            existing.setShape(updatedDesign.getShape());
            existing.setFlavor(updatedDesign.getFlavor());
            existing.setDecoration(updatedDesign.getDecoration());
            existing.setFilling(updatedDesign.getFilling());
            existing.setCakeMessage(updatedDesign.getCakeMessage());
            existing.setEstimatedPrice(updatedDesign.getEstimatedPrice());
            return repository.save(existing);
        }
        return null;
    }

    public void deleteDesign(Long id) {
        repository.deleteById(id);
    }
}