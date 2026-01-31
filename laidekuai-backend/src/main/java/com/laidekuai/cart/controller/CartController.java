package com.laidekuai.cart.controller;

import com.laidekuai.cart.dto.AddToCartRequest;
import com.laidekuai.cart.dto.CartItemResponse;
import com.laidekuai.cart.dto.UpdateCartRequest;
import com.laidekuai.cart.service.CartService;
import com.laidekuai.common.dto.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 *
 * @author Laidekuai Team
 */
@Slf4j
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        return Long.parseLong(userIdObj.toString());
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping
    public Result<CartItemResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return cartService.addToCart(request, userId);
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/{id}")
    public Result<CartItemResponse> updateCartItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return cartService.updateCartItem(id, request, userId);
    }

    /**
     * 从购物车删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> removeFromCart(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return cartService.removeFromCart(id, userId);
    }

    /**
     * 获取我的购物车
     */
    @GetMapping
    public Result<List<CartItemResponse>> getMyCart(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return cartService.getMyCart(userId);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Result<Void> clearCart(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return cartService.clearCart(userId);
    }
}
