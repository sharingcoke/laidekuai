import { test, expect } from '@playwright/test';

const baseURL = process.env.BASE_URL || 'http://localhost:5173';

const setAuth = async (page) => {
  await page.addInitScript(() => {
    localStorage.setItem('token', 'token-123');
    localStorage.setItem('user', JSON.stringify({ id: 1, role: 'BUYER', nickName: '测试用户' }));
  });
};

test.describe('order pages', () => {
  test('order confirm submits and success page renders', async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem('token', 'token-123');
      localStorage.setItem('user', JSON.stringify({ id: 1, role: 'BUYER', nickName: '测试用户' }));
      sessionStorage.setItem('checkoutItems', JSON.stringify([
        { goodsId: 1, quantity: 1, cartId: 11, goodsTitle: '商品A', goodsPrice: 88.0 }
      ]));
    });

    await page.route('**/api/addresses', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 0,
          message: 'ok',
          data: [
            {
              id: 1,
              receiverName: '张三',
              receiverPhone: '13800000000',
              province: '北京',
              city: '北京',
              district: '朝阳',
              detail: 'XX路',
              isDefault: 1
            }
          ]
        })
      });
    });

    await page.route(/\/api\/orders(\?|$)/, async route => {
      if (route.request().method() !== 'POST') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ code: 0, message: 'ok', data: { records: [], total: 0 } })
        });
        return;
      }
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 0,
          message: 'ok',
          data: [
            { id: 1001, orderNo: 'NO1001', sellerId: 9, sellerName: '卖家A', totalAmount: 88, status: 'PENDING_PAY' },
            { id: 1002, orderNo: 'NO1002', sellerId: 10, sellerName: '卖家B', totalAmount: 39.9, status: 'PENDING_PAY' }
          ]
        })
      });
    });

    await page.goto(`${baseURL}/order/confirm`);
    await page.getByRole('button', { name: /提交订单/ }).click();
    await expect(page.getByRole('heading', { name: '下单成功' })).toBeVisible();
    await expect(page.locator('.success-card')).toHaveCount(2);
  });

  test('order list renders actions', async ({ page }) => {
    await setAuth(page);

    await page.route(/\/api\/orders(\?|$)/, async route => {
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
                orderNo: 'NO1001',
                status: 'PENDING_PAY',
                sellerName: '卖家A',
                totalAmount: 88,
                shippingFee: 0,
                createdAt: '2026-02-21 10:00',
                items: [
                  { id: 11, goodsTitle: '商品A', goodsCover: '[]', price: 88, quantity: 1, itemStatus: 'PENDING_PAY' }
                ]
              },
              {
                id: 2,
                orderNo: 'NO1002',
                status: 'SHIPPED',
                sellerName: '卖家B',
                totalAmount: 39.9,
                shippingFee: 0,
                createdAt: '2026-02-21 11:00',
                items: [
                  { id: 12, goodsTitle: '商品B', goodsCover: '[]', price: 39.9, quantity: 1, itemStatus: 'SHIPPED' }
                ]
              }
            ],
            total: 2
          }
        })
      });
    });

    await page.goto(`${baseURL}/orders`);
    await expect(page.getByText('NO1001')).toBeVisible();
    await expect(page.getByRole('button', { name: '立即支付' })).toBeVisible();
    await expect(page.getByRole('button', { name: '确认收货' })).toBeVisible();
  });

  test('order detail renders info and logistics', async ({ page }) => {
    await setAuth(page);

    await page.route(/\/api\/orders\/101(\?|$)/, async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 0,
          message: 'ok',
          data: {
            id: 101,
            orderNo: 'NO101',
            status: 'SHIPPED',
            sellerName: '卖家C',
            totalAmount: 99,
            createdAt: '2026-02-21 12:00',
            receiverName: '张三',
            receiverPhone: '13800000000',
            receiverAddress: '北京市朝阳区XX路',
            items: [
              {
                id: 21,
                goodsTitle: '商品C',
                price: 99,
                quantity: 1,
                amount: 99,
                itemStatus: 'SHIPPED',
                shipCompany: '顺丰',
                trackingNo: 'SF123'
              }
            ]
          }
        })
      });
    });

    await page.goto(`${baseURL}/orders/101`);
    await expect(page.getByRole('heading', { name: '订单详情' })).toBeVisible();
    await expect(page.getByText('NO101')).toBeVisible();
    await expect(page.getByText('顺丰')).toBeVisible();
    await expect(page.getByRole('button', { name: '确认收货' })).toBeVisible();
  });
});
