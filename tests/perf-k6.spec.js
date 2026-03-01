import { test, expect } from "@playwright/test";
import fs from "fs";
import path from "path";

const goodsScriptPath = path.resolve("perf/k6/goods_list.js");
const orderScriptPath = path.resolve("perf/k6/order_create.js");

function validateGoodsScript(text) {
  const errors = [];
  if (!text.includes("http_req_duration: [\"p(95)<500\"]")) {
    errors.push("missing_goods_p95_threshold");
  }
  if (!text.includes("/goods?page=1&size=10")) {
    errors.push("missing_goods_endpoint");
  }
  if (!text.includes("\"goods list business code 0\"")) {
    errors.push("missing_goods_business_check");
  }
  return errors;
}

function validateOrderScript(text) {
  const errors = [];
  if (!text.includes("http_req_duration: [\"p(95)<800\"]")) {
    errors.push("missing_order_p95_threshold");
  }
  if (!text.includes("/auth/login")) {
    errors.push("missing_login_endpoint");
  }
  if (!text.includes("/orders")) {
    errors.push("missing_order_endpoint");
  }
  if (!text.includes("\"order create business code 0\"")) {
    errors.push("missing_order_business_check");
  }
  return errors;
}

test("k6 requirement scripts include expected success-path assertions", async () => {
  const goodsText = fs.readFileSync(goodsScriptPath, "utf8");
  const orderText = fs.readFileSync(orderScriptPath, "utf8");

  expect(validateGoodsScript(goodsText)).toEqual([]);
  expect(validateOrderScript(orderText)).toEqual([]);
});

test("k6 requirement validation detects threshold drift as boundary failure", async () => {
  const goodsText = fs.readFileSync(goodsScriptPath, "utf8");
  const brokenGoods = goodsText.replace("p(95)<500", "p(95)<1200");
  const orderText = fs.readFileSync(orderScriptPath, "utf8");
  const brokenOrder = orderText.replace("p(95)<800", "p(95)<1500");

  expect(validateGoodsScript(brokenGoods)).toContain("missing_goods_p95_threshold");
  expect(validateOrderScript(brokenOrder)).toContain("missing_order_p95_threshold");
});
