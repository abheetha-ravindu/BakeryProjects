package com.example.bakeryshop.services;

import com.example.bakeryshop.model.CakeOption;
import com.example.bakeryshop.model.Flavour;
import com.example.bakeryshop.model.Size;
import com.example.bakeryshop.model.Topping;
import com.example.bakeryshop.repisitory.CakeOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CakeOptionService {

    @Autowired
    private CakeOptionRepository repository;

    public List<CakeOption> getAllOptions() {
        return repository.findAll();
    }

    public List<CakeOption> getAvailableOptions() {
        return repository.findAll();
    }

    public CakeOption createOption(Map<String, Object> payload) {
        String type = (String) payload.get("optionType");
        String name = (String) payload.get("name");
        double price = Double.parseDouble(payload.get("price").toString());

        CakeOption option;

        // Polymorphism
        if ("FLAVOUR".equalsIgnoreCase(type)) {
            option = new Flavour();
        } else if ("SIZE".equalsIgnoreCase(type) || "SHAPE".equalsIgnoreCase(type)) {
            option = new Size();
        } else {
            option = new Topping();
        }

        option.setName(name);
        option.setPrice(price);

        return repository.save(option);
    }

    public CakeOption updateOption(Long id, Map<String, Object> payload) {
        CakeOption option = repository.findById(id).orElse(null);
        if (option != null) {
            if (payload.containsKey("name")) {
                option.setName((String) payload.get("name"));
            }
            if (payload.containsKey("price")) {
                option.setPrice(Double.parseDouble(payload.get("price").toString()));
            }
            return repository.save(option);
        }
        return null;
    }

    public void deleteOption(Long id) {
        repository.deleteById(id);
    }
}