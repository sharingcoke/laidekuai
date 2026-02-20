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
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_user_status (status),
  INDEX idx_user_role (role)
);

CREATE TABLE category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  parent_id BIGINT,
  level TINYINT NOT NULL DEFAULT 1,
  sort_order INT DEFAULT 0,
  icon_url VARCHAR(255),
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_parent_level (parent_id, level)
);

CREATE TABLE notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  published_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_notice_status (status),
  INDEX idx_notice_published_at (published_at)
);

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
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_address_user_id (user_id)
);

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
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_goods_seller_category (seller_id, category_id),
  INDEX idx_goods_status_created (status, created_at),
  INDEX idx_goods_category_id (category_id)
);

CREATE TABLE cart (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goods_id BIGINT NOT NULL,
  goods_title VARCHAR(100) NOT NULL,
  goods_image VARCHAR(255),
  goods_price DECIMAL(12,2) NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_cart_user_goods (user_id, goods_id),
  INDEX idx_cart_user_id (user_id)
);

CREATE TABLE favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goods_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_favorite_user_goods (user_id, goods_id),
  INDEX idx_favorite_user_id (user_id)
);

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
  UNIQUE KEY uk_orders_order_no (order_no),
  INDEX idx_orders_buyer (buyer_id),
  INDEX idx_orders_seller (seller_id),
  INDEX idx_orders_status (status),
  INDEX idx_orders_created_at (created_at),
  INDEX idx_orders_is_settled (is_settled),
  INDEX idx_orders_status_created (status, created_at)
);

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
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_order_item_order_id (order_id),
  INDEX idx_order_item_seller_id (seller_id),
  INDEX idx_order_item_status (item_status),
  INDEX idx_order_item_order_status (order_status)
);

CREATE TABLE review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  order_item_id BIGINT NOT NULL,
  goods_id BIGINT NOT NULL,
  buyer_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  rating TINYINT NOT NULL,
  content TEXT,
  images TEXT,
  is_anonymous TINYINT NOT NULL DEFAULT 0,
  reply TEXT,
  reply_time DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_review_order_item (order_item_id),
  INDEX idx_review_order_id (order_id),
  INDEX idx_review_goods_id (goods_id),
  INDEX idx_review_buyer_id (buyer_id),
  INDEX idx_review_seller_id (seller_id),
  INDEX idx_review_created_at (created_at)
);

CREATE TABLE message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  goods_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  parent_id BIGINT,
  content TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_message_goods_id (goods_id),
  INDEX idx_message_sender_id (sender_id),
  INDEX idx_message_parent_id (parent_id),
  INDEX idx_message_created_at (created_at)
);

INSERT INTO user (username, password_hash, role, nick_name) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', 'admin');

INSERT INTO category (name, parent_id, level, sort_order, status) VALUES
('Electronics', NULL, 1, 1, 'ENABLED'),
('Clothing', NULL, 1, 2, 'ENABLED'),
('Books', NULL, 1, 3, 'ENABLED'),
('Phone', 1, 2, 1, 'ENABLED'),
('Laptop', 1, 2, 2, 'ENABLED'),
('Tablet', 1, 2, 3, 'ENABLED'),
('Menswear', 2, 2, 1, 'ENABLED'),
('Womenswear', 2, 2, 2, 'ENABLED'),
('Shoes', 2, 2, 3, 'ENABLED');
