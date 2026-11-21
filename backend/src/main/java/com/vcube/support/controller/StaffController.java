package com.vcube.support.controller;

import com.vcube.support.dto.Dtos.*;
import com.vcube.support.entity.User;
import com.vcube.support.repository.UserRepository;
import com.vcube.support.service.TicketService;
import com.vcube.support.service.TicketService.TicketView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** Agent / Admin only (see SecurityConfig: /api/staff/** requires AGENT or ADMIN) */
@RestController @RequestMapping("/api/staff") @RequiredArgsConstructor
public class StaffController {
    private final TicketService s;
    private final UserRepository users;

    @GetMapping("/tickets") public List<TicketView> all() { return s.all(); }

    @GetMapping("/agents")
    public List<Map<String, Object>> agents() {
        return users.findByRole(User.Role.AGENT).stream().map(u -> Map.<String, Object>of("id", u.getId(), "name", u.getName())).toList();
    }

    @PutMapping("/tickets/{id}/status") public TicketView status(@PathVariable Long id, @Valid @RequestBody ValueReq r) { return s.setStatus(id, r.value()); }
    @PutMapping("/tickets/{id}/priority") public TicketView priority(@PathVariable Long id, @Valid @RequestBody ValueReq r) { return s.setPriority(id, r.value()); }
    @PutMapping("/tickets/{id}/assign")
    public TicketView assign(@PathVariable Long id, @Valid @RequestBody AssignReq r, Authentication a) { return s.assign(id, r.agentId(), s.me(a)); }

    @PostMapping("/tickets/{id}/ai/resolution") public TicketView resolution(@PathVariable Long id) { return s.aiResolution(id); }
    @PostMapping("/tickets/{id}/ai/reply") public TicketView reply(@PathVariable Long id) { return s.aiReply(id); }

    @GetMapping("/analytics") public Map<String, Object> analytics() { return s.analytics(); }
}
