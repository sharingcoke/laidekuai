import { test, expect } from '@playwright/test';

const baseURL = process.env.BASE_URL || 'http://localhost:5173';

const setBuyer = async (page) => {
  await page.addInitScript(() => {
    localStorage.setItem('token', 'token-123');
    localStorage.setItem('user', JSON.stringify({ id: 2, role: 'BUYER', nickName: '买家' }));
  });
};

test('favorite list renders items', async ({ page }) => {
  await setBuyer(page);

  await page.route('**/api/favorites**', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 0,
        message: 'ok',
        data: {
          records: [
            {
              id: 1,
              title: '二手相机',
              price: 1999,
              stock: 2,
              status: 'APPROVED',
              imageUrls: '["/static/files/20260221/camera.jpg"]'
            }
          ],
          total: 1
        }
      })
    });
  });

  await page.goto(`${baseURL}/favorites`);
  await expect(page.getByText('我的收藏')).toBeVisible();
  await expect(page.getByText('二手相机')).toBeVisible();
});
