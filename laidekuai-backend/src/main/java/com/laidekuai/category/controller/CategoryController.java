package com.laidekuai.category.controller;

import com.laidekuai.category.dto.CategoryCreateRequest;
import com.laidekuai.category.dto.CategoryUpdateRequest;
import com.laidekuai.category.entity.Category;
import com.laidekuai.category.service.CategoryService;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 *
 * @author Laidekuai Team
 */
@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    private Long currentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    /**
     * 获取分类树形列表（公开接口）
     */
    @GetMapping
    public Result<List<Category>> getCategoryTree() {
        log.info("获取分类树形列表");
        return categoryService.getCategoryTree();
    }

    /**
     * 获取分类详情（公开接口）
     */
    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        log.info("获取分类详情，分类ID: {}", id);
        return categoryService.getCategoryById(id);
    }

    /**
     * 创建分类（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Category> createCategory(
            @Valid @RequestBody CategoryCreateRequest request,
            HttpServletRequest httpRequest) {
        Long adminId = currentUserId();
        log.info("创建分类，名称: {}, 父分类ID: {}, 操作人: {}", request.getName(), request.getParentId(), adminId);
        return categoryService.createCategory(request);
    }

    /**
     * 更新分类（管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Category> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request,
            HttpServletRequest httpRequest) {
        Long adminId = currentUserId();
        log.info("更新分类，分类ID: {}, 操作人: {}", id, adminId);
        return categoryService.updateCategory(id, request);
    }

    /**
     * 删除分类（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCategory(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long adminId = currentUserId();
        log.info("删除分类，分类ID: {}, 操作人: {}", id, adminId);
        return categoryService.deleteCategory(id);
    }
}
