package com.laidekuai.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.goods.entity.Goods;
import com.laidekuai.goods.mapper.GoodsMapper;
import com.laidekuai.order.entity.Order;
import com.laidekuai.order.entity.OrderItem;
import com.laidekuai.order.mapper.OrderItemMapper;
import com.laidekuai.order.mapper.OrderMapper;
import com.laidekuai.review.dto.ReviewDTO;
import com.laidekuai.review.dto.ReviewRequest;
import com.laidekuai.review.entity.Review;
import com.laidekuai.review.mapper.ReviewMapper;
import com.laidekuai.review.service.ReviewService;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价服务实现类
 *
 * @author Laidekuai Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ReviewDTO> createReview(ReviewRequest request, Long userId) {
        log.info("用户 {} 评价订单项 {}", userId, request.getOrderItemId());

        boolean isAdmin = SecurityUtils.isAdmin();

        // 1. 校验订单项
        OrderItem item = orderItemMapper.selectById(request.getOrderItemId());
        if (item == null || item.getDeleted() == 1) {
            return Result.error(ErrorCode.BAD_REQUEST.getCode(), "订单项不存在");
        }

        // 2. 校验订单
        Order order = orderMapper.selectById(item.getOrderId());
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }

        // 只有买家可以评价（管理员可跳过）
        if (!isAdmin && !order.getBuyerId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 只有已完成/已退款订单才能评价
        if (!"COMPLETED".equals(order.getStatus()) && !"REFUNDED".equals(order.getStatus())) {
            return Result.error(ErrorCode.ORDER_STATUS_ERROR);
        }

        // 订单完成/退款 30 天内可评价（管理员不受限制）
        if (!isAdmin) {
            LocalDateTime baseTime = resolveReviewBaseTime(order);
            if (baseTime != null && baseTime.plusDays(30).isBefore(LocalDateTime.now())) {
                return Result.error(ErrorCode.VALIDATION_FAILED.getCode(), "超过评价时限（30天）");
            }
        }

        // 订单项唯一评价约束
        if (reviewMapper.countByOrderItemId(item.getId()) > 0) {
            return Result.error(ErrorCode.CONFLICT);
        }

        // 3. 创建评价
        Review review = new Review();
        review.setOrderId(order.getId());
        review.setOrderItemId(item.getId());
        review.setGoodsId(item.getGoodsId());
        review.setBuyerId(order.getBuyerId());
        review.setSellerId(item.getSellerId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setIsAnonymous(request.getIsAnonymous() != null && request.getIsAnonymous() ? 1 : 0);
        review.setIsRefunded("REFUNDED".equals(order.getStatus()) ? 1 : 0);
        review.setStatus("visible");
        review.setCreatedAt(LocalDateTime.now());

        // 处理图片
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            review.setImages("[\"" + String.join("\",\"", request.getImages()) + "\"]");
        }

        reviewMapper.insert(review);
        log.info("评价创建成功, ID: {}", review.getId());

        // 获取用户昵称
        User user = userMapper.selectById(order.getBuyerId());
        String nickname = user != null ? user.getNickName() : null;
        ReviewDTO dto = ReviewDTO.fromReview(review, nickname);
        Goods goods = goodsMapper.selectById(item.getGoodsId());
        if (goods != null) {
            dto.setGoodsTitle(goods.getTitle());
        }

        return Result.success(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ReviewDTO> replyReview(Long reviewId, String reply, Long userId) {
        log.info("卖家/管理员 {} 回复评价 {}", userId, reviewId);

        Review review = reviewMapper.selectById(reviewId);
        if (review == null || review.getDeleted() == 1) {
            return Result.error(ErrorCode.REVIEW_NOT_FOUND);
        }

        boolean isAdmin = SecurityUtils.isAdmin();
        if (!isAdmin) {
            OrderItem item = orderItemMapper.selectById(review.getOrderItemId());
            if (item == null || !item.getSellerId().equals(userId)) {
                return Result.error(ErrorCode.FORBIDDEN);
            }
        }

        // 更新回复
        review.setReply(reply);
        review.setReplyTime(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        reviewMapper.updateById(review);

        log.info("评价 {} 回复成功", reviewId);

        User user = userMapper.selectById(review.getBuyerId());
        String nickname = user != null ? user.getNickName() : null;

        ReviewDTO dto = ReviewDTO.fromReview(review, nickname);
        Goods goods = goodsMapper.selectById(review.getGoodsId());
        if (goods != null) {
            dto.setGoodsTitle(goods.getTitle());
        }

        return Result.success(dto);
    }

    @Override
    public Result<PageResult<ReviewDTO>> listGoodsReviews(Long goodsId, Long page, Long size) {
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getGoodsId, goodsId)
                .eq(Review::getDeleted, 0)
                .eq(Review::getStatus, "visible")
                .orderByDesc(Review::getCreatedAt);

        Page<Review> result = reviewMapper.selectPage(pageParam, wrapper);

        List<ReviewDTO> dtos = result.getRecords().stream()
                .map(review -> {
                    User user = userMapper.selectById(review.getBuyerId());
                    String nickname = user != null ? user.getNickName() : null;
                    return ReviewDTO.fromReview(review, nickname);
                })
                .collect(Collectors.toList());

        PageResult<ReviewDTO> pageResult = new PageResult<>();
        pageResult.setRecords(dtos);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());

        return Result.success(pageResult);
    }

    @Override
    public Result<Double> getGoodsRating(Long goodsId) {
        Double rating = reviewMapper.getAverageRatingByGoodsId(goodsId);
        return Result.success(rating != null ? rating : 0.0);
    }

    @Override
    public Result<PageResult<ReviewDTO>> listMyReviews(Long userId, Long page, Long size) {
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getBuyerId, userId)
                .eq(Review::getDeleted, 0)
                .eq(Review::getStatus, "visible")
                .orderByDesc(Review::getCreatedAt);

        Page<Review> result = reviewMapper.selectPage(pageParam, wrapper);

        List<ReviewDTO> dtos = result.getRecords().stream()
                .map(review -> {
                    User user = userMapper.selectById(review.getBuyerId());
                    String nickname = user != null ? user.getNickName() : null;
                    ReviewDTO dto = ReviewDTO.fromReview(review, nickname);
                    Goods goods = goodsMapper.selectById(review.getGoodsId());
                    if (goods != null) {
                        dto.setGoodsTitle(goods.getTitle());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        PageResult<ReviewDTO> pageResult = new PageResult<>();
        pageResult.setRecords(dtos);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());

        return Result.success(pageResult);
    }

    @Override
    public Result<PageResult<ReviewDTO>> listAdminReviews(String status, Long page, Long size) {
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getDeleted, 0);
        if (status != null && !status.isBlank()) {
            wrapper.eq(Review::getStatus, status);
        }
        wrapper.orderByDesc(Review::getCreatedAt);

        Page<Review> result = reviewMapper.selectPage(pageParam, wrapper);
        List<ReviewDTO> dtos = result.getRecords().stream()
                .map(review -> {
                    User user = userMapper.selectById(review.getBuyerId());
                    String nickname = user != null ? user.getNickName() : null;
                    ReviewDTO dto = ReviewDTO.fromReview(review, nickname);
                    Goods goods = goodsMapper.selectById(review.getGoodsId());
                    if (goods != null) {
                        dto.setGoodsTitle(goods.getTitle());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        PageResult<ReviewDTO> pageResult = new PageResult<>();
        pageResult.setRecords(dtos);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());

        return Result.success(pageResult);
    }

    private LocalDateTime resolveReviewBaseTime(Order order) {
        if ("COMPLETED".equals(order.getStatus())) {
            if (order.getSettledTime() != null) {
                return order.getSettledTime();
            }
            if (order.getUpdatedAt() != null) {
                return order.getUpdatedAt();
            }
            return order.getCreatedAt();
        }
        if ("REFUNDED".equals(order.getStatus())) {
            if (order.getCancelTime() != null) {
                return order.getCancelTime();
            }
            if (order.getUpdatedAt() != null) {
                return order.getUpdatedAt();
            }
            return order.getCreatedAt();
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> hideReview(Long reviewId, Long adminId) {
        log.info("管理员 {} 隐藏评价 {}", adminId, reviewId);

        LambdaUpdateWrapper<Review> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Review::getId, reviewId)
                .eq(Review::getDeleted, 0)
                .eq(Review::getStatus, "visible")
                .set(Review::getStatus, "hidden")
                .set(Review::getUpdatedAt, LocalDateTime.now());

        int updated = reviewMapper.update(null, wrapper);
        if (updated == 0) {
            return Result.error(ErrorCode.CONFLICT);
        }
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteReview(Long reviewId, Long adminId) {
        log.info("管理员 {} 删除评价 {}", adminId, reviewId);

        LambdaUpdateWrapper<Review> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Review::getId, reviewId)
                .eq(Review::getDeleted, 0)
                .set(Review::getStatus, "deleted")
                .set(Review::getUpdatedAt, LocalDateTime.now());

        int updated = reviewMapper.update(null, wrapper);
        if (updated == 0) {
            return Result.error(ErrorCode.CONFLICT);
        }
        return Result.success();
    }
}
