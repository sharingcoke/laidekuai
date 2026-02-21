CREATE TABLE audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT,
  action VARCHAR(50) NOT NULL,
  operator_id BIGINT NOT NULL,
  operator_role VARCHAR(20) NOT NULL,
  reason VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_audit_order_id (order_id),
  INDEX idx_audit_action (action),
  INDEX idx_audit_operator (operator_id),
  INDEX idx_audit_created_at (created_at)
);
