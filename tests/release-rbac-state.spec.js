import { test, expect } from '@playwright/test';
import { spawn } from 'child_process';
import path from 'path';
import http from 'http';

const script = path.resolve('deliverables/release/verify-rbac-state.ps1');

const runScript = (baseUrl, extraArgs = []) => {
  return new Promise((resolve) => {
    const child = spawn(
      'powershell',
      [
        '-NoProfile',
        '-ExecutionPolicy',
        'Bypass',
        '-File',
        script,
        '-BaseUrl',
        baseUrl,
        '-EnableStateChecks',
        'true',
        '-BuyerToken',
        'buyer-token',
        '-SellerToken',
        'seller-token',
        '-IllegalPayOrderId',
        '200',
        '-IllegalCancelOrderId',
        '201',
        '-IllegalRefundOrderId',
        '202',
        '-IllegalShipOrderItemId',
        '300',
        ...extraArgs,
      ],
      { stdio: ['ignore', 'pipe', 'pipe'] }
    );

    let stdout = '';
    let stderr = '';
    const timer = setTimeout(() => child.kill(), 30000);

    child.stdout.on('data', (chunk) => {
      stdout += chunk.toString();
    });
    child.stderr.on('data', (chunk) => {
      stderr += chunk.toString();
    });
    child.on('close', (code) => {
      clearTimeout(timer);
      resolve({ status: code, stdout, stderr });
    });
  });
};

const createServer = (invalidStateCode = false) => {
  const sockets = new Set();
  const server = http.createServer((req, res) => {
    const auth = req.headers.authorization || '';
    const isAnonymous = auth === '';

    if (req.url.startsWith('/api/goods')) {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end('{"code":0}');
      return;
    }

    const protectedPrefixes = [
      '/api/admin/disputes',
      '/api/seller/order-items/1/ship',
      '/api/admin/order-items/1/ship',
      '/api/admin/orders/1/refund/approve',
      '/api/admin/reviews/1/hide',
      '/api/admin/messages/1/hide',
    ];
    if (protectedPrefixes.some((prefix) => req.url.startsWith(prefix))) {
      if (isAnonymous) {
        res.writeHead(401, { 'Content-Type': 'application/json' });
        res.end('{"code":40101}');
      } else {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end('{"code":0}');
      }
      return;
    }

    if (auth === 'Bearer buyer-token' && req.method === 'POST' && req.url.startsWith('/api/orders/200/pay')) {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ code: invalidStateCode ? 0 : 40901 }));
      return;
    }
    if (auth === 'Bearer buyer-token' && req.method === 'POST' && req.url.startsWith('/api/orders/201/cancel')) {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end('{"code":40901}');
      return;
    }
    if (auth === 'Bearer buyer-token' && req.method === 'POST' && req.url.startsWith('/api/orders/202/refund')) {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end('{"code":40901}');
      return;
    }
    if (auth === 'Bearer seller-token' && req.method === 'POST' && req.url.startsWith('/api/seller/order-items/300/ship')) {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end('{"code":40901}');
      return;
    }

    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end('{"code":404}');
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

test('rbac + state smoke script succeeds with expected responses', async () => {
  const server = createServer(false);
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const { port } = server.address();
  const baseUrl = `http://127.0.0.1:${port}`;

  const result = await runScript(baseUrl);
  const output = `${result.stdout}${result.stderr}`;
  server.forceClose();

  expect(result.status).toBe(0);
  expect(output).toContain('RBAC_STATE_OK goods_list_public');
  expect(output).toContain('RBAC_STATE_OK illegal_pay_returns_40901');
  expect(output).toContain('RBAC_STATE_ALL_OK');
});

test('rbac + state smoke script fails when illegal state code drifts', async () => {
  const server = createServer(true);
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const { port } = server.address();
  const baseUrl = `http://127.0.0.1:${port}`;

  const result = await runScript(baseUrl);
  const output = `${result.stdout}${result.stderr}`;
  server.forceClose();

  expect(result.status).not.toBe(0);
  expect(output).toContain('RBAC_STATE_FAIL illegal_pay_returns_40901');
});
