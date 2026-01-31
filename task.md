# 来得快二手交易平台 - 开发任务分解

> **项目名称**: 来得快 (Laidekuai)
> **版本**: V1.0
> **创建日期**: 2026-01-31
> **技术栈**: Spring Boot 3.2 + Vue 3 + MySQL 8.0

---

## 📋 目录

1. [项目概述](#1-项目概述)
2. [技术架构](#2-技术架构)
3. [工程目录结构](#3-工程目录结构)
4. [任务分解策略](#4-任务分解策略)
5. [开发任务清单](#5-开发任务清单)
6. [代码审查方案](#6-代码审查方案)
7. [单元测试方案](#7-单元测试方案)
8. [任务完成标准](#8-任务完成标准)

---

## 1. 项目概述

### 1.1 项目简介
"来得快"是一个 C2C 二手交易平台，支持用户发布商品、浏览购买、在线交易、评价反馈等核心功能。采用买卖一体模式，所有登录用户均可发布商品。

### 1.2 核心特性
- ✅ 按卖家拆单（一个订单只属于一个卖家）
- ✅ 库存实时锁定与释放
- ✅ 15分钟未支付自动取消
- ✅ 未发货退款支持
- ✅ 争议处理机制（2次驳回后强制争议）
- ✅ 评价系统（按订单项评价）

### 1.3 约束条件
- V1 阶段仅支持"未发货退款"
- 拆单后分别支付
- 全场包邮（运费字段保留但不使用）
- 无物理外键，应用层校验
- 买卖一体，无卖家申请流程

---

## 2. 技术架构

### 2.1 技术栈

#### 后端
```
Spring Boot 3.2.0
├── Spring Security 6.x (JWT认证)
├── Spring Data JPA / MyBatis Plus 3.5.x
├── MySQL 8.0
├── Flyway (DDL版本管理)
├── Lombok
└── Java 17+
```

#### 前端
```
Vue 3.4+
├── Vite 5.0+
├── Element Plus
├── Vue Router 4.x
├── Pinia
├── Axios
└── TypeScript 5.x
```

#### 开发工具
- 后端IDE: IntelliJ IDEA
- 前端IDE: VS Code
- API测试: Postman / Swagger
- 数据库管理: Navicat / DBeaver

### 2.2 架构分层

```
┌─────────────────────────────────────────────────────┐
│                   前端层 (Vue 3)                    │
│  ┌─────────────────────────────────────────────┐  │
│  │  UI Components (Element Plus)              │  │
│  │  State Management (Pinia)                 │  │
│  │  HTTP Client (Axios + Interceptors)        │  │
│  └─────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
                      ↕ HTTP (JSON)
┌─────────────────────────────────────────────────────┐
│                 API 网关层 (Nginx)                 │
└─────────────────────────────────────────────────────┘
                      ↕
┌─────────────────────────────────────────────────────┐
│              后端层 (Spring Boot 3.2)              │
│  ┌─────────────────────────────────────────────┐  │
│  │  Controller Layer (REST API)                 │  │
│  │  ┌─────────────────────────────────────┐    │  │
│  │  │  Service Layer (业务逻辑)             │    │  │
│  │  │  ┌───────────────────────────────┐ │    │  │
│  │  │  │  Repository Layer (MyBatis Plus) │ │    │  │
│  │  │  │  ┌────────────────────────────┐ │ │    │  │
│  │  │  │  │   Entity (JPA/MyBatis)      │ │ │    │  │
│  │  │  │  └────────────────────────────┘ │ │    │  │
│  │  │  └───────────────────────────────┘ │    │  │
│  │  └─────────────────────────────────────┘    │  │
│  │  Security Layer (Spring Security + JWT)     │  │
│  │  Scheduler (定时任务)                       │  │
│  │  File Storage Service                     │  │
│  └─────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
                      ↕ SQL
┌─────────────────────────────────────────────────────┐
│              数据层 (MySQL 8.0)                   │
│  ┌─────────────────────────────────────────────┐  │
│  │  数据库表 (按功能模块组织)                 │  │
│  │  - 用户权限: user, role                     │  │
│  │  - 商品模块: goods, category, cart, goods_images│  │
│  │  - 订单模块: orders, order_item              │  │
│  │  - 交易模块: dispute, audit_log             │  │
│  │  - 内容模块: message, message_reply         │  │
│  │  - 评价模块: review                         │  │
│  └─────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

### 2.3 模块划分

```
laidekuai/
├── common/          # 通用模块
│   ├── security/     # 安全认证
│   ├── exception/    # 异常处理
│   ├── util/         # 工具类
│   └── config/       # 配置类
├── user/            # 用户模块
├── goods/           # 商品模块
├── order/           # 订单模块
├── payment/         # 支付模块（模拟）
├── cart/            # 购物车模块
├── dispute/         # 争议处理模块
├── review/          # 评价模块
├── message/         # 留言模块
├── file/            # 文件上传模块
└── scheduler/       # 定时任务模块
```

---

## 3. 工程目录结构

### 3.1 后端目录结构

```
laidekuai-backend/
├── pom.xml
├── src/main/
│   ├── java/com/laidekuai/
│   │   ├── LaidekuaiApplication.java          # 启动类
│   │   ├── common/                            # 通用模块
│   │   │   ├── config/                        # 配置类
│   │   │   │   ├── SecurityConfig.java       # Spring Security配置
│   │   │   │   ├── MyBatisPlusConfig.java   # MP配置
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   └── UploadConfig.java
│   │   │   ├── exception/                     # 异常处理
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── ServiceExceptionHandler.java
│   │   │   │   └── ResponseEntityAdvice.java
│   │   │   ├── util/                           # 工具类
│   │   │   │   ├── JwtUtil.java
│   │   │   │   ├── FileUtil.java
│   │   │   │   ├── BeanCopyUtil.java
│   │   │   │   └── OrderNoGenerator.java
│   │   │   ├── dto/                            # 数据传输对象
│   │   │   │   ├── Result.java                # 统一响应体
│   │   │   │   ├── PageResult.java             # 分页响应
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   └── ...
│   │   │   ├── enums/                          # 枚举类
│   │   │   │   ├── OrderStatus.java
│   │   │   │   ├── Role.java
│   │   │   │   └── ...
│   │   │   └── constants/                      # 常量
│   │   │       ├── ErrorCode.java
│   │   │       └── RedisKey.java
│   │   ├── security/                         # 安全模块
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtUserDetailsService.java
│   │   │   └── SecurityUtils.java
│   │   ├── user/                            # 用户模块
│   │   │   ├── controller/   UserController.java
│   │   │   ├── service/       UserService.java
│   │   │   ├── mapper/       UserMapper.java
│   │   │   └── entity/       User.java
│   │   ├── goods/                           # 商品模块
│   │   │   ├── controller/   GoodsController.java
│   │   │   ├── service/       GoodsService.java
│   │   │   ├── mapper/       GoodsMapper.java
│   │   │   └── entity/       Goods.java
│   │   ├── order/                           # 订单模块
│   │   │   ├── controller/   OrderController.java
│   │   │   ├── service/       OrderService.java
│   │   │   ├── mapper/       OrderMapper.java
│   │   │   ├── entity/       Order.java
│   │   │   ├── entity/       OrderItem.java
│   │   │   └── dto/          OrderCreateRequest.java
│   │   ├── cart/                            # 购物车模块
│   │   │   ├── controller/   CartController.java
│   │   │   ├── service/       CartService.java
│   │   │   ├── mapper/       CartMapper.java
│   │   │   └── entity/       Cart.java
│   │   ├── payment/                         # 支付模块
│   │   │   ├── controller/   PaymentController.java
│   │   │   ├── service/       PaymentService.java
│   │   │   └── dto/          PaymentRequest.java
│   │   ├── dispute/                         # 争议模块
│   │   │   ├── controller/   DisputeController.java
│   │   │   ├── service/       DisputeService.java
│   │   │   ├── mapper/       DisputeMapper.java
│   │   │   └── entity/       Dispute.java
│   │   ├── review/                          # 评价模块
│   │   │   ├── controller/   ReviewController.java
│   │   │   ├── service/       ReviewService.java
│   │   │   ├── mapper/       ReviewMapper.java
│   │   │   └── entity/       Review.java
│   │   ├── message/                         # 留言模块
│   │   │   ├── controller/   MessageController.java
│   │   │   ├── service/       MessageService.java
│   │   │   ├── mapper/       MessageMapper.java
│   │   │   └── entity/       Message.java
│   │   ├── file/                            # 文件上传模块
│   │   │   ├── controller/   FileController.java
│   │   │   ├── service/       FileStorageService.java
│   │   │   └── config/       UploadConfig.java
│   │   ├── scheduler/                        # 定时任务模块
│   │   │   ├── job/         OrderTimeoutCancelJob.java
│   │   │   └── config/      SchedulerConfig.java
│   │   └── resources/
│   │       ├── application.yml               # 主配置
│   │       ├── application-dev.yml           # 开发环境
│   │       ├── application-prod.yml          # 生产环境
│   │       ├── mapper/                       # MyBatis XML
│   │       │   ├── UserMapper.xml
│   │       │   ├── GoodsMapper.xml
│       │       │   └── ...
│   │       └── db/migration/               # Flyway DDL脚本
│   │           ├── V1__init.sql
│   │           └── V1__*.sql
└── uploads/                                   # 文件上传目录
```

### 3.2 前端目录结构

```
laidekuai-frontend/
├── public/
├── src/
│   ├── api/                                # API接口封装
│   │   ├── user.js
│   │   ├── auth.js
│   │   ├── goods.js
│   │   ├── order.js
│   │   ├── cart.js
│   │   ├── payment.js
│   │   ├── dispute.js
│   │   ├── review.js
│   │   └── message.js
│   ├── assets/                            # 静态资源
│   ├── components/                         # 通用组件
│   │   ├── common/
│   │   │   ├── Pagination.vue           # 分页组件
│   │   │   ├── ImageUpload.vue          # 图片上传组件
│   │   │   ├── RichTextEditor.vue       # 富文本编辑器
│   │   │   └── ...
│   │   ├── layout/
│   │   │   ├── Header.vue              # 页头
│   │   │   ├── Footer.vue              # 页脚
│   │   │   ├── Sidebar.vue             # 侧边栏（管理员）
│   │   │   └── Breadcrumb.vue          # 面包屑
│   │   └── business/
│   │       ├── GoodsCard.vue           # 商品卡片
│   │       ├── OrderCard.vue           # 订单卡片
│   │       ├── MessageItem.vue         # 留言项
│   │       └── ...
│   ├── composables/                        # 组合式函数
│   │   ├── usePagination.js
│   │   ├── useForm.js
│   │   ├── useDialog.js
│   │   └── ...
│   ├── router/                             # 路由配置
│   │   ├── index.js
│   │   └── routes/
│   ├── stores/                             # 状态管理 (Pinia)
│   │   ├── user.js
│   │   ├── goods.js
│   │   ├── cart.js
│   │   ├── order.js
│   │   └── auth.js
│   ├── utils/                              # 工具函数
│   │   ├── request.js                    # Axios封装
│   │   ├── storage.js                     # 本地存储
│   │   ├── validate.js                    # 表单验证
│   │   └── date.js                        # 日期处理
│   ├── views/                              # 页面视图
│   │   ├── layout/                         # 布局页面
│   │   ├── user/                           # 用户模块
│   │   │   ├── Login.vue
│   │   │   ├── Register.vue
│   │   │   └── Profile.vue
│   │   ├── goods/                          # 商品模块
│   │   │   ├── GoodsList.vue             # 商品列表
│   │   │   ├── GoodsDetail.vue           # 商品详情
│   │   │   ├── GoodsPublish.vue          # 发布/编辑商品
│   │   │   └── GoodsManage.vue           # 我的商品管理
│   │   ├── order/                          # 订单模块
│   │   │   ├── OrderList.vue             # 订单列表
│   │   │   ├── OrderDetail.vue           # 订单详情
│   │   │   ├── OrderConfirm.vue          # 下单确认
│   │   │   └── OrderSuccess.vue          # 下单成功
│   │   ├── cart/                           # 购物车模块
│   │   │   ├── CartList.vue              # 购物车
│   │   │   └── ...
│   │   ├── dispute/                        # 争议模块
│   │   │   ├── DisputeList.vue            # 争议列表（管理员）
││   │   │   └── DisputeDetail.vue          # 争议详情
│   │   ├── review/                         # 评价模块
│   │   │   ├── ReviewList.vue            # 评价列表
│   │   │   └── ReviewForm.vue            # 评价表单
│   │   ├── message/                        # 留言模块
│   │   │   ├── MessageList.vue            # 留言列表
│   │   │   └── ...
│   │   ├── admin/                          # 管理员模块
│   │   │   ├── Dashboard.vue             # 管理员首页
│   │   │   ├── UserManage.vue            # 用户管理
│   │   │   ├── GoodsAudit.vue            # 商品审核
│   │   │   ├── OrderManage.vue           # 订单管理
│   │   │   ├── RefundAudit.vue           # 退款审核
│   │   │   └── SystemConfig.vue           # 系统配置
│   │   └── index.vue                        # 首页
│   ├── App.vue                             # 根组件
│   └── main.js                             # 入口文件
├── .env.development                          # 开发环境变量
├── .env.production                           # 生产环境变量
├── .env.staging                              # 预发布环境变量
├── vite.config.js                           # Vite配置
├── package.json
└── README.md
```

---

## 4. 任务分解策略

### 4.1 任务分解原则

1. **按功能模块拆分**：每个任务对应一个完整的功能模块
2. **依赖关系明确**：后端API优先，前端页面依赖后端接口
3. **粒度适中**：单个任务预计1-3天完成
4. **独立可测试**：每个任务完成后可独立进行单元测试

### 4.2 任务执行流程

```
┌─────────────────────────────────────────────────┐
│              任务执行流程图                        │
└─────────────────────────────────────────────────┘

  开始任务
     │
     ├─→ 1. 理解需求和技术方案
     │
     ├─→ 2. 创建基础项目结构
     │
     ├─→ 3. 编写代码（按任务清单）
     │     │
     │     ├─→ 3.1 Entity层（实体类）
     │     ├─→ 3.2 Mapper层（接口+XML）
     │     ├─→ 3.3 Service层（业务逻辑）
     │     ├─→ 3.4 Controller层（API接口）
     │     └─ 3.5 前端页面（如涉及）
     │
     ├─→ 4. 自测（开发者自测）
     │
     ├─→ 5. 单元测试编写
     │
     ├─→ 6. 运行单元测试
     │     └─→ 必须全部通过 ✓
     │
     ├─→ 7. 代码审查
     │     ├─→ 同行审查（强制）
     │     ├─→ 审查清单检查（逐项检查）
     │     └── 审查意见修改
     │     └── 必须通过 ✓
     │
     ├─→ 8. 集成测试（可选）
     │
     └─→ 9. 任务完成 ✓
           │
           └─→ 进入下一个任务
```

### 4.3 任务编号规则

```
格式: [阶段]-[模块]-[序号]

示例:
- INFRA-01: 项目初始化
- AUTH-01: 用户认证
- USER-01: 用户管理
- GOODS-01: 商品管理
- ORDER-01: 订单创建
```

---

## 5. 开发任务清单

### 阶段0: 项目基础设施 (INFRA)

---

#### INFRA-01: 项目初始化搭建

**目标**: 搭建前后端基础项目结构

**后端任务**:
1. 创建Spring Boot项目（v3.2.0）
2. 配置Maven依赖（pom.xml）
   - Spring Boot Starter Web
   - Spring Security
   - MyBatis Plus
   - MySQL Driver
   - Lombok
   - Validation
   - Test
3. 配置application.yml
4. 创建基础目录结构
5. 配置Flyway
6. 创建通用配置类
7. 配置Logback日志

**前端任务**:
1. 使用Vite创建Vue 3项目
2. 配置package.json依赖
3. 安装Element Plus
4. 配置Vue Router
5. 配置Pinia
6. 配置Axios
7. 创建基础目录结构
8. 配置Vite别名路径

**代码审查要点**:
- [ ] 项目结构是否符合规范
- [ ] 依赖版本是否正确
- [ ] 配置文件是否完整

**单元测试要点**:
- [ ] 项目能否正常启动
- [ ] 基础配置是否加载成功

**完成标准**:
- [ ] 后端项目能启动，端口9090可访问
- [ ] 前端项目能启动，端口5173可访问
- [ ] Flyway配置正确，能识别迁移脚本
- [ ] 基础日志能正常输出

---

#### INFRA-02: 数据库设计与DDL脚本

**目标**: 创建Flyway迁移脚本

**任务内容**:
1. 创建 `V1__init.sql` DDL脚本
2. 按需求文档第15章定义所有表结构
3. 添加所有索引定义
4. 添加外键关系说明（注释，不创建物理外键）

**表清单**:
- user
- role
- category
- goods
- cart
- address
- orders
- order_item
- message
- message_reply
- dispute
- favorite
- notice
- review
- audit_log

**代码审查要点**:
- [ ] 所有表字段定义完整
- [ ] 主键、索引正确配置
- [ ] 字段类型、长度、默认值合理
- [ ] 表关系正确（parent_id, seller_id等）

**单元测试要点**:
- [ ] DDL脚本能成功执行
- [ ] 表结构创建成功
- [ ] 索引创建成功
- [ ] 验证表关系正确性

**完成标准**:
- [ ] V1__init.sql执行成功无错误
- [ ] 所有表创建完成
- [ ] 索引创建完成
- [ ] Flyway_schema_history表记录迁移版本

---

#### INFRA-03: 通用模块实现

**目标**: 实现通用工具类和配置

**后端任务**:
1. 统一响应体 Result<T>
2. 分页响应 PageResult<T>
3. 全局异常处理器
4. 请求ID拦截器（用于日志追踪）
5. 跨域配置
6. JWT工具类
7. 枚举类定义
8. 错误码定义

**前端任务**:
1. Axios封装（请求/响应拦截器）
2. 本地存储封装
3. 表单验证工具
4. 日期格式化工具
5. 通用分页组件
6. 全局样式配置

**代码审查要点**:
- [ ] Result<T>字段完整（code、message、data、timestamp）
- [ ] 异常处理覆盖所有异常类型
- [ ] JWT生成和验证逻辑正确
- [ ] Axios拦截器正确处理token过期
- [ ] 错误码枚举完整

**单元测试要点**:
- [ ] Result<T>序列化/反序列化
- [ ] 异常处理器能捕获并返回正确错误码
- [ ] JWT工具类生成/验证token正确
- [ ] Axios拦截器正确处理401跳转

**完成标准**:
- [ ] 所有通用类编写完成
- [ ] 单元测试覆盖率 > 80%
- [ ] 代码审查通过

---

### 阶段1: 用户认证模块 (AUTH)

---

#### AUTH-01: 用户认证与授权

**目标**: 实现JWT认证体系

**后端任务**:
1. 实体类: User
2. Mapper: UserMapper + UserMapper.xml
3. Service: UserService
   - register(): 用户注册
   - login(): 用户登录
   - getCurrentUser(): 获取当前用户信息
4. Controller: AuthController
   - POST /api/auth/register
   - POST /api/auth/login
   - GET /api/auth/me
5. SecurityConfig: Spring Security配置
6. JWT工具类: JwtTokenProvider
7. JWT过滤器: JwtAuthenticationFilter
8. 自定义UserDetailsService

**前端任务**:
1. 创建auth.js API文件
2. 创建stores/auth.js (Pinia)
3. 页面开发:
   - Login.vue (登录页)
   - Register.vue (注册页)

**代码审查要点**:
- [ ] 密码使用BCrypt加密存储
- [ ] JWT token包含必要信息（userId、username、role）
- [ ] token过期时间配置合理
- [ ] 登录接口返回token正确
- [ ] Spring Security配置正确
- [ ] 用户状态（ACTIVE/DISABLED）校验

**单元测试要点**:
- [ ] 注册功能：用户名重复返回正确错误码
- [ ] 注册功能：密码正确加密
- [ ] 登录功能：正确返回token
- [ ] 登录功能：密码错误返回401
- [ ] 登录功能：用户禁用返回403
- [ ] JWT token验证正确
- [ ] token过期自动刷新机制（V1不实现，但需验证过期跳转登录）

**测试用例**:
```java
@SpringBootTest
@AutoConfigureMockMvc
class UserServiceTest {

    @Test
    void register_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setNickName("测试用户");

        // When
        Result<User> result = userService.register(request);

        // Then
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData().getUsername()).isEqualTo("testuser");
        assertThat(result.getData().getPassword()).isNotEqualTo("password123"); // 已加密
    }

    @Test
    void register_UsernameDuplicate_Returns40901() {
        // Given: 数据库已存在testuser
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        // When
        Result<User> result = userService.register(request);

        // Then
        assertThat(result.getCode()).isEqualTo(40901); // 用户名重复
    }

    @Test
    void login_Success_ReturnsToken() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        // When
        Result<Map<String, String>> result = userService.login(request);

        // Then
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData()).containsKey("token");
        assertThat(result.getData().get("token")).isNotEmpty();
    }
}
```

**完成标准**:
- [ ] 用户注册功能完成
- [ ] 用户登录功能完成（返回JWT token）
- [ ] JWT认证配置完成
- [ ] 单元测试覆盖率 > 80%
- [ ] 代码审查通过
- [ ] 前端登录/注册页面完成

---

### 阶段2: 用户管理模块 (USER)

---

#### USER-01: 用户基础功能

**目标**: 实现用户信息管理

**后端任务**:
1. Controller: UserController
   - GET /api/users/{id}: 获取用户信息
   - PUT /api/users/{id}: 更新用户信息
   - PUT /api/users/{id}/password: 修改密码
   - GET /api/admin/users: 管理员获取用户列表
2. Service: UserServiceImpl
   - getUserInfo()
   - updateUserInfo()
   - changePassword()
   - listUsers()
3. DTO: UserUpdateRequest, PasswordChangeRequest
4. 状态更新功能

**代码审查要点**:
- [ ] 只能修改自己的信息或管理员可修改所有
- [ ] 修改密码需要验证旧密码
- [ ] 用户状态禁用逻辑正确
- [ ] 管理员查询支持分页和筛选

**单元测试要点**:
- [ ] 用户只能更新自己的信息
- [ ] 管理员可更新所有用户
- [ ] 旧密码校验正确
- [ ] 分页查询正确

**完成标准**:
- [ ] 用户信息更新功能完成
- [ ] 修改密码功能完成
- [ ] 管理员用户列表功能完成
- [ ] 单元测试覆盖率 > 80%
- [ ] 代码审查通过

---

### 阶段3: 商品模块 (GOODS)

---

#### GOODS-01: 商品基础管理

**目标**: 实现商品的CRUD和审核功能

**后端任务**:
1. Entity: Goods
2. Mapper: GoodsMapper + GoodsMapper.xml
3. Service: GoodsService
   - createGoods(): 创建商品
   - updateGoods(): 更新商品
   - deleteGoods(): 下架商品（软删除）
   - getGoodsDetail(): 商品详情
   - listGoods(): 商品列表/搜索
   - submitForAudit(): 提交审核
   - approveGoods(): 审核通过（管理员）
   - rejectGoods(): 审核驳回（管理员）
4. Controller: GoodsController
5. DTO: GoodsCreateRequest, GoodsUpdateRequest, GoodsSearchRequest

**前端任务**:
1. 创建goods.js API文件
2. 创建stores/goods.js (Pinia)
3. 页面开发:
   - GoodsList.vue (商品列表)
   - GoodsDetail.vue (商品详情)
   - GoodsPublish.vue (发布/编辑商品)
   - GoodsManage.vue (我的商品管理)
4. 组件开发:
   - ImageUpload.vue (图片上传组件)
   - RichTextEditor.vue (富文本编辑器)
   - CategorySelect.vue (级联分类选择器)

**代码审查要点**:
- [ ] 商品图片存储为JSON数组
- [ ] 库存扣减使用条件UPDATE（防超卖）
- [ ] 商品状态机（DRAFT→PENDING→APPROVED等）
- [ ] 分类选择3级级联正确
- [ ] 审核流程正确
- [ ] 搜索采用前缀匹配（title LIKE keyword%）
- [ ] 分页查询性能考虑

**单元测试要点**:
- [ ] 商品创建时库存扣减正确
- [ ] 库存不足无法下单
- [ ] 商品状态转换正确
- [ ] 分类级联正确
- [ ] 搜索功能正确（前缀匹配）
- [ ] 审核流程正确

**测试用例**:
```java
@SpringBootTest
@Transactional
class GoodsServiceTest {

    @Test
    void create_Goods_DeductsStock_Success() {
        // Given: 库存10
        GoodsCreateRequest request = new GoodsCreateRequest();
        request.setStock(10);

        // When: 创建商品
        Result<Goods> result = goodsService.createGoods(request);

        // Then: 库存扣减为0
        Goods goods = goodsMapper.selectById(result.getData().getId());
        assertThat(goods.getStock()).isEqualTo(0);
    }

    @Test
    void listGoods_WithKeyword_ReturnsPrefixMatchedResults() {
        // Given: 搜索"iPhone"
        String keyword = "iPhone";

        // When: 搜索
        PageResult<Goods> result = goodsService.listGoods(keyword, 1, 10);

        // Then: 只返回前缀匹配的结果
        assertThat(result.getRecords()).allMatch(g ->
            g.getTitle().startsWith(keyword)
        );
    }

    @Test
    void approveGoods_StatusChangesToApproved() {
        // Given: PENDING商品
        Goods goods = createTestGoods(Status.PENDING);

        // When: 审核通过
        goodsService.approveGoods(goods.getId());

        // Then: 状态变为APPROVED
        assertThat(goods.getStatus()).isEqualTo(Status.APPROVED);
    }
}
```

**完成标准**:
- [ ] 商品CRUD功能完成
- [ ] 商品审核功能完成
- [ ] 商品搜索功能完成（前缀匹配）
- [ ] 图片上传功能完成
- [ ] 级联分类功能完成
- [ ] 单元测试覆盖率 > 80%
- [ ] 代码审查通过
- [ ] 前端商品页面完成

---

#### GOODS-02: 分类管理

**目标**: 实现3级分类管理

**后端任务**:
1. Entity: Category
2. Mapper: CategoryMapper + CategoryMapper.xml
3. Service: CategoryService
   - createCategory(): 创建分类
   - updateCategory(): 更新分类
   - deleteCategory(): 删除分类（校验引用）
   - listCategories(): 分类列表（树形结构）
   - getCategoryTree(): 获取分类树
4. Controller: CategoryController

**代码审查要点**:
- [ ] 3级分类层级约束正确
- [ ] 父子ID存在性校验
- [ ] level字段正确设置（1/2/3）
- [ ] 循环引用检测
- [ ] 删除前校验子分类和商品引用

**单元测试要点**:
- [ ] 创建分类时level计算正确
- [ ] 3级分类约束正确
- [ ] 循环引用被正确检测
- [ ] 删除有引用的分类返回正确错误码
- [ ] 分类树结构正确

**完成标准**:
- [ ] 分类CRUD功能完成
- [ ] 分类树查询功能完成
- [ ] 单元测试覆盖率 > 80%
- [ ] 代码审查通过

---

### 阶段4: 购物车模块 (CART)

---

#### CART-01: 购物车基础功能

**目标**: 实现购物车管理

**后端任务**:
1. Entity: Cart
2. Mapper: CartMapper + CartMapper.xml
3. Service: CartService
   - addToCart(): 加入购物车
   - updateCartItem(): 修改数量
   - removeFromCart(): 删除购物车项
   - getMyCart(): 我的购物车
4. Controller: CartController

**代码审查要点**:
- [ ] 同一商品同一用户只能有一条记录（UNIQUE约束）
- [ ] 数量必须>=1
- [ ] 商品必须存在且状态=APPROVED
- [ ] 返回购物车时关联商品最新信息

**单元测试要点**:
- [ ] 重复添加同一商品更新数量而非新增记录
- [ ] 修改数量边界值测试（0, 负数）
- [ ] 删除非归属购物车项返回403
- [ ] 商品下架后仍可查看购物车

**完成标准**:
- [ ] 购物车CRUD功能完成
- [ ] 单元测试覆盖率 > 80%
- [ ] 代码审查通过
- [ ] 前端购物车页面完成

---

### 阶段5: 订单模块 (ORDER) - 核心模块

---

#### ORDER-01: 订单创建与拆单

**目标**: 实现订单创建和按卖家拆单逻辑

**难度**: ⭐⭐⭐⭐⭐ (核心业务逻辑，涉及库存锁定、事务管理、拆单算法)

**后端任务**:
1. Entity: Order, OrderItem
2. Mapper: OrderMapper + OrderMapper.xml, OrderItemMapper + OrderItemMapper.xml
3. DTO: OrderCreateRequest, OrderItemRequest
4. Service: OrderService
   - createOrder(): 创建订单（核心逻辑）
     - 参数校验
     - 风控检查（活跃订单>10拒绝）
     - 按seller_id分组
     - 库存扣减（条件UPDATE）
     - 创建订单主单
     - 创建订单项
     - 返回订单列表
   - getMyOrders(): 我的订单列表
   - getOrderDetail(): 订单详情
5. OrderNoGenerator: 订单号生成器（雪花算法或时间戳+序列号）

---

### 🔴 核心逻辑详解

#### 1. 完整的订单创建流程（超详细伪代码）

```java
/**
 * 创建订单 - 核心业务逻辑
 *
 * 业务规则：
 * 1. 一个订单严格属于一个卖家（按seller_id拆单）
 * 2. 库存扣减使用条件UPDATE保证原子性（防超卖）
 * 3. 风控检查：活跃订单（PENDING_PAY + PAID）> 10 拒绝
 * 4. 订单快照：创建时保存商品、收货地址的快照数据
 * 5. 事务一致性：任何步骤失败需完整回滚
 */
@Transactional(rollbackFor = Exception.class)
public Result<List<OrderDTO>> createOrder(OrderCreateRequest request) {
    Long currentUserId = SecurityUtils.getCurrentUserId();

    // ========== 第1步：参数校验 ==========
    log.info("开始创建订单，买家ID: {}, 商品项数: {}", currentUserId, request.getItems().size());

    // 1.1 校验请求参数非空
    if (request.getItems() == null || request.getItems().isEmpty()) {
        return Result.error(400, "订单商品不能为空");
    }

    // 1.2 校验每项商品的合法性
    for (OrderItemRequest item : request.getItems()) {
        // 商品ID必须存在
        if (item.getGoodsId() == null) {
            return Result.error(400, "商品ID不能为空");
        }
        // 数量必须>0
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            return Result.error(400, "商品数量必须大于0");
        }
        // 数量不能超过100（防刷单）
        if (item.getQuantity() > 100) {
            return Result.error(400, "单个商品数量不能超过100");
        }
    }

    // 1.3 校验收货地址
    Address address = addressMapper.selectById(request.getAddressId());
    if (address == null || !address.getUserId().equals(currentUserId)) {
        return Result.error(404, "收货地址不存在");
    }

    // ========== 第2步：风控检查 ==========
    // 2.1 统计活跃订单数（PENDING_PAY + PAID，不含REFUNDING）
    int activeOrderCount = orderMapper.countActiveOrders(currentUserId);
    log.info("买家当前活跃订单数: {}", activeOrderCount);

    if (activeOrderCount >= 10) {
        log.warn("买家 {} 活跃订单数超限: {}", currentUserId, activeOrderCount);
        return Result.error(40903, "活跃订单数已达上限，请先完成或取消现有订单");
    }

    // 2.2 检查商品状态（只允许购买APPROVED状态的商品）
    List<Long> goodsIds = request.getItems().stream()
        .map(OrderItemRequest::getGoodsId)
        .collect(Collectors.toList());

    List<Goods> goodsList = goodsMapper.selectBatchIds(goodsIds);
    for (Goods goods : goodsList) {
        if (goods.getStatus() != GoodsStatus.APPROVED) {
            return Result.error(400, "商品[" + goods.getTitle() + "]状态异常，无法购买");
        }
        if (goods.getSellerId().equals(currentUserId)) {
            return Result.error(400, "不能购买自己发布的商品");
        }
    }

    // ========== 第3步：按卖家分组 ==========
    // 核心算法：Stream流按seller_id分组
    Map<Long, List<OrderItemRequest>> sellerGroups = request.getItems().stream()
        .collect(Collectors.groupingBy(item -> {
            Goods goods = goodsMap.get(item.getGoodsId());
            return goods.getSellerId();
        }));

    log.info("订单涉及 {} 个卖家，拆分为 {} 个订单", sellerGroups.size(), sellerGroups.size());

    // ========== 第4步：扣减库存并创建订单 ==========
    List<Order> createdOrders = new ArrayList<>();
    List<OrderItem> allOrderItems = new ArrayList<>();

    try {
        // 遍历每个卖家的商品组
        for (Map.Entry<Long, List<OrderItemRequest>> entry : sellerGroups.entrySet()) {
            Long sellerId = entry.getKey();
            List<OrderItemRequest> items = entry.getValue();

            log.info("处理卖家 {} 的订单，商品数: {}", sellerId, items.size());

            // 4.1 逐个扣减库存（使用条件UPDATE保证原子性）
            for (OrderItemRequest item : items) {
                Goods goods = goodsMap.get(item.getGoodsId());

                // 关键SQL：UPDATE goods SET stock = stock - #{quantity}
                //         WHERE id = #{goodsId} AND stock >= #{quantity}
                int rows = goodsMapper.deductStock(item.getGoodsId(), item.getQuantity());

                if (rows == 0) {
                    // 库存不足或商品不存在，事务回滚
                    log.error("商品 {} 库存不足，当前库存: {}, 需求: {}",
                        goods.getId(), goods.getStock(), item.getQuantity());
                    throw new BusinessException(40902, "商品[" + goods.getTitle() + "]库存不足");
                }

                log.info("商品 {} 库存扣减成功，扣减数量: {}", goods.getId(), item.getQuantity());
            }

            // 4.2 创建订单主单
            Order order = new Order();
            order.setOrderNo(OrderNoGenerator.generate()); // 雪花算法或时间戳+序列号
            order.setBuyerId(currentUserId);
            order.setSellerId(sellerId);

            // 计算订单总金额
            BigDecimal totalAmount = items.stream()
                .map(item -> {
                    Goods goods = goodsMap.get(item.getGoodsId());
                    return goods.getPrice().multiply(new BigDecimal(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            order.setTotalAmount(totalAmount);
            order.setStatus(OrderStatus.PENDING_PAY);
            order.setReceiverName(address.getName());
            order.setReceiverPhone(address.getPhone());
            order.setReceiverAddress(address.getFullAddress());

            // 插入订单
            orderMapper.insert(order);
            log.info("订单创建成功，订单号: {}", order.getOrderNo());
            createdOrders.add(order);

            // 4.3 创建订单项（保存快照）
            for (OrderItemRequest item : items) {
                Goods goods = goodsMap.get(item.getGoodsId());

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setGoodsId(goods.getId());
                orderItem.setSellerId(sellerId);

                // ===== 快照数据 =====
                orderItem.setGoodsTitle(goods.getTitle());        // 商品标题快照
                orderItem.setGoodsCover(goods.getCoverUrl());     // 商品封面快照
                orderItem.setPrice(goods.getPrice());             // 价格快照
                orderItem.setQuantity(item.getQuantity());
                orderItem.setAmount(goods.getPrice().multiply(new BigDecimal(item.getQuantity())));

                orderItem.setItemStatus(ItemStatus.PENDING_PAY);
                orderItem.setOrderStatus(OrderStatus.PENDING_PAY); // 冗余字段，便于查询

                orderItemMapper.insert(orderItem);
                allOrderItems.add(orderItem);
                log.info("订单项创建成功，商品: {}, 数量: {}, 金额: {}",
                    goods.getTitle(), item.getQuantity(), orderItem.getAmount());
            }
        }

        log.info("订单创建完成，共创建 {} 个订单，{} 个订单项", createdOrders.size(), allOrderItems.size());

        // ========== 第5步：返回结果 ==========
        List<OrderDTO> orderDTOs = createdOrders.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return Result.success(orderDTOs);

    } catch (BusinessException e) {
        // 业务异常，事务自动回滚
        log.error("订单创建失败，业务异常: {}", e.getMessage());
        throw e;

    } catch (Exception e) {
        // 系统异常，事务自动回滚
        log.error("订单创建失败，系统异常", e);
        throw new BusinessException(500, "系统错误，订单创建失败");
    }
}
```

#### 2. 库存扣减Mapper方法（关键SQL）

```xml
<!-- OrderMapper.xml -->

<!-- 条件UPDATE扣减库存，保证原子性 -->
<update id="deductStock">
    UPDATE goods
    SET stock = stock - #{quantity},
        updated_at = NOW()
    WHERE id = #{goodsId}
      AND stock >= #{quantity}
      AND status = 'APPROVED'
      AND deleted = 0
</update>

<!-- 说明：
  1. stock >= #{quantity} 条件确保库存充足时才扣减，防止超卖
  2. 返回值影响行数：=1表示成功，=0表示库存不足或商品不存在
  3. status = 'APPROVED' 确保只能购买已上架商品
  4. deleted = 0 确保不购买已删除商品
-->

<!-- 统计活跃订单数（用于风控） -->
<select id="countActiveOrders" resultType="int">
    SELECT COUNT(*)
    FROM orders
    WHERE buyer_id = #{buyerId}
      AND status IN ('PENDING_PAY', 'PAID')
      AND deleted = 0
</select>

<!-- 说明：
  1. 只统计 PENDING_PAY 和 PAID 状态
  2. 不统计 REFUNDING 状态（订单已进入退款流程）
  3. 不统计 CANCELED, COMPLETED, REFUNDED 状态
-->
```

#### 3. 订单号生成器（雪花算法简化版）

```java
/**
 * 订单号生成器
 *
 * 格式: 时间戳(41位) + 机器ID(5位) + 序列号(12位) = 18位十进制数
 * 示例: 2026013112345678901
 */
public class OrderNoGenerator {

    // 起始时间戳 (2026-01-01 00:00:00)
    private static final long EPOCH = 1735660800000L;

    // 机器ID（5位，支持0-31）
    private final long machineId;

    // 序列号（12位，支持0-4095）
    private long sequence = 0L;

    // 上次生成时间戳
    private long lastTimestamp = -1L;

    public OrderNoGenerator(long machineId) {
        if (machineId < 0 || machineId > 31) {
            throw new IllegalArgumentException("机器ID必须在0-31之间");
        }
        this.machineId = machineId;
    }

    /**
     * 生成订单号（线程安全）
     */
    public synchronized String generate() {
        long timestamp = System.currentTimeMillis() - EPOCH;

        // 时钟回拨检测
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，订单号生成失败");
        }

        // 同一毫秒内，序列号递增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & 0xFFF; // 12位掩码
            if (sequence == 0) {
                // 序列号溢出，等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 新毫秒，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 拼接订单号
        long orderId = (timestamp << 17)        // 时间戳左移17位
                     | (machineId << 12)        // 机器ID左移12位
                     | sequence;                // 序列号

        return String.valueOf(orderId);
    }

    /**
     * 等待下一毫秒
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis() - EPOCH;
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis() - EPOCH;
        }
        return timestamp;
    }
}

/*
 * 订单号示例解析：
 * 2026013112345678901
 * - 前8位: 20260131 (日期)
 * - 后10位: 12345678901 (唯一序列)
 *
 * 优点：
 * 1. 全局唯一
 * 2. 按时间有序
 * 3. 性能高（单机每毫秒可生成4096个）
 * 4. 包含时间信息，便于排查
 */
```

---

### 🔴 代码审查要点（超详细清单）

#### A. 功能完整性检查
- [ ] **风控检查正确性**
  - [ ] 活跃订单统计只包含PENDING_PAY和PAID状态
  - [ ] REFUNDING状态不纳入统计
  - [ ] 活跃订单数>=10时拒绝下单
  - [ ] 风控提示信息清晰
- [ ] **库存扣减原子性**
  - [ ] 使用条件UPDATE：`WHERE id = ? AND stock >= ?`
  - [ ] 检查UPDATE返回行数，=0时抛出异常
  - [ ] 库存不足时事务完整回滚
- [ ] **拆单逻辑正确性**
  - [ ] 按seller_id正确分组
  - [ ] 一个订单只包含一个卖家的商品
  - [ ] 多卖家商品拆分为多个订单
  - [ ] 每个订单的金额计算正确
- [ ] **快照数据完整性**
  - [ ] goods_title字段保存
  - [ ] goods_cover字段保存
  - [ ] price字段保存（当前价格快照）
  - [ ] 收货地址完整保存
- [ ] **订单号唯一性**
  - [ ] 使用雪花算法或类似算法
  - [ ] 线程安全保证
  - [ ] 时钟回拨处理

#### B. 事务管理检查
- [ ] **事务边界正确**
  - [ ] `@Transactional`注解在方法上
  - [ ] `rollbackFor = Exception.class`指定回滚异常
  - [ ] 任何异常都触发回滚
- [ ] **事务回滚验证**
  - [ ] 库存扣减失败时回滚
  - [ ] 订单插入失败时回滚
  - [ ] 任何异常都回滚已扣减的库存
- [ ] **隔离级别验证**
  - [ ] 使用默认隔离级别（READ_COMMITTED）
  - [ ] 库存查询使用悲观锁（SELECT FOR UPDATE）或乐观锁（版本号）

#### C. 性能检查
- [ ] **批量查询优化**
  - [ ] 商品信息批量查询（`selectBatchIds`）
  - [ ] 避免循环查库
  - [ ] 使用Map缓存商品信息
- [ ] **数据库索引**
  - [ ] goods表有stock索引（可选，库存查询优化）
  - [ ] orders表有buyer_id+status联合索引（风控查询优化）
  - [ ] order_item表有order_id索引
- [ ] **N+1查询检查**
  - [ ] 无循环查库
  - [ ] 无循环查商品信息
  - [ ] 无循环查卖家信息

#### D. 安全性检查
- [ ] **权限校验**
  - [ ] 使用`SecurityUtils.getCurrentUserId()`获取当前用户
  - [ ] 不接受请求中的buyer_id（防篡改）
  - [ ] 收货地址归属校验
- [ ] **输入校验**
  - [ ] 商品数量>0校验
  - [ ] 商品数量<=100校验（防刷单）
  - [ ] 收货地址非空校验
- [ ] **业务规则校验**
  - [ ] 不允许购买自己的商品
  - [ ] 只允许购买APPROVED状态的商品
- [ ] **敏感数据处理**
  - [ ] 日志不输出完整手机号
  - [ ] 日志不输出完整地址

#### E. 异常处理检查
- [ ] **异常捕获完整**
  - [ ] 捕获`BusinessException`并重新抛出
  - [ ] 捕获通用`Exception`并转换为业务异常
- [ ] **错误码规范**
  - [ ] 400：参数错误
  - [ ] 40902：库存不足
  - [ ] 40903：活跃订单超限
  - [ ] 500：系统错误
- [ ] **错误信息清晰**
  - [ ] 库存不足时提示商品名称
  - [ ] 风控拦截时提示当前订单数

#### F. 日志检查
- [ ] **关键日志完整**
  - [ ] 订单创建开始日志
  - [ ] 每个订单创建成功日志
  - [ ] 库存扣减成功日志
  - [ ] 库存不足错误日志
  - [ ] 订单创建完成日志
- [ ] **日志级别正确**
  - [ ] INFO：正常流程
  - [ ] WARN：风控拦截
  - [ ] ERROR：业务异常

---

### 🔴 单元测试要点（超详细清单）

#### A. 正常场景测试
- [ ] **单个商品单个卖家**
  - [ ] 库存充足时创建成功
  - [ ] 订单号生成唯一
  - [ ] 库存正确扣减
  - [ ] 订单金额计算正确
- [ ] **多个商品单个卖家**
  - [ ] 创建一个订单
  - [ ] 订单包含多个订单项
  - [ ] 总金额计算正确
- [ ] **多个商品多个卖家**
  - [ ] 正确拆单
  - [ ] 每个卖家一个订单
  - [ ] 每个订单金额正确

#### B. 异常场景测试
- [ ] **库存不足**
  - [ ] 单个商品库存不足
  - [ ] 部分商品库存不足
  - [ ] 库存不足时事务回滚
  - [ ] 返回正确错误码40902
- [ ] **风控超限**
  - [ ] 活跃订单=10时拒绝
  - [ ] 活跃订单>10时拒绝
  - [ ] 返回正确错误码40903
  - [ ] REFUNDING状态不纳入统计
- [ ] **商品状态异常**
  - [ ] DRAFT状态商品不可购买
  - [ ] PENDING状态商品不可购买
  - [ ] 已下架商品不可购买
- [ ] **购买自己的商品**
  - [ ] 返回错误提示
  - [ ] 不创建订单

#### C. 边界值测试
- [ ] **商品数量边界**
  - [ ] 数量=0时拒绝
  - [ ] 数量=负数时拒绝
  - [ ] 数量=1时正常
  - [ ] 数量=100时正常
  - [ ] 数量>100时拒绝
- [ ] **库存边界**
  - [ ] 需求数量=库存数量时成功
  - [ ] 需求数量=库存数量+1时失败
- [ ] **活跃订单数边界**
  - [ ] 活跃订单=9时允许
  - [ ] 活跃订单=10时拒绝

#### D. 并发测试
- [ ] **并发下单**
  - [ ] 10个线程同时下单，库存=5
  - [ ] 只有5个订单成功
  - [ ] 其他订单返回库存不足
- [ ] **库存一致性**
  - [ ] 并发扣减后库存>=0
  - [ ] 不出现负库存

---

### 🔴 详细测试用例（可直接运行）

```java
/**
 * 订单服务单元测试
 *
 * 测试策略：
 * 1. 使用H2内存数据库，每个测试独立
 * 2. @Transactional确保测试后回滚
 * 3. 覆盖正常场景、异常场景、边界值、并发
 */
@SpringBootTest
@Transactional
@Rollback
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    private User buyer;
    private User seller1, seller2, seller3;
    private Goods goods1, goods2, goods3;
    private Address address;

    /**
     * 测试数据准备
     */
    @BeforeEach
    void setUp() {
        // 创建买家
        buyer = new User();
        buyer.setUsername("buyer");
        buyer.setPassword("password");
        buyer.setRole(Role.BUYER);
        userMapper.insert(buyer);

        // 创建卖家
        seller1 = createSeller("seller1");
        seller2 = createSeller("seller2");
        seller3 = createSeller("seller3");

        // 创建商品
        goods1 = createGoods(seller1.getId(), "商品1", 100, 10);  // 库存10
        goods2 = createGoods(seller1.getId(), "商品2", 200, 5);   // 库存5
        goods3 = createGoods(seller2.getId(), "商品3", 300, 8);   // 库存8

        // 创建收货地址
        address = new Address();
        address.setUserId(buyer.getId());
        address.setName("张三");
        address.setPhone("13800138000");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDetail("南山区");
        addressMapper.insert(address);

        // 设置当前用户（模拟登录）
        SecurityUtils.setCurrentUser(buyer);
    }

    // ========== 正常场景测试 ==========

    @Test
    @DisplayName("正常场景：单个商品单个卖家下单成功")
    void createOrder_SingleItemSingleSeller_Success() {
        // Given: 商品1库存10，要购买3个
        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of(
            new OrderItemRequest(goods1.getId(), 3)
        ));

        // When: 创建订单
        Result<List<OrderDTO>> result = orderService.createOrder(request);

        // Then: 验证结果
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData()).hasSize(1);

        OrderDTO order = result.getData().get(0);
        assertThat(order.getOrderNo()).isNotNull();
        assertThat(order.getBuyerId()).isEqualTo(buyer.getId());
        assertThat(order.getSellerId()).isEqualTo(seller1.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAY);
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("300.00")); // 100*3

        // Then: 验证库存扣减
        Goods updatedGoods = goodsMapper.selectById(goods1.getId());
        assertThat(updatedGoods.getStock()).isEqualTo(7); // 10-3=7

        // Then: 验证订单项
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getGoodsTitle()).isEqualTo("商品1");
        assertThat(items.get(0).getPrice()).isEqualByComparingTo("100");
        assertThat(items.get(0).getQuantity()).isEqualTo(3);
        assertThat(items.get(0).getAmount()).isEqualByComparingTo("300");
    }

    @Test
    @DisplayName("正常场景：多个商品单个卖家创建一个订单")
    void createOrder_MultipleItemsSingleSeller_OneOrder() {
        // Given: 同一卖家的2个商品
        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of(
            new OrderItemRequest(goods1.getId(), 2),  // 商品1，2个
            new OrderItemRequest(goods2.getId(), 3)   // 商品2，3个
        ));

        // When
        Result<List<OrderDTO>> result = orderService.createOrder(request);

        // Then: 创建1个订单
        assertThat(result.getData()).hasSize(1);
        OrderDTO order = result.getData().get(0);

        // Then: 订单有2个订单项
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        assertThat(items).hasSize(2);

        // Then: 总金额 = 100*2 + 200*3 = 800
        assertThat(order.getTotalAmount()).isEqualByComparingTo("800");

        // Then: 库存正确扣减
        assertThat(goodsMapper.selectById(goods1.getId()).getStock()).isEqualTo(8);  // 10-2
        assertThat(goodsMapper.selectById(goods2.getId()).getStock()).isEqualTo(2);  // 5-3
    }

    @Test
    @DisplayName("正常场景：多个商品多个卖家拆单")
    void createOrder_MultipleItemsMultipleSellers_SplitCorrectly() {
        // Given: 3个卖家的商品
        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of(
            new OrderItemRequest(goods1.getId(), 1),  // seller1
            new OrderItemRequest(goods3.getId(), 2)   // seller2
        ));

        // When
        Result<List<OrderDTO>> result = orderService.createOrder(request);

        // Then: 拆分为2个订单
        assertThat(result.getData()).hasSize(2);

        // Then: 每个订单属于不同卖家
        Set<Long> sellerIds = result.getData().stream()
            .map(OrderDTO::getSellerId)
            .collect(Collectors.toSet());
        assertThat(sellerIds).containsExactlyInAnyOrder(seller1.getId(), seller2.getId());

        // Then: 每个订单的金额正确
        for (OrderDTO order : result.getData()) {
            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
            BigDecimal expectedAmount = items.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(order.getTotalAmount()).isEqualByComparingTo(expectedAmount);
        }
    }

    // ========== 异常场景测试 ==========

    @Test
    @DisplayName("异常场景：库存不足时下单失败")
    void createOrder_StockInsufficient_Failure() {
        // Given: 商品1库存10，要购买15个
        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of(
            new OrderItemRequest(goods1.getId(), 15)
        ));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("code", 40902);

        // Then: 库存未扣减
        assertThat(goodsMapper.selectById(goods1.getId()).getStock()).isEqualTo(10);

        // Then: 未创建订单
        assertThat(orderMapper.selectList(null)).isEmpty();
    }

    @Test
    @DisplayName("异常场景：库存刚好满足时下单成功")
    void createOrder_StockJustEnough_Success() {
        // Given: 商品1库存10，要购买10个
        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of(
            new OrderItemRequest(goods1.getId(), 10)
        ));

        // When
        Result<List<OrderDTO>> result = orderService.createOrder(request);

        // Then: 成功
        assertThat(result.getCode()).isEqualTo(0);

        // Then: 库存扣减为0
        assertThat(goodsMapper.selectById(goods1.getId()).getStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("异常场景：活跃订单数超限时拒绝下单")
    void createOrder_ActiveOrdersExceeded_Failure() {
        // Given: 创建10个活跃订单
        for (int i = 0; i < 10; i++) {
            createTestOrder(buyer.getId(), OrderStatus.PENDING_PAY);
        }

        // Given: 活跃订单数=10
        assertThat(orderMapper.countActiveOrders(buyer.getId())).isEqualTo(10);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of(
            new OrderItemRequest(goods1.getId(), 1)
        ));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("code", 40903);
    }

    @Test
    @DisplayName("异常场景：REFUNDING状态不计入活跃订单")
    void createOrder_REFUNDINGNotCounted_AsActiveOrder() {
        // Given: 创建9个PENDING_PAY订单 + 1个REFUNDING订单
        for (int i = 0; i < 9; i++) {
            createTestOrder(buyer.getId(), OrderStatus.PENDING_PAY);
        }
        createTestOrder(buyer.getId(), OrderStatus.REFUNDING);

        // Given: 活跃订单数=9（REFUNDING不计入）
        assertThat(orderMapper.countActiveOrders(buyer.getId())).isEqualTo(9);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of(
            new OrderItemRequest(goods1.getId(), 1)
        ));

        // When: 允许下单
        Result<List<OrderDTO>> result = orderService.createOrder(request);

        // Then: 成功
        assertThat(result.getCode()).isEqualTo(0);
    }

    @Test
    @DisplayName("异常场景：商品状态为PENDING时不可购买")
    void createOrder_GoodsStatusPENDING_Failure() {
        // Given: 商品状态为PENDING
        goods1.setStatus(GoodsStatus.PENDING);
        goodsMapper.updateById(goods1);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of(
            new OrderItemRequest(goods1.getId(), 1)
        ));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("异常场景：不能购买自己的商品")
    void createOrder_BuyOwnGoods_Failure() {
        // Given: 当前用户是seller1
        SecurityUtils.setCurrentUser(seller1);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of(
            new OrderItemRequest(goods1.getId(), 1)  // seller1的商品
        ));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(BusinessException.class);
    }

    // ========== 边界值测试 ==========

    @Test
    @DisplayName("边界值：商品数量=0时拒绝")
    void createOrder_QuantityZero_Failure() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of(
            new OrderItemRequest(goods1.getId(), 0)
        ));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("code", 400);
    }

    @Test
    @DisplayName("边界值：商品数量=负数时拒绝")
    void createOrder_QuantityNegative_Failure() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of(
            new OrderItemRequest(goods1.getId(), -1)
        ));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("边界值：商品数量=100时允许")
    void createOrder_Quantity100_Success() {
        // Given: 库存150
        goods1.setStock(150);
        goodsMapper.updateById(goods1);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of(
            new OrderItemRequest(goods1.getId(), 100)
        ));

        // When
        Result<List<OrderDTO>> result = orderService.createOrder(request);

        // Then: 成功
        assertThat(result.getCode()).isEqualTo(0);
    }

    @Test
    @DisplayName("边界值：商品数量>100时拒绝")
    void createOrder_QuantityExceed100_Failure() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of(
            new OrderItemRequest(goods1.getId(), 101)
        ));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("code", 400);
    }

    @Test
    @DisplayName("边界值：空订单列表拒绝")
    void createOrder_EmptyItems_Failure() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setAddressId(address.getId());
        request.setItems(List.of());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(BusinessException.class);
    }

    // ========== 并发测试 ==========

    @Test
    @DisplayName("并发场景：10个线程竞争库存5的商品")
    void createOrder_Concurrent_Only5Success() throws InterruptedException {
        // Given: 商品1库存5
        goods1.setStock(5);
        goodsMapper.updateById(goods1);

        // Given: 10个线程同时下单，每个要1个
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await(); // 等待同时开始

                    OrderCreateRequest request = new OrderCreateRequest();
                    request.setAddressId(address.getId());
                    request.setItems(List.of(
                        new OrderItemRequest(goods1.getId(), 1)
                    ));

                    Result<List<OrderDTO>> result = orderService.createOrder(request);
                    if (result.getCode() == 0) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        // When: 同时开始
        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);

        // Then: 只有5个成功
        assertThat(successCount.get()).isEqualTo(5);
        assertThat(failureCount.get()).isEqualTo(5);

        // Then: 库存正确扣减为0
        assertThat(goodsMapper.selectById(goods1.getId()).getStock()).isEqualTo(0);
    }

    // ========== 辅助方法 ==========

    private User createSeller(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");
        user.setRole(Role.SELLER);
        userMapper.insert(user);
        return user;
    }

    private Goods createGoods(Long sellerId, String title, int price, int stock) {
        Goods goods = new Goods();
        goods.setSellerId(sellerId);
        goods.setTitle(title);
        goods.setPrice(new BigDecimal(price));
        goods.setStock(stock);
        goods.setStatus(GoodsStatus.APPROVED);
        goodsMapper.insert(goods);
        return goods;
    }

    private void createTestOrder(Long buyerId, OrderStatus status) {
        Order order = new Order();
        order.setOrderNo(OrderNoGenerator.generate());
        order.setBuyerId(buyerId);
        order.setSellerId(seller1.getId());
        order.setStatus(status);
        order.setTotalAmount(BigDecimal.ZERO);
        orderMapper.insert(order);
    }
}
```

---

### 🔴 性能测试用例

```java
/**
 * 订单创建性能测试
 */
