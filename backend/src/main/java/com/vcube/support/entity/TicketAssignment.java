package com.vcube.support.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "ticket_assignments") @Getter @Setter @NoArgsConstructor
public class TicketAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Ticket ticket;
    @ManyToOne(optional = false) private User agent;
    @ManyToOne private User assignedBy;   // null = auto-assigned
    private LocalDateTime assignedAt = LocalDateTime.now();
}
