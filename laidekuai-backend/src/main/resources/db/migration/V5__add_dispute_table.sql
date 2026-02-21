CREATE TABLE dispute (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  applicant_id BIGINT NOT NULL,
  applicant_type VARCHAR(20) NOT NULL,
  reason VARCHAR(255) NOT NULL,
  status VARCHAR(20) NOT NULL,
  resolution VARCHAR(20),
  admin_id BIGINT,
  admin_note VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at DATETIME,
  UNIQUE KEY uk_dispute_order (order_id),
  INDEX idx_dispute_order (order_id),
  INDEX idx_dispute_status (status)
);