@SpringBootTest
class OrderServicePerformanceTest {

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("性能测试：单次下单耗时<500ms")
    void createOrder_Performance_LessThan500ms() {
        // Given: 准备数据
        OrderCreateRequest request = buildOrderRequest();

        // When: 测量耗时
        long startTime = System.currentTimeMillis();
        Result<List<OrderDTO>> result = orderService.createOrder(request);
        long endTime = System.currentTimeMillis();

        // Then: 验证结果
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(endTime - startTime).isLessThan(500);
    }

    @Test
    @DisplayName("性能测试：100个并发下单TPS>50")
    void createOrder_Concurrent_TPSGreaterThan50() throws InterruptedException {
        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    orderService.createOrder(buildOrderRequest());
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        endLatch.await(30, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double tps = threadCount * 1000.0 / duration;

        assertThat(tps).isGreaterThan(50);
    }
}
```

---

### 🔴 完成标准（超详细清单）

#### A. 功能完成标准
- [ ] **基础功能**
  - [ ] 订单创建功能完成
  - [ ] 按卖家拆单功能完成
  - [ ] 库存扣减功能完成
  - [ ] 订单号生成功能完成
  - [ ] 快照数据保存功能完成
  - [ ] 风控检查功能完成
- [ ] **异常处理**
  - [ ] 库存不足时正确拒绝
  - [ ] 风控超限时正确拒绝
  - [ ] 商品状态异常时正确拒绝
  - [ ] 所有异常正确回滚事务

#### B. 性能标准
- [ ] **响应时间**
  - [ ] 单次下单 < 500ms
  - [ ] 100并发下单TPS > 50
- [ ] **数据库性能**
  - [ ] 库存扣减查询 < 10ms
  - [ ] 风控查询 < 20ms
  - [ ] 订单创建 < 50ms

#### C. 质量标准
- [ ] **代码质量**
  - [ ] 代码规范符合
  - [ ] 无重复代码
  - [ ] 复杂逻辑有注释
  - [ ] 命名规范
- [ ] **测试覆盖**
  - [ ] 单元测试覆盖率 > 85%
  - [ ] 所有测试用例通过
  - [ ] 包含并发测试
  - [ ] 包含性能测试
- [ ] **代码审查**
  - [ ] 代码审查通过
  - [ ] 无必须修复问题
  - [ ] 无严重问题

#### D. 前端完成标准
- [ ] **前端页面**
  - [ ] 下单确认页面完成
  - [ ] 拆单展示清晰
  - [ ] 错误提示清晰
  - [ ] API调用正确

---

#### ORDER-02: 订单支付与状态流转

**目标**: 实现订单支付和状态转换

**难度**: ⭐⭐⭐⭐ (涉及状态机、幂等性、库存回滚)

**后端任务**:
1. Controller: OrderController
   - POST /api/orders/{id}/pay: 模拟支付
   - POST /api/orders/{id}/cancel: 取消订单
2. Service: OrderService
   - payOrder(): 支付订单
   - cancelOrder(): 取消订单
   - getOrderDetail(): 订单详情
   - getMyOrders(): 我的订单列表

---

### 🔴 核心逻辑详解

#### 1. 订单状态机图

```
订单状态转换图:
┌──────────────┐
│ PENDING_PAY  │  ← 初始状态
└──────┬───────┘
       │ pay()
       ↓
┌──────────────┐     cancel()     ┌──────────┐
│    PAID      ├─────────────────→│ CANCELED  │
└──────┬───────┘                  └──────────┘
       │
       ├──────────────────────────┐
       │                          │
       ↓                          ↓
┌──────────────┐          ┌──────────────┐
│  REFUNDING   │          │   SHIPPED    │
└──────────────┘          └──────┬───────┘
       │                          │
       ↓                          │ confirmReceived()
┌──────────────┐                  ↓
│   DISPUTED   │          ┌──────────────┐
└──────┬───────┘          │  COMPLETED   │
       │                  └──────────────┘
       └──→ 裁决后 → REFUNDED 或 COMPLETED
```

#### 2. 支付订单（详细伪代码）

```java
/**
 * 支付订单（V1为模拟支付）
 *
 * 业务规则：
 * 1. 只有PENDING_PAY状态可以支付
 * 2. 支付成功后状态变为PAID
 * 3. 记录支付时间pay_time
 * 4. 幂等性：已支付订单不可重复支付
 */
@Transactional(rollbackFor = Exception.class)
public Result<Void> payOrder(Long orderId) {
    Long currentUserId = SecurityUtils.getCurrentUserId();

    log.info("开始支付订单，订单ID: {}, 用户: {}", orderId, currentUserId);

    // ========== 第1步：查询订单 ==========
    Order order = orderMapper.selectById(orderId);
    if (order == null) {
        return Result.error(404, "订单不存在");
    }

    // ========== 第2步：权限校验 ==========
    if (!order.getBuyerId().equals(currentUserId)) {
        return Result.error(403, "无权操作此订单");
    }

    // ========== 第3步：状态校验 ==========
    if (order.getStatus() != OrderStatus.PENDING_PAY) {
        log.warn("订单状态异常，无法支付，当前状态: {}", order.getStatus());

        // 幂等性处理：已支付订单返回成功
        if (order.getStatus() == OrderStatus.PAID) {
            return Result.success(null);
        }

        return Result.error(400, "订单状态异常，无法支付");
    }

    // ========== 第4步：模拟支付 ==========
    // V1版本：模拟支付，直接返回成功
    // V2版本：对接支付宝/微信支付
    boolean paymentSuccess = simulatePayment();

    if (!paymentSuccess) {
        return Result.error(500, "支付失败，请重试");
    }

    // ========== 第5步：更新订单状态 ==========
    order.setStatus(OrderStatus.PAID);
    order.setPayTime(LocalDateTime.now());

    int rows = orderMapper.updateById(order);
    if (rows == 0) {
        // 并发情况下，订单可能已被其他线程处理
        throw new BusinessException(409, "订单状态已变更，请刷新后重试");
    }

    // ========== 第6步：更新订单项状态 ==========
    List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
    for (OrderItem item : items) {
        item.setItemStatus(ItemStatus.PAID);
        item.setOrderStatus(OrderStatus.PAID);
        orderItemMapper.updateById(item);
    }

    log.info("订单支付成功，订单ID: {}, 订单号: {}", orderId, order.getOrderNo());

    return Result.success(null);
}

/**
 * 模拟支付（V1版本）
 */
private boolean simulatePayment() {
    // 模拟支付延迟
    try {
        Thread.sleep(100);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return false;
    }

    // V1版本：90%成功率
    return Math.random() < 0.9;
}
```

#### 3. 取消订单（详细伪代码）

```java
/**
 * 取消订单
 *
 * 业务规则：
 * 1. 只有PENDING_PAY状态可以取消
 * 2. 取消后释放已锁定的库存
 * 3. 幂等性：已取消订单不可重复取消
 */
@Transactional(rollbackFor = Exception.class)
public Result<Void> cancelOrder(Long orderId, String reason) {
    Long currentUserId = SecurityUtils.getCurrentUserId();

    log.info("开始取消订单，订单ID: {}, 用户: {}, 原因: {}", orderId, currentUserId, reason);

    // ========== 第1步：查询订单 ==========
    Order order = orderMapper.selectById(orderId);
    if (order == null) {
        return Result.error(404, "订单不存在");
    }

    // ========== 第2步：权限校验 ==========
    if (!order.getBuyerId().equals(currentUserId)) {
        return Result.error(403, "无权操作此订单");
    }

    // ========== 第3步：状态校验 ==========
    if (order.getStatus() != OrderStatus.PENDING_PAY) {
        log.warn("订单状态异常，无法取消，当前状态: {}", order.getStatus());

        // 幂等性处理：已取消订单返回成功
        if (order.getStatus() == OrderStatus.CANCELED) {
            return Result.success(null);
        }

        return Result.error(400, "订单状态异常，无法取消");
    }

    // ========== 第4步：查询订单项 ==========
    List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);

