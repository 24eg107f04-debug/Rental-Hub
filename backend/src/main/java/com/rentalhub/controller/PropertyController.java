package com.rentalhub.controller;

import com.rentalhub.entity.Property;
import com.rentalhub.repository.PropertyRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin(origins = "http://localhost:5173")
public class PropertyController {

    private final PropertyRepository propertyRepository;

    public PropertyController(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @GetMapping
    public List<Property> getAllProperties(
            @RequestParam(required = false) String search) {

        if (search == null || search.trim().isEmpty()) {
            return propertyRepository.findAll();
        }

        return propertyRepository
                .findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(search, search);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getProperty(@PathVariable Long id) {
        return propertyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Property> addProperty(@Valid @RequestBody Property property) {
        property.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(propertyRepository.save(property));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Property> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody Property updatedProperty) {

        return propertyRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(updatedProperty.getTitle());
                    existing.setLocation(updatedProperty.getLocation());
                    existing.setPrice(updatedProperty.getPrice());
                    existing.setDescription(updatedProperty.getDescription());
                    existing.setOwnerName(updatedProperty.getOwnerName());
                    existing.setOwnerEmail(updatedProperty.getOwnerEmail());
                    existing.setOwnerPhone(updatedProperty.getOwnerPhone());

                    return ResponseEntity.ok(propertyRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        if (!propertyRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        propertyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
