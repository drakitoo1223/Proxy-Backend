package com.proxy.platform.service;

import com.proxy.platform.model.GenerationRequest;
import com.proxy.platform.model.GenerationResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class MockAIGenerationService implements IAService {

    private static final List<String> RESPONSES = List.of(
            "The system generated a concise summary based on your prompt.",
            "The AI response was created successfully and is ready for review.",
            "This draft was generated using the platform proxy layer and the internal AI service.",
            "A safe and structured answer was produced for the request provided by the user.",
            "The content has been generated with a controlled template to validate quota and rate-limit behavior."
    );

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        try {
            Thread.sleep(1200L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String text = RESPONSES.get(Math.abs(request.getPrompt().hashCode()) % RESPONSES.size());
        return new GenerationResponse(
                request.getUserId(),
                request.getPlan() == null ? "FREE" : request.getPlan(),
                estimateTokens(request.getPrompt()),
                text,
                Instant.now()
        );
    }

    public int estimateTokens(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return 0;
        }

        return Math.max(1, (int) Math.ceil(prompt.trim().split("\\s+").length * 1.5));
    }
}
