package com.laidekuai.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.laidekuai.common.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author Laidekuai Team
 */
@Data
@TableName("user")
public class User {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（唯一）
     */
    private String username;

    /**
     * 密码哈希（BCrypt加密）
     */
    private String passwordHash;

    /**
     * 角色
     */
    private Role role;

    /**
     * 状态
     */
    private String status;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像URL
     */
    private String avatarUrl;

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
