import { test, expect } from "@playwright/test";
import fs from "fs";
import os from "os";
import path from "path";
import { spawnSync } from "child_process";

const script = path.resolve("deliverables/release/verify-flyway-history.ps1");

function writeTempMigrationDir() {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "flyway-migrations-"));
  fs.writeFileSync(path.join(dir, "V1__init.sql"), "--", "utf8");
  fs.writeFileSync(path.join(dir, "V2_1__message_upgrade.sql"), "--", "utf8");
  fs.writeFileSync(path.join(dir, "V3__extra.sql"), "--", "utf8");
  return dir;
}

function writeSnapshot(obj) {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "flyway-snapshot-"));
  const file = path.join(dir, "history.json");
  fs.writeFileSync(file, JSON.stringify(obj), "utf8");
  return { dir, file };
}

function runScript(migrationDir, snapshotPath) {
  return spawnSync(
    "powershell",
    [
      "-NoProfile",
      "-ExecutionPolicy",
      "Bypass",
      "-File",
      script,
      "-MigrationDir",
      migrationDir,
      "-HistorySnapshotPath",
      snapshotPath,
    ],
    { encoding: "utf8" }
  );
}

test("flyway guard succeeds when all expected versions are successful", async () => {
  const migrationDir = writeTempMigrationDir();
  const { dir: snapshotDir, file: snapshotFile } = writeSnapshot({
    tableExists: true,
    rows: [
      { version: "1", success: 1 },
      { version: "2.1", success: 1 },
      { version: "3", success: 1 },
    ],
  });

  const result = runScript(migrationDir, snapshotFile);
  const output = `${result.stdout}${result.stderr}`;

  fs.rmSync(migrationDir, { recursive: true, force: true });
  fs.rmSync(snapshotDir, { recursive: true, force: true });

  expect(result.status).toBe(0);
  expect(output).toContain("FLYWAY_GUARD_OK");
});

test("flyway guard fails when one migration version is missing", async () => {
  const migrationDir = writeTempMigrationDir();
  const { dir: snapshotDir, file: snapshotFile } = writeSnapshot({
    tableExists: true,
    rows: [
      { version: "1", success: 1 },
      { version: "2.1", success: 1 },
    ],
  });

  const result = runScript(migrationDir, snapshotFile);
  const output = `${result.stdout}${result.stderr}`;

  fs.rmSync(migrationDir, { recursive: true, force: true });
  fs.rmSync(snapshotDir, { recursive: true, force: true });

  expect(result.status).not.toBe(0);
  expect(output).toContain("FLYWAY_GUARD_FAIL missing_versions=3");
});
