package com.laidekuai.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laidekuai.review.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评价Mapper接口
 *
 * @author Laidekuai Team
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /**
     * 查询商品评价列表
     *
     * @param goodsId 商品ID
     * @return 评价列表
     */
    List<Review> selectByGoodsId(@Param("goodsId") Long goodsId);

    /**
     * 查询订单是否已评价
     *
     * @param orderId 订单ID
     * @return 评价数量
     */
    int countByOrderId(@Param("orderId") Long orderId);

    /**
     * 查询订单项是否已评价
     *
     * @param orderItemId 订单项ID
     * @return 评价数量
     */
    int countByOrderItemId(@Param("orderItemId") Long orderItemId);

    /**
     * 计算商品平均评分
     *
     * @param goodsId 商品ID
     * @return 平均评分
     */
    Double getAverageRatingByGoodsId(@Param("goodsId") Long goodsId);
}
