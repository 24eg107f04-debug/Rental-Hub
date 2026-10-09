package com.rentalhub.config;

import com.rentalhub.entity.User;
import com.rentalhub.repository.UserRepository;
import com.rentalhub.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean UserDetailsService userDetailsService(UserRepository repo) {
        return email -> repo.findByEmail(email).map(this::details)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private UserDetails details(User u) {
        return org.springframework.security.core.userdetails.User.withUsername(u.getEmail())
                .password(u.getPassword()).roles(u.getRole()).build();
    }

    @Bean SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/properties/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/properties/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/properties/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/properties/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
