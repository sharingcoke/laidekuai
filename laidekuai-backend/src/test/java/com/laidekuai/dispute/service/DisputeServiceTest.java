package com.laidekuai.dispute.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.common.dto.Result;
import com.laidekuai.dispute.dto.DisputeDTO;
import com.laidekuai.dispute.entity.Dispute;
import com.laidekuai.dispute.mapper.DisputeMapper;
import com.laidekuai.dispute.service.impl.DisputeServiceImpl;
import com.laidekuai.goods.mapper.GoodsMapper;
import com.laidekuai.order.entity.Order;
import com.laidekuai.order.entity.OrderItem;
import com.laidekuai.order.mapper.OrderItemMapper;
import com.laidekuai.order.mapper.OrderMapper;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @Mock
    private DisputeMapper disputeMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private GoodsMapper goodsMapper;

    @InjectMocks
    private DisputeServiceImpl disputeService;

    @Test
    void listAdminDisputes_ReturnsApplicantName() {
        Dispute dispute = new Dispute();
        dispute.setId(1L);
        dispute.setOrderId(10L);
        dispute.setApplicantId(3L);
        dispute.setApplicantType("BUYER");
        dispute.setReason("原因");
        dispute.setStatus("PENDING");
        dispute.setCreatedAt(LocalDateTime.now());

        Page<Dispute> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(dispute));

        Order order = new Order();
        order.setId(10L);
        order.setOrderNo("NO1001");
        order.setBuyerId(3L);
        order.setSellerId(5L);
        order.setTotalAmount(BigDecimal.valueOf(99));

        User buyer = new User();
        buyer.setId(3L);
        buyer.setNickName("张三");

        User seller = new User();
        seller.setId(5L);
        seller.setNickName("李四");

        when(disputeMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(orderMapper.selectBatchIds(any())).thenReturn(List.of(order));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(buyer, seller));

        Result<?> result = disputeService.listAdminDisputes(null, null, 1L, 10L);

        assertEquals(0, result.getCode());
        var pageResult = (com.laidekuai.common.dto.PageResult<DisputeDTO>) result.getData();
        assertEquals(1, pageResult.getRecords().size());
        assertEquals("张三", pageResult.getRecords().get(0).getApplicantName());
    }

    @Test
    void resolveDispute_Refund_UpdatesOrderAndDispute() {
        Dispute dispute = new Dispute();
        dispute.setId(2L);
        dispute.setOrderId(20L);
        dispute.setStatus("PENDING");

        Order order = new Order();
        order.setId(20L);
        order.setStatus("DISPUTED");

        OrderItem item = new OrderItem();
        item.setGoodsId(7L);
        item.setQuantity(2);

        when(disputeMapper.selectById(2L)).thenReturn(dispute);
        when(orderMapper.selectById(20L)).thenReturn(order);
        when(orderItemMapper.selectByOrderId(20L)).thenReturn(List.of(item));
        when(orderMapper.updateById(any())).thenReturn(1);
        when(orderItemMapper.updateStatusByOrderId(eq(20L), anyString())).thenReturn(1);
        when(orderItemMapper.updateOrderStatusByOrderId(eq(20L), anyString())).thenReturn(1);
        when(disputeMapper.updateById(any())).thenReturn(1);

        Result<Void> result = disputeService.resolveDispute(2L, "REFUND", "同意", 99L);

        assertEquals(0, result.getCode());
        verify(goodsMapper).releaseStock(7L, 2);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).updateById(orderCaptor.capture());
        assertEquals("REFUNDED", orderCaptor.getValue().getStatus());

        ArgumentCaptor<Dispute> disputeCaptor = ArgumentCaptor.forClass(Dispute.class);
        verify(disputeMapper).updateById(disputeCaptor.capture());
        assertEquals("RESOLVED", disputeCaptor.getValue().getStatus());
        assertNotNull(disputeCaptor.getValue().getResolvedAt());
    }
}
