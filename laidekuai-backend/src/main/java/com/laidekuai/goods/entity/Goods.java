package com.laidekuai.goods.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.laidekuai.common.enums.GoodsStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 *
 * @author Laidekuai Team
 */
@Data
@TableName("goods")
public class Goods {

    /**
     * 商品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 商品图片URL列表（JSON数组格式）
     */
    private String imageUrls;

    /**
     * 商品状态
     */
    private GoodsStatus status;

    /**
     * 审核驳回原因
     */
    private String auditReason;

    /**
     * 审核人ID
     */
    private Long auditBy;

    /**
     * 审核时间
     */
    private LocalDateTime auditAt;

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
