package com.research.management.assistant;

import jakarta.validation.constraints.NotBlank;

public record AssistantChatRequest(
        @NotBlank(message = "message must not be blank")
        String message,
        String projectId
) {
}
