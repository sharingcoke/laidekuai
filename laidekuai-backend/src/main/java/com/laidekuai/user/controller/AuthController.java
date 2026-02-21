package com.laidekuai.user.controller;

import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.LoginRequest;
import com.laidekuai.common.dto.RegisterRequest;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.JwtUtil;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author Laidekuai Team
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     *
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求，用户名: {}", request.getUsername());
        return userService.register(request);
    }

    /**
     * 用户登录
     *
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求，用户名: {}", request.getUsername());
        return userService.login(request);
    }

    /**
     * 获取当前用户信息
     *
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }

        token = token.substring(7); // 去掉 "Bearer " 前缀

        // 验证token
        if (!jwtUtil.validateToken(token)) {
            return Result.error(ErrorCode.TOKEN_EXPIRED);
        }

        // 从token中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);

        log.info("获取当前用户信息，用户ID: {}", userId);

        return userService.getCurrentUser(userId);
    }
}
