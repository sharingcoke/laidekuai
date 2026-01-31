package com.laidekuai.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品状态枚举
 *
 * @author Laidekuai Team
 */
@Getter
@AllArgsConstructor
public enum GoodsStatus {

    /**
     * 草稿
     */
    DRAFT("DRAFT", "草稿"),

    /**
     * 待审核
     */
    PENDING("PENDING", "待审核"),

    /**
     * 已上架
     */
    APPROVED("APPROVED", "已上架"),

    /**
     * 已驳回
     */
    REJECTED("REJECTED", "已驳回"),

    /**
     * 已下架
     */
    OFFLINE("OFFLINE", "已下架");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;
}
