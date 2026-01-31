package com.laidekuai.cart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laidekuai.cart.dto.AddToCartRequest;
import com.laidekuai.cart.dto.UpdateCartRequest;
import com.laidekuai.cart.entity.Cart;
import com.laidekuai.cart.mapper.CartMapper;
import com.laidekuai.cart.service.impl.CartServiceImpl;
import com.laidekuai.common.enums.GoodsStatus;
import com.laidekuai.goods.entity.Goods;
import com.laidekuai.goods.mapper.GoodsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 购物车服务测试
 *
 * @author Laidekuai Team
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartMapper cartMapper;

    @Mock
    private GoodsMapper goodsMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private Goods testGoods;
    private AddToCartRequest addToCartRequest;
    private UpdateCartRequest updateCartRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试商品
        testGoods = new Goods();
        testGoods.setId(1L);
        testGoods.setTitle("测试商品");
        testGoods.setPrice(new BigDecimal("99.99"));
        testGoods.setStock(100);
        testGoods.setStatus(GoodsStatus.APPROVED);
        testGoods.setImageUrls("[\"url1\", \"url2\"]");
        testGoods.setCreatedAt(LocalDateTime.now());

        // 初始化测试购物车
        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUserId(100L);
        testCart.setGoodsId(1L);
        testCart.setGoodsTitle("测试商品");
        testCart.setGoodsImage("url1");
        testCart.setGoodsPrice(new BigDecimal("99.99"));
        testCart.setQuantity(2);
        testCart.setCreatedAt(LocalDateTime.now());
        testCart.setUpdatedAt(LocalDateTime.now());

        // 初始化添加购物车请求
        addToCartRequest = new AddToCartRequest();
        addToCartRequest.setGoodsId(1L);
        addToCartRequest.setQuantity(2);

        // 初始化更新购物车请求
        updateCartRequest = new UpdateCartRequest();
        updateCartRequest.setQuantity(5);
    }

    @Test
    void testAddToCart_Success_NewItem() {
        // Given
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);
        when(cartMapper.findByUserAndGoods(100L, 1L)).thenReturn(null);
        when(cartMapper.insert(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setId(1L);
            return 1;
        });

        // When
        var result = cartService.addToCart(addToCartRequest, 100L);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getData().getGoodsId());
        assertEquals(2, result.getData().getQuantity());
        verify(cartMapper, times(1)).insert(any(Cart.class));
    }

    @Test
    void testAddToCart_Success_ExistingItem() {
        // Given
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);
        when(cartMapper.findByUserAndGoods(100L, 1L)).thenReturn(testCart);
        when(cartMapper.updateById(any(Cart.class))).thenReturn(1);

        // When
        var result = cartService.addToCart(addToCartRequest, 100L);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(4, result.getData().getQuantity()); // 2 + 2 = 4
        verify(cartMapper, times(1)).updateById(any(Cart.class));
        verify(cartMapper, never()).insert(any(Cart.class));
    }

    @Test
    void testAddToCart_GoodsNotFound() {
        // Given
        when(goodsMapper.selectById(1L)).thenReturn(null);

        // When
        var result = cartService.addToCart(addToCartRequest, 100L);

        // Then
        assertFalse(result.isSuccess());
        verify(cartMapper, never()).insert(any(Cart.class));
    }

    @Test
    void testAddToCart_GoodsNotApproved() {
        // Given
        testGoods.setStatus(GoodsStatus.DRAFT);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);

        // When
        var result = cartService.addToCart(addToCartRequest, 100L);

        // Then
        assertFalse(result.isSuccess());
        verify(cartMapper, never()).insert(any(Cart.class));
    }

    @Test
    void testAddToCart_InsufficientStock() {
        // Given
        testGoods.setStock(1);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);

        // When
        var result = cartService.addToCart(addToCartRequest, 100L);

        // Then
        assertFalse(result.isSuccess());
        verify(cartMapper, never()).insert(any(Cart.class));
    }

    @Test
    void testUpdateCartItem_Success() {
        // Given
        when(cartMapper.selectById(1L)).thenReturn(testCart);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);
        when(cartMapper.updateById(any(Cart.class))).thenReturn(1);

        // When
        var result = cartService.updateCartItem(1L, updateCartRequest, 100L);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(5, result.getData().getQuantity());
        verify(cartMapper, times(1)).updateById(any(Cart.class));
    }

    @Test
    void testUpdateCartItem_CartNotFound() {
        // Given
        when(cartMapper.selectById(1L)).thenReturn(null);

        // When
        var result = cartService.updateCartItem(1L, updateCartRequest, 100L);

        // Then
        assertFalse(result.isSuccess());
        verify(cartMapper, never()).updateById(any(Cart.class));
    }

    @Test
    void testUpdateCartItem_Forbidden() {
        // Given
        when(cartMapper.selectById(1L)).thenReturn(testCart);

        // When
        var result = cartService.updateCartItem(1L, updateCartRequest, 999L);

        // Then
        assertFalse(result.isSuccess());
        verify(cartMapper, never()).updateById(any(Cart.class));
    }

    @Test
    void testUpdateCartItem_InsufficientStock() {
        // Given
        testGoods.setStock(3);
        when(cartMapper.selectById(1L)).thenReturn(testCart);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);

        // When
        var result = cartService.updateCartItem(1L, updateCartRequest, 100L);

        // Then
        assertFalse(result.isSuccess());
        verify(cartMapper, never()).updateById(any(Cart.class));
    }

    @Test
    void testRemoveFromCart_Success() {
        // Given
        when(cartMapper.selectById(1L)).thenReturn(testCart);
        when(cartMapper.deleteById((Serializable) 1L)).thenReturn(1);

        // When
        var result = cartService.removeFromCart(1L, 100L);

        // Then
        assertTrue(result.isSuccess());
        verify(cartMapper, times(1)).deleteById((Serializable) 1L);
    }

    @Test
    void testRemoveFromCart_CartNotFound() {
        // Given
        when(cartMapper.selectById(1L)).thenReturn(null);

        // When
        var result = cartService.removeFromCart(1L, 100L);

        // Then
        assertFalse(result.isSuccess());
        verify(cartMapper, never()).deleteById(any(Serializable.class));
    }

    @Test
    void testRemoveFromCart_Forbidden() {
        // Given
        when(cartMapper.selectById(1L)).thenReturn(testCart);

        // When
        var result = cartService.removeFromCart(1L, 999L);

        // Then
        assertFalse(result.isSuccess());
        verify(cartMapper, never()).deleteById(any(Serializable.class));
    }

    @Test
    void testGetMyCart_Success() {
        // Given
        when(cartMapper.selectList(any())).thenReturn(Arrays.asList(testCart));
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);

        // When
        var result = cartService.getMyCart(100L);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("测试商品", result.getData().get(0).getGoodsTitle());
    }

    @Test
    void testGetMyCart_EmptyCart() {
        // Given
        when(cartMapper.selectList(any())).thenReturn(Collections.emptyList());

        // When
        var result = cartService.getMyCart(100L);

        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void testGetMyCart_GoodsDeleted() {
        // Given
        when(cartMapper.selectList(any())).thenReturn(Arrays.asList(testCart));
        when(goodsMapper.selectById(1L)).thenReturn(null);

        // When
        var result = cartService.getMyCart(100L);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("NOT_FOUND", result.getData().get(0).getGoodsStatus());
    }

    @Test
    void testClearCart_Success() {
        // Given
        when(cartMapper.selectList(any())).thenReturn(Arrays.asList(testCart));
        when(cartMapper.deleteById((Serializable) 1L)).thenReturn(1);

        // When
        var result = cartService.clearCart(100L);

        // Then
        assertTrue(result.isSuccess());
        verify(cartMapper, times(1)).deleteById((Serializable) 1L);
    }

    @Test
    void testClearCart_EmptyCart() {
        // Given
        when(cartMapper.selectList(any())).thenReturn(Collections.emptyList());

        // When
        var result = cartService.clearCart(100L);

        // Then
        assertTrue(result.isSuccess());
        verify(cartMapper, never()).deleteById(any(Serializable.class));
    }
}
