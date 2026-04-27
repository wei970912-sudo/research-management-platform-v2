package com.research.management.assistant;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "assistant.openai-compatible")
public record OpenAiCompatibleProperties(
        boolean enabled,
        String baseUrl,
        String apiKey,
        String model,
        String systemPrompt
) {
}

