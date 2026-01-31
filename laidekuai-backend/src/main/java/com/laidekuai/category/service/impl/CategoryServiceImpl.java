package com.laidekuai.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laidekuai.category.entity.Category;
import com.laideai.category.mapper.CategoryMapper;
import com.laidekuai.category.service.CategoryService;
import com.laidekuai.common.dto.ErrorCode;
import com.laideki.common.dto.Result;
import com.laidekuai.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        wrapper.orderByAsc(Category::getSort);

        List<Category> allCategories = categoryMapper.selectList(wrapper);

        // 2. 构建树形结构
        List<Category> tree = buildTree(allCategories, 0L);

        log.info("分类树构建完成，根节点数: {}", tree.size());

        return Result.success(tree);
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
                .filter(category -> parentId.equals(category.getParentId()))
                .peek(category -> {
                    // 递归构建子分类
                    category.setChildren(buildTree(allCategories, category.getId()));
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Category> createCategory(Category category) {
        log.info("创建分类，名称: {}, 父分类ID: {}, 层级: {}",
                category.getName(), category.getParentId(), category.getLevel());

        // 1. 校验层级：固定3级分类
        if (category.getLevel() < 1 || category.getLevel() > 3) {
            return Result.error(400, "分类层级必须在1-3之间");
        }

        // 2. 校验父分类存在性（非一级分类）
        if (category.getParentId() != null) {
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                return Result.error(400, "父分类不存在");
            }

            // 3. 校验层级关系
            if (category.getLevel() != parent.getLevel() + 1) {
                return Result.error(400, "分类层级不正确");
            }
        }

        // 4. 一级分类的parentId必须为null
        if (category.getLevel() == 1 && category.getParentId() != null) {
            return Result.error(400, "一级分类的父分类ID必须为空");
        }

        // 5. 校验分类名称唯一性
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, category.getName());
        if (categoryMapper.selectCount(wrapper) > 0) {
            return Result.error(400, "分类名称已存在");
        }

        // 6. 保存到数据库
        category.setStatus(CategoryStatus.ENABLED);
        categoryMapper.insert(category);

        log.info("分类创建成功，分类ID: {}, 名称: {}", category.getId(), category.getName());

        return Result.success(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Category> updateCategory(Long categoryId, Category category) {
        log.info("更新分类，分类ID: {}", categoryId);

        // 1. 查询原分类
        Category existingCategory = categoryMapper.selectById(categoryId);
        if (existingCategory == null) {
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 2. 如果修改了parentId，检查循环引用
        if (category.getParentId() != null && !category.getParentId().equals(categoryId)) {
            if (hasCircularReference(categoryId, category.getParentId())) {
                return Result.error(ErrorCode.CATEGORY_CIRCULAR_REF);
            }
        }

        // 3. 更新分类
        category.setId(categoryId);
        categoryMapper.updateById(category);

        log.info("分类更新成功，分类ID: {}", categoryId);

        return Result.success(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteCategory(Long categoryId) {
        log.info("删除分类，分类ID: {}", categoryId);

        // 1. 查询分类
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        // 2. 检查是否有子分类
        LambdaQueryWrapper<Category> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(Category::getParentId, categoryId);
        long childCount = categoryMapper.selectCount(childWrapper);
        if (childCount > 0) {
            return Result.error(400, "该分类下有子分类，无法删除");
        }

        // 3. 检查是否有商品引用（需要GoodsMapper，暂时跳过）
        // TODO: 添加商品引用检查

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
