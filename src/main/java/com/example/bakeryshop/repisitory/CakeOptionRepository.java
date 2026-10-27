package com.example.bakeryshop.repisitory;

import com.example.bakeryshop.model.CakeOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CakeOptionRepository extends JpaRepository<CakeOption, Long> {
}