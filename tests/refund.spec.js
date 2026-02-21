import { test, expect } from '@playwright/test';

const baseURL = process.env.BASE_URL || 'http://localhost:5173';

const setBuyer = async (page) => {
  await page.addInitScript(() => {
    localStorage.setItem('token', 'token-123');
    localStorage.setItem('user', JSON.stringify({ id: 2, role: 'BUYER', nickName: '买家' }));
  });
};

test('refund detail shows status and reason', async ({ page }) => {
  await setBuyer(page);

  await page.route('**/api/orders/1', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 0,
        message: 'ok',
        data: {
          id: 1,
          orderNo: 'NO1001',
          status: 'REFUNDING',
          refundReason: '未发货想取消',
          refundRequestCount: 1,
          updatedAt: '2026-02-22 10:00'
        }
      })
    });
  });

  await page.goto(`${baseURL}/orders/1/refund`);
  await expect(page.getByText('退款详情')).toBeVisible();
  await expect(page.getByText('退款中')).toBeVisible();
  await expect(page.getByText('未发货想取消')).toBeVisible();
});
