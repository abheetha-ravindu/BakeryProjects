package com.example.bakeryshop.config;

import com.example.bakeryshop.model.AdminUser;
import com.example.bakeryshop.repository.AdminUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedAdmin(AdminUserRepository adminRepo) {
        return args -> {
            if (adminRepo.findByUsername("admin") == null) {
                adminRepo.save(new AdminUser("admin", "admin@sweetbloom.com", "admin123"));
            }
        };
    }
}
