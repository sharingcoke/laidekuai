import { test, expect } from '@playwright/test';
import fs from 'fs';
import path from 'path';

const planPath = path.resolve('docs/improvement-plan.md');

test('improvement plan marks all gap items as closed', async () => {
  const content = fs.readFileSync(planPath, 'utf8');
  const closedRows = (content.match(/\| 已关闭 \|/g) || []).length;

  expect(content).toContain('## Gap 关闭状态（2026-03-01）');
  expect(closedRows).toBeGreaterThanOrEqual(12);
});

test('improvement plan publish suggestion is conditional release', async () => {
  const content = fs.readFileSync(planPath, 'utf8');

  expect(content).toContain('结论：**有条件可发布**');
  expect(content).not.toContain('结论：**不可发布**');
});
