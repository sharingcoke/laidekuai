package com.laidekuai.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laidekuai.goods.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品Mapper
 *
 * @author Laidekuai Team
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 条件扣减库存（防超卖）
     *
     * @param goodsId 商品ID
     * @param quantity 扣减数量
     * @return 影响行数
     */
    int deductStock(@Param("goodsId") Long goodsId, @Param("quantity") Integer quantity);

    /**
     * 释放库存（取消订单/退款时）
     *
     * @param goodsId 商品ID
     * @param quantity 释放数量
     * @return 影响行数
     */
    int releaseStock(@Param("goodsId") Long goodsId, @Param("quantity") Integer quantity);
}
