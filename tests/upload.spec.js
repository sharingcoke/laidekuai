import { test, expect } from '@playwright/test';

test('upload returns relative file path', async ({ page }) => {
  await page.route('**/api/files/upload', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ code: 0, message: 'ok', data: '/static/files/20260221/abc.jpg' })
    });
  });

  await page.setContent(`
    <base href="http://localhost/" />
    <input id="file" type="file" />
    <button id="upload">upload</button>
    <div id="result"></div>
    <script>
      const btn = document.getElementById('upload');
      btn.addEventListener('click', async () => {
        const fileInput = document.getElementById('file');
        const form = new FormData();
        form.append('file', fileInput.files[0]);
        const res = await fetch('http://localhost/api/files/upload', { method: 'POST', body: form });
        const data = await res.json();
        document.getElementById('result').textContent = data.data || '';
      });
    </script>
  `);

  await page.setInputFiles('#file', {
    name: 'demo.jpg',
    mimeType: 'image/jpeg',
    buffer: Buffer.from('test')
  });

  await expect(page.locator('#file')).toHaveJSProperty('files.length', 1);

  await Promise.all([
    page.waitForResponse('**/api/files/upload'),
    page.click('#upload')
  ]);
  await expect(page.locator('#result')).toHaveText('/static/files/20260221/abc.jpg');
});
