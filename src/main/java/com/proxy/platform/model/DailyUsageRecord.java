package com.proxy.platform.model;

import java.time.LocalDate;

public record DailyUsageRecord(LocalDate date, long usedTokens) {
}
