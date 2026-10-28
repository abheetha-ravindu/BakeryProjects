package com.example.bakeryshop;

import com.bakery.bakeryplatform.model.BaseEntity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.example.bakeryshop", "com.example.bakeryprojects", "com.bakery.bakeryplatform"})
@EntityScan(basePackageClasses = {BakeryShopApplication.class, BaseEntity.class, com.example.bakeryprojects.model.User.class})
@EnableJpaRepositories(basePackages = {
        "com.example.bakeryshop.repisitory",
        "com.example.bakeryprojects.repository",
        "com.bakery.bakeryplatform.repository",
        "com.example.bakeryshop.billing"
})
public class BakeryShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(BakeryShopApplication.class, args);
    }

}
