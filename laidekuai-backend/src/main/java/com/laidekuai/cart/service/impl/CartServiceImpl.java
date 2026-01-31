package com.laidekuai.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laidekuai.cart.dto.AddToCartRequest;
import com.laidekuai.cart.dto.CartItemResponse;
import com.laidekuai.cart.dto.UpdateCartRequest;
import com.laidekuai.cart.entity.Cart;
import com.laidekuai.cart.mapper.CartMapper;
import com.laidekuai.cart.service.CartService;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.enums.GoodsStatus;
import com.laidekuai.goods.entity.Goods;
import com.laidekuai.goods.mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务实现
 *
 * @author Laidekuai Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final GoodsMapper goodsMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<CartItemResponse> addToCart(AddToCartRequest request, Long userId) {
        log.info("添加商品到购物车，用户ID: {}, 商品ID: {}, 数量: {}", userId, request.getGoodsId(), request.getQuantity());

        // 1. 查询商品是否存在
        Goods goods = goodsMapper.selectById(request.getGoodsId());
        if (goods == null) {
            log.warn("商品不存在: {}", request.getGoodsId());
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 2. 检查商品状态（只有已上架的商品才能加入购物车）
        if (goods.getStatus() != GoodsStatus.APPROVED) {
            log.warn("商品未上架，商品ID: {}, 状态: {}", request.getGoodsId(), goods.getStatus());
            return Result.error(ErrorCode.GOODS_STATUS_ERROR);
        }

        // 3. 检查库存
        if (goods.getStock() < request.getQuantity()) {
            log.warn("商品库存不足，商品ID: {}, 库存: {}, 请求数量: {}",
                    request.getGoodsId(), goods.getStock(), request.getQuantity());
            return Result.error(ErrorCode.GOODS_STOCK_INSUFFICIENT);
        }

        // 4. 查询购物车是否已有该商品
        Cart existingCart = cartMapper.findByUserAndGoods(userId, request.getGoodsId());

        if (existingCart != null) {
            // 已存在，更新数量
            existingCart.setQuantity(existingCart.getQuantity() + request.getQuantity());
            existingCart.setUpdatedAt(LocalDateTime.now());
            cartMapper.updateById(existingCart);
            log.info("购物车商品数量更新成功，购物车ID: {}, 新数量: {}", existingCart.getId(), existingCart.getQuantity());
            return Result.success(buildCartItemResponse(existingCart, goods));
        } else {
            // 不存在，创建新记录
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setGoodsId(goods.getId());
            cart.setGoodsTitle(goods.getTitle());
            cart.setGoodsImage(getFirstImage(goods.getImageUrls()));
            cart.setGoodsPrice(goods.getPrice());
            cart.setQuantity(request.getQuantity());
            cart.setCreatedAt(LocalDateTime.now());
            cart.setUpdatedAt(LocalDateTime.now());

            cartMapper.insert(cart);
            log.info("商品添加到购物车成功，购物车ID: {}", cart.getId());
            return Result.success(buildCartItemResponse(cart, goods));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<CartItemResponse> updateCartItem(Long cartId, UpdateCartRequest request, Long userId) {
        log.info("更新购物车商品数量，购物车ID: {}, 用户ID: {}, 数量: {}", cartId, userId, request.getQuantity());

        // 1. 查询购物车记录
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            log.warn("购物车记录不存在: {}", cartId);
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // 2. 权限校验：只能修改自己的购物车
        if (!cart.getUserId().equals(userId)) {
            log.warn("无权修改购物车记录，购物车ID: {}, 用户ID: {}, 当前用户: {}",
                    cartId, cart.getUserId(), userId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 3. 查询商品最新信息（检查库存）
        Goods goods = goodsMapper.selectById(cart.getGoodsId());
        if (goods == null) {
            log.warn("商品不存在: {}", cart.getGoodsId());
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 4. 检查库存
        if (goods.getStock() < request.getQuantity()) {
            log.warn("商品库存不足，商品ID: {}, 库存: {}, 请求数量: {}",
                    cart.getGoodsId(), goods.getStock(), request.getQuantity());
            return Result.error(ErrorCode.GOODS_STOCK_INSUFFICIENT);
        }

        // 5. 更新数量
        cart.setQuantity(request.getQuantity());
        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateById(cart);

        log.info("购物车商品数量更新成功，购物车ID: {}", cartId);

        return Result.success(buildCartItemResponse(cart, goods));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> removeFromCart(Long cartId, Long userId) {
        log.info("从购物车删除商品，购物车ID: {}, 用户ID: {}", cartId, userId);

        // 1. 查询购物车记录
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            log.warn("购物车记录不存在: {}", cartId);
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // 2. 权限校验：只能删除自己的购物车
        if (!cart.getUserId().equals(userId)) {
            log.warn("无权删除购物车记录，购物车ID: {}, 用户ID: {}, 当前用户: {}",
                    cartId, cart.getUserId(), userId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 3. 删除记录
        cartMapper.deleteById(cartId);

        log.info("购物车商品删除成功，购物车ID: {}", cartId);

        return Result.success();
    }

    @Override
    public Result<List<CartItemResponse>> getMyCart(Long userId) {
        log.info("获取我的购物车，用户ID: {}", userId);

        // 1. 查询购物车列表
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.orderByDesc(Cart::getCreatedAt);

        List<Cart> cartList = cartMapper.selectList(wrapper);

        // 2. 关联商品最新信息
        List<CartItemResponse> responseList = cartList.stream()
                .map(cart -> {
                    Goods goods = goodsMapper.selectById(cart.getGoodsId());
                    return buildCartItemResponse(cart, goods);
                })
                .collect(Collectors.toList());

        log.info("购物车查询成功，用户ID: {}, 商品数量: {}", userId, responseList.size());

        return Result.success(responseList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> clearCart(Long userId) {
        log.info("清空购物车，用户ID: {}", userId);

        // 1. 查询所有购物车记录
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);

        List<Cart> cartList = cartMapper.selectList(wrapper);

        // 2. 逐个删除
        for (Cart cart : cartList) {
            cartMapper.deleteById(cart.getId());
        }

        log.info("购物车清空成功，用户ID: {}, 删除记录数: {}", userId, cartList.size());

        return Result.success();
    }

    /**
     * 构建购物车项响应
     */
    private CartItemResponse buildCartItemResponse(Cart cart, Goods goods) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cart.getId());
        response.setGoodsId(cart.getGoodsId());
        response.setGoodsTitle(cart.getGoodsTitle());
        response.setGoodsImage(cart.getGoodsImage());
        response.setGoodsPrice(cart.getGoodsPrice());
        response.setQuantity(cart.getQuantity());

        // 关联商品最新信息
        if (goods != null) {
            response.setGoodsStatus(goods.getStatus().name());
            response.setGoodsStock(goods.getStock());
        } else {
            response.setGoodsStatus("NOT_FOUND");
            response.setGoodsStock(0);
        }

        return response;
    }

    /**
     * 从JSON数组中获取第一张图片
     */
    private String getFirstImage(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.isEmpty()) {
            return null;
        }
        try {
            String[] urls = objectMapper.readValue(imageUrlsJson, String[].class);
            return urls != null && urls.length > 0 ? urls[0] : null;
        } catch (JsonProcessingException e) {
            log.warn("解析商品图片URL失败: {}", imageUrlsJson, e);
            return null;
        }
    }
}
