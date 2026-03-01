param(
  [string]$BackendConfigDir = "laidekuai-backend/src/main/resources"
)

$ErrorActionPreference = "Stop"

function Read-RequiredFile {
  param([string]$Path)
  if (-not (Test-Path -Path $Path -PathType Leaf)) {
    Write-Host ("CONFIG_GUARD_FAIL missing_file=" + $Path)
    exit 1
  }
  return Get-Content -Path $Path -Raw
}

function Extract-KeyValue {
  param(
    [string]$Text,
    [string]$Regex
  )
  $m = [regex]::Match($Text, $Regex, [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor [System.Text.RegularExpressions.RegexOptions]::Multiline)
  if (-not $m.Success) {
    return $null
  }
  return $m.Groups[1].Value.Trim()
}

$basePath = Resolve-Path $BackendConfigDir
$devPath = Join-Path $basePath "application-dev.yml"
$prodPath = Join-Path $basePath "application-prod.yml"

$devText = Read-RequiredFile -Path $devPath
$prodText = Read-RequiredFile -Path $prodPath

$devUrl = Extract-KeyValue -Text $devText -Regex "^\s*url:\s*(.+)$"
$prodUrl = Extract-KeyValue -Text $prodText -Regex "^\s*url:\s*(.+)$"

if ([string]::IsNullOrWhiteSpace($devUrl)) {
  Write-Host "CONFIG_GUARD_FAIL dev_datasource_url_missing"
  exit 1
}
if ([string]::IsNullOrWhiteSpace($prodUrl)) {
  Write-Host "CONFIG_GUARD_FAIL prod_datasource_url_missing"
  exit 1
}
if ($devUrl -eq $prodUrl) {
  Write-Host "CONFIG_GUARD_FAIL datasource_url_not_isolated"
  exit 1
}

$prodProfile = [regex]::IsMatch($prodText, "on-profile:\s*prod", [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
if (-not $prodProfile) {
  Write-Host "CONFIG_GUARD_FAIL prod_profile_binding_missing"
  exit 1
}

$prodStrictSecret = [regex]::IsMatch($prodText, "secret:\s*\$\{JWT_SECRET\}\s*$", [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor [System.Text.RegularExpressions.RegexOptions]::Multiline)
if (-not $prodStrictSecret) {
  Write-Host "CONFIG_GUARD_FAIL prod_jwt_secret_not_strict_env"
  exit 1
}

Write-Host "CONFIG_GUARD_OK"
Write-Host ("CONFIG_GUARD_OK datasource_dev=" + $devUrl)
Write-Host ("CONFIG_GUARD_OK datasource_prod=" + $prodUrl)
exit 0
