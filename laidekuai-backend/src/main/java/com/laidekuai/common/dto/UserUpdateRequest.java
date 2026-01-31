package com.laidekuai.common.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户信息请求
 *
 * @author Laidekuai Team
 */
@Data
public class UserUpdateRequest {

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickName;

    /**
     * 手机号
     */
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    /**
     * 头像URL
     */
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatarUrl;
}
