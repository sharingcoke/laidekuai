-- 来得快二手交易平台 - 初始化数据库脚本
-- 版本: V1.0
-- 日期: 2026-01-31

-- ========== 用户表 ==========
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'BUYER' COMMENT 'BUYER/SELLER/ADMIN',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
  nick_name VARCHAR(50),
  phone VARCHAR(20),
  avatar_url VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE INDEX idx_user_status ON user(status);
CREATE INDEX idx_user_role ON user(role);

-- ========== 分类表 ==========
CREATE TABLE category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  parent_id BIGINT COMMENT '父分类ID，NULL表示一级分类',
  level TINYINT NOT NULL DEFAULT 1 COMMENT '分类层级：1/2/3',
  sort INT DEFAULT 0 COMMENT '排序',
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE INDEX idx_category_parent_level ON category(parent_id, level);
CREATE INDEX idx_category_status ON category(status);

-- ========== 商品表 ==========
CREATE TABLE goods (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  seller_id BIGINT NOT NULL COMMENT '卖家ID',
  category_id BIGINT NOT NULL COMMENT '分类ID',
  title VARCHAR(100) NOT NULL COMMENT '商品标题',
  sub_title VARCHAR(200) COMMENT '副标题',
  price DECIMAL(12,2) NOT NULL COMMENT '价格',
  stock INT NOT NULL DEFAULT 0 COMMENT '库存',
  detail TEXT COMMENT '商品详情（富文本）',
  image_urls TEXT COMMENT '商品图片URL列表，JSON数组格式，如["url1","url2","url3"]',
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PENDING/APPROVED/REJECTED/OFFLINE',
  audit_reason VARCHAR(500) COMMENT '审核驳回原因',
  audit_by BIGINT COMMENT '审核人ID',
  audit_at DATETIME COMMENT '审核时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE INDEX idx_goods_seller_category ON goods(seller_id, category_id);
CREATE INDEX idx_goods_status_created ON goods(status, created_at);
CREATE INDEX idx_goods_category_id ON goods(category_id);

-- ========== 购物车表 ==========
CREATE TABLE cart (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  goods_id BIGINT NOT NULL COMMENT '商品ID',
  quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_cart_user_goods (user_id, goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

CREATE INDEX idx_cart_user_id ON cart(user_id);

-- ========== 收货地址表 ==========
CREATE TABLE address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
  province VARCHAR(50) COMMENT '省',
  city VARCHAR(50) COMMENT '市',
  district VARCHAR(50) COMMENT '区',
  detail VARCHAR(255) COMMENT '详细地址',
  is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认地址',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DELETED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

CREATE INDEX idx_address_user_id ON address(user_id);

-- ========== 订单表 ==========
CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(32) NOT NULL COMMENT '订单号',
  buyer_id BIGINT NOT NULL COMMENT '买家ID',
  seller_id BIGINT NOT NULL COMMENT '卖家ID',
  total_amount DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
  shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '运费（V1固定为0）',
  status VARCHAR(20) NOT NULL COMMENT 'PENDING_PAY/PAID/SHIPPED/COMPLETED/CANCELED/REFUNDING/REFUNDED/DISPUTED',
  cancel_reason VARCHAR(20) COMMENT '取消原因：TIMEOUT/USER/BUYER_REFUND/ADMIN',
  cancel_time DATETIME COMMENT '取消时间',
  dispute_time DATETIME COMMENT '争议时间',
  remark VARCHAR(255) COMMENT '订单备注',
  pay_time DATETIME COMMENT '支付时间',
  address_id BIGINT COMMENT '收货地址ID（快照）',
  receiver_name VARCHAR(50) COMMENT '收货人姓名（快照）',
  receiver_phone VARCHAR(20) COMMENT '收货人电话（快照）',
  receiver_address VARCHAR(255) COMMENT '收货地址（快照）',
  is_settled TINYINT NOT NULL DEFAULT 0 COMMENT '是否已结算：0未结算/1已结算',
  settled_time DATETIME COMMENT '结算时间',
  refund_request_count TINYINT NOT NULL DEFAULT 0 COMMENT '退款申请次数，达到2次后第3次申请触发争议',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_orders_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE INDEX idx_orders_buyer ON orders(buyer_id);
CREATE INDEX idx_orders_seller ON orders(seller_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_is_settled ON orders(is_settled);
CREATE INDEX idx_orders_status_created ON orders(status, created_at);

-- ========== 订单项表 ==========
CREATE TABLE order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL COMMENT '订单ID',
  goods_id BIGINT NOT NULL COMMENT '商品ID',
  seller_id BIGINT NOT NULL COMMENT '卖家ID',
  goods_title VARCHAR(100) NOT NULL COMMENT '商品标题（快照）',
  goods_cover VARCHAR(255) COMMENT '商品封面（快照）',
  price DECIMAL(12,2) NOT NULL COMMENT '商品单价（快照）',
  quantity INT NOT NULL COMMENT '购买数量',
  amount DECIMAL(12,2) NOT NULL COMMENT '小计金额',
  item_status VARCHAR(20) NOT NULL COMMENT 'PENDING_PAY/PAID/SHIPPED/COMPLETED/CANCELED',
  order_status VARCHAR(20) NOT NULL COMMENT '冗余订单主单状态，便于查询"待发货订单项"时排除REFUNDING/REFUNDED/DISPUTED',
  ship_company VARCHAR(50) COMMENT '物流公司',
  tracking_no VARCHAR(64) COMMENT '物流单号',
  ship_time DATETIME COMMENT '发货时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

CREATE INDEX idx_order_item_order_id ON order_item(order_id);
CREATE INDEX idx_order_item_seller_id ON order_item(seller_id);
CREATE INDEX idx_order_item_status ON order_item(item_status);
CREATE INDEX idx_order_item_order_status ON order_item(order_status);

-- ========== 争议表 ==========
CREATE TABLE dispute (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL COMMENT '订单ID',
  buyer_id BIGINT NOT NULL COMMENT '买家ID',
  seller_id BIGINT NOT NULL COMMENT '卖家ID',
  reason VARCHAR(500) COMMENT '争议原因',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RESOLVED',
  resolution VARCHAR(20) COMMENT '裁决结果：APPROVE_REFUND/REJECT_REFUND',
  remark VARCHAR(500) COMMENT '裁决备注',
  resolver_id BIGINT COMMENT '裁决人ID（管理员）',
  resolve_time DATETIME COMMENT '裁决时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='争议表';

CREATE INDEX idx_dispute_order_id ON dispute(order_id);
CREATE INDEX idx_dispute_status ON dispute(status);
CREATE INDEX idx_dispute_created_at ON dispute(created_at);

-- ========== 审计日志表 ==========
CREATE TABLE audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL COMMENT '订单ID',
  action VARCHAR(50) NOT NULL COMMENT '操作类型：REFUND_APPROVE/REFUND_REJECT/SHIP_FOR_GOODS等',
  operator_id BIGINT NOT NULL COMMENT '操作人ID',
  operator_role VARCHAR(20) NOT NULL COMMENT '操作人角色：BUYER/SELLER/ADMIN',
  reason VARCHAR(255) COMMENT '操作原因',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

CREATE INDEX idx_audit_log_order_id ON audit_log(order_id);

-- ========== 收藏表 ==========
CREATE TABLE favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  goods_id BIGINT NOT NULL COMMENT '商品ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_favorite_user_goods (user_id, goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- ========== 留言表 ==========
CREATE TABLE message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  goods_id BIGINT NOT NULL COMMENT '商品ID',
  user_id BIGINT NOT NULL COMMENT '留言用户ID',
  content TEXT NOT NULL COMMENT '留言内容',
  is_purchased TINYINT NOT NULL DEFAULT 0 COMMENT '是否已购买该商品（实时JOIN计算）',
  status VARCHAR(20) NOT NULL DEFAULT 'visible' COMMENT 'visible/hidden/deleted',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品留言表';

CREATE INDEX idx_message_goods_id ON message(goods_id);
CREATE INDEX idx_message_user_id ON message(user_id);
CREATE INDEX idx_message_status ON message(status);
CREATE INDEX idx_message_is_purchased ON message(is_purchased);
CREATE INDEX idx_message_created_at ON message(created_at);

-- ========== 留言回复表 ==========
CREATE TABLE message_reply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  message_id BIGINT NOT NULL COMMENT '留言ID',
  replier_id BIGINT NOT NULL COMMENT '回复人ID',
  content TEXT NOT NULL COMMENT '回复内容',
  status VARCHAR(20) NOT NULL DEFAULT 'visible' COMMENT 'visible/hidden/deleted',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言回复表';

CREATE INDEX idx_message_reply_message_id ON message_reply(message_id);
CREATE INDEX idx_message_reply_replier_id ON message_reply(replier_id);
CREATE INDEX idx_message_reply_status ON message_reply(status);
CREATE INDEX idx_message_reply_created_at ON message_reply(created_at);

-- ========== 评价表 ==========
CREATE TABLE review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL COMMENT '订单ID',
  order_item_id BIGINT NOT NULL COMMENT '订单项ID',
  goods_id BIGINT NOT NULL COMMENT '商品ID',
  buyer_id BIGINT NOT NULL COMMENT '买家ID',
  is_refunded TINYINT NOT NULL DEFAULT 0 COMMENT '是否退款评价（不计入评分）',
  status VARCHAR(20) NOT NULL DEFAULT 'visible' COMMENT 'visible/hidden',
  rating TINYINT NOT NULL COMMENT '评分1-5',
  content TEXT COMMENT '评价内容',
  images TEXT COMMENT '评价图片URL列表，JSON数组',
  seller_reply TEXT COMMENT '卖家回复',
  reply_time DATETIME COMMENT '回复时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_review_order_item (order_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

CREATE INDEX idx_review_order_id ON review(order_id);
CREATE INDEX idx_review_goods_id ON review(goods_id);
CREATE INDEX idx_review_buyer_id ON review(buyer_id);
CREATE INDEX idx_review_created_at ON review(created_at);
CREATE INDEX idx_review_status ON review(status);

-- ========== 公告表 ==========
CREATE TABLE notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL COMMENT '公告标题',
  content TEXT NOT NULL COMMENT '公告内容',
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/OFFLINE',
  published_at DATETIME COMMENT '发布时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

CREATE INDEX idx_notice_status ON notice(status);
CREATE INDEX idx_notice_published_at ON notice(published_at);

-- ========== 初始化管理员账号 ==========
INSERT INTO user (username, password_hash, role, nick_name) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', '系统管理员');
-- 密码为: admin123 (BCrypt加密)

-- ========== 初始化分类数据 ==========
INSERT INTO category (name, parent_id, level, sort, status) VALUES
('数码产品', NULL, 1, 1, 'ENABLED'),
('服装鞋包', NULL, 1, 2, 'ENABLED'),
('图书文具', NULL, 1, 3, 'ENABLED'),
('手机', 1, 2, 1, 'ENABLED'),
('电脑', 1, 2, 2, 'ENABLED'),
('平板', 1, 2, 3, 'ENABLED'),
('男装', 2, 2, 1, 'ENABLED'),
('女装', 2, 2, 2, 'ENABLED'),
('鞋靴', 2, 2, 3, 'ENABLED');
