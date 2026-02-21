package com.laidekuai.audit.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志返回对象
 */
@Data
public class AuditLogDTO {
    private Long id;
    private Long orderId;
    private String orderNo;
    private String action;
    private String operator;
    private String operatorRole;
    private String remark;
    private LocalDateTime createdAt;
}
