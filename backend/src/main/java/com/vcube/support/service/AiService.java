package com.vcube.support.service;

import com.fasterxml.jackson.databind.*;
import com.vcube.support.entity.Ticket;
import org.slf4j.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * All LLM features go through Spring AI's ChatClient (backend only).
 * If the LLM is unreachable / no API key, rule-based fallbacks keep the app working.
 */
@Service
public class AiService {
    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private final ChatClient chat;
    private final ObjectMapper om = new ObjectMapper();

    public AiService(ChatClient.Builder builder) { this.chat = builder.build(); }

    public record Analysis(String category, String sentiment, String priority, String summary) {}

    /** user text is passed as a template parameter so braces in complaints never break the prompt */
    private String ask(String system, String input) {
        return chat.prompt().system(system).user(u -> u.text("{input}").param("input", input)).call().content();
    }

    // 1+2+3) Complaint classification, sentiment analysis, priority prediction (+ short summary)
    public Analysis analyze(String subject, String desc, List<String> cats) {
        try {
            String out = ask("You are a customer-support triage engine. Reply with ONE JSON object only, no markdown, "
                    + "using exactly these keys: category, sentiment, priority, summary. "
                    + "category must be exactly one of: " + String.join(", ", cats) + ". "
                    + "sentiment must be POSITIVE, NEUTRAL or NEGATIVE. "
                    + "priority must be LOW, MEDIUM, HIGH or CRITICAL (CRITICAL = security breach, money lost, total outage; "
                    + "HIGH = very angry customer or blocked service; MEDIUM = normal issue; LOW = question or minor feedback). "
                    + "summary is at most 20 words.", "Subject: " + subject + "\nComplaint: " + desc);
            JsonNode n = om.readTree(out.substring(out.indexOf('{'), out.lastIndexOf('}') + 1));
            return new Analysis(n.path("category").asText("General Inquiry"),
                    pick(n.path("sentiment").asText(), "POSITIVE", "NEGATIVE", "NEUTRAL"),
                    pick(n.path("priority").asText(), "LOW", "HIGH", "CRITICAL", "MEDIUM"),
                    n.path("summary").asText(""));
        } catch (Exception e) {
            log.warn("LLM unavailable, using rule-based analysis: {}", e.getMessage());
            return fallback((subject + " " + desc).toLowerCase());
        }
    }

    // 4) Suggested resolution for the support agent
    public String suggestResolution(Ticket t) {
        try {
            return ask("You are a senior customer-support lead. Give the agent a concise numbered resolution plan (max 5 steps) "
                    + "for this complaint. Be practical and specific.", ticketText(t));
        } catch (Exception e) {
            return "1. Acknowledge the issue and confirm details with the customer.\n2. Investigate the " + name(t)
                    + " problem in the relevant system.\n3. Apply a fix or compensation as per policy.\n4. Update the customer and confirm resolution.\n5. Close the ticket after feedback.";
        }
    }

    // 5) Customer response generator
    public String customerReply(Ticket t) {
        try {
            return ask("You are a support agent. Write a polite, empathetic reply (max 120 words) to the customer about their complaint. "
                    + "Match the tone to their sentiment. Sign off as 'Support Team'.", ticketText(t));
        } catch (Exception e) {
            return "Hi " + t.getCustomer().getName() + ",\n\nThank you for contacting us about \"" + t.getSubject()
                    + "\". We're sorry for the inconvenience and our team is working on it. We'll update you shortly.\n\nSupport Team";
        }
    }

    // AI assistant chat - uses the customer's real ticket data
    public String assistant(String message, List<Ticket> tickets) {
        StringBuilder sb = new StringBuilder("Customer's tickets:\n");
        tickets.stream().limit(8).forEach(t -> sb.append(t.getTicketNumber()).append(" | ").append(t.getSubject())
                .append(" | status ").append(t.getStatus()).append(" | priority ").append(t.getPriority()).append('\n'));
        if (tickets.isEmpty()) sb.append("(none yet)\n");
        try {
            return ask("You are the virtual assistant of a customer-support portal. Help customers raise complaints, track tickets "
                    + "and understand statuses (OPEN, IN_PROGRESS, RESOLVED, CLOSED). Use the ticket data given. Be brief, friendly and accurate; "
                    + "never invent ticket data. Pages: Raise Complaint, Track Complaints.", sb + "\nQuestion: " + message);
        } catch (Exception e) {
            String m = message.toLowerCase();
            if (m.contains("status") || m.contains("track") || m.contains("ticket"))
                return tickets.isEmpty() ? "You have no tickets yet. Use 'Raise Complaint' to create one."
                        : sb.toString() + "Open 'Track Complaints' for full details.";
            return "I can help you raise a complaint (Raise Complaint page) or check a ticket (Track Complaints page). What do you need?";
        }
    }

    private String ticketText(Ticket t) {
        return "Ticket " + t.getTicketNumber() + "\nSubject: " + t.getSubject() + "\nCategory: " + name(t) + "\nPriority: " + t.getPriority()
                + "\nSentiment: " + t.getSentiment() + "\nCustomer: " + t.getCustomer().getName() + "\nComplaint: " + t.getDescription();
    }
    private String name(Ticket t) { return t.getCategory() == null ? "General" : t.getCategory().getName(); }

    /** last arg = default */
    private String pick(String v, String... opts) {
        for (int i = 0; i < opts.length - 1; i++) if (opts[i].equalsIgnoreCase(v.trim())) return opts[i];
        return opts[opts.length - 1];
    }

    private Analysis fallback(String s) {
        String cat = has(s, "refund", "money back") ? "Refund" : has(s, "payment", "bill", "charge", "invoice") ? "Billing"
                : has(s, "deliver", "shipping", "courier", "parcel", "package") ? "Delivery"
                : has(s, "broken", "damaged", "defect", "quality") ? "Product Quality"
                : has(s, "login", "password", "account", "otp") ? "Account Access"
                : has(s, "error", "bug", "crash", "not working", "app", "website") ? "Technical Issue" : "General Inquiry";
        String sent = has(s, "angry", "worst", "terrible", "horrible", "fraud", "scam", "useless", "bad", "not working", "late", "disappointed") ? "NEGATIVE"
                : has(s, "thank", "great", "good", "love", "happy") ? "POSITIVE" : "NEUTRAL";
        String pr = has(s, "fraud", "hacked", "unauthorized", "legal", "lawsuit", "charged twice", "outage") ? "CRITICAL"
                : has(s, "urgent", "asap", "immediately", "not working", "angry", "worst") ? "HIGH" : sent.equals("POSITIVE") ? "LOW" : "MEDIUM";
        return new Analysis(cat, sent, pr, "Customer reports a " + cat.toLowerCase() + " issue.");
    }
    private boolean has(String s, String... words) { for (String w : words) if (s.contains(w)) return true; return false; }
}
