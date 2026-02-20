ALTER TABLE review
  ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'visible' AFTER reply_time;
