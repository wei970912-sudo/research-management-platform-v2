package com.research.management.auth;

public record AuthUserResponse(
        String username,
        String displayName,
        String role
) {
}
