package com.proxy.platform.controller;

import com.proxy.platform.model.DailyUsageRecord;
import com.proxy.platform.model.GenerationRequest;
import com.proxy.platform.model.GenerationResponse;
import com.proxy.platform.model.Plan;
import com.proxy.platform.model.QuotaStatus;
import com.proxy.platform.service.RateLimitProxyService;
import com.proxy.platform.service.UsageTracker;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProxyController {

    private final RateLimitProxyService rateLimitProxyService;
    private final UsageTracker usageTracker;

    public ProxyController(RateLimitProxyService rateLimitProxyService, UsageTracker usageTracker) {
        this.rateLimitProxyService = rateLimitProxyService;
        this.usageTracker = usageTracker;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@Valid @RequestBody GenerationRequest request) {
        try {
            String normalizedPlan = request.getPlan() == null ? "FREE" : request.getPlan();
            request.setPlan(normalizedPlan);
            usageTracker.registerPlan(request.getUserId(), Plan.fromString(normalizedPlan));
            GenerationResponse response = rateLimitProxyService.generate(request);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            return buildError(ex.getStatusCode().value(), ex.getReason());
        }
    }

    @GetMapping("/quota/status")
    public ResponseEntity<QuotaStatus> status(@RequestParam String userId, @RequestParam(defaultValue = "FREE") String plan) {
        Plan normalizedPlan = Plan.fromString(plan);
        usageTracker.registerPlan(userId, normalizedPlan);
        return ResponseEntity.ok(usageTracker.getQuotaStatus(userId, normalizedPlan));
    }

    @GetMapping("/quota/history")
    public ResponseEntity<List<DailyUsageRecord>> history(@RequestParam String userId) {
        return ResponseEntity.ok(usageTracker.getHistory(userId));
    }

    @PostMapping("/quota/update")
    public ResponseEntity<Map<String, Object>> updatePlan(@RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        String plan = payload.getOrDefault("plan", "FREE");

        if (userId == null || userId.isBlank()) {
            return buildError(HttpStatus.BAD_REQUEST.value(), "userId is required.");
        }

        Plan nextPlan = Plan.fromString(plan);
        usageTracker.registerPlan(userId, nextPlan);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("plan", nextPlan.name());
        result.put("status", "plan updated successfully");
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> buildError(int status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }
}
