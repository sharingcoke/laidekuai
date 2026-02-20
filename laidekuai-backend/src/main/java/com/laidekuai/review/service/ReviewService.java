package com.laidekuai.review.service;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.review.dto.ReviewDTO;
import com.laidekuai.review.dto.ReviewRequest;

/**
 * 评价服务接口
 *
 * @author Laidekuai Team
 */
public interface ReviewService {

    /**
     * 创建评价
     *
     * @param request 评价请求
     * @param userId  当前用户ID
     * @return 创建结果
     */
    Result<ReviewDTO> createReview(ReviewRequest request, Long userId);

    /**
     * 卖家回复评价
     *
     * @param reviewId 评价ID
     * @param reply    回复内容
     * @param userId   当前用户ID（卖家）
     * @return 回复结果
     */
    Result<ReviewDTO> replyReview(Long reviewId, String reply, Long userId);

    /**
     * 获取商品评价列表
     *
     * @param goodsId 商品ID
     * @param page    页码
     * @param size    每页大小
     * @return 评价列表
     */
    Result<PageResult<ReviewDTO>> listGoodsReviews(Long goodsId, Long page, Long size);

    /**
     * 获取商品平均评分
     *
     * @param goodsId 商品ID
     * @return 平均评分
     */
    Result<Double> getGoodsRating(Long goodsId);

    /**
     * 获取我的评价列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 评价列表
     */
    Result<PageResult<ReviewDTO>> listMyReviews(Long userId, Long page, Long size);

    /**
     * 管理员评价列表
     *
     * @param status 状态（visible/hidden/deleted）
     * @param page   页码
     * @param size   每页大小
     * @return 评价列表
     */
    Result<PageResult<ReviewDTO>> listAdminReviews(String status, Long page, Long size);

    /**
     * 管理员隐藏评价
     *
     * @param reviewId 评价ID
     * @param adminId  管理员ID
     * @return 结果
     */
    Result<Void> hideReview(Long reviewId, Long adminId);

    /**
     * 管理员删除评价（软删）
     *
     * @param reviewId 评价ID
     * @param adminId  管理员ID
     * @return 结果
     */
    Result<Void> deleteReview(Long reviewId, Long adminId);
}
