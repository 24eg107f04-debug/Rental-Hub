
package com.rentalhub.controller;

import com.rentalhub.dto.UserResponse;
import com.rentalhub.entity.User;
import com.rentalhub.repository.UserRepository;
import com.rentalhub.security.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public AuthController(
            UserRepository users,
            PasswordEncoder encoder,
            JwtUtil jwtUtil
    ) {
        this.users = users;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        if (user.getName() == null || user.getName().isBlank()
                || user.getEmail() == null || user.getEmail().isBlank()
                || user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Name, email and password are required"));
        }

        String email = user.getEmail().trim().toLowerCase();

        if (users.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message",
                            "Email already registered"));
        }

        user.setId(null);
        user.setName(user.getName().trim());
        user.setEmail(email);
        user.setRole("USER");
        user.setPassword(encoder.encode(user.getPassword()));

        User savedUser = users.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.from(savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> body
    ) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Email and password are required"));
        }

        return users.findByEmail(email.trim().toLowerCase())
                .filter(user ->
                        encoder.matches(password, user.getPassword()))
                .<ResponseEntity<?>>map(user -> {
                    Map<String, Object> result = new HashMap<>();

                    result.put("user", UserResponse.from(user));
                    result.put(
                            "token",
                            jwtUtil.generateToken(
                                    user.getEmail(),
                                    user.getRole()
                            )
                    );

                    return ResponseEntity.ok(result);
                })
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message",
                                        "Invalid email or password")));
    }
}
