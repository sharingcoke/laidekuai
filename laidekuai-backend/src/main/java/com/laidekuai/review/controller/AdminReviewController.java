package com.laidekuai.review.controller;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.review.dto.ReviewDTO;
import com.laidekuai.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员评价控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    /**
     * GET /api/admin/reviews
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<ReviewDTO>> listReviews(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        return reviewService.listAdminReviews(status, page, size);
    }

    /**
     * POST /api/admin/reviews/{id}/hide
     */
    @PostMapping("/{id}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> hideReview(@PathVariable("id") Long reviewId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return reviewService.hideReview(reviewId, adminId);
    }

    /**
     * POST /api/admin/reviews/{id}/delete
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteReview(@PathVariable("id") Long reviewId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        return reviewService.deleteReview(reviewId, adminId);
    }
}
