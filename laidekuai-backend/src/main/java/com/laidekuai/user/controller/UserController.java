package com.laidekuai.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.PasswordChangeRequest;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.dto.UserUpdateRequest;
import com.laidekuai.common.enums.Role;
import com.laidekuai.common.util.JwtUtil;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author Laidekuai Team
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 获取用户信息
     *
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public Result<User> getUserInfo(@PathVariable Long id, HttpServletRequest request) {
        // 从token获取当前用户ID
        Long currentUserId = getCurrentUserId(request);

        log.info("获取用户信息，用户ID: {}, 当前用户: {}", id, currentUserId);

        return userService.getUserInfo(id, currentUserId);
    }

    /**
     * 更新用户信息
     *
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public Result<User> updateUserInfo(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest) {
        // 从token获取当前用户ID
        Long currentUserId = getCurrentUserId(httpRequest);

        log.info("更新用户信息，用户ID: {}, 当前用户: {}", id, currentUserId);

        return userService.updateUserInfo(id, request, currentUserId);
    }

    /**
     * 修改密码
     *
     * PUT /api/users/{id}/password
     */
    @PutMapping("/{id}/password")
    public Result<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordChangeRequest request,
            HttpServletRequest httpRequest) {
        // 从token获取当前用户ID
        Long currentUserId = getCurrentUserId(httpRequest);

        log.info("修改密码，用户ID: {}, 当前用户: {}", id, currentUserId);

        return userService.changePassword(id, request, currentUserId);
    }

    /**
     * 用户列表（管理员）
     *
     * GET /api/admin/users
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<User>> listUsers(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String status) {

        log.info("查询用户列表，页码: {}, 每页大小: {}, 状态: {}", page, size, status);

        return userService.listUsers(page, size, status);
    }

    /**
     * 更新用户状态（管理员）
     *
     * PUT /api/admin/users/{id}/status
     */
    @PutMapping("/admin/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        log.info("更新用户状态，用户ID: {}, 状态: {}", id, status);

        return userService.updateUserStatus(id, status);
    }

    /**
     * 从请求中获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }
}
