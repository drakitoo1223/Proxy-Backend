package com.proxy.platform.model;

import java.time.Instant;

public record QuotaStatus(
        String userId,
        Plan plan,
        long tokensUsed,
        long tokensRemaining,
        Instant resetDate,
        String status
) {
}
