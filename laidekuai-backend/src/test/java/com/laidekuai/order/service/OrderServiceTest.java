package com.laidekuai.order.service;

import com.laidekuai.address.entity.Address;
import com.laidekuai.address.mapper.AddressMapper;
import com.laidekuai.audit.service.AuditLogService;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.OrderNoGenerator;
import com.laidekuai.goods.entity.Goods;
import com.laidekuai.goods.mapper.GoodsMapper;
import com.laidekuai.order.dto.OrderCreateRequest;
import com.laidekuai.order.dto.OrderItemRequest;
import com.laidekuai.order.dto.ShipRequest;
import com.laidekuai.order.entity.Order;
import com.laidekuai.order.entity.OrderItem;
import com.laidekuai.order.mapper.OrderItemMapper;
import com.laidekuai.order.mapper.OrderMapper;
import com.laidekuai.order.service.impl.OrderServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private GoodsMapper goodsMapper;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private OrderNoGenerator orderNoGenerator;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Address address;
    private Goods goods;

    @BeforeEach
    void setUp() {
        address = new Address();
        address.setId(10L);
        address.setUserId(100L);
        address.setDeleted(0);
        address.setReceiverName("Test");
        address.setReceiverPhone("13000000000");
        address.setProvince("P");
        address.setCity("C");
        address.setDistrict("D");
        address.setDetail("Street 1");

        goods = new Goods();
        goods.setId(1L);
        goods.setSellerId(200L);
        goods.setStatus(com.laidekuai.common.enums.GoodsStatus.APPROVED);
        goods.setStock(10);
        goods.setPrice(new BigDecimal("5.00"));
        goods.setTitle("G1");
        goods.setImageUrls("[\"img\"]");
    }

    @Test
    void testCreateOrder_MergesDuplicateItems() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(10L);

        OrderItemRequest item1 = new OrderItemRequest();
        item1.setGoodsId(1L);
        item1.setQuantity(1);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setGoodsId(1L);
        item2.setQuantity(2);

        List<OrderItemRequest> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        request.setItems(items);

        when(orderMapper.countActiveOrders(100L)).thenReturn(0);
        when(addressMapper.selectById(10L)).thenReturn(address);
        when(goodsMapper.selectBatchIds(anyList())).thenReturn(List.of(goods));
        when(goodsMapper.deductStock(1L, 3)).thenReturn(1);
        when(orderNoGenerator.generate()).thenReturn("NO1");
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(99L);
            return 1;
        });
        when(orderItemMapper.batchInsert(anyList())).thenReturn(1);

        Result<List<com.laidekuai.order.dto.OrderDTO>> result = orderService.createOrder(request, 100L);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());

        ArgumentCaptor<List<OrderItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderItemMapper).batchInsert(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(3, captor.getValue().get(0).getQuantity());
        verify(goodsMapper).deductStock(1L, 3);
    }

    @Test
    void testHandleRefund_Approved_UpdatesItemStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setSellerId(10L);
        order.setStatus("REFUNDING");
        order.setDeleted(0);
        order.setOrderNo("O1");

        OrderItem item = new OrderItem();
        item.setGoodsId(5L);
        item.setQuantity(2);

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderItemMapper.selectByOrderId(1L)).thenReturn(List.of(item));
        when(goodsMapper.releaseStock(5L, 2)).thenReturn(1);
        when(orderItemMapper.updateStatusByOrderId(1L, "REFUNDED")).thenReturn(1);
        when(orderMapper.update(any(), any())).thenReturn(1);

        Result<Void> result = orderService.handleRefund(1L, 10L, true, "ok");

        assertTrue(result.isSuccess());
        verify(goodsMapper).releaseStock(5L, 2);
        verify(orderItemMapper).updateStatusByOrderId(1L, "REFUNDED");

        verify(orderMapper).update(any(), any());
    }

    @Test
    void testCancelOrderSystem_Idempotent() {
        Order order = new Order();
        order.setId(1L);
        order.setDeleted(0);
        order.setOrderNo("O1");

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.markCanceledIfPending(eq(1L), eq("TIMEOUT"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0);

        Result<Void> result = orderService.cancelOrderSystem(1L);

        assertTrue(result.isSuccess());
        verify(orderItemMapper, never()).selectByOrderId(anyLong());
        verify(goodsMapper, never()).releaseStock(anyLong(), anyInt());
        verify(orderItemMapper, never()).updateStatusByOrderId(anyLong(), anyString());
    }

    @Test
    void testCancelOrderSystem_RepeatOnlyReleasesStockOnce() {
        Order order = new Order();
        order.setId(1L);
        order.setDeleted(0);
        order.setOrderNo("O1");

        OrderItem item = new OrderItem();
        item.setId(11L);
        item.setGoodsId(9L);
        item.setQuantity(2);

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.markCanceledIfPending(eq(1L), eq("TIMEOUT"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(1, 0);
        when(orderItemMapper.selectByOrderId(1L)).thenReturn(List.of(item));
        when(goodsMapper.releaseStock(9L, 2)).thenReturn(1);
        when(orderItemMapper.updateStatusByOrderId(1L, "CANCELED")).thenReturn(1);

        Result<Void> first = orderService.cancelOrderSystem(1L);
        Result<Void> second = orderService.cancelOrderSystem(1L);

        assertTrue(first.isSuccess());
        assertTrue(second.isSuccess());
        verify(goodsMapper, times(1)).releaseStock(9L, 2);
        verify(orderItemMapper, times(1)).updateStatusByOrderId(1L, "CANCELED");
    }

    @Test
    void testShipOrderItem_NoAggregateWhenRemainingItems() {
        Order order = new Order();
        order.setId(1L);
        order.setSellerId(10L);
        order.setStatus("PAID");
        order.setDeleted(0);

        OrderItem item = new OrderItem();
        item.setId(11L);
        item.setOrderId(1L);
        item.setSellerId(10L);
        item.setItemStatus("PAID");
        item.setDeleted(0);

        ShipRequest request = new ShipRequest();
        request.setShipCompany("SF");
        request.setTrackingNo("T1");

        when(orderItemMapper.selectById(11L)).thenReturn(item);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderItemMapper.updateById(any(OrderItem.class))).thenReturn(1);
        when(orderItemMapper.countUnshippedItems(1L)).thenReturn(1);

        Result<com.laidekuai.order.dto.OrderItemDTO> result = orderService.shipOrderItem(11L, request, 10L);

        assertTrue(result.isSuccess());
        assertEquals("SHIPPED", item.getItemStatus());
        verify(orderMapper, never()).markShippedIfPaid(anyLong(), any(LocalDateTime.class));
        verify(orderItemMapper, never()).updateStatusByOrderId(anyLong(), anyString());
    }

    @Test
    void testShipOrderItem_AggregatesWhenAllShipped() {
        Order order = new Order();
        order.setId(1L);
        order.setSellerId(10L);
        order.setStatus("PAID");
        order.setDeleted(0);

        OrderItem item = new OrderItem();
        item.setId(11L);
        item.setOrderId(1L);
        item.setSellerId(10L);
        item.setItemStatus("PAID");
        item.setDeleted(0);

        ShipRequest request = new ShipRequest();
        request.setShipCompany("SF");
        request.setTrackingNo("T2");

        when(orderItemMapper.selectById(11L)).thenReturn(item);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderItemMapper.updateById(any(OrderItem.class))).thenReturn(1);
        when(orderItemMapper.countUnshippedItems(1L)).thenReturn(0);
        when(orderMapper.markShippedIfPaid(eq(1L), any(LocalDateTime.class))).thenReturn(1);
        when(orderItemMapper.updateStatusByOrderId(1L, "SHIPPED")).thenReturn(1);

        Result<com.laidekuai.order.dto.OrderItemDTO> result = orderService.shipOrderItem(11L, request, 10L);

        assertTrue(result.isSuccess());
        verify(orderMapper).markShippedIfPaid(eq(1L), any(LocalDateTime.class));
        verify(orderItemMapper).updateStatusByOrderId(1L, "SHIPPED");
    }

    @Test
    void testListAdminOrders_ReturnsItems() {
        Order order = new Order();
        order.setId(1L);
        order.setDeleted(0);

        OrderItem item = new OrderItem();
        item.setId(2L);
        item.setOrderId(1L);

        Page<Order> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(order));
        page.setTotal(1);

        when(orderMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(orderItemMapper.selectByOrderId(1L)).thenReturn(Collections.singletonList(item));

        Result<com.laidekuai.common.dto.PageResult<com.laidekuai.order.dto.OrderDTO>> result =
                orderService.listAdminOrders(null, null, null, null, null, null, 1L, 10L);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().getRecords().size());
        assertEquals(1, result.getData().getRecords().get(0).getItems().size());
    }
}
