package com.research.management.auth;

public record UserSummaryResponse(
        String username,
        String displayName,
        String role
) {
}
