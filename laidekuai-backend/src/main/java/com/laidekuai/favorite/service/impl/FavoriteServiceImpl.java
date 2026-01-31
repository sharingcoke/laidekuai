package com.laidekuai.favorite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.favorite.entity.Favorite;
import com.laidekuai.favorite.mapper.FavoriteMapper;
import com.laidekuai.favorite.service.FavoriteService;
import com.laidekuai.goods.entity.Goods;
import com.laidekuai.goods.mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务实现类
 *
 * @author Laidekuai Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final GoodsMapper goodsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> addFavorite(Long goodsId, Long userId) {
        log.info("用户 {} 收藏商品 {}", userId, goodsId);

        // 校验商品存在
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null || goods.getDeleted() == 1) {
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 检查是否已收藏
        Favorite existing = favoriteMapper.selectByUserAndGoods(userId, goodsId);
        if (existing != null) {
            return Result.error(40601, "已收藏该商品");
        }

        // 添加收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(goodsId);
        favorite.setCreatedAt(LocalDateTime.now());
        favoriteMapper.insert(favorite);

        log.info("收藏成功, ID: {}", favorite.getId());
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> removeFavorite(Long goodsId, Long userId) {
        log.info("用户 {} 取消收藏商品 {}", userId, goodsId);

        Favorite existing = favoriteMapper.selectByUserAndGoods(userId, goodsId);
        if (existing == null) {
            return Result.error(40602, "未收藏该商品");
        }

        favoriteMapper.deleteById(existing.getId());
        log.info("取消收藏成功");
        return Result.success();
    }

    @Override
    public Result<Boolean> isFavorite(Long goodsId, Long userId) {
        Favorite existing = favoriteMapper.selectByUserAndGoods(userId, goodsId);
        return Result.success(existing != null);
    }

    @Override
    public Result<PageResult<Goods>> listMyFavorites(Long userId, Long page, Long size) {
        Page<Favorite> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getDeleted, 0)
                .orderByDesc(Favorite::getCreatedAt);

        Page<Favorite> result = favoriteMapper.selectPage(pageParam, wrapper);

        // 查询商品详情
        List<Long> goodsIds = result.getRecords().stream()
                .map(Favorite::getGoodsId)
                .collect(Collectors.toList());

        List<Goods> goodsList = goodsIds.isEmpty() ? List.of() : goodsMapper.selectBatchIds(goodsIds);

        PageResult<Goods> pageResult = new PageResult<>();
        pageResult.setRecords(goodsList);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());

        return Result.success(pageResult);
    }

    @Override
    public Result<Integer> getFavoriteCount(Long goodsId) {
        int count = favoriteMapper.countByGoodsId(goodsId);
        return Result.success(count);
    }
}
