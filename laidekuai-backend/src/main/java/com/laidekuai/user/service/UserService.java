package com.laidekuai.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.common.dto.LoginRequest;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.PasswordChangeRequest;
import com.laidekuai.common.dto.RegisterRequest;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.dto.UserUpdateRequest;
import com.laidekuai.user.entity.User;

/**
 * 用户服务接口
 *
 * @author Laidekuai Team
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册结果
     */
    Result<User> register(RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录结果（包含token）
     */
    Result<LoginResult> login(LoginRequest request);

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    Result<User> getCurrentUser(Long userId);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @param currentUserId 当前用户ID（用于权限校验）
     * @return 用户信息
     */
    Result<User> getUserInfo(Long userId, Long currentUserId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param request 更新请求
     * @param currentUserId 当前用户ID（用于权限校验）
     * @return 更新结果
     */
    Result<User> updateUserInfo(Long userId, UserUpdateRequest request, Long currentUserId);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param request 修改密码请求
     * @param currentUserId 当前用户ID（用于权限校验）
     * @return 修改结果
     */
    Result<Void> changePassword(Long userId, PasswordChangeRequest request, Long currentUserId);

    /**
     * 用户列表（管理员）
     *
     * @param page 页码
     * @param size 每页大小
     * @param status 状态筛选（可选）
     * @return 用户列表
     */
    Result<PageResult<User>> listUsers(Long page, Long size, String status);

    /**
     * 更新用户状态（管理员）
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 更新结果
     */
    Result<Void> updateUserStatus(Long userId, String status);

    /**
     * 登录结果
     */
    class LoginResult {
        private String token;
        private User user;

        public LoginResult(String token, User user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public User getUser() {
            return user;
        }
    }
}
