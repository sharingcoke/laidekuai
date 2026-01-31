package com.laidekuai.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.enums.GoodsStatus;
import com.laidekuai.common.exception.BusinessException;
import com.laidekuai.goods.dto.GoodsCreateRequest;
import com.laidekuai.goods.dto.GoodsSearchRequest;
import com.laidekuai.goods.dto.GoodsUpdateRequest;
import com.laidekuai.goods.entity.Goods;
import com.laidekuai.goods.mapper.GoodsMapper;
import com.laidekuai.goods.service.GoodsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 商品服务实现
 *
 * @author Laidekuai Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    private final GoodsMapper goodsMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Goods> createGoods(GoodsCreateRequest request, Long sellerId) {
        log.info("创建商品，卖家ID: {}, 标题: {}", sellerId, request.getTitle());

        // 1. 验证图片URL列表格式
        String imageUrlsJson;
        try {
            imageUrlsJson = objectMapper.writeValueAsString(request.getImageUrls());
        } catch (JsonProcessingException e) {
            log.warn("图片URL列表格式错误: {}", request.getImageUrls());
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // 2. 创建商品
        Goods goods = new Goods();
        goods.setSellerId(sellerId);
        goods.setCategoryId(request.getCategoryId());
        goods.setTitle(request.getTitle());
        goods.setSubTitle(request.getSubTitle());
        goods.setPrice(request.getPrice());
        goods.setStock(request.getStock());
        goods.setDetail(request.getDetail());
        goods.setImageUrls(imageUrlsJson);
        goods.setStatus(GoodsStatus.DRAFT);
        goods.setCreatedAt(LocalDateTime.now());
        goods.setUpdatedAt(LocalDateTime.now());

        // 3. 保存到数据库
        goodsMapper.insert(goods);

        log.info("商品创建成功，商品ID: {}, 卖家ID: {}", goods.getId(), sellerId);

        return Result.success(goods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Goods> updateGoods(Long goodsId, GoodsUpdateRequest request, Long sellerId) {
        log.info("更新商品，商品ID: {}, 卖家ID: {}", goodsId, sellerId);

        // 1. 查询商品
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            log.warn("商品不存在: {}", goodsId);
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 2. 权限校验：只能修改自己的商品
        if (!goods.getSellerId().equals(sellerId)) {
            log.warn("无权修改商品，商品ID: {}, 卖家ID: {}, 当前用户: {}", goodsId, goods.getSellerId(), sellerId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 3. 状态校验：只能修改草稿或已驳回状态的商品
        if (goods.getStatus() != GoodsStatus.DRAFT && goods.getStatus() != GoodsStatus.REJECTED) {
            log.warn("商品状态不允许修改，商品ID: {}, 状态: {}", goodsId, goods.getStatus());
            return Result.error(ErrorCode.GOODS_STATUS_ERROR);
        }

        // 4. 更新字段
        if (request.getCategoryId() != null) {
            goods.setCategoryId(request.getCategoryId());
        }
        if (StringUtils.hasText(request.getTitle())) {
            goods.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getSubTitle())) {
            goods.setSubTitle(request.getSubTitle());
        }
        if (request.getPrice() != null) {
            goods.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            goods.setStock(request.getStock());
        }
        if (StringUtils.hasText(request.getDetail())) {
            goods.setDetail(request.getDetail());
        }
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            try {
                String imageUrlsJson = objectMapper.writeValueAsString(request.getImageUrls());
                goods.setImageUrls(imageUrlsJson);
            } catch (JsonProcessingException e) {
                log.warn("图片URL列表格式错误: {}", request.getImageUrls());
                return Result.error(ErrorCode.BAD_REQUEST);
            }
        }

        // 5. 如果是已驳回状态，更新后重置为草稿
        if (goods.getStatus() == GoodsStatus.REJECTED) {
            goods.setStatus(GoodsStatus.DRAFT);
            goods.setAuditReason(null);
        }

        goods.setUpdatedAt(LocalDateTime.now());

        // 6. 保存到数据库
        goodsMapper.updateById(goods);

        log.info("商品更新成功，商品ID: {}", goodsId);

        return Result.success(goods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteGoods(Long goodsId, Long sellerId) {
        log.info("删除商品，商品ID: {}, 卖家ID: {}", goodsId, sellerId);

        // 1. 查询商品
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            log.warn("商品不存在: {}", goodsId);
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 2. 权限校验：只能删除自己的商品
        if (!goods.getSellerId().equals(sellerId)) {
            log.warn("无权删除商品，商品ID: {}, 卖家ID: {}, 当前用户: {}", goodsId, goods.getSellerId(), sellerId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 3. 状态校验：只能删除草稿或已下架状态的商品
        if (goods.getStatus() != GoodsStatus.DRAFT && goods.getStatus() != GoodsStatus.OFFLINE) {
            log.warn("商品状态不允许删除，商品ID: {}, 状态: {}", goodsId, goods.getStatus());
            return Result.error(ErrorCode.GOODS_STATUS_ERROR);
        }

        // 4. 软删除
        goodsMapper.deleteById(goodsId);

        log.info("商品删除成功，商品ID: {}", goodsId);

        return Result.success();
    }

    @Override
    public Result<Goods> getGoodsDetail(Long goodsId) {
        log.info("获取商品详情，商品ID: {}", goodsId);

        // 1. 查询商品
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            log.warn("商品不存在: {}", goodsId);
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 2. 只返回已上架的商品
        if (goods.getStatus() != GoodsStatus.APPROVED) {
            log.warn("商品未上架，商品ID: {}, 状态: {}", goodsId, goods.getStatus());
            // 对于非买家（卖家或管理员），可以查看自己的商品或待审核的商品
            // 这里简化处理，返回商品详情但前端根据状态判断
        }

        return Result.success(goods);
    }

    @Override
    public Result<PageResult<Goods>> listGoods(GoodsSearchRequest request) {
        log.info("查询商品列表，关键词: {}, 分类: {}, 页码: {}, 每页: {}",
                request.getKeyword(), request.getCategoryId(), request.getPage(), request.getSize());

        // 1. 构建分页参数
        long pageNo = (request.getPage() == null || request.getPage() <= 0) ? 1 : request.getPage();
        long size = (request.getSize() == null || request.getSize() <= 0) ? 10 : Math.min(request.getSize(), 50);
        Page<Goods> pageParam = new Page<>(pageNo, size);

        // 2. 构建查询条件
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();

        // 只查询已上架的商品
        wrapper.eq(Goods::getStatus, GoodsStatus.APPROVED);

        // 关键词前缀匹配（标题）
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.likeRight(Goods::getTitle, request.getKeyword());
        }

        // 分类筛选
        if (request.getCategoryId() != null) {
            wrapper.eq(Goods::getCategoryId, request.getCategoryId());
        }

        // 排序
        if ("price".equalsIgnoreCase(request.getSortBy())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(Goods::getPrice);
            } else {
                wrapper.orderByDesc(Goods::getPrice);
            }
        } else if ("stock".equalsIgnoreCase(request.getSortBy())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(Goods::getStock);
            } else {
                wrapper.orderByDesc(Goods::getStock);
            }
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(Goods::getCreatedAt);
        }

        // 3. 执行分页查询
        Page<Goods> pageResult = goodsMapper.selectPage(pageParam, wrapper);

        // 4. 构建返回结果
        PageResult<Goods> result = PageResult.of(
                pageResult.getRecords(),
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize()
        );

        log.info("查询商品列表成功，共 {} 条记录", result.getTotal());

        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> submitForAudit(Long goodsId, Long sellerId) {
        log.info("提交审核，商品ID: {}, 卖家ID: {}", goodsId, sellerId);

        // 1. 查询商品
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            log.warn("商品不存在: {}", goodsId);
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 2. 权限校验：只能提交自己的商品
        if (!goods.getSellerId().equals(sellerId)) {
            log.warn("无权提交审核，商品ID: {}, 卖家ID: {}, 当前用户: {}", goodsId, goods.getSellerId(), sellerId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 3. 状态校验：只能提交草稿或已驳回状态的商品
        if (goods.getStatus() != GoodsStatus.DRAFT && goods.getStatus() != GoodsStatus.REJECTED) {
            log.warn("商品状态不允许提交审核，商品ID: {}, 状态: {}", goodsId, goods.getStatus());
            return Result.error(ErrorCode.GOODS_STATUS_ERROR);
        }

        // 4. 更新状态为待审核
        goods.setStatus(GoodsStatus.PENDING);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);

        log.info("商品提交审核成功，商品ID: {}", goodsId);

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> approveGoods(Long goodsId, Long adminId) {
        log.info("审核通过商品，商品ID: {}, 管理员ID: {}", goodsId, adminId);

        // 1. 查询商品
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            log.warn("商品不存在: {}", goodsId);
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 2. 状态校验：只能审核待审核状态的商品
        if (goods.getStatus() != GoodsStatus.PENDING) {
            log.warn("商品状态不允许审核，商品ID: {}, 状态: {}", goodsId, goods.getStatus());
            return Result.error(ErrorCode.GOODS_STATUS_ERROR);
        }

        // 3. 更新状态为已上架
        goods.setStatus(GoodsStatus.APPROVED);
        goods.setAuditBy(adminId);
        goods.setAuditAt(LocalDateTime.now());
        goods.setAuditReason(null);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);

        log.info("商品审核通过，商品ID: {}", goodsId);

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> rejectGoods(Long goodsId, String reason, Long adminId) {
        log.info("审核驳回商品，商品ID: {}, 原因: {}, 管理员ID: {}", goodsId, reason, adminId);

        // 1. 查询商品
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            log.warn("商品不存在: {}", goodsId);
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 2. 状态校验：只能审核待审核状态的商品
        if (goods.getStatus() != GoodsStatus.PENDING) {
            log.warn("商品状态不允许审核，商品ID: {}, 状态: {}", goodsId, goods.getStatus());
            return Result.error(ErrorCode.GOODS_STATUS_ERROR);
        }

        // 3. 更新状态为已驳回
        goods.setStatus(GoodsStatus.REJECTED);
        goods.setAuditBy(adminId);
        goods.setAuditAt(LocalDateTime.now());
        goods.setAuditReason(reason);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);

        log.info("商品审核驳回，商品ID: {}, 原因: {}", goodsId, reason);

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> offlineGoods(Long goodsId, Long sellerId) {
        log.info("手动下架商品，商品ID: {}, 卖家ID: {}", goodsId, sellerId);

        // 1. 查询商品
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            log.warn("商品不存在: {}", goodsId);
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 2. 权限校验：只能下架自己的商品
        if (!goods.getSellerId().equals(sellerId)) {
            log.warn("无权下架商品，商品ID: {}, 卖家ID: {}, 当前用户: {}", goodsId, goods.getSellerId(), sellerId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 3. 状态校验：只能下架已上架的商品
        if (goods.getStatus() != GoodsStatus.APPROVED) {
            log.warn("商品状态不允许下架，商品ID: {}, 状态: {}", goodsId, goods.getStatus());
            return Result.error(ErrorCode.GOODS_STATUS_ERROR);
        }

        // 4. 更新状态为已下架
        goods.setStatus(GoodsStatus.OFFLINE);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);

        log.info("商品下架成功，商品ID: {}", goodsId);

        return Result.success();
    }

    @Override
    public Result<PageResult<Goods>> listMyGoods(Long sellerId, Long page, Long size, String status, String keyword) {
        // 1. 分页参数
        long pageNo = (page == null || page <= 0) ? 1 : page;
        long pageSize = (size == null || size <= 0) ? 10 : size;
        Page<Goods> pageParam = new Page<>(pageNo, pageSize);

        // 2. 查询条件
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getSellerId, sellerId);
        
        // 状态筛选
        if (StringUtils.hasText(status)) {
             try {
                 GoodsStatus statusEnum = GoodsStatus.valueOf(status);
                 wrapper.eq(Goods::getStatus, statusEnum);
             } catch (IllegalArgumentException e) {
                 // ignore invalid status
             }
        }

        // 关键词
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Goods::getTitle, keyword);
        }

        wrapper.orderByDesc(Goods::getCreatedAt);

        // 3. 执行查询
        Page<Goods> pageResult = goodsMapper.selectPage(pageParam, wrapper);

        return Result.success(PageResult.of(
                pageResult.getRecords(),
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize()
        ));
    }
}
