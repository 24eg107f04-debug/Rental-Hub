package com.rentalhub.controller;

import com.rentalhub.dto.UserResponse;
import com.rentalhub.entity.User;
import com.rentalhub.repository.UserRepository;
import com.rentalhub.security.JwtUtil;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.users = users; this.encoder = encoder; this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (users.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email already registered"));
        }
        user.setId(null); user.setRole("USER"); user.setPassword(encoder.encode(user.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(users.save(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        if (email == null || password == null) return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
        return users.findByEmail(email)
                .filter(u -> encoder.matches(password, u.getPassword()))
                .<ResponseEntity<?>>map(u -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("user", UserResponse.from(u));
                    result.put("token", jwtUtil.generateToken(u.getEmail(), u.getRole()));
                    return ResponseEntity.ok(result);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password")));
    }
}
