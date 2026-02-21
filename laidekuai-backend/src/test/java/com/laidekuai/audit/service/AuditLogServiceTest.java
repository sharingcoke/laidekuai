package com.laidekuai.audit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.audit.dto.AuditLogDTO;
import com.laidekuai.audit.entity.AuditLog;
import com.laidekuai.audit.mapper.AuditLogMapper;
import com.laidekuai.audit.service.impl.AuditLogServiceImpl;
import com.laidekuai.common.dto.Result;
import com.laidekuai.order.entity.Order;
import com.laidekuai.order.mapper.OrderMapper;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogMapper auditLogMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Test
    void record_MissingOperator_DoesNothing() {
        auditLogService.record(1L, "REFUND_APPROVE", null, "ADMIN", null);
        verify(auditLogMapper, never()).insert(any(AuditLog.class));
    }

    @Test
    void listAdminLogs_MapsOrderAndOperator() {
        AuditLog logRecord = new AuditLog();
        logRecord.setId(1L);
        logRecord.setOrderId(100L);
        logRecord.setAction("REFUND_APPROVE");
        logRecord.setOperatorId(9L);
        logRecord.setOperatorRole("ADMIN");
        logRecord.setReason("ok");
        logRecord.setCreatedAt(LocalDateTime.now());

        Page<AuditLog> page = new Page<>(1, 10);
        page.setRecords(List.of(logRecord));
        page.setTotal(1);

        when(auditLogMapper.selectPage(any(Page.class), any())).thenReturn(page);

        Order order = new Order();
        order.setId(100L);
        order.setOrderNo("NO100");
        when(orderMapper.selectBatchIds(any())).thenReturn(List.of(order));

        User user = new User();
        user.setId(9L);
        user.setNickName("admin");
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(user));

        Result<com.laidekuai.common.dto.PageResult<AuditLogDTO>> result =
                auditLogService.listAdminLogs(null, null, null, 1L, 10L);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().getRecords().size());
        AuditLogDTO dto = result.getData().getRecords().get(0);
        assertEquals("NO100", dto.getOrderNo());
        assertEquals("admin", dto.getOperator());
        assertEquals("REFUND_APPROVE", dto.getAction());
    }
}
