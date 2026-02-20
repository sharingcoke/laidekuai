package com.laidekuai.goods.controller;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.goods.dto.GoodsCreateRequest;
import com.laidekuai.goods.dto.GoodsSearchRequest;
import com.laidekuai.goods.dto.GoodsUpdateRequest;
import com.laidekuai.goods.entity.Goods;
import com.laidekuai.goods.service.GoodsService;
import com.laidekuai.common.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 *
 * @author Laidekuai Team
 */
@Slf4j
@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    private Long currentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    /**
     * 创建商品（卖家）
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public Result<Goods> createGoods(
            @Valid @RequestBody GoodsCreateRequest request,
            HttpServletRequest httpRequest) {
        Long sellerId = currentUserId();
        return goodsService.createGoods(request, sellerId);
    }

    /**
     * 更新商品（卖家）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public Result<Goods> updateGoods(
            @PathVariable Long id,
            @Valid @RequestBody GoodsUpdateRequest request,
            HttpServletRequest httpRequest) {
        Long sellerId = currentUserId();
        return goodsService.updateGoods(id, request, sellerId);
    }

    /**
     * 删除商品（卖家）- 软删除
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public Result<Void> deleteGoods(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long sellerId = currentUserId();
        return goodsService.deleteGoods(id, sellerId);
    }

    /**
     * 商品详情
     */
    @GetMapping("/{id}")
    public Result<Goods> getGoodsDetail(@PathVariable Long id) {
        return goodsService.getGoodsDetail(id, SecurityUtils.getCurrentUserId(), SecurityUtils.isAdmin());
    }

    /**
     * 商品列表/搜索
     */
    @GetMapping
    public Result<PageResult<Goods>> listGoods(GoodsSearchRequest request) {
        return goodsService.listGoods(request);
    }

    /**
     * 提交审核（卖家）
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public Result<Void> submitForAudit(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long sellerId = currentUserId();
        return goodsService.submitForAudit(id, sellerId);
    }

    /**
     * 审核通过（管理员）
     */
    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> approveGoods(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long adminId = currentUserId();
        return goodsService.approveGoods(id, adminId);
    }

    /**
     * 审核驳回（管理员）
     */
    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> rejectGoods(
            @PathVariable Long id,
            @RequestParam String reason,
            HttpServletRequest httpRequest) {
        Long adminId = currentUserId();
        return goodsService.rejectGoods(id, reason, adminId);
    }

    /**
     * 手动下架商品（卖家）
     */
    @PostMapping("/{id}/offline")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public Result<Void> offlineGoods(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long sellerId = currentUserId();
        return goodsService.offlineGoods(id, sellerId);
    }

    /**
     * 查询我的商品列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public Result<PageResult<Goods>> listMyGoods(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        Long sellerId = currentUserId();
        return goodsService.listMyGoods(sellerId, page, size, status, keyword);
    }

    /**
     * 管理员查询商品列表
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Goods>> listAdminGoods(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return goodsService.listAdminGoods(page, size, status, keyword, categoryId);
    }
}
