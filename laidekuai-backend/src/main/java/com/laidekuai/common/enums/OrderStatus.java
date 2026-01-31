package com.laidekuai.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 *
 * @author Laidekuai Team
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {

    /**
     * 待支付
     */
    PENDING_PAY("PENDING_PAY", "待支付"),

    /**
     * 已支付
     */
    PAID("PAID", "已支付"),

    /**
     * 已发货
     */
    SHIPPED("SHIPPED", "已发货"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 已取消
     */
    CANCELED("CANCELED", "已取消"),

    /**
     * 退款中
     */
    REFUNDING("REFUNDING", "退款中"),

    /**
     * 已退款
     */
    REFUNDED("REFUNDED", "已退款"),

    /**
     * 争议中
     */
    DISPUTED("DISPUTED", "争议中");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;
}
