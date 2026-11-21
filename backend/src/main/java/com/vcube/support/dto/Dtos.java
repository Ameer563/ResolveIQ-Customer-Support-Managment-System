package com.vcube.support.dto;

import jakarta.validation.constraints.*;

public final class Dtos {
    private Dtos() {}
    public record RegisterReq(@NotBlank String name, @NotBlank @Email String email,
                              @NotBlank @Size(min = 6, message = "must be at least 6 characters") String password, String phone) {}
    public record LoginReq(@NotBlank String email, @NotBlank String password) {}
    public record AuthRes(String token, String name, String email, String role) {}
    public record TicketReq(@NotBlank @Size(max = 150) String subject,
                            @NotBlank @Size(min = 10, message = "must be at least 10 characters") String description) {}
    public record CommentReq(@NotBlank String message, boolean internal) {}
    public record FeedbackReq(@Min(1) @Max(5) int rating, @Size(max = 1000) String feedback) {}
    public record ValueReq(@NotBlank String value) {}
    public record AssignReq(@NotNull Long agentId) {}
    public record ChatReq(@NotBlank String message) {}
}
