package com.laidekuai.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项实体类
 *
 * @author Laidekuai Team
 */
@Data
@TableName("order_item")
public class OrderItem {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 商品标题（快照）
     */
    private String goodsTitle;

    /**
     * 商品封面（快照）
     */
    private String goodsCover;

    /**
     * 商品单价（快照）
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 小计金额
     */
    private BigDecimal amount;

    /**
     * 订单项状态：PENDING_PAY/PAID/SHIPPED/COMPLETED/CANCELED
     */
    private String itemStatus;

    /**
     * 冗余订单主单状态
     */
    private String orderStatus;

    /**
     * 物流公司
     */
    private String shipCompany;

    /**
     * 物流单号
     */
    private String trackingNo;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

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
