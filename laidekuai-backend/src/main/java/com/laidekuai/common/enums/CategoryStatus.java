package com.laidekuai.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分类状态枚举
 *
 * @author Laidekuai Team
 */
@Getter
@AllArgsConstructor
public enum CategoryStatus {

    /**
     * 启用
     */
    ENABLED("ENABLED", "启用"),

    /**
     * 禁用
     */
    DISABLED("DISABLED", "禁用");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;
}
