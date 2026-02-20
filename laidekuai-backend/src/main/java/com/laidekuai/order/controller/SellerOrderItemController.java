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
 * Seller order item controller.
 */
@Slf4j
@RestController
@RequestMapping("/seller/order-items")
@RequiredArgsConstructor
public class SellerOrderItemController {

    private final OrderService orderService;

    /**
     * POST /api/seller/order-items/{id}/ship
     */
    @PostMapping("/{id}/ship")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public Result<OrderItemDTO> shipOrderItem(
            @PathVariable("id") Long orderItemId,
            @Valid @RequestBody ShipRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderService.shipOrderItem(orderItemId, request, userId);
    }
}
