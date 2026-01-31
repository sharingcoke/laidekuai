package com.laidekuai.cart.service;

import com.laidekuai.cart.dto.AddToCartRequest;
import com.laidekuai.cart.dto.CartItemResponse;
import com.laidekuai.cart.dto.UpdateCartRequest;
import com.laidekuai.common.dto.Result;

import java.util.List;

/**
 * 购物车服务接口
 *
 * @author Laidekuai Team
 */
public interface CartService {

    /**
     * 添加商品到购物车
     *
     * @param request 添加请求
     * @param userId  用户ID
     * @return 添加结果
     */
    Result<CartItemResponse> addToCart(AddToCartRequest request, Long userId);

    /**
     * 更新购物车商品数量
     *
     * @param cartId   购物车ID
     * @param request  更新请求
     * @param userId   用户ID
     * @return 更新结果
     */
    Result<CartItemResponse> updateCartItem(Long cartId, UpdateCartRequest request, Long userId);

    /**
     * 从购物车删除商品
     *
     * @param cartId 购物车ID
     * @param userId 用户ID
     * @return 删除结果
     */
    Result<Void> removeFromCart(Long cartId, Long userId);

    /**
     * 获取我的购物车
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    Result<List<CartItemResponse>> getMyCart(Long userId);

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     * @return 清空结果
     */
    Result<Void> clearCart(Long userId);
}
