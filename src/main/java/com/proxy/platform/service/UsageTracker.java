package com.proxy.platform.service;

import com.proxy.platform.model.DailyUsageRecord;
import com.proxy.platform.model.Plan;
import com.proxy.platform.model.QuotaStatus;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UsageTracker {

    private final Map<String, UserQuotaState> userStates = new ConcurrentHashMap<>();

    public synchronized void registerPlan(String userId, Plan plan) {
        userStates.computeIfAbsent(userId, id -> new UserQuotaState()).setPlan(plan);
    }

    public synchronized boolean canPassRateLimit(String userId, Plan plan) {
        UserQuotaState state = userStates.computeIfAbsent(userId, id -> new UserQuotaState());
        state.setPlan(plan);

        int limit = plan.getRequestsPerMinute();
        if (limit < 0) {
            return true;
        }

        long now = System.currentTimeMillis();
        Deque<Long> requests = state.getRequestsPerMinute();

        while (!requests.isEmpty() && now - requests.peekFirst() > 60_000L) {
            requests.pollFirst();
        }

        if (requests.size() >= limit) {
            return false;
        }

        requests.addLast(now);
        return true;
    }

    public synchronized boolean canConsumeQuota(String userId, Plan plan, long tokensUsed) {
        UserQuotaState state = userStates.computeIfAbsent(userId, id -> new UserQuotaState());
        state.setPlan(plan);
        if (plan.getMonthlyTokenQuota() < 0) {
            return true;
        }

        ensureCurrentMonth(state);
        return state.getTokensUsedThisMonth() + tokensUsed <= plan.getMonthlyTokenQuota();
    }

    public synchronized void consumeQuota(String userId, Plan plan, long tokensUsed) {
        UserQuotaState state = userStates.computeIfAbsent(userId, id -> new UserQuotaState());
        state.setPlan(plan);
        ensureCurrentMonth(state);
        state.setTokensUsedThisMonth(state.getTokensUsedThisMonth() + tokensUsed);

        LocalDate today = LocalDate.now();
        state.getDailyUsage().merge(today, tokensUsed, Long::sum);
    }

    public synchronized QuotaStatus getQuotaStatus(String userId, Plan plan) {
        UserQuotaState state = userStates.computeIfAbsent(userId, id -> new UserQuotaState());
        state.setPlan(plan);
        ensureCurrentMonth(state);

        long totalQuota = plan.getMonthlyTokenQuota();
        long used = state.getTokensUsedThisMonth();
        long remaining = totalQuota < 0 ? Long.MAX_VALUE : Math.max(0L, totalQuota - used);

        return new QuotaStatus(
                userId,
                plan,
                used,
                remaining,
                resetDateForNextMonth(),
                remaining == 0 ? "limit reached" : "active"
        );
    }

    public synchronized List<DailyUsageRecord> getHistory(String userId) {
        UserQuotaState state = userStates.computeIfAbsent(userId, id -> new UserQuotaState());
        LocalDate today = LocalDate.now();
        List<DailyUsageRecord> records = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long used = state.getDailyUsage().getOrDefault(date, 0L);
            records.add(new DailyUsageRecord(date, used));
        }

        return records;
    }

    public synchronized void resetRequestWindow(String userId) {
        UserQuotaState state = userStates.computeIfAbsent(userId, id -> new UserQuotaState());
        state.getRequestsPerMinute().clear();
    }

    public synchronized void resetMonthlyQuota() {
        for (UserQuotaState state : userStates.values()) {
            state.setTokensUsedThisMonth(0L);
            state.getDailyUsage().clear();
        }
    }

    public synchronized Set<String> getUsers() {
        return new HashSet<>(userStates.keySet());
    }

    private void ensureCurrentMonth(UserQuotaState state) {
        YearMonth currentMonth = YearMonth.now();
        if (state.getCurrentMonth() == null || !state.getCurrentMonth().equals(currentMonth)) {
            state.setCurrentMonth(currentMonth);
            state.setTokensUsedThisMonth(0L);
        }
    }

    private Instant resetDateForNextMonth() {
        LocalDate firstDay = LocalDate.now().withDayOfMonth(1).plusMonths(1);
        return firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public static class UserQuotaState {
        private Plan plan = Plan.FREE;
        private YearMonth currentMonth = YearMonth.now();
        private long tokensUsedThisMonth;
        private final Deque<Long> requestsPerMinute = new ArrayDeque<>();
        private final Map<LocalDate, Long> dailyUsage = new HashMap<>();

        public Plan getPlan() {
            return plan;
        }

        public void setPlan(Plan plan) {
            this.plan = plan;
        }

        public YearMonth getCurrentMonth() {
            return currentMonth;
        }

        public void setCurrentMonth(YearMonth currentMonth) {
            this.currentMonth = currentMonth;
        }

        public long getTokensUsedThisMonth() {
            return tokensUsedThisMonth;
        }

        public void setTokensUsedThisMonth(long tokensUsedThisMonth) {
            this.tokensUsedThisMonth = tokensUsedThisMonth;
        }

        public Deque<Long> getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public Map<LocalDate, Long> getDailyUsage() {
            return dailyUsage;
        }
    }
}
