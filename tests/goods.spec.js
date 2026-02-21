import { test, expect } from '@playwright/test';

const baseURL = process.env.BASE_URL || 'http://localhost:5173';

test.describe('goods pages', () => {
  const lastMessage = (page) => page.locator('.el-message__content').last();

  test('goods list renders items', async ({ page }) => {
    await page.route(/\/api\/goods(\?|$)/, async route => {
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
                title: '二手手机',
                price: 1999,
                imageUrls: JSON.stringify(['/files/20260221/a.jpg'])
              }
            ],
            total: 1
          }
        })
      });
    });

    await page.goto(`${baseURL}/goods`);
    await expect(page.locator('.goods-card')).toHaveCount(1);
    await expect(page.locator('.goods-title')).toHaveText('二手手机');
  });

  test('goods detail renders main info', async ({ page }) => {
    await page.route(/\/api\/goods\/1(\?|$)/, async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 0,
          message: 'ok',
          data: {
            id: 1,
            title: '二手手机',
            price: 1999,
            stock: 3,
            status: 'ON_SHELF',
            sellerId: 10,
            imageUrls: JSON.stringify(['/files/20260221/a.jpg']),
            detail: '成色良好'
          }
        })
      });
    });

    await page.route('**/api/reviews/goods/1/rating', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, message: 'ok', data: { rating: 4.5, count: 0 } })
      });
    });

    await page.route('**/api/reviews/goods/1**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, message: 'ok', data: { records: [], total: 0 } })
      });
    });

    await page.route('**/api/goods/1/messages**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, message: 'ok', data: { records: [], total: 0 } })
      });
    });

    await page.goto(`${baseURL}/goods/1`);
    await expect(page.locator('h1.title')).toHaveText('二手手机');
  });

  test('publish validation and upload backfill', async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem('token', 'token-123');
      localStorage.setItem('user', JSON.stringify({ id: 1, role: 'BUYER', nickName: '测试用户' }));
    });

    await page.route('**/api/files/upload', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, message: 'ok', data: '/files/20260221/upload.jpg' })
      });
    });

    await page.goto(`${baseURL}/goods/publish`);

    await page.getByRole('button', { name: '立即发布' }).click();
    await expect(lastMessage(page)).toHaveText('请填写必填项');

    const fileInput = page.locator('.el-upload input[type="file"]').first();
    await fileInput.setInputFiles({
      name: 'demo.jpg',
      mimeType: 'image/jpeg',
      buffer: Buffer.from('test')
    });
    await page.waitForResponse('**/api/files/upload');
    await expect(page.locator('.el-upload-list__item')).toHaveCount(1);
  });
});
