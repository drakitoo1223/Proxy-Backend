package com.proxy.platform.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ResetScheduler {

    private final UsageTracker usageTracker;

    public ResetScheduler(UsageTracker usageTracker) {
        this.usageTracker = usageTracker;
    }

    @Scheduled(cron = "0 * * * * *")
    public void resetRateLimitWindow() {
        for (String userId : usageTracker.getUsers()) {
            usageTracker.resetRequestWindow(userId);
        }
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void resetMonthlyQuota() {
        usageTracker.resetMonthlyQuota();
    }
}
