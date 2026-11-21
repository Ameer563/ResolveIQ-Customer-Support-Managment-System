package com.vcube.support.controller;

import com.vcube.support.dto.Dtos.*;
import com.vcube.support.entity.User;
import com.vcube.support.repository.UserRepository;
import com.vcube.support.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder enc;
    private final JwtService jwt;

    @GetMapping("/health") public Map<String, String> health() { return Map.of("status", "UP"); }

    @PostMapping("/auth/register")
    public Map<String, String> register(@Valid @RequestBody RegisterReq r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        User u = new User();
        u.setName(r.name().trim()); u.setEmail(email); u.setPhone(r.phone()); u.setPassword(enc.encode(r.password())); u.setRole(User.Role.CUSTOMER);
        users.save(u);
        return Map.of("message", "Registered successfully. Please login.");
    }

    @PostMapping("/auth/login")
    public AuthRes login(@Valid @RequestBody LoginReq r) {
        User u = users.findByEmail(r.email().trim().toLowerCase())
                .filter(x -> enc.matches(r.password(), x.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return new AuthRes(jwt.generate(u.getEmail(), u.getRole().name()), u.getName(), u.getEmail(), u.getRole().name());
    }
}
