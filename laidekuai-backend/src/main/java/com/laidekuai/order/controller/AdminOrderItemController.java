package com.laidekuai.order.controller;

import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.order.dto.OrderItemDTO;
import com.laidekuai.order.dto.ShipRequest;
import com.laidekuai.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin order item controller.
 */
@Slf4j
@RestController
@RequestMapping("/admin/order-items")
@RequiredArgsConstructor
public class AdminOrderItemController {

    private final OrderService orderService;

    /**
     * POST /api/admin/order-items/{id}/ship
     */
    @PostMapping("/{id}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<OrderItemDTO> shipOrderItemByAdmin(
            @PathVariable("id") Long orderItemId,
            @Valid @RequestBody ShipRequest request) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return orderService.shipOrderItemByAdmin(orderItemId, request, adminId);
    }
}
