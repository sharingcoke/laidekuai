package com.laidekuai.user.service;

import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.LoginRequest;
import com.laidekuai.common.dto.RegisterRequest;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.enums.Role;
import com.laidekuai.common.util.JwtUtil;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.mapper.UserMapper;
import com.laidekuai.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 *
 * @author Laidekuai Team
 */
@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, jwtUtil);
    }

    @Test
    @DisplayName("正常场景：用户注册成功")
    void register_Success() {
        // Given: 注册请求
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setNickName("测试用户");

        // Mock: 用户名不存在
        when(userMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            return 1;
        });

        // When: 执行注册
        Result<User> result = userService.register(request);

        // Then: 验证结果
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getUsername()).isEqualTo("testuser");
        assertThat(result.getData().getPasswordHash()).isNull(); // 密码已清除
        assertThat(result.getData().getRole()).isEqualTo(Role.BUYER);
        assertThat(result.getData().getStatus()).isEqualTo("ACTIVE");

        // Then: 验证密码已加密
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertThat(passwordEncoder.matches("password123", userCaptor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("异常场景：用户名重复返回40901")
    void register_UsernameDuplicate_Returns40901() {
        // Given: 用户名已存在
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setNickName("测试用户");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");

        when(userMapper.selectOne(any())).thenReturn(existingUser);

        // When: 执行注册
        Result<User> result = userService.register(request);

        // Then: 验证结果
        assertThat(result.getCode()).isEqualTo(ErrorCode.USERNAME_DUPLICATE.getCode());
        assertThat(result.getMessage()).isEqualTo(ErrorCode.USERNAME_DUPLICATE.getMessage());

        // Then: 未执行插入
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("正常场景：用户登录成功返回token")
    void login_Success_ReturnsToken() {
        // Given: 登录请求
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        // Given: 用户存在且密码匹配
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole(Role.BUYER);
        user.setStatus("ACTIVE");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(jwtUtil.generateToken(1L, "testuser", "BUYER")).thenReturn("mock-jwt-token");
        when(jwtUtil.getExpiration()).thenReturn(604800000L);

        // When: 执行登录
        Result<?> result = userService.login(request);

        // Then: 验证结果
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData()).isNotNull();

        UserService.LoginResult loginResult = (UserService.LoginResult) result.getData();
        assertThat(loginResult.getAccessToken()).isEqualTo("mock-jwt-token");
        assertThat(loginResult.getTokenType()).isEqualTo("Bearer");
        assertThat(loginResult.getExpiresIn()).isEqualTo(604800000L);
        assertThat(loginResult.getUser().getUsername()).isEqualTo("testuser");
        assertThat(loginResult.getUser().getPasswordHash()).isNull(); // 密码已清除
    }

    @Test
    @DisplayName("异常场景：用户不存在返回40101")
    void login_UserNotFound_Returns40101() {
        // Given: 用户不存在
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userMapper.selectOne(any())).thenReturn(null);

        // When: 执行登录
        Result<?> result = userService.login(request);

        // Then: 验证结果
        assertThat(result.getCode()).isEqualTo(ErrorCode.USER_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("异常场景：密码错误返回40103")
    void login_PasswordError_Returns40103() {
        // Given: 用户存在但密码错误
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setStatus("ACTIVE");

        when(userMapper.selectOne(any())).thenReturn(user);

        // When: 执行登录
        Result<?> result = userService.login(request);

        // Then: 验证结果
        assertThat(result.getCode()).isEqualTo(ErrorCode.PASSWORD_ERROR.getCode());
    }

    @Test
    @DisplayName("异常场景：用户禁用返回40104")
    void login_UserDisabled_Returns40104() {
        // Given: 用户已被禁用
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setStatus("DISABLED");

        when(userMapper.selectOne(any())).thenReturn(user);

        // When: 执行登录
        Result<?> result = userService.login(request);

        // Then: 验证结果
        assertThat(result.getCode()).isEqualTo(ErrorCode.USER_DISABLED.getCode());
    }

    @Test
    @DisplayName("正常场景：获取当前用户信息成功")
    void getCurrentUser_Success() {
        // Given: 用户ID
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setPasswordHash("hashedpassword");
        user.setStatus("ACTIVE");

        when(userMapper.selectById(userId)).thenReturn(user);

        // When: 获取用户信息
        Result<User> result = userService.getCurrentUser(userId);

        // Then: 验证结果
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData().getId()).isEqualTo(userId);
        assertThat(result.getData().getPasswordHash()).isNull(); // 密码已清除
    }

    @Test
    @DisplayName("异常场景：用户不存在返回40101")
    void getCurrentUser_UserNotFound_Returns40101() {
        // Given: 用户ID
        Long userId = 999L;

        when(userMapper.selectById(userId)).thenReturn(null);

        // When: 获取用户信息
        Result<User> result = userService.getCurrentUser(userId);

        // Then: 验证结果
        assertThat(result.getCode()).isEqualTo(ErrorCode.USER_NOT_FOUND.getCode());
    }
}
