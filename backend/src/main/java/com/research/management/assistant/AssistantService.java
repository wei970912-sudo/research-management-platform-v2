package com.research.management.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.research.management.foundation.UnifiedDataStore;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Map;

@Service
public class AssistantService {

    private final OpenAiCompatibleProperties properties;
    private final UnifiedDataStore unifiedDataStore;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AssistantService(
            OpenAiCompatibleProperties properties,
            UnifiedDataStore unifiedDataStore,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.unifiedDataStore = unifiedDataStore;
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public AssistantChatResponse chat(AssistantChatRequest request) {
        String context = unifiedDataStore.buildAssistantContext(request.projectId());
        List<AssistantChatResponse.AssistantReference> references = unifiedDataStore.buildAssistantReferences(request.projectId())
                .stream()
                .map(item -> new AssistantChatResponse.AssistantReference(
                        item.get("type"),
                        item.get("id"),
                        item.get("title")
                ))
                .toList();

        if (!properties.enabled()) {
            return new AssistantChatResponse(
                    "openai-compatible",
                    properties.model(),
                    "Assistant is disabled. Enable assistant.openai-compatible.enabled in configuration.",
                    request.projectId(),
                    references
            );
        }

        try {
            Map<String, Object> payload = Map.of(
                    "model", properties.model(),
                    "stream", false,
                    "temperature", 0.2,
                    "messages", List.of(
                            Map.of("role", "system", "content", properties.systemPrompt()),
                            Map.of("role", "system", "content", context),
                            Map.of("role", "user", "content", request.message())
                    )
            );

            RestClient.RequestBodySpec spec = restClient.post()
                    .uri(properties.baseUrl() + "/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON);

            if (properties.apiKey() != null && !properties.apiKey().isBlank()) {
                spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey());
            }

            byte[] responseBytes = spec.body(payload)
                    .retrieve()
                    .body(byte[].class);
            String responseBody = new String(responseBytes, StandardCharsets.UTF_8);

            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) {
                content = "Model returned a response, but no usable content was parsed.";
            }

            return new AssistantChatResponse(
                    "openai-compatible",
                    properties.model(),
                    content,
                    request.projectId(),
                    references
            );
        } catch (Exception ex) {
            String fallback = """
                    Could not connect to the local research assistant model.
                    Please check:
                    1. Ollama is running
                    2. The OpenAI-compatible base URL is correct
                    3. Model %s has been pulled
                    4. The current URL %s is reachable

                    Original error: %s
                    """.formatted(properties.model(), properties.baseUrl(), ex.getMessage());

            return new AssistantChatResponse(
                    "openai-compatible",
                    properties.model(),
                    fallback,
                    request.projectId(),
                    references
            );
        }
    }
}
