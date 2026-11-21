package com.vcube.support.controller;

import com.vcube.support.dto.Dtos.*;
import com.vcube.support.entity.Category;
import com.vcube.support.repository.CategoryRepository;
import com.vcube.support.service.*;
import com.vcube.support.service.TicketService.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class TicketController {
    private final TicketService s;
    private final CategoryRepository cats;
    private final AiService ai;

    @GetMapping("/categories") public List<Category> categories() { return cats.findAll(); }

    @PostMapping("/tickets")
    public TicketView create(@Valid @RequestBody TicketReq r, Authentication a) { return s.create(s.me(a), r.subject(), r.description()); }

    @GetMapping("/tickets/mine") public List<TicketView> mine(Authentication a) { return s.mine(s.me(a)); }

    @GetMapping("/tickets/{id}") public TicketView get(@PathVariable Long id, Authentication a) { return s.view(s.allowed(id, s.me(a))); }

    @GetMapping("/tickets/{id}/comments") public List<CommentView> comments(@PathVariable Long id, Authentication a) { return s.comments(id, s.me(a)); }

    @PostMapping("/tickets/{id}/comments")
    public Map<String, String> comment(@PathVariable Long id, @Valid @RequestBody CommentReq r, Authentication a) {
        s.addComment(id, s.me(a), r.message(), r.internal());
        return Map.of("message", "Comment added");
    }

    @PostMapping("/tickets/{id}/feedback")
    public TicketView feedback(@PathVariable Long id, @Valid @RequestBody FeedbackReq r, Authentication a) {
        return s.feedback(id, s.me(a), r.rating(), r.feedback());
    }

    /** AI assistant chat - grounded in the logged-in customer's real tickets */
    @PostMapping("/ai/assistant")
    public Map<String, String> assistant(@Valid @RequestBody ChatReq r, Authentication a) {
        return Map.of("reply", ai.assistant(r.message(), s.mineRaw(s.me(a))));
    }
}
