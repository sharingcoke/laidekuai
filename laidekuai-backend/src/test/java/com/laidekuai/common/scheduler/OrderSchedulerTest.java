package com.laidekuai.common.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.laidekuai.common.dto.Result;
import com.laidekuai.order.entity.Order;
import com.laidekuai.order.mapper.OrderMapper;
import com.laidekuai.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSchedulerTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderService orderService;

    @Mock
    private SchedulerMetrics schedulerMetrics;

    @Test
    void cancelTimeoutOrders_BoundaryIncluded_UsesDbTime() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("PENDING_PAY");
        order.setOrderNo("O1");

        when(orderMapper.selectList(any())).thenReturn(List.of(order));
        when(orderService.cancelOrderSystem(1L)).thenReturn(Result.success());

        Order canceled = new Order();
        canceled.setId(1L);
        canceled.setStatus("CANCELED");
        canceled.setCancelReason("TIMEOUT");
        when(orderMapper.selectById(1L)).thenReturn(canceled);

        OrderScheduler scheduler = new OrderScheduler(orderMapper, orderService, schedulerMetrics);
        setField(scheduler, "timeoutMinutes", 15);

        ArgumentCaptor<QueryWrapper<Order>> captor = ArgumentCaptor.forClass(QueryWrapper.class);

        scheduler.cancelTimeoutOrders();

        verify(orderMapper).selectList(captor.capture());
        verify(orderService).cancelOrderSystem(1L);
        verify(schedulerMetrics).recordRun(eq(1), eq(1), anyLong());

        String custom = String.valueOf(captor.getValue().getCustomSqlSegment());
        assertTrue(custom.contains("DATE_SUB"), "sql=" + custom);
        assertTrue(custom.contains("<="), "sql=" + custom);
        assertTrue(custom.contains("LIMIT"), "sql=" + custom);
    }

    @Test
    void cancelTimeoutOrders_NotPendingNoCancel() throws Exception {
        Order order = new Order();
        order.setId(2L);
        order.setStatus("PAID");
        order.setOrderNo("O2");

        when(orderMapper.selectList(any())).thenReturn(List.of(order));

        OrderScheduler scheduler = new OrderScheduler(orderMapper, orderService, schedulerMetrics);
        setField(scheduler, "timeoutMinutes", 15);

        scheduler.cancelTimeoutOrders();

        verify(orderService, never()).cancelOrderSystem(anyLong());
        verify(schedulerMetrics).recordRun(eq(1), eq(0), anyLong());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
