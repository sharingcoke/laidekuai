package com.laidekuai.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.LoginRequest;
import com.laidekuai.common.dto.RegisterRequest;
import com.laidekuai.common.dto.Result;
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
}
