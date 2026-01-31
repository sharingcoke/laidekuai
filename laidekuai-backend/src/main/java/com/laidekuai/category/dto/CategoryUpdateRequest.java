package com.laidekuai.category.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新分类请求
 *
 * @author Laidekuai Team
 */
@Data
public class CategoryUpdateRequest {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID
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
