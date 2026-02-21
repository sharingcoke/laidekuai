package com.laidekuai.review.service;

import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.Result;
import com.laidekuai.goods.mapper.GoodsMapper;
import com.laidekuai.order.entity.Order;
import com.laidekuai.order.entity.OrderItem;
import com.laidekuai.order.mapper.OrderItemMapper;
import com.laidekuai.order.mapper.OrderMapper;
import com.laidekuai.review.dto.ReviewDTO;
import com.laidekuai.review.dto.ReviewRequest;
import com.laidekuai.review.entity.Review;
import com.laidekuai.review.mapper.ReviewMapper;
import com.laidekuai.review.service.impl.ReviewServiceImpl;
import com.laidekuai.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private GoodsMapper goodsMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void testCreateReview_DuplicateOrderItem() {
        ReviewRequest request = new ReviewRequest();
        request.setOrderItemId(1L);
        request.setRating(5);
        request.setContent("good");

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrderId(10L);
        item.setGoodsId(20L);
        item.setSellerId(30L);
        item.setDeleted(0);

        Order order = new Order();
        order.setId(10L);
        order.setBuyerId(100L);
        order.setStatus("COMPLETED");
        order.setSettledTime(LocalDateTime.now().minusDays(1));
        order.setDeleted(0);

        when(orderItemMapper.selectById(1L)).thenReturn(item);
        when(orderMapper.selectById(10L)).thenReturn(order);
        when(reviewMapper.countByOrderItemId(1L)).thenReturn(1);

        Result<ReviewDTO> result = reviewService.createReview(request, 100L);

        assertEquals(ErrorCode.CONFLICT.getCode(), result.getCode());
        verify(reviewMapper, never()).insert(any(Review.class));
    }

    @Test
    void testCreateReview_Timeout() {
        ReviewRequest request = new ReviewRequest();
        request.setOrderItemId(2L);
        request.setRating(5);

        OrderItem item = new OrderItem();
        item.setId(2L);
        item.setOrderId(20L);
        item.setGoodsId(22L);
        item.setSellerId(33L);
        item.setDeleted(0);

        Order order = new Order();
        order.setId(20L);
        order.setBuyerId(100L);
        order.setStatus("COMPLETED");
        order.setSettledTime(LocalDateTime.now().minusDays(31));
        order.setDeleted(0);

        when(orderItemMapper.selectById(2L)).thenReturn(item);
        when(orderMapper.selectById(20L)).thenReturn(order);
        when(reviewMapper.countByOrderItemId(2L)).thenReturn(0);

        Result<ReviewDTO> result = reviewService.createReview(request, 100L);

        assertEquals(ErrorCode.VALIDATION_FAILED.getCode(), result.getCode());
        verify(reviewMapper, never()).insert(any(Review.class));
    }

    @Test
    void testCreateReview_RefundedMarksFlag() {
        ReviewRequest request = new ReviewRequest();
        request.setOrderItemId(3L);
        request.setRating(4);

        OrderItem item = new OrderItem();
        item.setId(3L);
        item.setOrderId(30L);
        item.setGoodsId(40L);
        item.setSellerId(50L);
        item.setDeleted(0);

        Order order = new Order();
        order.setId(30L);
        order.setBuyerId(100L);
        order.setStatus("REFUNDED");
        order.setCancelTime(LocalDateTime.now().minusDays(2));
        order.setDeleted(0);

        when(orderItemMapper.selectById(3L)).thenReturn(item);
        when(orderMapper.selectById(30L)).thenReturn(order);
        when(reviewMapper.countByOrderItemId(3L)).thenReturn(0);
        when(reviewMapper.insert(any(Review.class))).thenReturn(1);

        Result<ReviewDTO> result = reviewService.createReview(request, 100L);

        assertTrue(result.isSuccess());

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getIsRefunded());
    }

    @Test
    void testCreateReview_NormalOrder() {
        ReviewRequest request = new ReviewRequest();
        request.setOrderItemId(4L);
        request.setRating(5);
        request.setContent("ok");

        OrderItem item = new OrderItem();
        item.setId(4L);
        item.setOrderId(40L);
        item.setGoodsId(44L);
        item.setSellerId(55L);
        item.setDeleted(0);

        Order order = new Order();
        order.setId(40L);
        order.setBuyerId(100L);
        order.setStatus("COMPLETED");
        order.setSettledTime(LocalDateTime.now().minusDays(5));
        order.setDeleted(0);

        when(orderItemMapper.selectById(4L)).thenReturn(item);
        when(orderMapper.selectById(40L)).thenReturn(order);
        when(reviewMapper.countByOrderItemId(4L)).thenReturn(0);
        when(reviewMapper.insert(any(Review.class))).thenReturn(1);

        Result<ReviewDTO> result = reviewService.createReview(request, 100L);

        assertTrue(result.isSuccess());

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewMapper).insert(captor.capture());
        assertEquals(0, captor.getValue().getIsRefunded());
    }
}
