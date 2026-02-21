package com.laidekuai.audit.controller;

import com.laidekuai.audit.dto.AuditLogDTO;
import com.laidekuai.audit.service.AuditLogService;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员审计日志接口
 */
@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<AuditLogDTO>> listLogs(
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        return auditLogService.listAdminLogs(orderNo, action, operator, page, size);
    }
}
