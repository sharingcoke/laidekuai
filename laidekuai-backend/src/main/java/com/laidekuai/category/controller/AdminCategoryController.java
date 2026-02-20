package com.laidekuai.category.controller;

import com.laidekuai.category.dto.CategoryCreateRequest;
import com.laidekuai.category.dto.CategoryUpdateRequest;
import com.laidekuai.category.entity.Category;
import com.laidekuai.category.service.CategoryService;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员分类控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    private Long currentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    /**
     * GET /api/admin/categories
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Category>> listCategories() {
        return categoryService.getCategoryTree();
    }

    /**
     * POST /api/admin/categories
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Category> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        Long adminId = currentUserId();
        log.info("管理员创建分类，名称: {}, 父分类ID: {}, 操作人: {}", request.getName(), request.getParentId(), adminId);
        return categoryService.createCategory(request);
    }

    /**
     * PUT /api/admin/categories/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Category> updateCategory(@PathVariable Long id,
                                           @Valid @RequestBody CategoryUpdateRequest request) {
        Long adminId = currentUserId();
        log.info("管理员更新分类，分类ID: {}, 操作人: {}", id, adminId);
        return categoryService.updateCategory(id, request);
    }

    /**
     * DELETE /api/admin/categories/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        Long adminId = currentUserId();
        log.info("管理员删除分类，分类ID: {}, 操作人: {}", id, adminId);
        return categoryService.deleteCategory(id);
    }
}
