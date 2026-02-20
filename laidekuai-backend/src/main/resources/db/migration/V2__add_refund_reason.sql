ALTER TABLE orders
  ADD COLUMN refund_reason VARCHAR(255) AFTER dispute_time;