    // ========== 第5步：释放库存 ==========
    for (OrderItem item : items) {
        // 关键SQL：UPDATE goods SET stock = stock + #{quantity}
        //         WHERE id = #{goodsId}
        int rows = goodsMapper.releaseStock(item.getGoodsId(), item.getQuantity());

        if (rows == 0) {
            log.error("库存释放失败，商品ID: {}, 数量: {}", item.getGoodsId(), item.getQuantity());
            // 注意：即使释放失败也继续（数据不一致时需要人工介入）
        }

        log.info("库存释放成功，商品ID: {}, 数量: {}", item.getGoodsId(), item.getQuantity());
    }

    // ========== 第6步：更新订单状态 ==========
    order.setStatus(OrderStatus.CANCELED);
    order.setCancelReason(reason);
    order.setCancelTime(LocalDateTime.now());

    int rows = orderMapper.updateById(order);
    if (rows == 0) {
        throw new BusinessException(409, "订单状态已变更，请刷新后重试");
    }

    // ========== 第7步：更新订单项状态 ==========
    for (OrderItem item : items) {
        item.setItemStatus(ItemStatus.CANCELED);
        item.setOrderStatus(OrderStatus.CANCELED);
        orderItemMapper.updateById(item);
    }

    log.info("订单取消成功，订单ID: {}, 订单号: {}", orderId, order.getOrderNo());

