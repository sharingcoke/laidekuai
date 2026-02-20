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
 * 璐墿杞︽湇鍔″疄鐜? *
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
        log.info("娣诲姞鍟嗗搧鍒拌喘鐗╄溅锛岀敤鎴稩D: {}, 鍟嗗搧ID: {}, 鏁伴噺: {}", userId, request.getGoodsId(), request.getQuantity());

        // 1. 鏌ヨ鍟嗗搧鏄惁瀛樺湪
        Goods goods = goodsMapper.selectById(request.getGoodsId());
        if (goods == null) {
            log.warn("鍟嗗搧涓嶅瓨鍦? {}", request.getGoodsId());
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 2. 妫€鏌ュ晢鍝佺姸鎬侊紙鍙湁宸蹭笂鏋剁殑鍟嗗搧鎵嶈兘鍔犲叆璐墿杞︼級
        if (goods.getStatus() != GoodsStatus.APPROVED) {
            log.warn("鍟嗗搧鏈笂鏋讹紝鍟嗗搧ID: {}, 鐘舵€? {}", request.getGoodsId(), goods.getStatus());
            return Result.error(ErrorCode.GOODS_STATUS_ERROR);
        }

        // 查询购物车是否已有该商品
        Cart existingCart = cartMapper.findByUserAndGoods(userId, request.getGoodsId());

        int targetQuantity = request.getQuantity();
        if (existingCart != null) {
            targetQuantity = existingCart.getQuantity() + request.getQuantity();
        }

        // 检查库存
        if (goods.getStock() < targetQuantity) {
            log.warn("商品库存不足，商品ID: {}, 库存: {}, 请求数量: {}",
                    request.getGoodsId(), goods.getStock(), targetQuantity);
            return Result.error(ErrorCode.GOODS_STOCK_INSUFFICIENT);
        }

        if (existingCart != null) {
            // 已存在，更新数量
            existingCart.setQuantity(targetQuantity);
            existingCart.setUpdatedAt(LocalDateTime.now());
            cartMapper.updateById(existingCart);
            log.info("璐墿杞﹀晢鍝佹暟閲忔洿鏂版垚鍔燂紝璐墿杞D: {}, 鏂版暟閲? {}", existingCart.getId(), existingCart.getQuantity());
            return Result.success(buildCartItemResponse(existingCart, goods));
        } else {
            // New cart item
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
            log.info("鍟嗗搧娣诲姞鍒拌喘鐗╄溅鎴愬姛锛岃喘鐗╄溅ID: {}", cart.getId());
            return Result.success(buildCartItemResponse(cart, goods));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<CartItemResponse> updateCartItem(Long cartId, UpdateCartRequest request, Long userId) {
        log.info("鏇存柊璐墿杞﹀晢鍝佹暟閲忥紝璐墿杞D: {}, 鐢ㄦ埛ID: {}, 鏁伴噺: {}", cartId, userId, request.getQuantity());

        // 1. Load cart record
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            log.warn("璐墿杞﹁褰曚笉瀛樺湪: {}", cartId);
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // 2. Ownership check
        if (!cart.getUserId().equals(userId)) {
            log.warn("鏃犳潈淇敼璐墿杞﹁褰曪紝璐墿杞D: {}, 鐢ㄦ埛ID: {}, 褰撳墠鐢ㄦ埛: {}",
                    cartId, cart.getUserId(), userId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 3. 鏌ヨ鍟嗗搧鏈€鏂颁俊鎭紙妫€鏌ュ簱瀛橈級
        Goods goods = goodsMapper.selectById(cart.getGoodsId());
        if (goods == null) {
            log.warn("鍟嗗搧涓嶅瓨鍦? {}", cart.getGoodsId());
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 4. Check stock
        if (goods.getStock() < request.getQuantity()) {
            log.warn("鍟嗗搧搴撳瓨涓嶈冻锛屽晢鍝両D: {}, 搴撳瓨: {}, 璇锋眰鏁伴噺: {}",
                    cart.getGoodsId(), goods.getStock(), request.getQuantity());
            return Result.error(ErrorCode.GOODS_STOCK_INSUFFICIENT);
        }

        // 5. 鏇存柊鏁伴噺
        cart.setQuantity(request.getQuantity());
        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateById(cart);

        log.info("璐墿杞﹀晢鍝佹暟閲忔洿鏂版垚鍔燂紝璐墿杞D: {}", cartId);

        return Result.success(buildCartItemResponse(cart, goods));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> removeFromCart(Long cartId, Long userId) {
        log.info("浠庤喘鐗╄溅鍒犻櫎鍟嗗搧锛岃喘鐗╄溅ID: {}, 鐢ㄦ埛ID: {}", cartId, userId);

        // 1. Load cart record
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            log.warn("璐墿杞﹁褰曚笉瀛樺湪: {}", cartId);
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // 2. Ownership check
        if (!cart.getUserId().equals(userId)) {
            log.warn("鏃犳潈鍒犻櫎璐墿杞﹁褰曪紝璐墿杞D: {}, 鐢ㄦ埛ID: {}, 褰撳墠鐢ㄦ埛: {}",
                    cartId, cart.getUserId(), userId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 3. 鍒犻櫎璁板綍
        cartMapper.deleteById(cartId);

        log.info("璐墿杞﹀晢鍝佸垹闄ゆ垚鍔燂紝璐墿杞D: {}", cartId);

        return Result.success();
    }

    @Override
    public Result<List<CartItemResponse>> getMyCart(Long userId) {
        log.info("鑾峰彇鎴戠殑璐墿杞︼紝鐢ㄦ埛ID: {}", userId);

        // 1. Query cart list
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.orderByDesc(Cart::getCreatedAt);

        List<Cart> cartList = cartMapper.selectList(wrapper);

        // 2. Build response with latest goods info
        List<CartItemResponse> responseList = cartList.stream()
                .map(cart -> {
                    Goods goods = goodsMapper.selectById(cart.getGoodsId());
                    return buildCartItemResponse(cart, goods);
                })
                .collect(Collectors.toList());

        log.info("璐墿杞︽煡璇㈡垚鍔燂紝鐢ㄦ埛ID: {}, 鍟嗗搧鏁伴噺: {}", userId, responseList.size());

        return Result.success(responseList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> clearCart(Long userId) {
        log.info("娓呯┖璐墿杞︼紝鐢ㄦ埛ID: {}", userId);

        // 1. 鏌ヨ鎵€鏈夎喘鐗╄溅璁板綍
        // 1. Query cart list
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);

        List<Cart> cartList = cartMapper.selectList(wrapper);

        // 2. 閫愪釜鍒犻櫎
        for (Cart cart : cartList) {
            cartMapper.deleteById(cart.getId());
        }

        log.info("璐墿杞︽竻绌烘垚鍔燂紝鐢ㄦ埛ID: {}, 鍒犻櫎璁板綍鏁? {}", userId, cartList.size());

        return Result.success();
    }

    /**
     * 鏋勫缓璐墿杞﹂」鍝嶅簲
     */
    private CartItemResponse buildCartItemResponse(Cart cart, Goods goods) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cart.getId());
        response.setGoodsId(cart.getGoodsId());
        response.setGoodsTitle(cart.getGoodsTitle());
        response.setGoodsImage(cart.getGoodsImage());
        response.setGoodsPrice(cart.getGoodsPrice());
        response.setQuantity(cart.getQuantity());

        // Attach latest goods info
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
     * 浠嶫SON鏁扮粍涓幏鍙栫涓€寮犲浘鐗?     */
    // Get first image from JSON array string
    private String getFirstImage(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.isEmpty()) {
            return null;
        }
        try {
            String[] urls = objectMapper.readValue(imageUrlsJson, String[].class);
            return urls != null && urls.length > 0 ? urls[0] : null;
        } catch (JsonProcessingException e) {
            log.warn("瑙ｆ瀽鍟嗗搧鍥剧墖URL澶辫触: {}", imageUrlsJson, e);
            return null;
        }
    }
}