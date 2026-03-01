package com.laidekuai.common.scheduler;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 调度任务运行指标（内存态）
 */
@Component
public class SchedulerMetrics {

    private final AtomicLong totalRuns = new AtomicLong(0);
    private final AtomicLong totalScanned = new AtomicLong(0);
    private final AtomicLong totalCanceled = new AtomicLong(0);

    @Getter
    private volatile LocalDateTime lastRunAt;

    @Getter
    private volatile long lastDurationMs;

    @Getter
    private volatile int lastScanned;

    @Getter
    private volatile int lastCanceled;

    public void recordRun(int scanned, int canceled, long durationMs) {
        totalRuns.incrementAndGet();
        totalScanned.addAndGet(scanned);
        totalCanceled.addAndGet(canceled);
        lastRunAt = LocalDateTime.now();
        lastDurationMs = durationMs;
        lastScanned = scanned;
        lastCanceled = canceled;
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRuns", totalRuns.get());
        result.put("totalScanned", totalScanned.get());
        result.put("totalCanceled", totalCanceled.get());
        result.put("lastRunAt", lastRunAt);
        result.put("lastDurationMs", lastDurationMs);
        result.put("lastScanned", lastScanned);
        result.put("lastCanceled", lastCanceled);
        return result;
    }
}
