package com.laidekuai.favorite.controller;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.favorite.service.FavoriteService;
import com.laidekuai.goods.entity.Goods;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 *
 * @author Laidekuai Team
 */
@Slf4j
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 添加收藏
     * POST /api/favorites/{goodsId}
     */
    @PostMapping("/{goodsId}")
    public Result<Void> addFavorite(@PathVariable("goodsId") Long goodsId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return favoriteService.addFavorite(goodsId, userId);
    }

    /**
     * 取消收藏
     * DELETE /api/favorites/{goodsId}
     */
    @DeleteMapping("/{goodsId}")
    public Result<Void> removeFavorite(@PathVariable("goodsId") Long goodsId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return favoriteService.removeFavorite(goodsId, userId);
    }

    /**
     * 检查是否已收藏
     * GET /api/favorites/{goodsId}/check
     */
    @GetMapping("/{goodsId}/check")
    public Result<Boolean> isFavorite(@PathVariable("goodsId") Long goodsId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return favoriteService.isFavorite(goodsId, userId);
    }

    /**
     * 我的收藏列表
     * GET /api/favorites
     */
    @GetMapping
    public Result<PageResult<Goods>> listMyFavorites(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return favoriteService.listMyFavorites(userId, page, size);
    }

    /**
     * 商品收藏数
     * GET /api/favorites/{goodsId}/count
     */
    @GetMapping("/{goodsId}/count")
    public Result<Integer> getFavoriteCount(@PathVariable("goodsId") Long goodsId) {
        return favoriteService.getFavoriteCount(goodsId);
    }
}
