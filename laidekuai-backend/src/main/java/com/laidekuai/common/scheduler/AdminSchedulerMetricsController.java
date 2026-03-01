package com.laidekuai.common.scheduler;

import com.laidekuai.common.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端调度指标查询
 */
@RestController
@RequestMapping("/admin/system/metrics")
@RequiredArgsConstructor
public class AdminSchedulerMetricsController {

    private final SchedulerMetrics schedulerMetrics;

    @GetMapping("/scheduler")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> schedulerMetrics() {
        return Result.success(schedulerMetrics.snapshot());
    }
}
