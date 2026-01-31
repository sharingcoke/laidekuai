
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'BUYER',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  nick_name VARCHAR(50),
  phone VARCHAR(20),
  avatar_url VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_user_status ON user(status);
CREATE INDEX idx_user_role ON user(role);

CREATE TABLE category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  parent_id BIGINT,
  level TINYINT NOT NULL DEFAULT 1,
  sort INT DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_category_parent_level ON category(parent_id, level);
CREATE INDEX idx_category_status ON category(status);

CREATE TABLE goods (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  seller_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  sub_title VARCHAR(200),
  price DECIMAL(12,2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  detail TEXT,
  image_urls TEXT,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  audit_reason VARCHAR(500),
  audit_by BIGINT,
  audit_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_goods_seller_category ON goods(seller_id, category_id);
CREATE INDEX idx_goods_status_created ON goods(status, created_at);
CREATE INDEX idx_goods_category_id ON goods(category_id);

CREATE TABLE cart (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goods_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_cart_user_goods (user_id, goods_id)
);

CREATE INDEX idx_cart_user_id ON cart(user_id);

CREATE TABLE address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  receiver_name VARCHAR(50) NOT NULL,
  receiver_phone VARCHAR(20) NOT NULL,
  province VARCHAR(50),
  city VARCHAR(50),
  district VARCHAR(50),
  detail VARCHAR(255),
  is_default TINYINT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_address_user_id ON address(user_id);

CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(32) NOT NULL,
  buyer_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL,
  shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL,
  cancel_reason VARCHAR(20),
  cancel_time DATETIME,
  dispute_time DATETIME,
  remark VARCHAR(255),
  pay_time DATETIME,
  address_id BIGINT,
  receiver_name VARCHAR(50),
  receiver_phone VARCHAR(20),
  receiver_address VARCHAR(255),
  is_settled TINYINT NOT NULL DEFAULT 0,
  settled_time DATETIME,
  refund_request_count TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_orders_order_no (order_no)
);

CREATE INDEX idx_orders_buyer ON orders(buyer_id);
CREATE INDEX idx_orders_seller ON orders(seller_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_is_settled ON orders(is_settled);
CREATE INDEX idx_orders_status_created ON orders(status, created_at);

CREATE TABLE order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  goods_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  goods_title VARCHAR(100) NOT NULL,
  goods_cover VARCHAR(255),
  price DECIMAL(12,2) NOT NULL,
  quantity INT NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  item_status VARCHAR(20) NOT NULL,
  order_status VARCHAR(20) NOT NULL,
  ship_company VARCHAR(50),
  tracking_no VARCHAR(64),
  ship_time DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_order_item_order_id ON order_item(order_id);
CREATE INDEX idx_order_item_seller_id ON order_item(seller_id);
CREATE INDEX idx_order_item_status ON order_item(item_status);
CREATE INDEX idx_order_item_order_status ON order_item(order_status);

CREATE TABLE dispute (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  buyer_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  reason VARCHAR(500),
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  resolution VARCHAR(20),
  remark VARCHAR(500),
  resolver_id BIGINT,
  resolve_time DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_dispute_order_id ON dispute(order_id);
CREATE INDEX idx_dispute_status ON dispute(status);
CREATE INDEX idx_dispute_created_at ON dispute(created_at);

CREATE TABLE audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  action VARCHAR(50) NOT NULL,
  operator_id BIGINT NOT NULL,
  operator_role VARCHAR(20) NOT NULL,
  reason VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_order_id ON audit_log(order_id);

CREATE TABLE favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goods_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_favorite_user_goods (user_id, goods_id)
);

CREATE TABLE message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  goods_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  is_purchased TINYINT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'visible',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_message_goods_id ON message(goods_id);
CREATE INDEX idx_message_user_id ON message(user_id);
CREATE INDEX idx_message_status ON message(status);
CREATE INDEX idx_message_is_purchased ON message(is_purchased);
CREATE INDEX idx_message_created_at ON message(created_at);

CREATE TABLE message_reply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  message_id BIGINT NOT NULL,
  replier_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'visible',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_message_reply_message_id ON message_reply(message_id);
CREATE INDEX idx_message_reply_replier_id ON message_reply(replier_id);
CREATE INDEX idx_message_reply_status ON message_reply(status);
CREATE INDEX idx_message_reply_created_at ON message_reply(created_at);

CREATE TABLE review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  order_item_id BIGINT NOT NULL,
  goods_id BIGINT NOT NULL,
  buyer_id BIGINT NOT NULL,
  is_refunded TINYINT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'visible',
  rating TINYINT NOT NULL,
  content TEXT,
  images TEXT,
  seller_reply TEXT,
  reply_time DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_review_order_item (order_item_id)
);

CREATE INDEX idx_review_order_id ON review(order_id);
CREATE INDEX idx_review_goods_id ON review(goods_id);
CREATE INDEX idx_review_buyer_id ON review(buyer_id);
CREATE INDEX idx_review_created_at ON review(created_at);
CREATE INDEX idx_review_status ON review(status);

CREATE TABLE notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  published_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_notice_status ON notice(status);
CREATE INDEX idx_notice_published_at ON notice(published_at);

INSERT INTO user (username, password_hash, role, nick_name) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', '系统管理员');

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
