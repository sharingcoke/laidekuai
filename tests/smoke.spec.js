import { test, expect } from '@playwright/test';

const baseURL = process.env.BASE_URL || 'http://localhost:5173';

test.describe('smoke', () => {
  test('logged-in user can access orders list', async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem('token', 'token-123');
      localStorage.setItem('user', JSON.stringify({ id: 1, role: 'BUYER', nickName: '测试用户' }));
    });

    await page.route(/\/api\/orders(\?|$)/, async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, message: 'ok', data: { records: [], total: 0 } })
      });
    });

    await page.goto(`${baseURL}/orders`);
    await expect(page.getByText('暂无订单')).toBeVisible();
  });

  test('guest is redirected to login when accessing protected route', async ({ page }) => {
    await page.goto(`${baseURL}/orders`);
    await expect(page).toHaveURL(/\/login/);
  });
});
