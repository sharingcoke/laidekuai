package com.laidekuai.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 *
 * @author Laidekuai Team
 */
@Data
@TableName("orders")
public class Order {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 运费（V1固定为0）
     */
    private BigDecimal shippingFee;

    /**
     * 订单状态：PENDING_PAY/PAID/SHIPPED/COMPLETED/CANCELED/REFUNDING/REFUNDED/DISPUTED
     */
    private String status;

    /**
     * 取消原因：TIMEOUT/USER/BUYER_REFUND/ADMIN
     */
    private String cancelReason;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 争议时间
     */
    private LocalDateTime disputeTime;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 收货地址ID（快照）
     */
    private Long addressId;

    /**
     * 收货人姓名（快照）
     */
    private String receiverName;

    /**
     * 收货人电话（快照）
     */
    private String receiverPhone;

    /**
     * 收货地址（快照）
     */
    private String receiverAddress;

    /**
     * 是否已结算：0未结算/1已结算
     */
    private Integer isSettled;

    /**
     * 结算时间
     */
    private LocalDateTime settledTime;

    /**
     * 退款申请次数
     */
    private Integer refundRequestCount;

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
