-- RELEASE-BLOCKER-01: Legacy V2 message upgrade -> V2.1 compatibility
-- Use on databases where V2 was the message upgrade (pre V2__add_refund_reason).
-- Idempotent: safe to re-run.

-- 1) If legacy V2 was "message upgrade", move it to 2.1 with current checksum.
UPDATE flyway_schema_history
SET version = '2.1',
    description = 'message upgrade',
    script = 'V2_1__message_upgrade.sql',
    checksum = 384404788
WHERE version = '2'
  AND (LOWER(description) LIKE '%message%' OR LOWER(script) LIKE '%message%');

-- 2) Ensure refund_reason exists (legacy DBs may miss V2__add_refund_reason).
ALTER TABLE orders ADD COLUMN IF NOT EXISTS refund_reason VARCHAR(255);

-- 3) Backfill flyway history for V2__add_refund_reason if missing.
INSERT INTO flyway_schema_history (
  installed_rank,
  version,
  description,
  type,
  script,
  checksum,
  installed_by,
  installed_on,
  execution_time,
  success
)
SELECT
  (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history),
  '2',
  'add refund reason',
  'SQL',
  'V2__add_refund_reason.sql',
  -447510392,
  'compat',
  CURRENT_TIMESTAMP,
  0,
  1
WHERE NOT EXISTS (
  SELECT 1 FROM flyway_schema_history
  WHERE version = '2' AND description = 'add refund reason'
);
