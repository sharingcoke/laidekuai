package com.laidekuai.goods.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laidekuai.common.enums.GoodsStatus;
import com.laidekuai.goods.dto.GoodsCreateRequest;
import com.laidekuai.goods.dto.GoodsSearchRequest;
import com.laidekuai.goods.dto.GoodsUpdateRequest;
import com.laidekuai.goods.entity.Goods;
import com.laidekuai.goods.mapper.GoodsMapper;
import com.laidekuai.goods.service.impl.GoodsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 商品服务测试
 *
 * @author Laidekuai Team
 */
@ExtendWith(MockitoExtension.class)
class GoodsServiceTest {

    @Mock
    private GoodsMapper goodsMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GoodsServiceImpl goodsService;

    private Goods testGoods;
    private GoodsCreateRequest createRequest;
    private GoodsUpdateRequest updateRequest;

    @BeforeEach
    void setUp() throws Exception {
        // 初始化测试数据
        testGoods = new Goods();
        testGoods.setId(1L);
        testGoods.setSellerId(100L);
        testGoods.setCategoryId(10L);
        testGoods.setTitle("测试商品");
        testGoods.setSubTitle("测试副标题");
        testGoods.setPrice(new BigDecimal("99.99"));
        testGoods.setStock(100);
        testGoods.setDetail("测试详情");
        testGoods.setImageUrls("[\"url1\", \"url2\"]");
        testGoods.setStatus(GoodsStatus.DRAFT);
        testGoods.setCreatedAt(LocalDateTime.now());
        testGoods.setUpdatedAt(LocalDateTime.now());

        createRequest = new GoodsCreateRequest();
        createRequest.setCategoryId(10L);
        createRequest.setTitle("测试商品");
        createRequest.setSubTitle("测试副标题");
        createRequest.setPrice(new BigDecimal("99.99"));
        createRequest.setStock(100);
        createRequest.setDetail("测试详情");
        createRequest.setImageUrls(Arrays.asList("url1", "url2"));

        updateRequest = new GoodsUpdateRequest();
        updateRequest.setTitle("更新后的商品");
        updateRequest.setPrice(new BigDecimal("199.99"));
    }

    @Test
    void testCreateGoods_Success() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(any())).thenReturn("[\"url1\", \"url2\"]");
        when(goodsMapper.insert(any(Goods.class))).thenAnswer(invocation -> {
            Goods goods = invocation.getArgument(0);
            goods.setId(1L);
            return 1;
        });

        // When
        var result = goodsService.createGoods(createRequest, 100L);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("测试商品", result.getData().getTitle());
        assertEquals(GoodsStatus.DRAFT, result.getData().getStatus());
        verify(goodsMapper, times(1)).insert(any(Goods.class));
    }

    @Test
    void testCreateGoods_InvalidImageUrls() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Invalid JSON"));

        // When
        var result = goodsService.createGoods(createRequest, 100L);

        // Then
        assertFalse(result.isSuccess());
        verify(goodsMapper, never()).insert(any(Goods.class));
    }

    @Test
    void testUpdateGoods_Success() throws Exception {
        // Given
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);
        when(goodsMapper.updateById(any(Goods.class))).thenReturn(1);

        // When
        var result = goodsService.updateGoods(1L, updateRequest, 100L);

        // Then
        assertTrue(result.isSuccess());
        verify(goodsMapper, times(1)).updateById(any(Goods.class));
    }

    @Test
    void testUpdateGoods_GoodsNotFound() {
        // Given
        when(goodsMapper.selectById(1L)).thenReturn(null);

        // When
        var result = goodsService.updateGoods(1L, updateRequest, 100L);

        // Then
        assertFalse(result.isSuccess());
        verify(goodsMapper, never()).updateById(any(Goods.class));
    }

    @Test
    void testUpdateGoods_Forbidden() {
        // Given
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);

        // When
        var result = goodsService.updateGoods(1L, updateRequest, 999L);

        // Then
        assertFalse(result.isSuccess());
        verify(goodsMapper, never()).updateById(any(Goods.class));
    }

    @Test
    void testDeleteGoods_Success() {
        // Given
        testGoods.setStatus(GoodsStatus.OFFLINE);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);
        when(goodsMapper.deleteById(1L)).thenReturn(1);

        // When
        var result = goodsService.deleteGoods(1L, 100L);

        // Then
        assertTrue(result.isSuccess());
        verify(goodsMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteGoods_InvalidStatus() {
        // Given
        testGoods.setStatus(GoodsStatus.APPROVED);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);

        // When
        var result = goodsService.deleteGoods(1L, 100L);

        // Then
        assertFalse(result.isSuccess());
        verify(goodsMapper, never()).deleteById(any());
    }

    @Test
    void testGetGoodsDetail_Success() {
        // Given
        testGoods.setStatus(GoodsStatus.APPROVED);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);

        // When
        var result = goodsService.getGoodsDetail(1L);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getData().getId());
    }

    @Test
    void testGetGoodsDetail_NotFound() {
        // Given
        when(goodsMapper.selectById(1L)).thenReturn(null);

        // When
        var result = goodsService.getGoodsDetail(1L);

        // Then
        assertFalse(result.isSuccess());
    }

    @Test
    void testSubmitForAudit_Success() {
        // Given
        testGoods.setStatus(GoodsStatus.DRAFT);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);
        when(goodsMapper.updateById(any(Goods.class))).thenReturn(1);

        // When
        var result = goodsService.submitForAudit(1L, 100L);

        // Then
        assertTrue(result.isSuccess());
        verify(goodsMapper, times(1)).updateById(any(Goods.class));
    }

    @Test
    void testSubmitForAudit_InvalidStatus() {
        // Given
        testGoods.setStatus(GoodsStatus.APPROVED);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);

        // When
        var result = goodsService.submitForAudit(1L, 100L);

        // Then
        assertFalse(result.isSuccess());
        verify(goodsMapper, never()).updateById(any(Goods.class));
    }

    @Test
    void testApproveGoods_Success() {
        // Given
        testGoods.setStatus(GoodsStatus.PENDING);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);
        when(goodsMapper.updateById(any(Goods.class))).thenReturn(1);

        // When
        var result = goodsService.approveGoods(1L, 1L);

        // Then
        assertTrue(result.isSuccess());
        verify(goodsMapper, times(1)).updateById(any(Goods.class));
    }

    @Test
    void testRejectGoods_Success() {
        // Given
        testGoods.setStatus(GoodsStatus.PENDING);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);
        when(goodsMapper.updateById(any(Goods.class))).thenReturn(1);

        // When
        var result = goodsService.rejectGoods(1L, "商品信息不完整", 1L);

        // Then
        assertTrue(result.isSuccess());
        verify(goodsMapper, times(1)).updateById(any(Goods.class));
    }

    @Test
    void testOfflineGoods_Success() {
        // Given
        testGoods.setStatus(GoodsStatus.APPROVED);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);
        when(goodsMapper.updateById(any(Goods.class))).thenReturn(1);

        // When
        var result = goodsService.offlineGoods(1L, 100L);

        // Then
        assertTrue(result.isSuccess());
        verify(goodsMapper, times(1)).updateById(any(Goods.class));
    }

    @Test
    void testOfflineGoods_InvalidStatus() {
        // Given
        testGoods.setStatus(GoodsStatus.DRAFT);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);

        // When
        var result = goodsService.offlineGoods(1L, 100L);

        // Then
        assertFalse(result.isSuccess());
        verify(goodsMapper, never()).updateById(any(Goods.class));
    }
}
