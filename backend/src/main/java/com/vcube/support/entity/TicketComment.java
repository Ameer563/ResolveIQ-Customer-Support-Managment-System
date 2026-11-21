package com.vcube.support.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "ticket_comments") @Getter @Setter @NoArgsConstructor
public class TicketComment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Ticket ticket;
    @ManyToOne(optional = false) private User author;
    @Column(columnDefinition = "TEXT", nullable = false) private String message;
    private boolean internal;   // internal notes are visible to staff only
    private LocalDateTime createdAt = LocalDateTime.now();
}