    return Result.success(null);
}
```

#### 4. Mapper方法（关键SQL）

```xml
<!-- OrderMapper.xml -->

<!-- 释放库存（取消订单时调用） -->
<update id="releaseStock">
    UPDATE goods
    SET stock = stock + #{quantity},
        updated_at = NOW()
    WHERE id = #{goodsId}
      AND deleted = 0
</update>

<!-- 说明：
  1. 取消订单时释放库存
  2. stock = stock + quantity 恢复库存
  3. 不检查商品状态（即使商品已下架也要释放库存）
-->

<!-- 支付订单（更新状态） -->
<update id="payOrder">
    UPDATE orders
    SET status = 'PAID',
        pay_time = NOW(),
        updated_at = NOW()
    WHERE id = #{orderId}
      AND status = 'PENDING_PAY'
      AND deleted = 0
</update>

<!-- 说明：
  1. 使用条件UPDATE保证幂等性
  2. 只有PENDING_PAY状态才能更新为PAID
  3. 返回行数=0表示订单状态已变更
-->
```

---

### 🔴 代码审查要点（超详细清单）

#### A. 状态机验证
- [ ] **支付状态转换**
  - [ ] 只有PENDING_PAY → PAID
  - [ ] PAID状态不可重复支付（幂等）
  - [ ] 其他状态不可支付
- [ ] **取消状态转换**
  - [ ] 只有PENDING_PAY → CANCELED
  - [ ] CANCELED状态不可重复取消（幂等）
  - [ ] 其他状态不可取消
- [ ] **状态回滚验证**
  - [ ] 异常时状态不改变
  - [ ] 事务回滚时库存正确处理

#### B. 幂等性保证
- [ ] **支付幂等性**
  - [ ] 已支付订单重复支付返回成功
  - [ ] 并发支付只成功一次
  - [ ] 使用条件UPDATE防止重复
- [ ] **取消幂等性**
  - [ ] 已取消订单重复取消返回成功
  - [ ] 并发取消只成功一次
- [ ] **库存幂等性**
  - [ ] 取消时库存释放正确
  - [ ] 重复取消不重复释放库存

#### C. 库存管理
- [ ] **库存释放**
  - [ ] 取消订单时释放库存
  - [ ] 释放数量=购买数量
  - [ ] 释放失败不影响取消流程
- [ ] **库存一致性**
  - [ ] 释放后stock>=0
  - [ ] 并发释放不出现负库存

#### D. 权限校验
- [ ] **支付权限**
  - [ ] 只有买家可以支付
  - [ ] 订单归属校验
- [ ] **取消权限**
  - [ ] 只有买家可以取消
  - [ ] 管理员也可以取消（如需）

---

### 🔴 单元测试要点（超详细清单）

#### A. 支付功能测试
- [ ] **正常场景**
  - [ ] PENDING_PAY订单支付成功
  - [ ] 支付后状态变为PAID
  - [ ] 支付时间正确记录
  - [ ] 订单项状态同步更新
- [ ] **异常场景**
  - [ ] PAID订单重复支付（幂等）
  - [ ] CANCELED订单不可支付
  - [ ] 不存在的订单
  - [ ] 无权支付他人订单

#### B. 取消功能测试
- [ ] **正常场景**
  - [ ] PENDING_PAY订单取消成功
  - [ ] 取消后状态变为CANCELED
  - [ ] 取消原因正确记录
  - [ ] 库存正确释放
- [ ] **异常场景**
  - [ ] CANCELED订单重复取消（幂等）
  - [ ] PAID订单不可取消
  - [ ] SHIPPED订单不可取消
  - [ ] 不存在的订单

#### C. 库存释放测试
- [ ] **释放逻辑**
  - [ ] 取消订单后库存增加
  - [ ] 释放数量=购买数量
  - [ ] 多商品订单全部释放
- [ ] **并发释放**
  - [ ] 并发取消不重复释放
  - [ ] 库存一致性保证

---

### 🔴 详细测试用例（可直接运行）

```java
/**
 * 订单支付与取消单元测试
 */
