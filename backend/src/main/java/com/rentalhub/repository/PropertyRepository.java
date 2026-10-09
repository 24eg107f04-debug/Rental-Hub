package com.rentalhub.repository;

import com.rentalhub.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(
            String title,
            String location
    );
}
