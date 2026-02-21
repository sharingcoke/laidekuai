import { test, expect } from '@playwright/test';

const baseURL = process.env.BASE_URL || 'http://localhost:5173';

test.describe('auth pages', () => {
  const lastMessage = (page) => page.locator('.el-message__content').last();

  test.beforeEach(async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.clear();
    });
  });

  test('login validation: username length and password length', async ({ page }) => {
    let called = 0;
    await page.route('**/api/auth/login', async route => {
      called += 1;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, message: 'ok', data: {} })
      });
    });

    await page.goto(`${baseURL}/login`);

    await page.getByPlaceholder('请输入用户名').fill('ab');
    await page.getByPlaceholder('请输入密码').fill('123456');
    await page.getByRole('button', { name: '登录' }).click();
    await expect(lastMessage(page)).toHaveText('用户名长度需在3-32个字符');

    await page.getByPlaceholder('请输入用户名').fill('user123');
    await page.getByPlaceholder('请输入密码').fill('123');
    await page.getByRole('button', { name: '登录' }).click();
    await expect(lastMessage(page)).toHaveText('密码长度需在6-32个字符');

    await page.waitForTimeout(200);
    expect(called).toBe(0);
  });

  test('register validation: username, password length, confirm match', async ({ page }) => {
    let called = 0;
    await page.route('**/api/auth/register', async route => {
      called += 1;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, message: 'ok', data: {} })
      });
    });

    await page.goto(`${baseURL}/register`);

    await page.getByPlaceholder('请输入用户名（至少3个字符）').fill('ab');
    await page.getByPlaceholder('请输入密码（至少6个字符）').fill('123456');
    await page.getByPlaceholder('请再次输入密码').fill('123456');
    await page.getByRole('button', { name: '注册' }).click();
    await expect(lastMessage(page)).toHaveText('用户名至少3个字符');

    await page.getByPlaceholder('请输入用户名（至少3个字符）').fill('user123');
    await page.getByPlaceholder('请输入密码（至少6个字符）').fill('123');
    await page.getByPlaceholder('请再次输入密码').fill('123');
    await page.getByRole('button', { name: '注册' }).click();
    await expect(lastMessage(page)).toHaveText('密码长度需在6-32个字符');

    await page.getByPlaceholder('请输入密码（至少6个字符）').fill('123456');
    await page.getByPlaceholder('请再次输入密码').fill('1234567');
    await page.getByRole('button', { name: '注册' }).click();
    await expect(lastMessage(page)).toHaveText('两次密码输入不一致');

    await page.waitForTimeout(200);
    expect(called).toBe(0);
  });

  test('login success persists token and redirects', async ({ page }) => {
    await page.route('**/api/auth/login', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 0,
          message: 'ok',
          data: {
            accessToken: 'token-123',
            tokenType: 'Bearer',
            expiresIn: 3600,
            user: { id: 1, username: 'u1', role: 'BUYER', nickName: '测试用户' }
          }
        })
      });
    });

    await page.goto(`${baseURL}/login?redirect=/profile`);
    await page.getByPlaceholder('请输入用户名').fill('user1');
    await page.getByPlaceholder('请输入密码').fill('123456');
    await page.getByRole('button', { name: '登录' }).click();

    await page.waitForURL('**/profile');
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBe('token-123');
  });

  test('401 on protected route redirects to login', async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem('token', 'token-401');
      localStorage.setItem('user', JSON.stringify({ id: 1, role: 'BUYER', nickName: '测试用户' }));
    });

    await page.route('**/api/addresses', async route => {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({ code: 40101, message: '未授权' })
      });
    });

    await page.goto(`${baseURL}/addresses`);
    await page.waitForURL('**/login');
    await expect(page.getByRole('heading', { name: '登录' })).toBeVisible();
  });
});
