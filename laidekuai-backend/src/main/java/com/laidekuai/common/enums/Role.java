package com.laidekuai.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author Laidekuai Team
 */
@Getter
@AllArgsConstructor
public enum Role {

    /**
     * 买家
     */
    BUYER("BUYER", "买家"),

    /**
     * 卖家
     */
    SELLER("SELLER", "卖家"),

    /**
     * 管理员
     */
    ADMIN("ADMIN", "管理员");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;
}
