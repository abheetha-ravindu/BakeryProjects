package com.example.bakeryshop.repisitory;

import com.example.bakeryshop.model.CakeDesign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CakeDesignRepository extends JpaRepository<CakeDesign, Long> {
}