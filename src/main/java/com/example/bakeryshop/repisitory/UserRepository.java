package com.example.bakeryshop.repisitory;

import com.example.bakeryshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

// Accesses User persistence logic.
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByEmail(String email);
}
