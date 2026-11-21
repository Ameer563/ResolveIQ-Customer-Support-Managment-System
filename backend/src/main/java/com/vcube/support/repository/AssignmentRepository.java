package com.vcube.support.repository;

import com.vcube.support.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<TicketAssignment, Long> {
    Optional<TicketAssignment> findTopByTicketOrderByAssignedAtDescIdDesc(Ticket ticket);
    long countByAgent(User agent);
}
