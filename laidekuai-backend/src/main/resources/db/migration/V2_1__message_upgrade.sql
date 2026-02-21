-- MESSAGE-01: message compatibility upgrade + message_reply table

-- 1) Extend message table (compatibility-first, keep legacy columns)
ALTER TABLE message ADD COLUMN IF NOT EXISTS user_id BIGINT NULL;
ALTER TABLE message ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'visible';
ALTER TABLE message ADD COLUMN IF NOT EXISTS is_purchased TINYINT NOT NULL DEFAULT 0;
ALTER TABLE message ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL;
ALTER TABLE message ADD COLUMN IF NOT EXISTS updated_by BIGINT NULL;

-- 2) Minimal backfill for compatibility
UPDATE message
SET user_id = sender_id
WHERE user_id IS NULL
  AND sender_id IS NOT NULL;

UPDATE message
SET status = CASE WHEN deleted = 1 THEN 'deleted' ELSE 'visible' END
WHERE status IS NULL;

UPDATE message
SET is_purchased = 0
WHERE is_purchased IS NULL;

UPDATE message
SET updated_at = created_at
WHERE updated_at IS NULL;

UPDATE message
SET updated_by = user_id
WHERE updated_by IS NULL
  AND user_id IS NOT NULL;

-- 3) Indexes (non-destructive, keep existing indexes)
CREATE INDEX IF NOT EXISTS idx_message_user_id ON message(user_id);
CREATE INDEX IF NOT EXISTS idx_message_status ON message(status);
CREATE INDEX IF NOT EXISTS idx_message_is_purchased ON message(is_purchased);
CREATE INDEX IF NOT EXISTS idx_message_goods_status ON message(goods_id, status, is_purchased, created_at);

-- 4) Create message_reply table
CREATE TABLE IF NOT EXISTS message_reply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  message_id BIGINT NOT NULL,
  replier_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'visible',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL,
  updated_by BIGINT NULL,
  INDEX idx_message_reply_message_id (message_id),
  INDEX idx_message_reply_replier_id (replier_id),
  INDEX idx_message_reply_status (status),
  INDEX idx_message_reply_created_at (created_at)
);
