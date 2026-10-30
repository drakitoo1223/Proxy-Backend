package com.proxy.platform.model;

import java.time.Instant;

public record GenerationResponse(
        String userId,
        String plan,
        int tokensUsed,
        String generatedText,
        Instant generatedAt
) {
}
