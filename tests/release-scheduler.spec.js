import { test, expect } from "@playwright/test";
import { spawn } from "child_process";
import http from "http";
import path from "path";

const script = path.resolve("deliverables/release/verify-scheduler-runtime.ps1");

function runScript(baseUrl) {
  return new Promise((resolve) => {
    const child = spawn(
      "powershell",
      [
        "-NoProfile",
        "-ExecutionPolicy",
        "Bypass",
        "-File",
        script,
        "-BaseUrl",
        baseUrl,
        "-AdminToken",
        "admin-token",
        "-WaitSeconds",
        "1",
        "-CheckIncrement",
        "true",
      ],
      { stdio: ["ignore", "pipe", "pipe"] }
    );

    let stdout = "";
    let stderr = "";
    const timer = setTimeout(() => child.kill(), 30000);
    child.stdout.on("data", (chunk) => {
      stdout += chunk.toString();
    });
    child.stderr.on("data", (chunk) => {
      stderr += chunk.toString();
    });
    child.on("close", (code) => {
      clearTimeout(timer);
      resolve({ status: code, stdout, stderr });
    });
  });
}

function createSchedulerServer(stable) {
  let callCount = 0;
  const sockets = new Set();
  const server = http.createServer((req, res) => {
    if (req.url === "/api/admin/system/metrics/scheduler") {
      const auth = req.headers.authorization || "";
      if (auth !== "Bearer admin-token") {
        res.writeHead(401, { "Content-Type": "application/json" });
        res.end('{"code":40101}');
        return;
      }

      callCount += 1;
      const totalRuns = stable ? 10 : callCount === 1 ? 10 : 11;
      const body = JSON.stringify({
        code: 0,
        data: {
          totalRuns,
          totalScanned: 100,
          totalCanceled: 5,
          lastRunAt: "2026-03-01 12:00:00",
          lastDurationMs: 45,
          lastScanned: 2,
          lastCanceled: 1,
        },
      });
      res.writeHead(200, { "Content-Type": "application/json" });
      res.end(body);
      return;
    }

    res.writeHead(404, { "Content-Type": "application/json" });
    res.end('{"code":40401}');
  });

  server.on("connection", (socket) => {
    sockets.add(socket);
    socket.on("close", () => sockets.delete(socket));
  });
  server.forceClose = () => {
    for (const socket of sockets) {
      socket.destroy();
    }
    server.close();
  };
  return server;
}

test("scheduler guard succeeds when metrics runs increment", async () => {
  const server = createSchedulerServer(false);
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const { port } = server.address();
  const result = await runScript(`http://127.0.0.1:${port}`);
  const output = `${result.stdout}${result.stderr}`;
  server.forceClose();

  expect(result.status).toBe(0);
  expect(output).toContain("SCHEDULER_GUARD_OK totalRuns_incremented");
  expect(output).toContain("SCHEDULER_GUARD_ALL_OK");
});

test("scheduler guard fails when metrics runs do not increment", async () => {
  const server = createSchedulerServer(true);
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const { port } = server.address();
  const result = await runScript(`http://127.0.0.1:${port}`);
  const output = `${result.stdout}${result.stderr}`;
  server.forceClose();

  expect(result.status).not.toBe(0);
  expect(output).toContain("SCHEDULER_GUARD_FAIL totalRuns_not_incremented");
});
