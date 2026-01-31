package com.laidekuai.user.controller;

import com.laidekuai.common.dto.PasswordChangeRequest;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.dto.UserUpdateRequest;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户相关接口（用户自身 + 管理员）
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ----------- 用户自服务 -----------

    /**
     * 获取用户信息（只能看自己或管理员）
     */
    @GetMapping("/users/{id}")
    public Result<User> getUserInfo(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }
        log.info("获取用户信息，用户ID: {}, 当前用户: {}", id, currentUserId);
        return userService.getUserInfo(id, currentUserId);
    }

    /**
     * 更新用户信息（只能改自己或管理员改所有）
     */
    @PutMapping("/users/{id}")
    public Result<User> updateUserInfo(@PathVariable Long id,
                                       @Valid @RequestBody UserUpdateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }
        log.info("更新用户信息，用户ID: {}, 当前用户: {}", id, currentUserId);
        return userService.updateUserInfo(id, request, currentUserId);
    }

    /**
     * 修改密码（仅本人）
     */
    @PutMapping("/users/{id}/password")
    public Result<Void> changePassword(@PathVariable Long id,
                                       @Valid @RequestBody PasswordChangeRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }
        log.info("修改密码，用户ID: {}, 当前用户: {}", id, currentUserId);
        return userService.changePassword(id, request, currentUserId);
    }

    // ----------- 管理员接口 -----------

    /**
     * 管理员分页查询用户
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<User>> listUsers(@RequestParam(defaultValue = "1") Long page,
                                              @RequestParam(defaultValue = "10") Long size,
                                              @RequestParam(required = false) String status) {
        log.info("管理员查询用户列表，页码: {}, 每页: {}, 状态: {}", page, size, status);
        return userService.listUsers(page, size, status);
    }

    /**
     * 管理员更新用户状态
     */
    @PutMapping("/admin/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                         @RequestParam String status) {
        log.info("管理员更新用户状态，用户ID: {}, 状态: {}", id, status);
        return userService.updateUserStatus(id, status);
    }
}
