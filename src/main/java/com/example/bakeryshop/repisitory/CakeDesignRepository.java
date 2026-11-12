package com.example.bakeryshop.repisitory;

import com.example.bakeryshop.model.CakeDesign;
import org.springframework.data.jpa.repository.JpaRepository;
// Accesses Cake Design persistence logic.

public interface CakeDesignRepository extends JpaRepository<CakeDesign, Long> {
}