@SpringBootTest
@Transactional
@Rollback
class OrderPaymentTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    private Order testOrder;
    private Goods testGoods;

    @BeforeEach
    void setUp() {
        // 创建测试商品（库存10）
        testGoods = new Goods();
        testGoods.setTitle("测试商品");
        testGoods.setPrice(new BigDecimal("100"));
        testGoods.setStock(10);
        goodsMapper.insert(testGoods);

        // 创建测试订单（PENDING_PAY状态）
        testOrder = new Order();
        testOrder.setOrderNo(OrderNoGenerator.generate());
        testOrder.setBuyerId(1L);
        testOrder.setSellerId(2L);
        testOrder.setStatus(OrderStatus.PENDING_PAY);
        testOrder.setTotalAmount(new BigDecimal("100"));
        orderMapper.insert(testOrder);

        // 创建订单项
        OrderItem item = new OrderItem();
        item.setOrderId(testOrder.getId());
        item.setGoodsId(testGoods.getId());
        item.setQuantity(1);
        item.setItemStatus(ItemStatus.PENDING_PAY);
        item.setOrderStatus(OrderStatus.PENDING_PAY);
        orderItemMapper.insert(item);

        // 扣减库存
        goodsMapper.deductStock(testGoods.getId(), 1);

        // 设置当前用户
        SecurityUtils.setCurrentUser(testOrder.getBuyerId());
    }

    // ========== 支付功能测试 ==========

    @Test
    @DisplayName("正常场景：支付订单成功")
    void payOrder_Success() {
        // When
        Result<Void> result = orderService.payOrder(testOrder.getId());

        // Then
        assertThat(result.getCode()).isEqualTo(0);

        // Then: 订单状态变为PAID
        Order order = orderMapper.selectById(testOrder.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPayTime()).isNotNull();

        // Then: 订单项状态同步更新
        OrderItem item = orderItemMapper.selectByOrderId(testOrder.getId()).get(0);
        assertThat(item.getItemStatus()).isEqualTo(ItemStatus.PAID);
        assertThat(item.getOrderStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("幂等性：重复支付已支付订单返回成功")
    void payOrder_AlreadyPaid_Idempotent() {
        // Given: 订单已支付
        orderService.payOrder(testOrder.getId());

        // When: 重复支付
        Result<Void> result = orderService.payOrder(testOrder.getId());

        // Then: 返回成功（幂等）
        assertThat(result.getCode()).isEqualTo(0);
    }

    @Test
    @DisplayName("异常场景：PAID订单不可取消")
    void cancelOrder_AlreadyPaid_Failure() {
        // Given: 订单已支付
        orderService.payOrder(testOrder.getId());

        // When: 尝试取消
        Result<Void> result = orderService.cancelOrder(testOrder.getId(), "不想要了");

        // Then
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("异常场景：无权支付他人订单")
    void payOrder_NotOwner_Failure() {
        // Given: 切换到其他用户
        SecurityUtils.setCurrentUser(999L);

        // When
        Result<Void> result = orderService.payOrder(testOrder.getId());

        // Then
        assertThat(result.getCode()).isEqualTo(403);
    }

    // ========== 取消功能测试 ==========

    @Test
    @DisplayName("正常场景：取消订单成功")
    void cancelOrder_Success() {
        // Given: 库存从10减到9
        assertThat(goodsMapper.selectById(testGoods.getId()).getStock()).isEqualTo(9);

        // When
        Result<Void> result = orderService.cancelOrder(testOrder.getId(), "不想要了");

        // Then
        assertThat(result.getCode()).isEqualTo(0);

        // Then: 订单状态变为CANCELED
        Order order = orderMapper.selectById(testOrder.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order.getCancelReason()).isEqualTo("不想要了");
        assertThat(order.getCancelTime()).isNotNull();

        // Then: 库存恢复到10
        assertThat(goodsMapper.selectById(testGoods.getId()).getStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("幂等性：重复取消已取消订单返回成功")
    void cancelOrder_AlreadyCanceled_Idempotent() {
        // Given: 订单已取消
        orderService.cancelOrder(testOrder.getId(), "不想要了");

        // When: 重复取消
        Result<Void> result = orderService.cancelOrder(testOrder.getId(), "不想要了");

        // Then: 返回成功（幂等）
        assertThat(result.getCode()).isEqualTo(0);
    }

    @Test
    @DisplayName("异常场景：支付后不可取消")
    void cancelOrder_AfterPayment_Failure() {
        // Given: 订单已支付
        orderService.payOrder(testOrder.getId());

        // When: 尝试取消
        Result<Void> result = orderService.cancelOrder(testOrder.getId(), "不想要了");

        // Then
        assertThat(result.getCode()).isEqualTo(400);

        // Then: 库存未释放
        assertThat(goodsMapper.selectById(testGoods.getId()).getStock()).isEqualTo(9);
    }

    @Test
    @DisplayName("异常场景：无权取消他人订单")
    void cancelOrder_NotOwner_Failure() {
        // Given: 切换到其他用户
        SecurityUtils.setCurrentUser(999L);

        // When
        Result<Void> result = orderService.cancelOrder(testOrder.getId(), "不想要了");

        // Then
        assertThat(result.getCode()).isEqualTo(403);
    }

    // ========== 库存释放测试 ==========

    @Test
    @DisplayName("库存释放：多商品订单全部释放")
    void cancelOrder_MultipleItems_AllStockReleased() {
        // Given: 添加第二个订单项
        Goods goods2 = new Goods();
        goods2.setTitle("商品2");
        goods2.setStock(20);
        goodsMapper.insert(goods2);

        OrderItem item2 = new OrderItem();
        item2.setOrderId(testOrder.getId());
        item2.setGoodsId(goods2.getId());
        item2.setQuantity(3);
        item2.setItemStatus(ItemStatus.PENDING_PAY);
        orderItemMapper.insert(item2);

        // 扣减库存
        goodsMapper.deductStock(goods2.getId(), 3);

        // When: 取消订单
        orderService.cancelOrder(testOrder.getId(), "不想要了");

        // Then: 两个商品的库存都恢复
        assertThat(goodsMapper.selectById(testGoods.getId()).getStock()).isEqualTo(10); // 恢复1
        assertThat(goodsMapper.selectById(goods2.getId()).getStock()).isEqualTo(20);    // 恢复3
    }

    @Test
    @DisplayName("并发取消：不重复释放库存")
    void cancelOrder_Concurrent_NoDuplicateRelease() throws InterruptedException {
        // Given: 库存9
        assertThat(goodsMapper.selectById(testGoods.getId()).getStock()).isEqualTo(9);

        // Given: 10个线程同时取消
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    orderService.cancelOrder(testOrder.getId(), "不想要了");
                } catch (Exception e) {
                    // 异常忽略
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);

        // Then: 库存只释放一次（恢复到10，不是恢复10次）
        assertThat(goodsMapper.selectById(testGoods.getId()).getStock()).isEqualTo(10);
    }
}
```

---

### 🔴 完成标准（超详细清单）

#### A. 功能完成标准
- [ ] **支付功能**
  - [ ] 支付接口实现
  - [ ] 状态转换正确
  - [ ] 支付时间记录
  - [ ] 幂等性保证
- [ ] **取消功能**
  - [ ] 取消接口实现
  - [ ] 状态转换正确
  - [ ] 取消原因记录
  - [ ] 库存释放正确
  - [ ] 幂等性保证

#### B. 性能标准
- [ ] **响应时间**
  - [ ] 支付接口 < 300ms
  - [ ] 取消接口 < 300ms
- [ ] **数据库性能**
  - [ ] 状态更新 < 20ms
  - [ ] 库存释放 < 20ms

#### C. 质量标准
- [ ] **测试覆盖**
  - [ ] 单元测试覆盖率 > 85%
  - [ ] 包含并发测试
  - [ ] 包含幂等性测试

---

#### ORDER-03: 发货与收货

**目标**: 实现卖家发货和买家收货

**后端任务**:
1. Controller: OrderController
   - POST /api/seller/order-items/{id}/ship: 卖家发货
   - POST /api/admin/order-items/{id}/ship: 管理员代发货
   - POST /api/orders/{id}/confirm: 确认收货
2. Service: OrderService
   - shipOrderItem(): 订单项发货
   - confirmReceived(): 确认收货
   - aggregateShipStatus(): 聚合发货状态

**代码审查要点**:
- [ ] 发货时状态：item_status PAID → SHIPPED
- [ ] 发货时写入物流信息
- [ ] 聚合发货逻辑：所有item shipped时order.status → SHIPPED
- [ ] 收货时状态：order.status SHIPPED → COMPLETED
- [ ] 确认收货时记录settled_time和is_settled=1
- [ ] REFUNDING/REFUNDED/DISPUTED状态不允许发货

**单元测试要点**:
- [ ] 发货状态转换正确
- [ ] 聚合发货逻辑正确
- [ ] 收货状态转换正确
- [ ] 禁止发货状态校验正确
- [ ] 物流信息记录完整

**完成标准**:
- [ ] 发货功能完成
- [ ] 确认收货功能完成
- [ ] 单元测试覆盖率 > 80%
- [ ] 代码审查通过
- [ ] 前端卖家发货页面完成

---

#### ORDER-04: 订单查询与展示

**目标**:
1. 买家查看自己的订单列表（按状态筛选）
2. 卖家查看自己的订单项列表
3. 管理员查看所有订单

**后端任务**:
1. Controller: OrderController
   - GET /api/orders: 我的订单列表（买家）
   - GET /api/seller/orders: 我的订单项列表（卖家）
   - GET /api/admin/orders: 管理员订单列表
   - GET /api/orders/{id}: 订单详情

**代码审查要点**:
- [ ] 买家只能查看自己的订单（buyer_id=当前用户）
- [ ] 卖家只能查看自己的订单项（orders.seller_id=当前用户）
- [ ] 管理员可查看所有订单
- [ ] 订单详情包含完整的订单项和物流信息
- [ ] 订单快照与商品当前信息区分

**单元测试要点**:
- [ ] 权限校验正确
- [ ] 分页查询正确
- [ ] 状态筛选正确
- [ ] 订单详情数据完整

**完成标准**:
- [ ] 订单查询功能完成
- [ ] 单元测试覆盖率 > 80%
- [ ] 代码审查通过
- [ ] 前端订单列表页面完成

---

### 阶段6: 争议处理模块 (DISPUTE)

---

#### DISPUTE-01: 争议处理机制

**目标**: 实现退款争议处理

**难度**: ⭐⭐⭐⭐⭐ (涉及复杂状态机、退款计数、争议触发)

**业务规则**:
- 买家申请退款被驳回2次后，订单自动进入争议状态（DISPUTED）
- 争议订单由管理员进行最终裁决
- 裁决结果：支持买家（退款）或支持卖家（继续交易）
- 争议解决后清零退款申请计数

**后端任务**:
1. Entity: Dispute
2. Mapper: DisputeMapper + DisputeMapper.xml
3. Service: DisputeService
   - createDispute(): 创建争议
   - resolveDispute(): 管理员裁决
   - getDisputeDetail(): 争议详情
   - listDisputes(): 争议列表（管理员）
4. Controller: DisputeController
5. OrderService扩展
   - 增加refund_request_count计数
   - 检测争议触发条件（2次驳回）

---

### 🔴 核心逻辑详解

#### 1. 争议状态机图

```
退款争议流程:
┌──────────────┐ 申请退款
│    PAID      │──────────┐
└──────┬───────┘          │
       │                  ↓
       │            ┌──────────────┐
       │            │  REFUNDING   │
       │            └──────┬───────┘
       │                   │
       │                   ↓ 驳回（第1次）
       │              refund_request_count++
       │                   │
       │                   ↓ 再次申请退款
       │            ┌──────────────┐
       │            │  REFUNDING   │
       │            └──────┬───────┘
       │                   │
       │                   ↓ 驳回（第2次）
       │              refund_request_count++
       │                   │
       │                   ↓ 自动触发
       │            ┌──────────────┐
       └───────────→│   DISPUTED   │←───────┐
       争议中       └──────┬───────┘        │
                          │                │
                          ↓ 管理员裁决      │
                    ┌──────────┐            │
                    │ 裁决结果  │            │
                    └────┬─────┘            │
                         │                  │
              ┌──────────┴──────────┐        │
              ↓                      ↓        │
        ┌──────────┐          ┌──────────┐  │
        │ REFUNDED │          │ COMPLETED │  │
        │ （退款）  │          │ （继续交易） │  │
        └──────────┘          └──────────┘  │
              ↓                      ↓        │
         refund_request_count清零   计数清零   │
                                          ────┘
```

#### 2. 退款驳回触发争议（详细伪代码）

```java
/**
 * 拒绝退款申请（卖家操作）
 *
 * 业务规则：
 * 1. 拒绝后refund_request_count加1
 * 2. 拒绝2次后自动进入争议状态
 * 3. 状态：REFUNDING → DISPUTED
 */
@Transactional(rollbackFor = Exception.class)
public Result<Void> rejectRefund(Long orderId, String reason) {
    Long currentUserId = SecurityUtils.getCurrentUserId();

    log.info("拒绝退款申请，订单ID: {}, 卖家: {}, 原因: {}", orderId, currentUserId, reason);

    // ========== 第1步：查询订单 ==========
    Order order = orderMapper.selectById(orderId);
    if (order == null) {
        return Result.error(404, "订单不存在");
    }

    // ========== 第2步：权限校验（只有卖家可操作）==========
    if (!order.getSellerId().equals(currentUserId)) {
        return Result.error(403, "只有卖家可以拒绝退款");
    }

    // ========== 第3步：状态校验 ==========
    if (order.getStatus() != OrderStatus.REFUNDING) {
        return Result.error(400, "订单状态异常，无法拒绝退款");
    }

    // ========== 第4步：退款申请计数+1 ==========
    int newCount = (order.getRefundRequestCount() == null ? 0 : order.getRefundRequestCount()) + 1;
    order.setRefundRequestCount(newCount);

    log.info("订单退款申请次数: {}", newCount);

    // ========== 第5步：判断是否触发争议 ==========
    if (newCount >= 2) {
        // 2次驳回，自动进入争议状态
        log.warn("订单 {} 退款申请被驳回2次，自动进入争议状态", orderId);

        order.setStatus(OrderStatus.DISPUTED);
        order.setDisputeTime(LocalDateTime.now());

        // ========== 第6步：创建争议记录 ==========
        Dispute dispute = new Dispute();
        dispute.setOrderId(orderId);
        dispute.setBuyerId(order.getBuyerId());
        dispute.setSellerId(order.getSellerId());
        dispute.setStatus(DisputeStatus.PENDING);
        dispute.setReason("退款申请被拒绝2次，自动进入争议流程");
        dispute.setCreateTime(LocalDateTime.now());

        disputeMapper.insert(dispute);

        log.info("争议记录创建成功，争议ID: {}", dispute.getId());
    }

    // ========== 第7步：记录拒绝原因 ==========
    // 可使用audit_log表记录拒绝历史
    auditLogService.recordRefundReject(orderId, currentUserId, reason, newCount);

    // ========== 第8步：更新订单 ==========
    int rows = orderMapper.updateById(order);
    if (rows == 0) {
        throw new BusinessException(409, "订单状态已变更，请刷新后重试");
    }

    log.info("退款拒绝完成，订单ID: {}, 当前状态: {}", orderId, order.getStatus());

    return Result.success(null);
}
```

#### 3. 管理员裁决争议（详细伪代码）

```java
/**
 * 管理员裁决争议
 *
 * 业务规则：
 * 1. 只有管理员可操作
 * 2. 裁决结果：APPROVE_REFUND（支持退款）或 REJECT_REFUND（拒绝退款）
 * 3. 裁决后清零refund_request_count
 * 4. 状态：DISPUTED → REFUNDED 或 DISPUTED → COMPLETED
 */
@Transactional(rollbackFor = Exception.class)
public Result<Void> resolveDispute(Long disputeId, DisputeResolution resolution, String remark) {
    Long currentUserId = SecurityUtils.getCurrentUserId();

    // ========== 第1步：权限校验（管理员）==========
    if (!SecurityUtils.isAdmin()) {
        return Result.error(403, "只有管理员可以裁决争议");
    }

    log.info("管理员 {} 开始裁决争议，争议ID: {}, 裁决结果: {}", currentUserId, disputeId, resolution);

    // ========== 第2步：查询争议 ==========
    Dispute dispute = disputeMapper.selectById(disputeId);
    if (dispute == null) {
        return Result.error(404, "争议不存在");
    }

    if (dispute.getStatus() != DisputeStatus.PENDING) {
        return Result.error(400, "争议已裁决，不可重复操作");
    }

    // ========== 第3步：查询订单 ==========
    Order order = orderMapper.selectById(dispute.getOrderId());
    if (order == null) {
        return Result.error(404, "订单不存在");
    }

    if (order.getStatus() != OrderStatus.DISPUTED) {
        return Result.error(400, "订单状态异常");
    }

    // ========== 第4步：执行裁决 ==========
    if (resolution == DisputeResolution.APPROVE_REFUND) {
        // ========== 情况A：支持买家（退款）==========

        log.info("裁决：支持买家，订单 {} 执行退款", order.getId());

        // 4.1 更新订单状态
        order.setStatus(OrderStatus.REFUNDED);
        order.setRefundTime(LocalDateTime.now());
        order.setRefundReason(remark != null ? remark : "管理员裁决：支持买家退款申请");

        // 4.2 释放库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        for (OrderItem item : items) {
            goodsMapper.releaseStock(item.getGoodsId(), item.getQuantity());
            log.info("库存释放，商品ID: {}, 数量: {}", item.getGoodsId(), item.getQuantity());
        }

    } else {
        // ========== 情况B：支持卖家（继续交易）==========

        log.info("裁决：支持卖家，订单 {} 继续交易", order.getId());

        // 4.1 更新订单状态
        order.setStatus(OrderStatus.PAID);
        // 订单回到PAID状态，卖家可以继续发货

        // 4.2 清零退款申请计数
        order.setRefundRequestCount(0);
    }

    // ========== 第5步：清零退款申请计数 ==========
    order.setRefundRequestCount(0);

    // ========== 第6步：更新订单 ==========
    int rows = orderMapper.updateById(order);
    if (rows == 0) {
        throw new BusinessException(409, "订单状态已变更");
    }

    // ========== 第7步：更新争议状态 ==========
    dispute.setStatus(DisputeStatus.RESOLVED);
    dispute.setResolution(resolution);
    dispute.setRemark(remark);
    dispute.setResolverId(currentUserId);
    dispute.setResolveTime(LocalDateTime.now());

    disputeMapper.updateById(dispute);

    // ========== 第8步：记录裁决日志 ==========
    auditLogService.recordDisputeResolution(disputeId, currentUserId, resolution, remark);

    log.info("争议裁决完成，争议ID: {}, 订单状态: {}", disputeId, order.getStatus());

    return Result.success(null);
}
```

#### 4. Mapper方法（关键SQL）

```xml
<!-- DisputeMapper.xml -->

<!-- 查询待处理争议列表 -->
<select id="selectPendingDisputes" resultType="Dispute">
    SELECT d.*, o.order_no, u1.username AS buyer_name, u2.username AS seller_name
    FROM dispute d
    LEFT JOIN orders o ON d.order_id = o.id
    LEFT JOIN user u1 ON d.buyer_id = u1.id
    LEFT JOIN user u2 ON d.seller_id = u2.id
    WHERE d.status = 'PENDING'
      AND d.deleted = 0
    ORDER BY d.create_time ASC
</select>

<!-- 说明：
  1. 查询所有待处理的争议
  2. 关联订单表获取订单号
  3. 关联用户表获取买卖家名称
  4. 按创建时间升序（先创建先处理）
-->

<!-- 更新争议状态 -->
<update id="updateDisputeStatus">
    UPDATE dispute
    SET status = #{status},
        resolution = #{resolution},
        remark = #{remark},
        resolver_id = #{resolverId},
        resolve_time = NOW(),
        updated_at = NOW()
    WHERE id = #{disputeId}
      AND status = 'PENDING'
      AND deleted = 0
</update>

<!-- 说明：
  1. 使用条件UPDATE保证幂等性
  2. 只有PENDING状态才能更新为RESOLVED
-->
```

---

### 🔴 代码审查要点（超详细清单）

#### A. 争议触发逻辑
- [ ] **触发条件正确**
  - [ ] refund_request_count=2时自动触发
  - [ ] 只有REFUNDING状态可以触发
  - [ ] 触发后状态变为DISPUTED
- [ ] **计数管理**
  - [ ] 拒绝退款时计数+1
  - [ ] 裁决后计数清零
  - [ ] 计数正确存储到数据库

#### B. 争议裁决逻辑
- [ ] **裁决权限**
  - [ ] 只有管理员可裁决
  - [ ] 非管理员返回403
- [ ] **裁决结果**
  - [ ] APPROVE_REFUND：订单状态→REFUNDED
  - [ ] REJECT_REFUND：订单状态→PAID
  - [ ] 裁决后计数清零
- [ ] **库存处理**
  - [ ] APPROVE_REFUND时释放库存
  - [ ] REJECT_REFUND时不释放库存

#### C. 状态机验证
- [ ] **状态转换正确**
  - [ ] REFUNDING → DISPUTED（2次驳回）
  - [ ] DISPUTED → REFUNDED（支持买家）
  - [ ] DISPUTED → PAID（支持卖家）
- [ ] **幂等性保证**
  - [ ] 已裁决争议不可重复裁决
  - [ ] 使用条件UPDATE防止重复

#### D. 数据完整性
- [ ] **争议记录完整**
  - [ ] 记录完整的争议原因
  - [ ] 记录完整的退款历史
  - [ ] 记录裁决结果和原因
- [ ] **日志记录完整**
  - [ ] 争议创建日志
  - [ ] 争议裁决日志
  - [ ] 库存操作日志

---

### 🔴 单元测试要点（超详细清单）

#### A. 争议触发测试
- [ ] **触发条件**
  - [ ] 第1次驳回不触发争议
  - [ ] 第2次驳回自动触发争议
  - [ ] 争议后订单状态变为DISPUTED
- [ ] **计数管理**
  - [ ] 拒绝时计数+1
  - [ ] 裁决后计数清零
  - [ ] 计数持久化到数据库

#### B. 争议裁决测试
- [ ] **支持买家**
  - [ ] 订单状态变为REFUNDED
  - [ ] 库存正确释放
  - [ ] 计数清零
- [ ] **支持卖家**
  - [ ] 订单状态变为PAID
  - [ ] 库存不释放
  - [ ] 计数清零

#### C. 权限测试
- [ ] **触发权限**
  - [ ] 只有卖家可驳回
  - [ ] 买家不可驳回
- [ ] **裁决权限**
  - [ ] 只有管理员可裁决
  - [ ] 买卖家不可裁决

---

### 🔴 详细测试用例（可直接运行）

```java
/**
 * 争议处理机制单元测试
 */
@SpringBootTest
@Transactional
@Rollback
class DisputeServiceTest {

    @Autowired
    private DisputeService disputeService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private DisputeMapper disputeMapper;

    private Order testOrder;
    private Goods testGoods;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testGoods = createTestGoods(100, 10);
        testOrder = createTestOrder(OrderStatus.PAID);

        // 设置当前用户为卖家
        SecurityUtils.setCurrentUser(testOrder.getSellerId());
    }

    // ========== 争议触发测试 ==========

    @Test
    @DisplayName("争议触发：第1次驳回不触发争议")
    void rejectRefund_FirstTime_NotTriggerDispute() {
        // Given: 订单申请退款
        orderService.applyRefund(testOrder.getId(), "不想要了");
        testOrder = orderMapper.selectById(testOrder.getId());
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.REFUNDING);

        // When: 第1次驳回
        Result<Void> result = disputeService.rejectRefund(testOrder.getId(), "商品完好，不支持退款");

        // Then: 成功
        assertThat(result.getCode()).isEqualTo(0);

        // Then: 计数=1
        testOrder = orderMapper.selectById(testOrder.getId());
        assertThat(testOrder.getRefundRequestCount()).isEqualTo(1);

        // Then: 状态仍为REFUNDING
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.REFUNDING);

        // Then: 无争议记录
        List<Dispute> disputes = disputeMapper.selectByOrderId(testOrder.getId());
        assertThat(disputes).isEmpty();
    }

    @Test
    @DisplayName("争议触发：第2次驳回自动触发争议")
    void rejectRefund_SecondTime_AutoTriggerDispute() {
        // Given: 第1次驳回
        orderService.applyRefund(testOrder.getId(), "不想要了");
        disputeService.rejectRefund(testOrder.getId(), "商品完好");

        // Given: 再次申请退款
        orderService.applyRefund(testOrder.getId(), "还是有问题");
        testOrder = orderMapper.selectById(testOrder.getId());
        assertThat(testOrder.getRefundRequestCount()).isEqualTo(1);

        // When: 第2次驳回
        Result<Void> result = disputeService.rejectRefund(testOrder.getId(), "坚持不退款");

        // Then: 成功
        assertThat(result.getCode()).isEqualTo(0);

        // Then: 计数=2
        testOrder = orderMapper.selectById(testOrder.getId());
        assertThat(testOrder.getRefundRequestCount()).isEqualTo(2);

        // Then: 状态变为DISPUTED
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.DISPUTED);
        assertThat(testOrder.getDisputeTime()).isNotNull();

        // Then: 创建争议记录
        List<Dispute> disputes = disputeMapper.selectByOrderId(testOrder.getId());
        assertThat(disputes).hasSize(1);
        assertThat(disputes.get(0).getStatus()).isEqualTo(DisputeStatus.PENDING);
    }

    // ========== 争议裁决测试 ==========

    @Test
    @DisplayName("争议裁决：支持买家（退款）")
    void resolveDispute_ApproveRefund_OrderRefunded() {
        // Given: 争议状态
        createDispute(testOrder.getId());

        // Given: 设置管理员
        SecurityUtils.setAdmin();

        // When: 裁决支持买家
        Result<Void> result = disputeService.resolveDispute(
            getDisputeId(),
            DisputeResolution.APPROVE_REFUND,
            "同意退款"
        );

        // Then: 成功
        assertThat(result.getCode()).isEqualTo(0);

        // Then: 订单状态变为REFUNDED
        testOrder = orderMapper.selectById(testOrder.getId());
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED);
        assertThat(testOrder.getRefundTime()).isNotNull();

        // Then: 计数清零
        assertThat(testOrder.getRefundRequestCount()).isEqualTo(0);

        // Then: 库存释放
        testGoods = goodsMapper.selectById(testGoods.getId());
        assertThat(testGoods.getStock()).isEqualTo(11); // 原来是10，扣减后变9，释放后变10
    }

    @Test
    @DisplayName("争议裁决：支持卖家（继续交易）")
    void resolveDispute_RejectRefund_OrderBackToPaid() {
        // Given: 争议状态
        createDispute(testOrder.getId());

        // Given: 设置管理员
        SecurityUtils.setAdmin();

        // When: 裁决支持卖家
        Result<Void> result = disputeService.resolveDispute(
            getDisputeId(),
            DisputeResolution.REJECT_REFUND,
            "拒绝退款，继续交易"
        );

        // Then: 成功
        assertThat(result.getCode()).isEqualTo(0);

        // Then: 订单状态变为PAID
        testOrder = orderMapper.selectById(testOrder.getId());
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        // Then: 计数清零
        assertThat(testOrder.getRefundRequestCount()).isEqualTo(0);

        // Then: 库存未释放
        testGoods = goodsMapper.selectById(testGoods.getId());
        assertThat(testGoods.getStock()).isEqualTo(9); // 仍然是扣减后的库存
    }

    @Test
    @DisplayName("异常场景：非管理员不能裁决")
    void resolveDispute_NotAdmin_Failure() {
        // Given: 争议状态
        createDispute(testOrder.getId());

        // Given: 当前用户是买家
        SecurityUtils.setCurrentUser(testOrder.getBuyerId());

        // When: 尝试裁决
        Result<Void> result = disputeService.resolveDispute(
            getDisputeId(),
            DisputeResolution.APPROVE_REFUND,
            "同意退款"
        );

        // Then
        assertThat(result.getCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("幂等性：重复裁决返回成功")
    void resolveDispute_AlreadyResolved_Idempotent() {
        // Given: 争议状态
        createDispute(testOrder.getId());

        // Given: 设置管理员
        SecurityUtils.setAdmin();

        // Given: 第1次裁决
        disputeService.resolveDispute(
            getDisputeId(),
            DisputeResolution.APPROVE_REFUND,
            "同意退款"
        );

        // When: 重复裁决
        Result<Void> result = disputeService.resolveDispute(
            getDisputeId(),
            DisputeResolution.APPROVE_REFUND,
            "同意退款"
        );

        // Then: 返回错误（不可重复裁决）
        assertThat(result.getCode()).isEqualTo(400);
    }

    // ========== 辅助方法 ==========

    private Long getDisputeId() {
        return disputeMapper.selectByOrderId(testOrder.getId()).get(0).getId();
    }

    private void createDispute(Long orderId) {
        // 创建争议记录
        orderService.applyRefund(orderId, "不想要了");
        disputeService.rejectRefund(orderId, "第1次驳回");
        orderService.applyRefund(orderId, "还是有问题");
        disputeService.rejectRefund(orderId, "第2次驳回，触发争议");

        // 验证争议已创建
        assertThat(orderMapper.selectById(orderId).getStatus()).isEqualTo(OrderStatus.DISPUTED);
    }
}
```

---

### 🔴 完成标准（超详细清单）

#### A. 功能完成标准
- [ ] **争议触发**
  - [ ] 2次驳回自动触发
  - [ ] 计数管理正确
  - [ ] 状态转换正确
- [ ] **争议裁决**
  - [ ] 管理员裁决接口
  - [ ] 支持买家/卖家两种结果
  - [ ] 裁决后计数清零

#### B. 质量标准
- [ ] **测试覆盖**
  - [ ] 单元测试覆盖率 > 85%
  - [ ] 包含争议触发测试
  - [ ] 包含裁决结果测试
  - [ ] 包含权限测试

---

### 阶段7: 评价模块 (REVIEW)

---

#### REVIEW-01: 买家评价

**目标**: 实现买家评价功能

**后端任务**:
1. Entity: Review
2. Mapper: ReviewMapper + ReviewMapper.xml
3. Service: ReviewService
   - createReview(): 创建评价
   - getReviewsByGoods(): 商品评价列表
   - getMyReviews(): 我的评价
4. Controller: ReviewController

**代码审查要点**:
- [ ] 订单完成后30天内可评价
- [ ] UNIQUE(order_item_id)约束
- [ ] 已评价不可修改
- [ ] 退款评价标记is_refunded=1
- [ ] 评价图片不限制数量

**单元测试要点**:
- [ ] 订单项唯一评价约束
- [ ] 评价时限校验（30天）
- [ ] 重复评价返回40901
- [ ] is_refunded标记正确

**完成标准**:
- [ ] 评价创建功能完成
- [ ] 评价查询功能完成
- [ ] 单元测试覆盖率 > 80%
- [ ] 代码审查通过
- [ ] 前端评价表单页面完成

---

#### REVIEW-02: 卖家回复评价

**目标**: 实现卖家回复评价功能

**后端任务**:
1. Service: ReviewService扩展
   - replyReview(): 卖家回复

**代码审查要点**:
- [ ] 只有卖家（order_item.seller_id）可回复
- [ ] 管理员也可回复
- [ ] 回复次数不限
- [ ] 回复时记录reply_time

**单元测试要点**:
- [ ] 权限校验正确
- [ ] 回复时间记录正确

**完成标准**:
- [ ] 卖家回复功能完成
- [ ] 单元测试覆盖率 > 80%
- [ ] 代码审查通过

---

### 阶段8: 留言模块 (MESSAGE)

---

#### MESSAGE-01: 商品留言功能

**目标**: 实现商品留言/回复功能

**后端任务**:
1. Entity: Message, MessageReply
2. Mapper: MessageMapper + MessageMapper.xml, MessageReplyMapper + MessageReplyMapper.xml
3. Service: MessageService
4. Controller: MessageController

**代码审查要点**:
- [ ] is_purchased实时JOIN计算
- [ ] 软删除（status=deleted）
- [ ] 隐藏（status=hidden）
- [ ] 管理员治理权限

**单元测试要点**:
- [ ] is_purchased计算正确
- [ ] 留言权限校验
- [ ] 状态管理正确

**完成标准**:
- [ ] 留言功能完成
- [ ] 回复功能完成
- [ ] 单元测试覆盖率 > 80%
- [ ] 代码审查通过
- [ ] 前端留言页面完成

---

### 阶段9: 文件上传模块 (FILE)

---

#### FILE-01: 文件上传功能

**目标**: 实现图片文件上传

**后端任务**:
1. Controller: FileController
2. Service: FileStorageService
   - uploadFile(): 上传文件
   - 配置白名单（jpg/png/webp）
   - 配置大小限制（20MB）
   - UUID文件名
3. 配置静态资源映射

**代码审查要点**:
- [ ] 文件类型白名单校验
- [ ] 文件大小限制20MB
- [ ] UUID文件名生成唯一
- [ ] 路径穿越防护
- [ ] 静态资源映射正确

**单元测试要点**:
- [ ] 文件类型校验正确
- [ ] 文件大小限制正确
- [ ] 路径穿越防护有效

**完成标准**:
- [ ] 文件上传功能完成
- [ ] 单元测试覆盖率 > 80%
- [ ] 代码审查通过
- [ ] 前端图片上传组件完成

---

### 阶段10: 定时任务 (SCHEDULER)

---

#### SCHEDULER-01: 订单超时取消

**目标**: 实现15分钟未支付自动取消

**难度**: ⭐⭐⭐⭐ (涉及定时任务、幂等性、并发安全)

**业务规则**:
- 订单创建后15分钟内未支付，自动取消
- 每分钟扫描一次超时订单
- 取消时释放已锁定库存
- 使用强幂等设计防止重复处理

**后端任务**:
1. 配置类: SchedulerConfig
2. 定时任务: OrderTimeoutCancelJob
   - 每分钟扫描15分钟未支付订单
   - 强幂等实现

---

### 🔴 核心逻辑详解

#### 1. 定时任务配置

```java
/**
 * 定时任务配置
 *
 * 使用Spring @Scheduled注解实现定时任务
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    /**
     * 订单超时取消定时任务
     *
     * Cron表达式: 0 */1 * * * ?
     * - 秒: 0
     * - 分: */1 (每分钟)
     * - 时: * (每小时)
     * - 日: * (每天)
     * - 月: * (每月)
     * - 周: ? (不指定)
     *
     * 执行频率: 每分钟的第0秒执行一次
     */
    @Bean
    public OrderTimeoutCancelJob orderTimeoutCancelJob() {
        return new OrderTimeoutCancelJob();
    }
}
```

#### 2. 订单超时取消任务（详细伪代码）

```java
/**
 * 订单超时取消定时任务
 *
 * 设计要点：
 * 1. 每分钟扫描一次超时订单
 * 2. 使用条件UPDATE保证强幂等性
 * 3. 批量处理避免长事务
 * 4. 异常处理防止单个订单失败影响整体
 */
