package com.studyhub.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhub.backend.dto.FlashcardPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class AnthropicService {

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001";

    @Value("${anthropic.api.key}")
    private String apiKey;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AnthropicService(ObjectMapper objectMapper) {
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    public List<FlashcardPair> generateFlashcards(String text) throws Exception {
        String prompt = String.format("""
            Generate flashcards from the following study notes. Create between 5 and 15 flashcards \
            with concise questions on the front and clear answers on the back.

            Return ONLY a valid JSON array with no other text, markdown, or explanation. \
            Use exactly this format:
            [{"front": "question", "back": "answer"}]

            Study notes:
            %s
            """, text);

        Map<String, Object> requestBody = Map.of(
            "model", MODEL,
            "max_tokens", 1024,
            "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        Map<?, ?> response = restClient.post()
            .uri(ANTHROPIC_API_URL)
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(Map.class);

        List<?> content = (List<?>) response.get("content");
        Map<?, ?> firstBlock = (Map<?, ?>) content.get(0);
        String jsonText = ((String) firstBlock.get("text")).trim();

        if (jsonText.startsWith("```")) {
            jsonText = jsonText.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        }

        return objectMapper.readValue(jsonText, new TypeReference<List<FlashcardPair>>() {});
    }
}