import { test, expect } from "@playwright/test";
import fs from "fs";
import os from "os";
import path from "path";
import { spawnSync } from "child_process";

const script = path.resolve("deliverables/release/verify-db-indexes.ps1");

function runScript(snapshotPath) {
  return spawnSync(
    "powershell",
    [
      "-NoProfile",
      "-ExecutionPolicy",
      "Bypass",
      "-File",
      script,
      "-IndexSnapshotPath",
      snapshotPath,
    ],
    { encoding: "utf8" }
  );
}

function writeSnapshot(data) {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "db-index-"));
  const file = path.join(dir, "indexes.json");
  fs.writeFileSync(file, JSON.stringify(data), "utf8");
  return { dir, file };
}

test("db index guard succeeds when required indexes are present", async () => {
  const { dir, file } = writeSnapshot({
    orders: ["idx_orders_buyer", "idx_orders_seller", "idx_orders_status_created"],
    order_item: ["idx_order_item_order_id", "idx_order_item_seller_id"],
    goods: ["idx_goods_category_id", "idx_goods_status_created"],
    dispute: ["idx_dispute_order", "idx_dispute_status"],
  });
  const result = runScript(file);
  const output = `${result.stdout}${result.stderr}`;
  fs.rmSync(dir, { recursive: true, force: true });

  expect(result.status).toBe(0);
  expect(output).toContain("DB_INDEX_OK");
});

test("db index guard fails when one required index is missing", async () => {
  const { dir, file } = writeSnapshot({
    orders: ["idx_orders_buyer", "idx_orders_seller"],
    order_item: ["idx_order_item_order_id", "idx_order_item_seller_id"],
    goods: ["idx_goods_category_id", "idx_goods_status_created"],
    dispute: ["idx_dispute_order", "idx_dispute_status"],
  });
  const result = runScript(file);
  const output = `${result.stdout}${result.stderr}`;
  fs.rmSync(dir, { recursive: true, force: true });

  expect(result.status).not.toBe(0);
  expect(output).toContain("DB_INDEX_FAIL missing=orders.idx_orders_status_created");
});
