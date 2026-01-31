package com.laidekuai.category.service;

import com.laidekuai.category.dto.CategoryCreateRequest;
import com.laidekuai.category.dto.CategoryUpdateRequest;
import com.laidekuai.category.entity.Category;
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
     * 获取分类详情
     *
     * @param categoryId 分类ID
     * @return 分类详情
     */
    Result<Category> getCategoryById(Long categoryId);

    /**
     * 创建分类（管理员）
     *
     * @param request 创建请求
     * @return 创建结果
     */
    Result<Category> createCategory(CategoryCreateRequest request);

    /**
     * 更新分类（管理员）
     *
     * @param categoryId 分类ID
     * @param request 更新请求
     * @return 更新结果
     */
    Result<Category> updateCategory(Long categoryId, CategoryUpdateRequest request);

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
