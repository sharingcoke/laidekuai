package com.laidekuai.audit.service;

import com.laidekuai.audit.dto.AuditLogDTO;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;

public interface AuditLogService {

    void record(Long orderId, String action, Long operatorId, String operatorRole, String reason);

    Result<PageResult<AuditLogDTO>> listAdminLogs(String orderNo, String action, String operator, Long page, Long size);
}
