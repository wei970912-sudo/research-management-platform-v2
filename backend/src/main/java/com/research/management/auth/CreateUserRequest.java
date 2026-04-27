package com.research.management.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String displayName,
        @NotBlank
        @Pattern(regexp = "ADMIN|VIEWER", message = "角色必须为 ADMIN 或 VIEWER")
        String role
) {
}
