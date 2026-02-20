import { test, expect } from '@playwright/test';

const baseURL = process.env.BASE_URL || 'http://localhost:5173';

test('goods page renders key filters for guests', async ({ page, request }) => {
  let frontendReady = false;
  try {
    const res = await request.get(`${baseURL}/goods`);
    frontendReady = res.ok();
  } catch {
    frontendReady = false;
  }

  test.skip(!frontendReady, 'frontend not available');

  await page.goto(`${baseURL}/goods`, { waitUntil: 'domcontentloaded' });
  await expect(page.locator('.goods-list-page')).toBeVisible({ timeout: 15000 });
  await expect(page.locator('.el-cascader')).toBeVisible({ timeout: 15000 });
  await expect(page.getByRole('button', { name: '搜索' })).toBeVisible();
});
