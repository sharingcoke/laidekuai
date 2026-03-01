import { test, expect } from "@playwright/test";
import fs from "fs";
import os from "os";
import path from "path";
import { spawnSync } from "child_process";

const script = path.resolve("deliverables/release/verify-config-guard.ps1");

function runScript(configDir) {
  return spawnSync(
    "powershell",
    [
      "-NoProfile",
      "-ExecutionPolicy",
      "Bypass",
      "-File",
      script,
      "-BackendConfigDir",
      configDir,
    ],
    { encoding: "utf8" }
  );
}

function makeTempConfigDir(devText, prodText) {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "release-config-"));
  fs.writeFileSync(path.join(dir, "application-dev.yml"), devText, "utf8");
  fs.writeFileSync(path.join(dir, "application-prod.yml"), prodText, "utf8");
  return dir;
}

test("config guard succeeds when dev/prod are isolated and prod secret is strict env", async () => {
  const dev = `
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:mem:laidekuai
`;
  const prod = `
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:mysql://db-prod:3306/laidekuai
app:
  jwt:
    secret: \${JWT_SECRET}
`;
  const dir = makeTempConfigDir(dev, prod);
  const result = runScript(dir);
  const output = `${result.stdout}${result.stderr}`;
  fs.rmSync(dir, { recursive: true, force: true });

  expect(result.status).toBe(0);
  expect(output).toContain("CONFIG_GUARD_OK");
});

test("config guard fails when prod keeps default jwt secret or datasource not isolated", async () => {
  const dev = `
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:mysql://same-host:3306/laidekuai
`;
  const prod = `
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:mysql://same-host:3306/laidekuai
app:
  jwt:
    secret: \${JWT_SECRET:default-secret}
`;
  const dir = makeTempConfigDir(dev, prod);
  const result = runScript(dir);
  const output = `${result.stdout}${result.stderr}`;
  fs.rmSync(dir, { recursive: true, force: true });

  expect(result.status).not.toBe(0);
  expect(output).toContain("CONFIG_GUARD_FAIL");
});
