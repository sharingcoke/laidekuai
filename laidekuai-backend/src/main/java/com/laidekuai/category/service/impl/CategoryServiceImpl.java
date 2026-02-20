package com.laidekuai.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laidekuai.category.dto.CategoryCreateRequest;
import com.laidekuai.category.dto.CategoryUpdateRequest;
import com.laidekuai.category.entity.Category;
import com.laidekuai.category.mapper.CategoryMapper;
import com.laidekuai.category.service.CategoryService;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.enums.CategoryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现
 *
 * @author Laidekuai Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public Result<List<Category>> getCategoryTree() {
        log.info("获取分类树");

        // 1. 查询所有启用的分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, CategoryStatus.ENABLED);
        wrapper.orderByAsc(Category::getSortOrder);

        List<Category> allCategories = categoryMapper.selectList(wrapper);

        // 2. 构建树形结构
        List<Category> tree = buildTree(allCategories, null);

        log.info("分类树构建完成，根节点数: {}", tree.size());

        return Result.success(tree);
    }

    @Override
    public Result<Category> getCategoryById(Long categoryId) {
        log.info("获取分类详情，分类ID: {}", categoryId);

        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            log.warn("分类不存在: {}", categoryId);
            return Result.error(ErrorCode.CATEGORY_NOT_FOUND);
        }

        return Result.success(category);
    }

    /**
     * 构建分类树
     *
     * @param allCategories 所有分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    private List<Category> buildTree(List<Category> allCategories, Long parentId) {
        return allCategories.stream()
                .filter(category -> {
                    // 处理null的情况：一级分类的parentId为null
                    if (parentId == null) {
                        return category.getParentId() == null;
                    }
                    return parentId.equals(category.getParentId());
                })
                .peek(category -> {
                    // 递归构建子分类
                    category.setChildren(buildTree(allCategories, category.getId()));
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Category> createCategory(CategoryCreateRequest request) {
        log.info("创建分类，名称: {}, 父分类ID: {}", request.getName(), request.getParentId());

        // 1. 校验分类名称唯一性
        LambdaQueryWrapper<Category> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(Category::getName, request.getName());
        if (categoryMapper.selectCount(nameWrapper) > 0) {
            log.warn("分类名称已存在: {}", request.getName());
            return Result.error(ErrorCode.BAD_REQUEST.getCode(), "分类名称已存在");
        }

        // 2. 计算分类层级
        Integer level = 1;
        if (request.getParentId() != null) {
            Category parent = categoryMapper.selectById(request.getParentId());
            if (parent == null) {
                log.warn("父分类不存在: {}", request.getParentId());
                return Result.error(ErrorCode.CATEGORY_NOT_FOUND);
            }
            level = parent.getLevel() + 1;

            // 3. 校验层级：最多3级
            if (level > 3) {
                log.warn("分类层级超过限制: {}", level);
                return Result.error(ErrorCode.CATEGORY_LEVEL_EXCEEDED);
            }
        }

        // 4. 创建分类
        Category category = new Category();
        category.setName(request.getName());
        category.setParentId(request.getParentId());
        category.setLevel(level);
        category.setSortOrder(request.getSortOrder());
        category.setIconUrl(request.getIconUrl());
        category.setStatus(CategoryStatus.ENABLED);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        // 5. 保存到数据库
        categoryMapper.insert(category);

        log.info("分类创建成功，分类ID: {}, 名称: {}", category.getId(), category.getName());

        return Result.success(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Category> updateCategory(Long categoryId, CategoryUpdateRequest request) {
        log.info("更新分类，分类ID: {}", categoryId);

        // 1. 查询原分类
        Category existingCategory = categoryMapper.selectById(categoryId);
        if (existingCategory == null) {
            log.warn("分类不存在: {}", categoryId);
            return Result.error(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 2. 如果修改了父分类，检查循环引用
        if (request.getParentId() != null && !request.getParentId().equals(existingCategory.getParentId())) {
            // 检查新父分类是否存在
            Category newParent = categoryMapper.selectById(request.getParentId());
            if (newParent == null) {
                log.warn("新父分类不存在: {}", request.getParentId());
                return Result.error(ErrorCode.CATEGORY_NOT_FOUND);
            }

            // 检查循环引用
            if (hasCircularReference(categoryId, request.getParentId())) {
                log.warn("检测到循环引用，分类ID: {}, 新父分类ID: {}", categoryId, request.getParentId());
                return Result.error(ErrorCode.CATEGORY_CIRCULAR_REF);
            }

            // 检查层级限制
            Integer newLevel = newParent.getLevel() + 1;
            if (newLevel > 3) {
                log.warn("分类层级超过限制: {}", newLevel);
                return Result.error(ErrorCode.CATEGORY_LEVEL_EXCEEDED);
            }

            existingCategory.setParentId(request.getParentId());
            existingCategory.setLevel(newLevel);
        }

        // 3. 更新其他字段
        if (StringUtils.hasText(request.getName())) {
            // 检查名称是否与其他分类重复
            LambdaQueryWrapper<Category> nameWrapper = new LambdaQueryWrapper<>();
            nameWrapper.eq(Category::getName, request.getName());
            nameWrapper.ne(Category::getId, categoryId);
            if (categoryMapper.selectCount(nameWrapper) > 0) {
                log.warn("分类名称已存在: {}", request.getName());
                return Result.error(ErrorCode.BAD_REQUEST.getCode(), "分类名称已存在");
            }
            existingCategory.setName(request.getName());
        }

        existingCategory.setSortOrder(request.getSortOrder());
        if (StringUtils.hasText(request.getIconUrl())) {
            existingCategory.setIconUrl(request.getIconUrl());
        }
        existingCategory.setUpdatedAt(LocalDateTime.now());

        // 4. 保存到数据库
        categoryMapper.updateById(existingCategory);

        log.info("分类更新成功，分类ID: {}", categoryId);

        return Result.success(existingCategory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteCategory(Long categoryId) {
        log.info("删除分类，分类ID: {}", categoryId);

        // 1. 查询分类
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            log.warn("分类不存在: {}", categoryId);
            return Result.error(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 2. 检查是否有子分类
        LambdaQueryWrapper<Category> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(Category::getParentId, categoryId);
        long childCount = categoryMapper.selectCount(childWrapper);
        if (childCount > 0) {
            log.warn("分类下有子分类，无法删除，分类ID: {}", categoryId);
            return Result.error(ErrorCode.BAD_REQUEST.getCode(), "该分类下有子分类，无法删除");
        }

        // 3. 检查是否有商品引用
        long goodsCount = categoryMapper.countGoodsByCategory(categoryId);
        if (goodsCount > 0) {
            log.warn("分类下有商品，无法删除，分类ID: {}", categoryId);
            return Result.error(ErrorCode.CONFLICT);
        }

        // 4. 软删除
        categoryMapper.deleteById(categoryId);

        log.info("分类删除成功，分类ID: {}", categoryId);

        return Result.success();
    }

    @Override
    public boolean hasCircularReference(Long categoryId, Long newParentId) {
        // 递归检查：从newParentId开始，看是否能回到categoryId
        Long currentId = newParentId;
        int maxDepth = 3; // 最大3级，防止无限循环

        while (currentId != null && maxDepth-- > 0) {
            if (currentId.equals(categoryId)) {
                return true; // 发现循环引用
            }

            Category current = categoryMapper.selectById(currentId);
            if (current == null) {
                break;
            }

            currentId = current.getParentId();
        }

        return false;
    }
}
