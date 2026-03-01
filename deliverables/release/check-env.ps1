param(
  [string]$RequireK6 = "true",
  [string[]]$ExtraRequired = @()
)

$ErrorActionPreference = "Stop"

function Test-CommandExists {
  param([string]$Name)
  return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

$requireK6Flag = $true
if ($RequireK6 -match "^(false|0)$") {
  $requireK6Flag = $false
} elseif ($RequireK6 -notmatch "^(true|1)$") {
  Write-Host "ENV_CHECK_FAIL invalid RequireK6 value, use true/false"
  exit 1
}

$required = @("java", "node", "npm")
if ($requireK6Flag) {
  $required += "k6"
}
if ($ExtraRequired.Count -gt 0) {
  $required += $ExtraRequired
}

$missing = @()
foreach ($cmd in $required) {
  if (-not (Test-CommandExists -Name $cmd)) {
    $missing += $cmd
  }
}

if ($missing.Count -gt 0) {
  Write-Host ("ENV_CHECK_FAIL missing: " + ($missing -join ", "))
  exit 1
}

Write-Host ("ENV_CHECK_OK required commands: " + ($required -join ", "))
exit 0
