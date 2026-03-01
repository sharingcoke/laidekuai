import { test, expect } from '@playwright/test';
import { spawn } from 'child_process';
import path from 'path';
import http from 'http';

const script = path.resolve('deliverables/release/verify-smoke.ps1');

const runScript = (baseUrl) => {
  return new Promise((resolve) => {
    const child = spawn(
    'powershell',
    ['-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', script, '-BaseUrl', baseUrl],
      { stdio: ['ignore', 'pipe', 'pipe'] }
    );

    let stdout = '';
    let stderr = '';
    let finished = false;
    const timer = setTimeout(() => {
      if (!finished) {
        child.kill();
      }
    }, 30000);

    child.stdout.on('data', (chunk) => {
      stdout += chunk.toString();
    });
    child.stderr.on('data', (chunk) => {
      stderr += chunk.toString();
    });

    child.on('close', (code) => {
      finished = true;
      clearTimeout(timer);
      resolve({ status: code, stdout, stderr });
    });
  });
};

const createServer = (goodsStatus) => {
  const sockets = new Set();
  const server = http.createServer((req, res) => {
    if (req.url.startsWith('/api/goods')) {
      res.writeHead(goodsStatus, { 'Content-Type': 'application/json' });
      res.end('{}');
      return;
    }
    if (req.url.startsWith('/api/admin/system/metrics/scheduler')) {
      res.writeHead(401, { 'Content-Type': 'application/json' });
      res.end('{}');
      return;
    }
    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end('{}');
  });
  server.on('connection', (socket) => {
    sockets.add(socket);
    socket.on('close', () => sockets.delete(socket));
  });
  server.forceClose = () => {
    for (const socket of sockets) {
      socket.destroy();
    }
    server.close();
  };
  return server;
};

test('smoke script success with public 200 and admin 401', async () => {
  const server = createServer(200);
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const address = server.address();
  const baseUrl = `http://127.0.0.1:${address.port}`;

  const result = await runScript(baseUrl);
  const output = `${result.stdout}${result.stderr}`;

  server.forceClose();

  expect(result.status).toBe(0);
  expect(output).toContain('SMOKE_OK goods_list');
  expect(output).toContain('SMOKE_OK scheduler_metrics_anon');
  expect(output).toContain('SMOKE_ALL_OK');
});

test('smoke script fails when goods endpoint is unhealthy', async () => {
  const server = createServer(500);
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const address = server.address();
  const baseUrl = `http://127.0.0.1:${address.port}`;

  const result = await runScript(baseUrl);
  const output = `${result.stdout}${result.stderr}`;

  server.forceClose();

  expect(result.status).not.toBe(0);
  expect(output).toContain('SMOKE_FAIL goods_list');
});
