ALTER TABLE review
  ADD COLUMN is_refunded TINYINT NOT NULL DEFAULT 0 AFTER is_anonymous;

CREATE INDEX idx_review_goods_refunded ON review (goods_id, is_refunded);
