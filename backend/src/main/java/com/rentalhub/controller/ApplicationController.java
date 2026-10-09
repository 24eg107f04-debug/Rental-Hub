package com.rentalhub.controller;

import com.rentalhub.dto.ApplicationResponse;
import com.rentalhub.entity.Application;
import com.rentalhub.entity.Property;
import com.rentalhub.entity.User;
import com.rentalhub.repository.ApplicationRepository;
import com.rentalhub.repository.PropertyRepository;
import com.rentalhub.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationRepository applications;
    private final UserRepository users;
    private final PropertyRepository properties;

    public ApplicationController(ApplicationRepository applications, UserRepository users, PropertyRepository properties) {
        this.applications = applications; this.users = users; this.properties = properties;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> apply(@RequestBody Map<String, Long> body, Authentication auth) {
        Long propertyId = body.get("propertyId");
        if (propertyId == null) return ResponseEntity.badRequest().body(Map.of("message", "propertyId is required"));
        User u = users.findByEmail(auth.getName()).orElse(null);
        Property p = properties.findById(propertyId).orElse(null);
        if (u == null || p == null) return ResponseEntity.badRequest().body(Map.of("message", "Invalid user or property"));
        if (applications.existsByUserIdAndPropertyId(u.getId(), p.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "You already applied for this property"));
        }
        Application saved = applications.save(new Application(null, u, p, "PENDING"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApplicationResponse.from(saved));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ApplicationResponse> all() {
        return applications.findAll().stream().map(ApplicationResponse::from).toList();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> userApplications(@PathVariable Long userId, Authentication auth) {
        User u = users.findByEmail(auth.getName()).orElse(null);
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && (u == null || !u.getId().equals(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You can view only your applications"));
        }
        return ResponseEntity.ok(applications.findByUserId(userId).stream().map(ApplicationResponse::from).toList());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(@PathVariable Long id) { return setStatus(id, "PENDING", "APPROVED"); }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(@PathVariable Long id) { return setStatus(id, "PENDING", "REJECTED"); }

    @PutMapping("/{id}/cancel-approval")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelApproval(@PathVariable Long id) { return setStatus(id, "APPROVED", "CANCELLED"); }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> cancel(@PathVariable Long id, Authentication auth) {
        Application a = applications.findById(id).orElse(null);
        User u = users.findByEmail(auth.getName()).orElse(null);
        if (a == null) return ResponseEntity.notFound().build();
        if (u == null || !a.getUser().getId().equals(u.getId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (!a.getStatus().equals("PENDING") && !a.getStatus().equals("APPROVED")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only pending or approved applications can be cancelled"));
        }
        a.setStatus("CANCELLED");
        return ResponseEntity.ok(ApplicationResponse.from(applications.save(a)));
    }

    private ResponseEntity<?> setStatus(Long id, String expected, String next) {
        Application a = applications.findById(id).orElse(null);
        if (a == null) return ResponseEntity.notFound().build();
        if (!expected.equals(a.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Application must be " + expected + " before it can be changed to " + next));
        }
        a.setStatus(next);
        return ResponseEntity.ok(ApplicationResponse.from(applications.save(a)));
    }
}