@Component
@Slf4j
public class OrderTimeoutCancelJob {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    /**
     * 超时时间（分钟）
     */
    private static final int TIMEOUT_MINUTES = 15;

    /**
     * 每批处理数量
     */
    private static final int BATCH_SIZE = 100;

    /**
     * 定时执行：每分钟执行一次
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void execute() {
        long startTime = System.currentTimeMillis();

        log.info("========== 订单超时取消任务开始 ==========");

        try {
            // ========== 第1步：查询超时订单 ==========
            // 查询条件：
            // 1. status = PENDING_PAY
            // 2. created_at < NOW() - INTERVAL 15 MINUTE
            // 3. deleted = 0
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);

            List<Order> timeoutOrders = orderMapper.selectTimeoutOrders(
                OrderStatus.PENDING_PAY,
                timeoutThreshold,
                BATCH_SIZE
            );

            if (timeoutOrders.isEmpty()) {
                log.info("无超时订单需要处理");
                return;
            }

            log.info("发现 {} 个超时订单", timeoutOrders.size());

            // ========== 第2步：批量处理超时订单 ==========
            int successCount = 0;
            int failCount = 0;

            for (Order order : timeoutOrders) {
                try {
                    // 处理单个订单
                    boolean success = cancelTimeoutOrder(order);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("订单 {} 超时取消失败", order.getId(), e);
                    failCount++;
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("订单超时取消任务完成，成功: {}, 失败: {}, 耗时: {}ms",
                successCount, failCount, duration);

        } catch (Exception e) {
            log.error("订单超时取消任务执行异常", e);
        }

        log.info("========== 订单超时取消任务结束 ==========");
    }

    /**
     * 取消单个超时订单
     *
     * 强幂等设计：
     * 使用条件UPDATE (WHERE status = PENDING_PAY AND updated_at = 原值)
     * 确保订单状态未变更时才执行取消
     *
     * @param order 超时订单
     * @return 是否取消成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelTimeoutOrder(Order order) {
        log.info("处理超时订单，订单ID: {}, 订单号: {}, 创建时间: {}",
            order.getId(), order.getOrderNo(), order.getCreatedAt());

        // ========== 第1步：查询订单项 ==========
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());

        // ========== 第2步：使用条件UPDATE取消订单（强幂等）==========
        // SQL: UPDATE orders
        //      SET status = 'CANCELED',
        //          cancel_reason = 'TIMEOUT',
     * //          cancel_time = NOW(),
     * //          updated_at = NOW()
     *      WHERE id = #{orderId}
     *        AND status = 'PENDING_PAY'
     *        AND updated_at = #{updatedAt}
     *        AND deleted = 0
        //
        // 说明：
     * // 1. status = 'PENDING_PAY' 确保只有待支付订单能取消
     * // 2. updated_at = #{updatedAt} 确保订单未被修改（乐观锁）
     * // 3. 返回行数=1表示成功，=0表示订单状态已变更（幂等）

        int rows = orderMapper.cancelOrderByIdempotent(
            order.getId(),
            order.getUpdatedAt()
        );

        if (rows == 0) {
            log.warn("订单 {} 状态已变更，跳过处理", order.getId());
            return false;
        }

        log.info("订单 {} 取消成功", order.getId());

        // ========== 第3步：释放库存 ==========
        for (OrderItem item : items) {
            int releaseRows = goodsMapper.releaseStock(item.getGoodsId(), item.getQuantity());

            if (releaseRows == 0) {
                log.error("库存释放失败，商品ID: {}, 数量: {}", item.getGoodsId(), item.getQuantity());
            } else {
                log.info("库存释放成功，商品ID: {}, 数量: {}", item.getGoodsId(), item.getQuantity());
            }
        }

        // ========== 第4步：更新订单项状态 ==========
        for (OrderItem item : items) {
            item.setItemStatus(ItemStatus.CANCELED);
            item.setOrderStatus(OrderStatus.CANCELED);
            orderItemMapper.updateById(item);
        }

        return true;
    }
}
```

#### 3. Mapper方法（关键SQL）

```xml
<!-- OrderMapper.xml -->

<!-- 查询超时订单 -->
<select id="selectTimeoutOrders" resultType="Order">
    SELECT *
    FROM orders
    WHERE status = #{status}
      AND created_at < #{timeoutThreshold}
      AND deleted = 0
    ORDER BY created_at ASC
    LIMIT #{pageSize}
</select>

<!-- 说明：
  1. 查询15分钟前创建的PENDING_PAY订单
  2. 按创建时间升序（先超时先处理）
  3. 限制查询数量避免一次处理过多
-->

<!-- 幂等取消订单 -->
<update id="cancelOrderByIdempotent">
    UPDATE orders
    SET status = 'CANCELED',
        cancel_reason = 'TIMEOUT',
        cancel_time = NOW(),
        updated_at = NOW()
    WHERE id = #{orderId}
      AND status = 'PENDING_PAY'
      AND updated_at = #{updatedAt}
      AND deleted = 0
</update>

<!-- 说明：
  1. 强幂等设计：使用updated_at作为乐观锁
  2. 只有订单未被修改时才能取消
  3. 返回行数：
  4.   =1: 取消成功
  5.   =0: 订单状态已变更（已支付/已取消等）
-->
```

---

### 🔴 幂等性设计详解

#### 1. 为什么需要幂等性？

**场景描述**:
```
时刻T0: 订单创建，status=PENDING_PAY, updated_at=T0
时刻T1: 定时任务扫描到该订单
时刻T2: 买家支付订单，status=PAID, updated_at=T2
时刻T3: 定时任务尝试取消订单（使用T0的updated_at）

问题：如果不用updated_at条件，订单会被错误取消！
```

#### 2. 幂等性解决方案

**方案：使用updated_at乐观锁**

```sql
UPDATE orders
SET status = 'CANCELED',
    cancel_reason = 'TIMEOUT',
    cancel_time = NOW(),
    updated_at = NOW()
WHERE id = 123
  AND status = 'PENDING_PAY'      -- 条件1: 必须是待支付状态
  AND updated_at = '2026-01-31 10:00:00'  -- 条件2: 订单未被修改
  AND deleted = 0
```

**结果分析**:
- 如果订单在T2被支付，updated_at变为T2，WHERE条件不匹配，UPDATE返回0行
- 定时任务检测到返回0行，知道订单已变更，跳过处理

---

### 🔴 代码审查要点（超详细清单）

#### A. 定时任务配置
- [ ] **Cron表达式正确**
  - [ ] `0 */1 * * * ?` 每分钟执行
  - [ ] 时区配置正确（使用服务器时区）
- [ ] **任务注册**
  - [ ] @EnableScheduling注解
  - [ ] @Component注解注册Bean
- [ ] **执行频率**
  - [ ] 每分钟扫描一次
  - [ ] 避免频率过高导致性能问题

#### B. 查询逻辑
- [ ] **超时计算正确**
  - [ ] `created_at < NOW() - INTERVAL 15 MINUTE`
  - [ ] 使用数据库时间而非应用时间
- [ ] **查询条件完整**
  - [ ] status = PENDING_PAY
  - [ ] deleted = 0
  - [ ] 分页限制数量

#### C. 幂等性保证
- [ ] **强幂等设计**
  - [ ] 使用updated_at乐观锁
  - [ ] 条件UPDATE防止重复处理
  - [ ] 检查返回行数判断结果
- [ ] **并发安全**
  - [ ] 两个任务同时运行不会重复处理
  - [ ] 订单支付后不会被取消

#### D. 库存管理
- [ ] **库存释放**
  - [ ] 取消时释放所有库存
  - [ ] 释放数量=购买数量
- [ ] **异常处理**
  - [ ] 释放失败不影响订单取消
  - [ ] 记录错误日志便于排查

#### E. 异常处理
- [ ] **任务级别异常**
  - [ ] try-catch包裹整个任务
  - [ ] 单个订单失败不影响其他订单
- [ ] **订单级别异常**
  - [ ] 单个订单处理失败记录日志
  - [ ] 继续处理下一个订单

---

### 🔴 单元测试要点（超详细清单）

#### A. 查询逻辑测试
- [ ] **超时订单查询**
  - [ ] 14分钟订单不查询
  - [ ] 15分钟订单查询到
  - [ ] 16分钟订单查询到
- [ ] **状态过滤**
  - [ ] 只查询PENDING_PAY状态
  - [ ] PAID订单不查询
  - [ ] CANCELED订单不查询

#### B. 幂等性测试
- [ ] **正常取消**
  - [ ] 超时订单正确取消
  - [ ] 库存正确释放
- [ ] **订单已支付**
  - [ ] 支付后不被取消
  - [ ] 返回false（幂等）
- [ ] **订单已取消**
  - [ ] 重复取消返回false
  - [ ] 不重复释放库存

#### C. 并发测试
- [ ] **定时任务并发**
  - [ ] 两个任务同时运行不重复处理
- [ ] **支付与取消并发**
  - [ ] 支付完成后不会被取消

---

### 🔴 详细测试用例（可直接运行）

```java
/**
 * 订单超时取消定时任务测试
 */
@SpringBootTest
@Transactional
class OrderTimeoutCancelJobTest {

