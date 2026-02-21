package com.laidekuai.audit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 */
@Data
@TableName("audit_log")
public class AuditLog {

    private Long id;
    private Long orderId;
    private String action;
    private Long operatorId;
    private String operatorRole;
    private String reason;
    private LocalDateTime createdAt;
}
