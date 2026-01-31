package com.laidekuai.category.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laidekuai.category.entity.Category;
import com.laidekuai.common.enums.CategoryStatus;
import com.laidekuai.common.dto.Result;

import java.util.List;

/**
 * 分类服务接口
 *
 * @author Laidekuai Team
 */
public interface CategoryService {

    /**
     * 获取分类树（只返回启用状态的分类）
     *
     * @return 分类树
     */
    Result<List<Category>> getCategoryTree();

    /**
     * 创建分类（管理员）
     *
     * @param category 分类信息
     * @return 创建结果
     */
    Result<Category> createCategory(Category category);

    /**
     * 更新分类（管理员）
     *
     * @param categoryId 分类ID
     * @param category 分类信息
     * @return 更新结果
     */
    Result<Category> updateCategory(Long categoryId, Category category);

    /**
     * 删除分类（管理员）
     *
     * @param categoryId 分类ID
     * @return 删除结果
     */
    Result<Void> deleteCategory(Long categoryId);

    /**
     * 检查循环引用
     *
     * @param categoryId 分类ID
     * @param newParentId 新的父分类ID
     * @return 是否存在循环引用
     */
    boolean hasCircularReference(Long categoryId, Long newParentId);
}
