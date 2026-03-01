import { test, expect } from "@playwright/test";
import { spawn } from "child_process";
import http from "http";
import path from "path";

const script = path.resolve("deliverables/release/verify-upload-static.ps1");

function runScript(baseUrl, token) {
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
        "-UserToken",
        token,
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

function createServer({ invalidUploadShouldFail }) {
  const sockets = new Set();
  const server = http.createServer((req, res) => {
    if (req.method === "POST" && req.url === "/api/files/upload") {
      const auth = req.headers.authorization || "";
      if (!auth.startsWith("Bearer ")) {
        res.writeHead(401, { "Content-Type": "application/json" });
        res.end('{"code":40101}');
        return;
      }
      let body = "";
      req.on("data", (chunk) => {
        body += chunk.toString("latin1");
      });
      req.on("end", () => {
        const isInvalid = body.includes("smoke-invalid.exe");
        if (isInvalid && invalidUploadShouldFail) {
          res.writeHead(200, { "Content-Type": "application/json" });
          res.end('{"code":40001}');
          return;
        }
        if (isInvalid && !invalidUploadShouldFail) {
          res.writeHead(200, { "Content-Type": "application/json" });
          res.end('{"code":0,"data":"/static/files/20260301/invalid.exe"}');
          return;
        }
        res.writeHead(200, { "Content-Type": "application/json" });
        res.end('{"code":0,"data":"/static/files/20260301/ok.png"}');
      });
      return;
    }

    if (req.method === "GET" && req.url === "/static/files/20260301/ok.png") {
      res.writeHead(200, { "Content-Type": "image/png" });
      res.end(Buffer.from([137, 80, 78, 71]));
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

test("upload static smoke succeeds with valid upload and invalid-type rejection", async () => {
  const server = createServer({ invalidUploadShouldFail: true });
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const { port } = server.address();
  const result = await runScript(`http://127.0.0.1:${port}`, "user-token");
  const output = `${result.stdout}${result.stderr}`;
  server.forceClose();

  expect(result.status).toBe(0);
  expect(output).toContain("UPLOAD_STATIC_OK upload_and_static_access");
  expect(output).toContain("UPLOAD_STATIC_OK invalid_type_rejected");
  expect(output).toContain("UPLOAD_STATIC_ALL_OK");
});

test("upload static smoke fails when invalid type is unexpectedly accepted", async () => {
  const server = createServer({ invalidUploadShouldFail: false });
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const { port } = server.address();
  const result = await runScript(`http://127.0.0.1:${port}`, "user-token");
  const output = `${result.stdout}${result.stderr}`;
  server.forceClose();

  expect(result.status).not.toBe(0);
  expect(output).toContain("UPLOAD_STATIC_FAIL invalid_upload_business_code=");
});
