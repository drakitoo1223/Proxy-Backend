package com.proxy.platform.service;

import com.proxy.platform.model.GenerationRequest;
import com.proxy.platform.model.GenerationResponse;
import com.proxy.platform.model.Plan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Service
public class RateLimitProxyService implements IAService {

    private final QuotaProxyService quotaProxyService;
    private final UsageTracker usageTracker;

    public RateLimitProxyService(QuotaProxyService quotaProxyService, UsageTracker usageTracker) {
        this.quotaProxyService = quotaProxyService;
        this.usageTracker = usageTracker;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        Plan plan = Plan.fromString(request.getPlan());
        if (!usageTracker.canPassRateLimit(request.getUserId(), plan)) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Rate limit exceeded. Please wait a minute and try again."
            );
        }

        return quotaProxyService.generate(request);
    }
}
