package com.laidekuai.review.controller;

import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.review.dto.ReviewDTO;
import com.laidekuai.review.dto.ReviewReplyRequest;
import com.laidekuai.review.dto.ReviewRequest;
import com.laidekuai.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 评价控制器
 *
 * @author Laidekuai Team
 */
@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 创建评价
     * POST /api/reviews
     */
    @PostMapping
    public Result<ReviewDTO> createReview(@Valid @RequestBody ReviewRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return reviewService.createReview(request, userId);
    }

    /**
     * 卖家回复评价
     * POST /api/reviews/{id}/reply
     */
    @PostMapping("/{id}/reply")
    public Result<ReviewDTO> replyReview(
            @PathVariable("id") Long reviewId,
            @Valid @RequestBody(required = false) ReviewReplyRequest request,
            @RequestParam(value = "content", required = false) String reply) {
        Long userId = SecurityUtils.getCurrentUserId();
        String content = request != null ? request.getSellerReply() : reply;
        if (content == null || content.isBlank()) {
            return Result.error(ErrorCode.VALIDATION_FAILED.getCode(), "回复内容不能为空");
        }
        return reviewService.replyReview(reviewId, content, userId);
    }

    /**
     * 商品评价列表
     * GET /api/reviews/goods/{goodsId}
     */
    @GetMapping("/goods/{goodsId}")
    public Result<PageResult<ReviewDTO>> listGoodsReviews(
            @PathVariable("goodsId") Long goodsId,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        return reviewService.listGoodsReviews(goodsId, page, size);
    }

    /**
     * 商品评分
     * GET /api/reviews/goods/{goodsId}/rating
     */
    @GetMapping("/goods/{goodsId}/rating")
    public Result<Double> getGoodsRating(@PathVariable("goodsId") Long goodsId) {
        return reviewService.getGoodsRating(goodsId);
    }

    /**
     * 我的评价列表
     * GET /api/reviews/my
     */
    @GetMapping("/my")
    public Result<PageResult<ReviewDTO>> listMyReviews(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return reviewService.listMyReviews(userId, page, size);
    }
}
