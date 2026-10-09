package com.rentalhub.controller;

import com.rentalhub.dto.UserResponse;
import com.rentalhub.entity.User;
import com.rentalhub.repository.ApplicationRepository;
import com.rentalhub.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository users;
    private final ApplicationRepository apps;

    public UserController(UserRepository users, ApplicationRepository apps) {
        this.users = users;
        this.apps = apps;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> all() {
        return users.findAll().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable Long id, Authentication auth) {
        User target = users.findById(id).orElse(null);
        if (target == null) return ResponseEntity.notFound().build();
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !target.getEmail().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You can view only your own profile"));
        }
        return ResponseEntity.ok(UserResponse.from(target));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        User target = users.findById(id).orElse(null);
        if (target == null) return ResponseEntity.notFound().build();
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if ("ADMIN".equals(target.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin account cannot be deleted"));
        }
        if (!admin && !target.getEmail().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You can delete only your own account"));
        }
        apps.deleteAll(apps.findByUserId(id));
        users.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
