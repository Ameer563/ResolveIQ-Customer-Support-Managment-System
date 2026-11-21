package com.vcube.support.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "tickets") @Getter @Setter @NoArgsConstructor
public class Ticket {
    public enum Status { OPEN, IN_PROGRESS, RESOLVED, CLOSED }
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true) private String ticketNumber;
    @Column(nullable = false, length = 150) private String subject;
    @Column(columnDefinition = "TEXT", nullable = false) private String description;
    @ManyToOne(optional = false) private User customer;
    @ManyToOne private Category category;

    @Column(length = 20, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING) private Status status = Status.OPEN;

    @Column(length = 20, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING) private Priority priority = Priority.MEDIUM;

    private String sentiment;                       // POSITIVE / NEUTRAL / NEGATIVE (AI)
    @Column(length = 500) private String aiSummary; // AI
    @Column(columnDefinition = "TEXT") private String suggestedResolution; // AI, for agents
    @Column(columnDefinition = "TEXT") private String aiReply;             // AI, draft customer reply
    private Integer rating;                         // customer feedback 1..5
    @Column(length = 1000) private String feedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
