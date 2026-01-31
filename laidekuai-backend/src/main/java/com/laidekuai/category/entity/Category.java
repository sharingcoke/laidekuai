package com.laidekuai.category.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.laidekuai.common.enums.CategoryStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类实体类
 *
 * @author Laidekuai Team
 */
@Data
@TableName("category")
public class Category {

    /**
     * 分类ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 层级：1/2/3
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 图标URL
     */
    private String iconUrl;

    /**
     * 状态
     */
    private CategoryStatus status;

    /**
     * 子分类列表（用于树形结构返回，不映射到数据库）
     */
    @TableField(exist = false)
    private List<Category> children;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
