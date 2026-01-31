package com.laidekuai.user.service;

import com.laidekuai.common.dto.LoginRequest;
import com.laidekuai.common.dto.RegisterRequest;
import com.laidekuai.common.dto.Result;
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
