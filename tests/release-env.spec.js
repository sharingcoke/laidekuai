import { test, expect } from '@playwright/test';
import { spawnSync } from 'child_process';
import path from 'path';

const script = path.resolve('deliverables/release/check-env.ps1');

const runScript = (args) => {
  return spawnSync(
    'powershell',
    ['-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', script, ...args],
    { encoding: 'utf8' }
  );
};

test('env check succeeds without k6 requirement', async () => {
  const result = runScript(['-RequireK6', 'false']);
  const output = `${result.stdout}${result.stderr}`;

  expect(result.status).toBe(0);
  expect(output).toContain('ENV_CHECK_OK');
});

test('env check fails when required command is missing', async () => {
  const result = runScript(['-RequireK6', 'false', '-ExtraRequired', 'definitely_missing_cmd_123']);
  const output = `${result.stdout}${result.stderr}`;

  expect(result.status).not.toBe(0);
  expect(output).toContain('ENV_CHECK_FAIL');
  expect(output).toContain('definitely_missing_cmd_123');
});
