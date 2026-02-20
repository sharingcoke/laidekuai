package com.laidekuai.goods.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.goods.dto.GoodsCreateRequest;
import com.laidekuai.goods.dto.GoodsSearchRequest;
import com.laidekuai.goods.dto.GoodsUpdateRequest;
import com.laidekuai.goods.entity.Goods;

/**
 * 商品服务接口
 *
 * @author Laidekuai Team
 */
public interface GoodsService {

    /**
     * 创建商品（卖家）
     *
     * @param request 创建请求
     * @param sellerId 卖家ID
     * @return 创建结果
     */
    Result<Goods> createGoods(GoodsCreateRequest request, Long sellerId);

    /**
     * 更新商品（卖家）
     *
     * @param goodsId 商品ID
     * @param request 更新请求
     * @param sellerId 卖家ID
     * @return 更新结果
     */
    Result<Goods> updateGoods(Long goodsId, GoodsUpdateRequest request, Long sellerId);

    /**
     * 下架商品（软删除）
     *
     * @param goodsId 商品ID
     * @param sellerId 卖家ID
     * @return 下架结果
     */
    Result<Void> deleteGoods(Long goodsId, Long sellerId);

    /**
     * 商品详情
     *
     * @param goodsId 商品ID
     * @return 商品详情
     */
    Result<Goods> getGoodsDetail(Long goodsId, Long currentUserId, boolean isAdmin);

    /**
     * 商品列表/搜索
     *
     * @param request 搜索请求
     * @return 商品列表
     */
    Result<PageResult<Goods>> listGoods(GoodsSearchRequest request);

    /**
     * 提交审核（卖家）
     *
     * @param goodsId 商品ID
     * @param sellerId 卖家ID
     * @return 提交结果
     */
    Result<Void> submitForAudit(Long goodsId, Long sellerId);

    /**
     * 审核通过（管理员）
     *
     * @param goodsId 商品ID
     * @param adminId 管理员ID
     * @return 审核结果
     */
    Result<Void> approveGoods(Long goodsId, Long adminId);

    /**
     * 审核驳回（管理员）
     *
     * @param goodsId 商品ID
     * @param reason 驳回原因
     * @param adminId 管理员ID
     * @return 审核结果
     */
    Result<Void> rejectGoods(Long goodsId, String reason, Long adminId);

    /**
     * 手动下架商品（卖家）
     *
     * @param goodsId 商品ID
     * @param sellerId 卖家ID
     * @return 下架结果
     */
    Result<Void> offlineGoods(Long goodsId, Long sellerId);

    /**
     * 查询我的商品列表
     *
     * @param sellerId 卖家ID
     * @param page 页码
     * @param size 每页大小
     * @param status 状态
     * @param keyword 关键词
     * @return 商品列表
     */
    Result<PageResult<Goods>> listMyGoods(Long sellerId, Long page, Long size, String status, String keyword);

    /**
     * 管理员查询商品列表
     */
    Result<PageResult<Goods>> listAdminGoods(Long page, Long size, String status, String keyword, Long categoryId);
}
