package com.laidekuai.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laidekuai.cart.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 购物车Mapper
 *
 * @author Laidekuai Team
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 获取用户购物车中指定商品的记录
     *
     * @param userId  用户ID
     * @param goodsId 商品ID
     * @return 购物车记录
     */
    Cart findByUserAndGoods(@Param("userId") Long userId, @Param("goodsId") Long goodsId);
}
