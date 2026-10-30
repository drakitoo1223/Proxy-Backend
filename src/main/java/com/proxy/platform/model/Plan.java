package com.proxy.platform.model;

public enum Plan {
    FREE(10, 50000L),
    PRO(60, 500000L),
    ENTERPRISE(-1, -1L);

    private final int requestsPerMinute;
    private final long monthlyTokenQuota;

    Plan(int requestsPerMinute, long monthlyTokenQuota) {
        this.requestsPerMinute = requestsPerMinute;
        this.monthlyTokenQuota = monthlyTokenQuota;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public long getMonthlyTokenQuota() {
        return monthlyTokenQuota;
    }

    public static Plan fromString(String value) {
        if (value == null) {
            return FREE;
        }

        return switch (value.trim().toUpperCase()) {
            case "PRO" -> PRO;
            case "ENTERPRISE" -> ENTERPRISE;
            default -> FREE;
        };
    }
}
