package com.vcube.support.service;

import com.vcube.support.entity.*;
import com.vcube.support.entity.Ticket.*;
import com.vcube.support.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class TicketService {
    private final TicketRepository tickets;
    private final CategoryRepository cats;
    private final UserRepository users;
    private final AssignmentRepository assigns;
    private final CommentRepository comments;
    private final AiService ai;

    public record TicketView(Long id, String ticketNumber, String subject, String description, String category, String status,
                             String priority, String sentiment, String aiSummary, String suggestedResolution, String aiReply,
                             Integer rating, String feedback, String customer, String assignedTo, Long assignedToId,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {}
    public record CommentView(Long id, String author, String role, String message, boolean internal, LocalDateTime createdAt) {}

    public User me(Authentication a) {
        return users.findByEmail(a.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login again"));
    }

    public TicketView view(Ticket t) {
        var asg = assigns.findTopByTicketOrderByAssignedAtDescIdDesc(t);
        return new TicketView(t.getId(), t.getTicketNumber(), t.getSubject(), t.getDescription(),
                t.getCategory() == null ? "Uncategorized" : t.getCategory().getName(), t.getStatus().name(), t.getPriority().name(),
                t.getSentiment(), t.getAiSummary(), t.getSuggestedResolution(), t.getAiReply(), t.getRating(), t.getFeedback(),
                t.getCustomer().getName(), asg.map(x -> x.getAgent().getName()).orElse(null), asg.map(x -> x.getAgent().getId()).orElse(null),
                t.getCreatedAt(), t.getUpdatedAt());
    }

    /** Ticket generation: AI classifies, scores sentiment, predicts priority, then auto-assigns to least-loaded agent. */
    public TicketView create(User u, String subject, String desc) {
        var an = ai.analyze(subject, desc, cats.findAll().stream().map(Category::getName).toList());
        Ticket t = new Ticket();
        t.setCustomer(u); t.setSubject(subject.trim()); t.setDescription(desc.trim());
        t.setCategory(cats.findByNameIgnoreCase(an.category()).or(() -> cats.findByNameIgnoreCase("General Inquiry")).orElse(null));
        t.setPriority(Priority.valueOf(an.priority())); t.setSentiment(an.sentiment()); t.setAiSummary(an.summary());
        tickets.save(t);
        t.setTicketNumber("TKT-" + (1000 + t.getId()));
        tickets.save(t);
        users.findByRole(User.Role.AGENT).stream().min(Comparator.comparingLong(assigns::countByAgent))
                .ifPresent(ag -> doAssign(t, ag, null));
        return view(t);
    }

    private void doAssign(Ticket t, User agent, User by) {
        TicketAssignment a = new TicketAssignment(); a.setTicket(t); a.setAgent(agent); a.setAssignedBy(by); assigns.save(a);
    }

    public List<TicketView> mine(User u) { return tickets.findByCustomerOrderByCreatedAtDesc(u).stream().map(this::view).toList(); }
    public List<TicketView> all() { return tickets.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList(); }
    public List<Ticket> mineRaw(User u) { return tickets.findByCustomerOrderByCreatedAtDesc(u); }

    public Ticket find(Long id) {
        return tickets.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
    }
    public Ticket allowed(Long id, User u) {
        Ticket t = find(id);
        if (u.getRole() == User.Role.CUSTOMER && !t.getCustomer().getId().equals(u.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your ticket");
        return t;
    }

    public List<CommentView> comments(Long id, User u) {
        Ticket t = allowed(id, u);
        boolean staff = u.getRole() != User.Role.CUSTOMER;
        return comments.findByTicketOrderByCreatedAtAsc(t).stream().filter(c -> staff || !c.isInternal())
                .map(c -> new CommentView(c.getId(), c.getAuthor().getName(), c.getAuthor().getRole().name(), c.getMessage(), c.isInternal(), c.getCreatedAt()))
                .toList();
    }

    public void addComment(Long id, User u, String msg, boolean internal) {
        Ticket t = allowed(id, u);
        boolean staff = u.getRole() != User.Role.CUSTOMER;
        TicketComment c = new TicketComment();
        c.setTicket(t); c.setAuthor(u); c.setMessage(msg.trim()); c.setInternal(staff && internal);
        comments.save(c);
        if (staff && !c.isInternal() && t.getStatus() == Status.OPEN) { t.setStatus(Status.IN_PROGRESS); tickets.save(t); }
    }

    public TicketView feedback(Long id, User u, int rating, String fb) {
        Ticket t = allowed(id, u);
        if (!t.getCustomer().getId().equals(u.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the customer can rate");
        if (t.getStatus() != Status.RESOLVED && t.getStatus() != Status.CLOSED)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feedback is possible once the ticket is resolved");
        if (t.getRating() != null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Feedback already submitted");
        t.setRating(rating); t.setFeedback(fb);
        return view(tickets.save(t));
    }

    // ---- staff operations ----
    public TicketView setStatus(Long id, String v) {
        Ticket t = find(id);
        try { t.setStatus(Status.valueOf(v.toUpperCase())); } catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status"); }
        return view(tickets.save(t));
    }
    public TicketView setPriority(Long id, String v) {
        Ticket t = find(id);
        try { t.setPriority(Priority.valueOf(v.toUpperCase())); } catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid priority"); }
        return view(tickets.save(t));
    }
    public TicketView assign(Long id, Long agentId, User by) {
        Ticket t = find(id);
        User agent = users.findById(agentId).filter(a -> a.getRole() != User.Role.CUSTOMER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agent not found"));
        doAssign(t, agent, by);
        return view(t);
    }
    public TicketView aiResolution(Long id) { Ticket t = find(id); t.setSuggestedResolution(ai.suggestResolution(t)); return view(tickets.save(t)); }
    public TicketView aiReply(Long id) { Ticket t = find(id); t.setAiReply(ai.customerReply(t)); return view(tickets.save(t)); }

    // ---- support analytics ----
    public Map<String, Object> analytics() {
        List<Ticket> all = tickets.findAll();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", all.size());
        m.put("byStatus", group(all, t -> t.getStatus().name()));
        m.put("byPriority", group(all, t -> t.getPriority().name()));
        m.put("byCategory", group(all, t -> t.getCategory() == null ? "Uncategorized" : t.getCategory().getName()));
        m.put("bySentiment", group(all, t -> t.getSentiment() == null ? "NEUTRAL" : t.getSentiment()));
        m.put("avgRating", all.stream().filter(t -> t.getRating() != null).mapToInt(Ticket::getRating).average().orElse(0));
        m.put("ratedCount", all.stream().filter(t -> t.getRating() != null).count());
        return m;
    }
    private Map<String, Long> group(List<Ticket> l, Function<Ticket, String> f) {
        return l.stream().collect(Collectors.groupingBy(f, TreeMap::new, Collectors.counting()));
    }
}
