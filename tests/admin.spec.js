import { test, expect } from '@playwright/test';

const baseURL = process.env.BASE_URL || 'http://localhost:5173';

const lastMessage = (page) => page.locator('.el-message__content').last();

const setAdmin = async (page) => {
  await page.addInitScript(() => {
    localStorage.setItem('token', 'token-123');
    localStorage.setItem('user', JSON.stringify({ id: 1, role: 'ADMIN', nickName: '管理员' }));
  });
};

const setBuyer = async (page) => {
  await page.addInitScript(() => {
    localStorage.setItem('token', 'token-123');
    localStorage.setItem('user', JSON.stringify({ id: 2, role: 'BUYER', nickName: '买家' }));
  });
};

test.describe('admin pages', () => {
  test('admin can access dashboard, disputes, and system config', async ({ page }) => {
    await setAdmin(page);

    await page.route('**/api/admin/orders**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, message: 'ok', data: { records: [], total: 12 } })
      });
    });

    await page.route('**/api/goods/admin/list**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, message: 'ok', data: { records: [], total: 5 } })
      });
    });

    await page.route('**/api/admin/users**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, message: 'ok', data: { records: [], total: 3 } })
      });
    });

    await page.route('**/api/admin/disputes**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 0,
          message: 'ok',
          data: {
            records: [
              { id: 1, orderId: 'NO1001', applicantName: '张三', reason: '描述不符', status: 'DISPUTED', createdAt: '2026-02-21 10:00' }
            ],
            total: 1
          }
        })
      });
    });

    await page.goto(`${baseURL}/admin/dashboard`);
    await expect(page.getByText('订单总数')).toBeVisible();
    await expect(page.getByText('12')).toBeVisible();

    await page.goto(`${baseURL}/admin/disputes`);
    await expect(page.getByText('NO1001')).toBeVisible();

    await page.goto(`${baseURL}/admin/system`);
    await page.getByRole('button', { name: '保存设置' }).click();
    await expect(lastMessage(page)).toHaveText('系统配置保存功能暂未开放');
  });

  test('non-admin is redirected away from admin pages', async ({ page }) => {
    await setBuyer(page);

    await page.goto(`${baseURL}/admin/dashboard`);
    await expect(page.getByRole('heading', { name: '来得快', exact: true })).toBeVisible();
  });
});

