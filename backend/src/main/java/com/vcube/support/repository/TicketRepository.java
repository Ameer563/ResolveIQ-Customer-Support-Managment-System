package com.vcube.support.repository;

import com.vcube.support.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Ticket> findAllByOrderByCreatedAtDesc();
}