    @Autowired
    private OrderTimeoutCancelJob orderTimeoutCancelJob;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    private Order testOrder;
    private Goods testGoods;

    @BeforeEach
    void setUp() {
        // 创建测试商品（库存10）
        testGoods = new Goods();
        testGoods.setTitle("测试商品");
        testGoods.setPrice(new BigDecimal("100"));
        testGoods.setStock(10);
        goodsMapper.insert(testGoods);

        // 创建测试订单（16分钟前创建，PENDING_PAY状态）
        testOrder = new Order();
        testOrder.setOrderNo(OrderNoGenerator.generate());
        testOrder.setBuyerId(1L);
        testOrder.setSellerId(2L);
        testOrder.setStatus(OrderStatus.PENDING_PAY);
        testOrder.setTotalAmount(new BigDecimal("100"));
        testOrder.setCreatedAt(LocalDateTime.now().minusMinutes(16)); // 16分钟前
        testOrder.setUpdatedAt(LocalDateTime.now().minusMinutes(16));
        orderMapper.insert(testOrder);

        // 创建订单项
        OrderItem item = new OrderItem();
        item.setOrderId(testOrder.getId());
        item.setGoodsId(testGoods.getId());
        item.setQuantity(1);
        item.setItemStatus(ItemStatus.PENDING_PAY);
        orderItemMapper.insert(item);

        // 扣减库存
        goodsMapper.deductStock(testGoods.getId(), 1);
    }

