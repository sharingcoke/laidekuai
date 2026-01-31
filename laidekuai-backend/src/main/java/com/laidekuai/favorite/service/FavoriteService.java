package com.laidekuai.favorite.service;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.goods.entity.Goods;

/**
 * 收藏服务接口
 *
 * @author Laidekuai Team
 */
public interface FavoriteService {

    /**
     * 添加收藏
     *
     * @param goodsId 商品ID
     * @param userId  用户ID
     * @return 操作结果
     */
    Result<Void> addFavorite(Long goodsId, Long userId);

    /**
     * 取消收藏
     *
     * @param goodsId 商品ID
     * @param userId  用户ID
     * @return 操作结果
     */
    Result<Void> removeFavorite(Long goodsId, Long userId);

    /**
     * 检查是否已收藏
     *
     * @param goodsId 商品ID
     * @param userId  用户ID
     * @return 是否已收藏
     */
    Result<Boolean> isFavorite(Long goodsId, Long userId);

    /**
     * 我的收藏列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 收藏商品列表
     */
    Result<PageResult<Goods>> listMyFavorites(Long userId, Long page, Long size);

    /**
     * 获取商品收藏数
     *
     * @param goodsId 商品ID
     * @return 收藏数
     */
    Result<Integer> getFavoriteCount(Long goodsId);
}
