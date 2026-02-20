import { test, expect } from '@playwright/test';

const baseURL = process.env.BASE_URL || 'http://localhost:5173';
const apiBase = process.env.API_BASE || 'http://localhost:9090/api';
const uniqueUser = () => `u${Date.now()}${Math.floor(Math.random() * 1000)}`;

const backendReady = async (request) => {
  try {
    const res = await request.get(`${apiBase}/categories`);
    return res.ok();
  } catch {
    return false;
  }
};

const registerAndLogin = async (request) => {
  const username = uniqueUser();
  const password = 'test1234';

  const reg = await request.post(`${apiBase}/auth/register`, {
    data: { username, password, nickName: 'test' }
  });
  if (!reg.ok()) {
    throw new Error('register failed');
  }

  const login = await request.post(`${apiBase}/auth/login`, {
    data: { username, password }
  });
  if (!login.ok()) {
    throw new Error('login failed');
  }
  const body = await login.json();
  const token = body.data.accessToken;
  return { token, user: body.data.user };
};

test.describe('Laidekuai basic flows', () => {
  test.setTimeout(60000);

  test('guest can browse goods list with filters', async ({ page }) => {
    await page.goto(`${baseURL}/goods`, { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('.goods-list-page', { timeout: 15000 });

    await expect(page.locator('.el-cascader')).toBeVisible({ timeout: 15000 });
    await expect(page.locator('input').first()).toBeVisible();

    await page.locator('.el-cascader').click();
    const firstCategory = page.locator('.el-cascader-menu__item').first();
    if (await firstCategory.count()) {
      await firstCategory.click();
    }

    const searchInput = page.locator('input').first();
    await searchInput.fill('test');
    await searchInput.press('Enter');
  });

  test('user can register, login, and access my goods', async ({ page, request }) => {
    if (!(await backendReady(request))) {
      test.skip(true, 'backend not available');
    }

    const { token, user } = await registerAndLogin(request);

    await page.goto(`${baseURL}/`, { waitUntil: 'domcontentloaded' });
    await page.evaluate(({ token, user }) => {
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
    }, { token, user });

    await page.goto(`${baseURL}/my-goods`, { waitUntil: 'domcontentloaded' });
    await expect(page.locator('.my-goods-page')).toBeVisible({ timeout: 15000 });
  });

  test('publish page shows validation on empty submit', async ({ page, request }) => {
    if (!(await backendReady(request))) {
      test.skip(true, 'backend not available');
    }

    const { token, user } = await registerAndLogin(request);

    await page.goto(`${baseURL}/`, { waitUntil: 'domcontentloaded' });
    await page.evaluate(({ token, user }) => {
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
    }, { token, user });

    await page.goto(`${baseURL}/goods/publish`, { waitUntil: 'domcontentloaded' });
    await page.waitForSelector('.publish-page', { timeout: 15000 });
    await page.locator('.publish-page .el-button--primary').first().click();
    await expect(page.locator('.el-message')).toBeVisible({ timeout: 10000 });
  });
});
