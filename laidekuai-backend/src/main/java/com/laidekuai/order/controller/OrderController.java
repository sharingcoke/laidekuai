package com.laidekuai.order.controller;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.order.dto.OrderCreateRequest;
import com.laidekuai.order.dto.OrderDTO;
import com.laidekuai.order.dto.ShipRequest;
import com.laidekuai.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 *
 * @author Laidekuai Team
 */
@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     * POST /api/orders
     */
    @PostMapping
    public Result<List<OrderDTO>> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderService.createOrder(request, userId);
    }

    /**
     * 支付订单（模拟）
     * POST /api/orders/{id}/pay
     */
    @PostMapping("/{id}/pay")
    public Result<OrderDTO> payOrder(@PathVariable("id") Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderService.payOrder(orderId, userId);
    }

    /**
     * 取消订单
     * POST /api/orders/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(
            @PathVariable("id") Long orderId,
            @RequestParam(value = "reason", required = false) String reason) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderService.cancelOrder(orderId, userId, reason);
    }

    /**
     * 订单详情
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public Result<OrderDTO> getOrderDetail(@PathVariable("id") Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderService.getOrderDetail(orderId, userId);
    }

    /**
     * 我的订单列表（买家）
     * GET /api/orders
     */
    @GetMapping
    public Result<PageResult<OrderDTO>> listMyOrders(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderService.listBuyerOrders(userId, status, page, size);
    }

    /**
     * 卖家订单列表
     * GET /api/orders/seller
     */
    @GetMapping("/seller")
    public Result<PageResult<OrderDTO>> listSellerOrders(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderService.listSellerOrders(userId, status, page, size);
    }

    /**
     * 卖家发货
     * POST /api/orders/{id}/ship
     */
    @PostMapping("/{id}/ship")
    public Result<OrderDTO> shipOrder(
            @PathVariable("id") Long orderId,
            @Valid @RequestBody ShipRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderService.shipOrder(orderId, request, userId);
    }

    /**
     * 买家确认收货
     * POST /api/orders/{id}/receive
     */
    @PostMapping("/{id}/receive")
    public Result<OrderDTO> confirmReceive(@PathVariable("id") Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderService.confirmReceive(orderId, userId);
    }

    /**
     * 买家申请退款
     * POST /api/orders/{id}/refund
     */
    @PostMapping("/{id}/refund")
    public Result<Void> requestRefund(
            @PathVariable("id") Long orderId,
            @RequestParam(value = "reason", required = false) String reason) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderService.requestRefund(orderId, userId, reason);
    }

    /**
     * 卖家处理退款
     * POST /api/orders/{id}/refund/handle
     */
    @PostMapping("/{id}/refund/handle")
    public Result<Void> handleRefund(
            @PathVariable("id") Long orderId,
            @RequestParam("approved") boolean approved,
            @RequestParam(value = "remark", required = false) String remark) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderService.handleRefund(orderId, userId, approved, remark);
    }
}

