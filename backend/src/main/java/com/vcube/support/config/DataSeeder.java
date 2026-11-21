package com.vcube.support.config;

import com.vcube.support.entity.*;
import com.vcube.support.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;

/** Seeds categories + demo staff accounts on first start. */
@Component @RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final CategoryRepository cats;
    private final UserRepository users;
    private final PasswordEncoder enc;

    @Override
    public void run(String... args) {
        for (String n : List.of("Billing", "Refund", "Technical Issue", "Delivery", "Product Quality", "Account Access", "General Inquiry")) {
            if (cats.findByNameIgnoreCase(n).isEmpty()) {
                Category c = new Category(); c.setName(n); c.setDescription(n + " related complaints"); cats.save(c);
            }
        }
        seed("Admin", "admin@support.com", "Admin@123", User.Role.ADMIN);
        seed("Priya Agent", "agent@support.com", "Agent@123", User.Role.AGENT);
        seed("Ravi Agent", "agent2@support.com", "Agent@123", User.Role.AGENT);
    }

    private void seed(String name, String email, String pw, User.Role role) {
        if (users.existsByEmail(email)) return;
        User u = new User(); u.setName(name); u.setEmail(email); u.setPassword(enc.encode(pw)); u.setRole(role);
        users.save(u);
    }
}
