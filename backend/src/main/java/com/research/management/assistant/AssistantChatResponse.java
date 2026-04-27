package com.research.management.assistant;

import java.util.List;

public record AssistantChatResponse(
        String provider,
        String model,
        String content,
        String groundedProjectId,
        List<AssistantReference> references
) {
    public record AssistantReference(
            String type,
            String id,
            String title
    ) {
    }
}

