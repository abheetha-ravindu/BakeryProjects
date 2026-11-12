package com.example.bakeryshop.repisitory;

import com.example.bakeryshop.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
// Accesses Admin User persistence logic.

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    AdminUser findByUsername(String username);

    AdminUser findByEmail(String email);
}
