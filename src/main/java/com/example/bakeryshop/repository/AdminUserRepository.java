package com.example.bakeryshop.repository;

import com.example.bakeryshop.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    AdminUser findByUsername(String username);

    AdminUser findByEmail(String email);
}
