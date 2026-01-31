package com.laidekuai.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建分类请求
 *
 * @author Laidekuai Team
 */
@Data
public class CategoryCreateRequest {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /**
     * 父分类ID（一级分类为null）
     */
    private Long parentId;

    /**
     * 排序号
     */
    @NotNull(message = "排序号不能为空")
    private Integer sortOrder;

    /**
     * 图标URL
     */
    private String iconUrl;
}
