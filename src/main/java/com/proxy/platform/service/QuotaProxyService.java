package com.proxy.platform.service;

import com.proxy.platform.model.GenerationRequest;
import com.proxy.platform.model.GenerationResponse;
import com.proxy.platform.model.Plan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuotaProxyService implements IAService {

    private final MockAIGenerationService delegate;
    private final UsageTracker usageTracker;

    public QuotaProxyService(MockAIGenerationService delegate, UsageTracker usageTracker) {
        this.delegate = delegate;
        this.usageTracker = usageTracker;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        Plan plan = Plan.fromString(request.getPlan());
        int tokensUsed = delegate.estimateTokens(request.getPrompt());

        if (!usageTracker.canConsumeQuota(request.getUserId(), plan, tokensUsed)) {
            throw new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    "Quota limit reached for this user."
            );
        }

        GenerationResponse response = delegate.generate(request);
        usageTracker.consumeQuota(request.getUserId(), plan, response.tokensUsed());
        return response;
    }
}