    @Test
    @DisplayName("正常场景：超时订单自动取消")
    void cancelTimeoutOrder_Success() {
        // Given: 库存从10减到9
        assertThat(goodsMapper.selectById(testGoods.getId()).getStock()).isEqualTo(9);

        // When: 执行定时任务
        orderTimeoutCancelJob.execute();

        // Then: 订单状态变为CANCELED
        Order order = orderMapper.selectById(testOrder.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order.getCancelReason()).isEqualTo("TIMEOUT");
        assertThat(order.getCancelTime()).isNotNull();

        // Then: 库存恢复到10
        assertThat(goodsMapper.selectById(testGoods.getId()).getStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("幂等性：已支付订单不会被取消")
    void cancelTimeoutOrder_AlreadyPaid_NotCanceled() {
        // Given: 订单已支付
        testOrder.setStatus(OrderStatus.PAID);
        testOrder.setPayTime(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(testOrder);

        // When: 执行定时任务
        orderTimeoutCancelJob.execute();

        // Then: 订单状态仍为PAID
        Order order = orderMapper.selectById(testOrder.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        // Then: 库存未释放（仍然是9）
        assertThat(goodsMapper.selectById(testGoods.getId()).getStock()).isEqualTo(9);
    }

    @Test
    @DisplayName("幂等性：已取消订单不会重复取消")
    void cancelTimeoutOrder_AlreadyCanceled_NotCanceled() {
        // Given: 订单已取消
        testOrder.setStatus(OrderStatus.CANCELED);
        testOrder.setCancelTime(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(testOrder);

        // Given: 库存已恢复
        goodsMapper.releaseStock(testGoods.getId(), 1);

        // When: 执行定时任务
        orderTimeoutCancelJob.execute();

        // Then: 库存仍然是10（没有重复释放变成11）
        assertThat(goodsMapper.selectById(testGoods.getId()).getStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("边界值：14分钟订单不取消")
    void cancelTimeoutOrder_14Minutes_NotCanceled() {
        // Given: 订单14分钟前创建
        testOrder.setCreatedAt(LocalDateTime.now().minusMinutes(14));
        testOrder.setUpdatedAt(LocalDateTime.now().minusMinutes(14));
        orderMapper.updateById(testOrder);

        // When: 执行定时任务
        orderTimeoutCancelJob.execute();

        // Then: 订单状态仍为PENDING_PAY
        Order order = orderMapper.selectById(testOrder.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAY);
    }

    @Test
    @DisplayName("边界值：15分钟订单取消")
    void cancelTimeoutOrder_15Minutes_Canceled() {
        // Given: 订单15分钟前创建
        testOrder.setCreatedAt(LocalDateTime.now().minusMinutes(15));
        testOrder.setUpdatedAt(LocalDateTime.now().minusMinutes(15));
        orderMapper.updateById(testOrder);

        // When: 执行定时任务
        orderTimeoutCancelJob.execute();

        // Then: 订单状态变为CANCELED
        Order order = orderMapper.selectById(testOrder.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("并发场景：支付与取消并发")
    void cancelTimeoutOrder_ConcurrentWithPayment_PaymentWins() throws InterruptedException {
        // Given: 两个线程，一个支付，一个取消
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);
        AtomicBoolean paymentWon = new AtomicBoolean(false);

        // 线程1：支付订单
        new Thread(() -> {
            try {
                startLatch.await();
                Thread.sleep(10); // 稍微延迟，确保取消线程先执行查询
                orderService.payOrder(testOrder.getId());
                paymentWon.set(true);
            } catch (Exception e) {
                // 忽略
            } finally {
                endLatch.countDown();
            }
        }).start();

        // 线程2：取消订单（定时任务）
        new Thread(() -> {
            try {
                startLatch.await();
                orderTimeoutCancelJob.cancelTimeoutOrder(testOrder);
            } catch (Exception e) {
                // 忽略
            } finally {
                endLatch.countDown();
            }
        }).start();

        startLatch.countDown();
        endLatch.await(5, TimeUnit.SECONDS);

        // Then: 支付成功，订单状态为PAID
        Order order = orderMapper.selectById(testOrder.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }
}
```

---

### 🔴 完成标准（超详细清单）

#### A. 功能完成标准
- [ ] **定时任务**
  - [ ] 每1分钟执行一次
  - [ ] 查询15分钟超时订单
  - [ ] 自动取消订单
  - [ ] 释放库存
- [ ] **幂等性**
  - [ ] 已支付订单不被取消
  - [ ] 已取消订单不重复处理
  - [ ] 使用updated_at乐观锁

#### B. 性能标准
- [ ] **执行效率**
  - [ ] 单次任务执行 < 10秒
  - [ ] 批量处理避免长事务
  - [ ] 不影响正常业务

#### C. 质量标准
- [ ] **测试覆盖**
  - [ ] 单元测试覆盖率 > 85%
  - [ ] 包含幂等性测试
  - [ ] 包含并发测试
  - [ ] 包含边界值测试

---

### 阶段11: 前端页面开发

---

#### FRONT-01: 用户认证页面

**目标**: 实现登录注册页面

**前端任务**:
1. 页面开发:
   - Login.vue (登录页)
   - Register.vue (注册页)
2. 状态管理: auth.js (Pinia)
3. API封装: auth.js
4. 表单验证: 登录/注册表单校验

**代码审查要点**:
- [ ] 表单验证规则完整
- [ ] 登录状态持久化
- [ ] Token过期自动跳转登录
- [ ] 密码强度要求
- [ ] 用户名唯一性校验

**完成标准**:
- [ ] 登录注册页面完成
- [ ] 表单验证完整
- [ ] API调用正确
- [ ] 代码审查通过

---

#### FRONT-02: 商品相关页面

**目标**: 实现商品列表和详情页面

**前端任务**:
1. 页面开发:
   - GoodsList.vue (商品列表)
   - GoodsDetail.vue (商品详情)
   - GoodsPublish.vue (发布/编辑商品)
   - GoodsManage.vue (我的商品管理)
2. 组件开发:
   - ImageUpload.vue (图片上传)
   - RichTextEditor.vue (富文本编辑器)
   - CategorySelect.vue (级联分类)

**代码审查要点**:
- [ ] 图片上传支持多图
- [ ] 分类级联选择正确
- [ ] 富文本编辑器功能
- [ ] 商品状态按钮显隐正确

**完成标准**:
- [ ] 商品页面完成
- [ ] 商品组件完成
- [ ] 代码审查通过

---

#### FRONT-03: 订单相关页面

**目标**: 实现订单全流程页面

**前端任务**:
1. 页面开发:
   - CartList.vue (购物车)
   - OrderConfirm.vue (下单确认)
   - OrderSuccess.vue (下单成功)
   - OrderList.vue (我的订单)
   - OrderDetail.vue (订单详情)

**代码审查要点**:
- [ ] 拆单展示清晰
- [ ] 订单状态按钮正确
- [ ] 支付流程完整
- [ ] 确认收货功能

**完成标准**:
- [ ] 订单页面完成
- [ ] 订单流程完整
- [ ] 代码审查通过

---

#### FRONT-04: 管理员后台页面

**目标**: 实现管理员后台页面

**前端任务**:
1. 页面开发:
   - Dashboard.vue (管理员首页)
   - UserManage.vue (用户管理)
   - GoodsAudit.vue (商品审核)
   - OrderManage.vue (订单管理)
   - RefundAudit.vue (退款审核)
   - DisputeList.vue (争议列表)
   - SystemConfig.vue (系统配置)

**代码审查要点**:
- [ ] 权限控制正确
- [ ] 数据筛选功能完整
- [ ] 审核流程完整

**完成标准**:
- [ ] 管理员页面完成
- [ ] 权限控制正确
- [ ] 代码审查通过

---

## 6. 代码审查方案

### 6.1 审查类型

#### 6.1.1 同行审查 (Peer Review)

**时机**: 每个任务完成后

**流程**:
```
1. 开发者提交代码审查申请
2. 指定审查员（至少1人）
3. 审查员进行代码审查（30-60分钟）
4. 填写审查意见并记录
5. 开发者修改问题
6. 审查员确认修改完成
```

**审查清单**:
```markdown
## 代码审查清单

### 基本信息
- [ ] 任务名称和编号
- [ ] 开发者姓名
- [ ] 审查员姓名
- [ ] 审查日期

### 代码质量
- [ ] 代码符合项目编码规范
- [ ] 命名规范合理（类名、方法名、变量名）
- [ ] 代码结构清晰，易于理解
- [ ] 无明显代码重复

### 功能完整性
- [ ] 所有需求功能都已实现
- [ ] 边界情况处理完善
- [ ] 异常处理完整

### 测试覆盖
- [ ] 单元测试覆盖率 > 80%
- [ ] 所有测试用例通过
- [ ] 测试用例覆盖正常场景和边界情况

### 安全性
- [ ] 无SQL注入风险
- [ ] 无XSS漏洞
- [ ] 权限校验正确
- [ ] 敏感数据加密存储
- [ ] 接口幂等性保证

### 性能考虑
- [ ] 数据库查询有索引支持
- [ ] 避免N+1查询
- [ ] 分页查询正确
- [ ] 大数据量查询优化

### 文档完整性
- [ ] 复杂逻辑有注释说明
- [ ] 公共API有Javadoc
- [ ] 审查意见已全部修改
```

#### 6.1.2 架构审查 (Architecture Review)

**时机**: 核心模块完成时

**审查内容**:
- [ ] 模块划分是否合理
- [ ] 模块间依赖是否清晰
- [ ] 接口定义是否规范
- [ ] 数据模型是否合理
- [ ] 是否存在循环依赖

### 6.2 审查工具

**后端工具**:
- SonarQube（代码质量检查）
- SpotBugs（潜在bug检查）
- FindBugs（安全漏洞检查）
- Checkstyle（代码规范检查）
- JaCoCo（代码复杂度检查）

**前端工具**:
- ESLint（代码规范）
- Prettier（代码格式化）
- Vue ESLint Plugin

---

## 7. 单元测试方案

### 7.1 测试框架

**后端测试框架**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
</dependency>
```

**前端测试框架**:
```json
{
  "name": "vitest",
  "scripts": {
    "test": "vitest",
    "coverage": "vitest --coverage"
  },
  "devDependencies": {
    "@vue/test-utils": "^1.0.0",
    "@vitest/coverage": "^1.0.0"
  }
}
```

### 7.2 测试分类

#### 7.2.1 单元测试

**后端单元测试**:
- Service层业务逻辑测试
- Controller层API测试（MockMvc）
- Mapper层SQL测试
- 工具类测试

**前端单元测试**:
- 组件测试（Vue Test Utils）
- Store测试（Pinia）
- API调用测试（Mock Axios）
- 工具函数测试

#### 7.2.2 集成测试

**后端集成测试**:
- API端到端测试
- 数据库集成测试
- 文件上传集成测试

**前端集成测试**:
- 页面组件集成测试
- 用户流程集成测试

### 7.3 测试用例编写规范

**命名规范**:
```
测试方法命名: {methodName}_{scenario}_{expectedResult}

示例:
- register_Success 注册成功
- register_UsernameDuplicate_Returns40901 用户名重复返回40901
- createOrder_StockInsufficient_ThrowsException 库存不足抛出异常
```

**测试结构**:
```java
@SpringBootTest
class UserServiceTest {

    @Test
    @DisplayName("注册成功 - 返回用户信息")
    void register_Success() {
        // Given: 准备测试数据

        // When: 执行被测方法

        // Then: 验证结果
    }
}
```

### 7.4 测试数据准备

**测试数据策略**:
- 使用H2内存数据库进行单元测试
- 使用Mock对象模拟依赖
- 每个测试独立准备数据（@BeforeEach）
- 测试完成后清理数据（@AfterEach）

**测试数据示例**:
```java
@BeforeEach
void setUp() {
    // 准备测试数据
    testUser = new User();
    testUser.setUsername("testuser");
    testUser.setPassword(passwordEncoder.encode("password123"));
    userMapper.insert(testUser);

    testGoods = new Goods();
    testGoods.setTitle("测试商品");
    testGoods.setStock(10);
    goodsMapper.insert(testGoods);
}

@AfterEach
void tearDown() {
    // 清理测试数据
    userMapper.deleteById(testUser.getId());
    goodsMapper.deleteById(testGoods.getId());
}
```

### 7.5 断言测试

**常用断言**:
```java
// 结果断言
assertThat(result.getCode()).isEqualTo(0);
assertThat(result.getData()).isNotNull();
assertThat(result.getMessage()).isEqualTo("ok");

// 异常断言
assertThatThrownBy(() -> userService.register(request))
    .isInstanceOf(BusinessException.class)
    .hasField("code", 40901);

// 集合断言
assertThat(result.getData())
    .extracting("username")
    .isEqualTo("testuser");

// 集合断言
assertThat(list)
    .hasSize(3)
    .allMatch(item -> item.getStatus() == Status.APPROVED);
```

---

## 8. 任务完成标准

### 8.1 代码质量标准

**编码规范**:
- [ ] 遵循阿里巴巴Java编码规范
- [ ] 类名使用帕斯卡命名法（UserService）
- [ ] 方法名使用驼峰命名法（getUserInfo）
- [ ] 常量使用全大写下划线（DEFAULT_PAGE_SIZE）
- [ ] 代码缩进为4个空格
- [ ] 行宽不超过120个字符

**注释规范**:
- [ ] 公共API必须有Javadoc注释
- [ ] 复杂业务逻辑必须有注释说明
- [ ] 所有override方法必须有@Override注解
- [ ] 接口param必须有@param注释

### 8.2 功能完成标准

**基础功能**:
- [ ] 所有CRUD操作正常
- [ ] 数据校验完整
- [ ] 异常处理完善
- [ ] 事务边界清晰

**业务逻辑**:
- [ ] 状态机转换正确
- [ ] 业务规则实现准确
- [ ] 权限校验严格
- [ ] 幂等性保证

**性能要求**:
- [ ] 商品列表查询 < 500ms
- [ ] 订单创建 < 800ms
- [ ] 订单列表查询 < 500ms

**安全性**:
- [ ] 无SQL注入风险
- [ ] 无XSS漏洞
- [ ] 认证授权正确
- [ ] 输入校验完整

### 8.3 验收标准

**代码审查**:
- [ ] 无必须修复的问题
- [ ] 无严重问题
- [ ] 无中等问题以上（可选修复）

**单元测试**:
- [ ] 测试覆盖率 > 80%
- [ ] 所有测试用例通过
- [ ] 无测试被跳过

**集成测试**:
- [ ] 核心流程可跑通
- [ ] 异常场景有处理

---

## 9. 任务执行建议

### 9.1 推荐执行顺序

```
第1周: 项目基础 + 用户认证
- INFRA-01, INFRA-02, INFRA-03
- AUTH-01

第2周: 商品模块（核心）
- GOODS-01, GOODS-02

第3周: 购物车 + 订单创建
- CART-01, ORDER-01, ORDER-02

第4周: 订单状态流转
- ORDER-03, ORDER-04

第5周: 争议 + 评价 + 留言
- DISPUTE-01, REVIEW-01, REVIEW-02, MESSAGE-01

第6周: 文件上传 + 定时任务
- FILE-01, SCHEDULER-01

第7周: 前端页面开发
- FRONT-01, FRONT-02, FRONT-03, FRONT-04

第8周: 系统测试与优化
- 集成测试
- 性能测试
- 安全测试
- Bug修复
```

### 9.2 并行开发策略

由于前后端分离，可以并行开发：

**第1-2周（后端优先）**:
- INFRA-01, INFRA-02, INFRA-03
- AUTH-01
- CART-01

**第3-4周（前后端并行）**:
- 后端: GOODS-01, ORDER-01
- 前端: FRONT-01, FRONT-02

**第5-6周（全栈并行）**:
- 所有剩余任务

---

## 10. 附录

### 10.1 开发环境要求

**后端**:
- JDK 17+
- Maven 3.8+
- IntelliJ IDEA 2023+
- MySQL 8.0+
- Postman (API测试)

**前端**:
- Node.js 18+
- VS Code
- Chrome/Edge（浏览器测试）

### 10.2 参考资料

- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [MyBatis Plus文档](https://baomidou.com/)
- [Vue 3官方文档](https://vuejs.org/)
- [Element Plus文档](https://element-plus.org/)
- [Flyway文档](https://flywaydb.org/documentation/)

### 10.3 问题追踪

在任务执行过程中遇到问题：
1. 查看需求文档（需求.md）
2. 查看任务文档（task.md）
3. 联系技术负责人

### 10.4 任务状态追踪

建议使用看板工具（如Jira/Trello）追踪任务状态：
- 待开始 (To Do)
- 进行中 (In Progress)
- 代码审查中 (In Review)
- 测试中 (Testing)
- 已完成 (Done)

---

## 附录A：任务检查清单模板

### 任务完成检查清单

**任务信息**:
- 任务编号: ___________
- 任务名称: ___________
- 开发者: ___________
- 审查者: ___________
- 完成日期: ___________

**代码提交检查**:
- [ ] 代码已提交到Git仓库
- [ ] 提交信息清晰（包含任务编号）
- [ ] 分支命名规范（feature/TASK-XXX）
- [ ] 无调试代码残留

**代码质量检查**:
- [ ] 无编译警告
- [ ] 无测试失败
- [ ] 代码规范符合
- [ ] 无TODO/FIXME残留（关键路径）

**功能完成检查**:
- [ ] 所有需求功能已实现
- [ ] API接口返回格式正确
- [ ] 前端页面交互完整
- [ ] 异常场景处理完善

**测试完成检查**:
- [ ] 单元测试覆盖率 > 80%
- [ ] 所有测试用例通过
- [ ] 测试用例覆盖边界情况
- [ ] 异常场景有测试

**代码审查完成检查**:
- [ ] 审查意见已全部处理
- [ ] 无必须修复的问题
- [ ] 审查通过确认

---

**任务批准**:
- 审查人签字: ___________
- 日期: ___________

---

**备注**: 本任务文档基于 `需求.md` v1.0 编写，如需求变更需同步更新本文档。
