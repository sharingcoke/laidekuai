package com.laidekuai.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.laidekuai.favorite.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 收藏Mapper接口
 *
 * @author Laidekuai Team
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 查询用户是否已收藏某商品
     *
     * @param userId  用户ID
     * @param goodsId 商品ID
     * @return 收藏记录
     */
    Favorite selectByUserAndGoods(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    /**
     * 统计商品收藏数
     *
     * @param goodsId 商品ID
     * @return 收藏数
     */
    int countByGoodsId(@Param("goodsId") Long goodsId);
}
