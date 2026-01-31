package com.laidekuai.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.PasswordChangeRequest;
import com.laidekuai.common.dto.LoginRequest;
import com.laidekuai.common.dto.RegisterRequest;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.dto.UserUpdateRequest;
import com.laidekuai.common.enums.Role;
import com.laidekuai.common.exception.BusinessException;
import com.laidekuai.common.util.JwtUtil;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.mapper.UserMapper;
import com.laidekuai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现
 *
 * @author Laidekuai Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<User> register(RegisterRequest request) {
        log.info("用户注册，用户名: {}", request.getUsername());

        // 1. 检查用户名是否已存在
        User existingUser = getUserByUsername(request.getUsername());
        if (existingUser != null) {
            log.warn("用户名已存在: {}", request.getUsername());
            return Result.error(ErrorCode.USERNAME_DUPLICATE);
        }

        // 2. 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickName(request.getNickName());
        user.setRole(Role.BUYER);  // 默认角色为BUYER
        user.setStatus("ACTIVE");

        // 3. 保存到数据库
        userMapper.insert(user);

        log.info("用户注册成功，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());

        // 4. 返回用户信息（不包含密码）
        user.setPasswordHash(null);
        return Result.success(user);
    }

    @Override
    public Result<LoginResult> login(LoginRequest request) {
        log.info("用户登录，用户名: {}", request.getUsername());

        // 1. 查询用户
        User user = getUserByUsername(request.getUsername());
        if (user == null) {
            log.warn("用户不存在: {}", request.getUsername());
            return Result.error(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("密码错误，用户名: {}", request.getUsername());
            return Result.error(ErrorCode.PASSWORD_ERROR);
        }

        // 3. 检查用户状态
        if (!"ACTIVE".equals(user.getStatus())) {
            log.warn("用户已被禁用，用户名: {}", request.getUsername());
            return Result.error(ErrorCode.USER_DISABLED);
        }

        // 4. 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        log.info("用户登录成功，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());

        // 5. 清除密码哈希后返回
        user.setPasswordHash(null);

        return Result.success(new LoginResult(token, user));
    }

    @Override
    public Result<User> getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(ErrorCode.USER_NOT_FOUND);
        }

        // 清除敏感信息
        user.setPasswordHash(null);
        return Result.success(user);
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public Result<User> getUserInfo(Long userId, Long currentUserId) {
        log.info("获取用户信息，用户ID: {}, 当前用户: {}", userId, currentUserId);

        // 1. 权限校验：只能查看自己的信息或管理员
        User currentUser = userMapper.selectById(currentUserId);
        if (!currentUserId.equals(userId) && currentUser.getRole() != Role.ADMIN) {
            log.warn("无权查看用户信息，当前用户: {}, 目标用户: {}", currentUserId, userId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 2. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(ErrorCode.USER_NOT_FOUND);
        }

        // 3. 清除敏感信息
        user.setPasswordHash(null);
        return Result.success(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<User> updateUserInfo(Long userId, UserUpdateRequest request, Long currentUserId) {
        log.info("更新用户信息，用户ID: {}, 当前用户: {}", userId, currentUserId);

        // 1. 权限校验：只能修改自己的信息或管理员
        User currentUser = userMapper.selectById(currentUserId);
        if (!currentUserId.equals(userId) && currentUser.getRole() != Role.ADMIN) {
            log.warn("无权修改用户信息，当前用户: {}, 目标用户: {}", currentUserId, userId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 2. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(ErrorCode.USER_NOT_FOUND);
        }

        // 3. 更新字段（只更新非空字段）
        if (StringUtils.hasText(request.getNickName())) {
            user.setNickName(request.getNickName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        // 4. 保存到数据库
        userMapper.updateById(user);

        log.info("用户信息更新成功，用户ID: {}", userId);

        // 5. 清除敏感信息后返回
        user.setPasswordHash(null);
        return Result.success(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> changePassword(Long userId, PasswordChangeRequest request, Long currentUserId) {
        log.info("修改密码，用户ID: {}, 当前用户: {}", userId, currentUserId);

        // 1. 权限校验：只能修改自己的密码
        if (!currentUserId.equals(userId)) {
            log.warn("无权修改密码，当前用户: {}, 目标用户: {}", currentUserId, userId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // 2. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(ErrorCode.USER_NOT_FOUND);
        }

        // 3. 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            log.warn("旧密码错误，用户ID: {}", userId);
            return Result.error(ErrorCode.PASSWORD_ERROR);
        }

        // 4. 更新密码
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);

        log.info("密码修改成功，用户ID: {}", userId);

        return Result.success();
    }

    @Override
    public Result<PageResult<User>> listUsers(Long page, Long size, String status) {
        log.info("查询用户列表，页码: {}, 每页大小: {}, 状态: {}", page, size, status);

        // 1. 构建分页参数
        Page<User> pageParam = new Page<>(page, size);

        // 2. 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        // 3. 执行分页查询
        Page<User> pageResult = userMapper.selectPage(pageParam, wrapper);

        // 4. 清除敏感信息
        pageResult.getRecords().forEach(user -> user.setPasswordHash(null));

        // 5. 构建返回结果
        PageResult<User> result = PageResult.of(
            pageResult.getRecords(),
            pageResult.getTotal(),
            pageResult.getCurrent(),
            pageResult.getSize()
        );

        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateUserStatus(Long userId, String status) {
        log.info("更新用户状态，用户ID: {}, 状态: {}", userId, status);

        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 更新状态
        user.setStatus(status);
        userMapper.updateById(user);

        log.info("用户状态更新成功，用户ID: {}, 状态: {}", userId, status);

        return Result.success();
    }
}
