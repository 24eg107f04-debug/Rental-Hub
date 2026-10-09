package com.rentalhub.config;

import com.rentalhub.entity.Property;
import com.rentalhub.entity.User;
import com.rentalhub.repository.PropertyRepository;
import com.rentalhub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner init(PropertyRepository properties, UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (properties.count() == 0) {
                properties.save(new Property(null, "2BHK Apartment", "Hyderabad", 15000.0,
                        "Modern 2BHK apartment with parking.", "Suresh Kumar", "suresh@example.com", "9876543210"));
                properties.save(new Property(null, "1BHK Flat", "Secunderabad", 10000.0,
                        "Affordable 1BHK flat near transport.", "Anita Reddy", "anita@example.com", "9123456780"));
                properties.save(new Property(null, "3BHK Family Home", "Madhapur", 28000.0,
                        "Spacious family home with modern facilities.", "Ramesh Rao", "ramesh@example.com", "9988776655"));
            } else {
                // Give older properties owner details if they came from a previous version.
                properties.findAll().forEach(p -> {
                    if (p.getOwnerName() == null || p.getOwnerName().isBlank()) p.setOwnerName("Property Owner");
                    if (p.getOwnerEmail() == null || p.getOwnerEmail().isBlank()) p.setOwnerEmail("owner@example.com");
                    if (p.getOwnerPhone() == null || p.getOwnerPhone().isBlank()) p.setOwnerPhone("9000000000");
                    properties.save(p);
                });
            }
            if (users.findByEmail("admin@rentalhub.com").isEmpty())
                users.save(new User(null, "Rental Hub Admin", "admin@rentalhub.com", encoder.encode("admin123"), "ADMIN"));
            if (users.findByEmail("user@rentalhub.com").isEmpty())
                users.save(new User(null, "Demo User", "user@rentalhub.com", encoder.encode("user123"), "USER"));
        };
    }
}
