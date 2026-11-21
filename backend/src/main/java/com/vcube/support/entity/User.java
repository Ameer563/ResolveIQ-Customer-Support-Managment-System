package com.vcube.support.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "users") @Getter @Setter @NoArgsConstructor
public class User {
    public enum Role { CUSTOMER, AGENT, ADMIN }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String password;
    private String phone;

    @Column(length = 20, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING) private Role role = Role.CUSTOMER;

    private LocalDateTime createdAt = LocalDateTime.now();
}