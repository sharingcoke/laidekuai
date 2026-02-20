package com.laidekuai.order.controller;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.order.dto.OrderDTO;
import com.laidekuai.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 管理员订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * GET /api/admin/orders
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<OrderDTO>> listAdminOrders(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "buyer_id", required = false) Long buyerId,
            @RequestParam(value = "seller_id", required = false) Long sellerId,
            @RequestParam(value = "order_no", required = false) String orderNo,
            @RequestParam(value = "start_time", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "end_time", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        return orderService.listAdminOrders(status, buyerId, sellerId, orderNo, startTime, endTime, page, size);
    }

    /**
     * POST /api/admin/orders/{id}/refund/approve
     */
    @PostMapping("/{id}/refund/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> approveRefund(@PathVariable("id") Long orderId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return orderService.approveRefundByAdmin(orderId, adminId);
    }

    /**
     * POST /api/admin/orders/{id}/refund/reject
     */
    @PostMapping("/{id}/refund/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> rejectRefund(@PathVariable("id") Long orderId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return orderService.rejectRefundByAdmin(orderId, adminId);
    }
}
