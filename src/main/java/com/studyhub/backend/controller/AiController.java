package com.studyhub.backend.controller;

import com.studyhub.backend.dto.FlashcardPair;
import com.studyhub.backend.dto.GenerateFlashcardsRequest;
import com.studyhub.backend.service.AnthropicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final int DAILY_LIMIT = 20;

    private final AnthropicService anthropicService;

    private final Map<String, Integer> dailyUsage = new ConcurrentHashMap<>();
    private volatile LocalDate currentDate = LocalDate.now();

    public AiController(AnthropicService anthropicService) {
        this.anthropicService = anthropicService;
    }

    private void resetIfNewDay() {
        LocalDate today = LocalDate.now();
        if (!today.equals(currentDate)) {
            dailyUsage.clear();
            currentDate = today;
        }
    }

    @PostMapping("/generate-flashcards")
    public ResponseEntity<?> generateFlashcards(
            @RequestBody GenerateFlashcardsRequest request,
            Authentication authentication) {
        try {
            resetIfNewDay();

            String userEmail = authentication.getName();
            int usage = dailyUsage.getOrDefault(userEmail, 0);

            if (usage >= DAILY_LIMIT) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Daily limit of " + DAILY_LIMIT + " AI generations reached. Try again tomorrow.");
            }

            List<FlashcardPair> pairs = anthropicService.generateFlashcards(request.getText());
            dailyUsage.put(userEmail, usage + 1);
            return ResponseEntity.ok(pairs);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to generate flashcards: " + e.getMessage());
        }
    }
}