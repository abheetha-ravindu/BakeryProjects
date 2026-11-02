package com.example.bakeryshop.repisitory;

import com.example.bakeryshop.model.CakeOption;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CakeOptionRepository extends JpaRepository<CakeOption, Long> {
}