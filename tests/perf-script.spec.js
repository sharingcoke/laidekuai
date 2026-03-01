import { test, expect } from '@playwright/test';
import fs from 'fs';
import path from 'path';

const scriptPath = path.resolve('deliverables/perf/k6-core-smoke.js');

test('k6 script includes success and failure scenarios', async () => {
  const text = fs.readFileSync(scriptPath, 'utf8');

  expect(text).toContain('public_goods_success');
  expect(text).toContain('admin_unauthorized_fail');
  expect(text).toContain('goods list status is 200');
  expect(text).toContain('admin endpoint rejects anonymous');
});

test('k6 script defines baseline thresholds', async () => {
  const text = fs.readFileSync(scriptPath, 'utf8');

  expect(text).toContain("http_req_failed");
  expect(text).toContain("checks");
  expect(text).toContain("p(95)<800");
});
