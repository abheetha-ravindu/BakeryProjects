package com.example.bakeryprojects.services;

import com.example.bakeryprojects.model.Delivery;
import com.example.bakeryprojects.repository.DeliveryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    public List<Delivery> findAll() {
        return deliveryRepository.findAll();
    }

    public Delivery create(Delivery delivery) {
        if (delivery.getStatus() == null || delivery.getStatus().trim().isEmpty()) {
            delivery.setStatus("Pending");
        }
        return deliveryRepository.save(delivery);
    }

    public void updateStatus(Long id, String status) {
        Delivery delivery = deliveryRepository.findById(id).orElse(null);
        if (delivery != null) {
            delivery.setStatus(status);
            deliveryRepository.save(delivery);
        }
    }

    public void deleteById(Long id) {
        deliveryRepository.deleteById(id);
    }
}
