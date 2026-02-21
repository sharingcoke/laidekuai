import { test, expect } from '@playwright/test';

const baseURL = process.env.BASE_URL || 'http://localhost:5173';

test.describe('message board', () => {
  test('user can view messages and reply on goods detail', async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('user', JSON.stringify({ id: 99, role: 'USER', nickName: 'buyer' }));
    });

    let replied = false;

    await page.route('**/*', async route => {
      const url = new URL(route.request().url());
      if (!url.pathname.startsWith('/api/')) {
        return route.continue();
      }

      if (url.pathname === '/api/goods/1') {
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            code: 0,
            data: {
              id: 1,
              title: '测试商品',
              subTitle: '留言回归',
              sellerId: 20,
              sellerName: '卖家A',
              price: 99,
              stock: 3,
              status: 'ON_SHELF',
              imageUrls: '[]',
              detail: '<p>detail</p>'
            }
          })
        });
      }

      if (url.pathname === '/api/reviews/goods/1') {
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ code: 0, data: { records: [], total: 0 } })
        });
      }

      if (url.pathname === '/api/reviews/goods/1/rating') {
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ code: 0, data: 0 })
        });
      }

      if (url.pathname === '/api/goods/1/messages') {
        const replies = replied
          ? [{ id: 2001, senderId: 99, senderName: 'buyer', content: '谢谢', createdAt: '2026-02-21 10:00:00' }]
          : [];
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            code: 0,
            data: {
              records: [
                {
                  id: 1001,
                  goodsId: 1,
                  senderId: 66,
                  senderName: '用户66',
                  content: '还有库存吗？',
                  isPurchased: 1,
                  createdAt: '2026-02-21 09:30:00',
                  replies
                }
              ],
              total: 1,
              page: 1,
              size: 10
            }
          })
        });
      }

      if (url.pathname === '/api/messages' && route.request().method() === 'POST') {
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ code: 0, data: { message_id: 3001 } })
        });
      }

      if (url.pathname === '/api/messages/1001/replies' && route.request().method() === 'POST') {
        replied = true;
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ code: 0, data: { reply_id: 2001 } })
        });
      }

      return route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, data: null })
      });
    });

    await page.goto(`${baseURL}/goods/1`, { waitUntil: 'domcontentloaded' });
    await expect(page.locator('.detail-message-section')).toBeVisible({ timeout: 15000 });

    await expect(page.locator('.message-card')).toHaveCount(1);
    await page.locator('.message-reply-form textarea').first().fill('谢谢');
    await page.locator('.message-reply-form button').first().click();

    await expect(page.locator('.reply-content')).toContainText('谢谢');
  });
});
