package com.laidekuai.cart.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 *
 * @author Laidekuai Team
 */
@Data
@TableName("cart")
public class Cart {

    /**
     * 购物车ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品标题（快照，方便展示）
     */
    private String goodsTitle;

    /**
     * 商品图片（快照，方便展示）
     */
    private String goodsImage;

    /**
     * 商品价格（快照，下单时使用）
     */
    private BigDecimal goodsPrice;

    /**
     * 购买数量
     */
    private Integer quantity;

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
