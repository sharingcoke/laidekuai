param(
  [string]$BaseUrl = "http://127.0.0.1:9090",
  [string]$ApiPrefix = "/api",
  [string]$AdminToken = "",
  [int]$WaitSeconds = 65,
  [string]$CheckIncrement = "true"
)

$ErrorActionPreference = "Stop"

function Parse-BoolFlag {
  param(
    [string]$Name,
    [string]$Value
  )
  if ($Value -match "^(true|1)$") {
    return $true
  }
  if ($Value -match "^(false|0)$") {
    return $false
  }
  Write-Host ("SCHEDULER_GUARD_FAIL invalid_" + $Name)
  exit 1
}

function Get-MetricsSnapshot {
  param(
    [string]$Url,
    [string]$Token
  )
  if ([string]::IsNullOrWhiteSpace($Token)) {
    Write-Host "SCHEDULER_GUARD_FAIL missing_admin_token"
    exit 1
  }
  $headers = @{ Authorization = "Bearer " + $Token }
  try {
    $resp = Invoke-WebRequest -UseBasicParsing -Method Get -Uri $Url -Headers $headers -TimeoutSec 10
    if ([int]$resp.StatusCode -ne 200) {
      Write-Host ("SCHEDULER_GUARD_FAIL metrics_status=" + $resp.StatusCode)
      exit 1
    }
    $json = $resp.Content | ConvertFrom-Json
    if ([int]$json.code -ne 0 -or $null -eq $json.data) {
      Write-Host ("SCHEDULER_GUARD_FAIL metrics_business_code=" + $json.code)
      exit 1
    }
    $requiredFields = @("totalRuns", "totalScanned", "totalCanceled", "lastRunAt", "lastDurationMs", "lastScanned", "lastCanceled")
    foreach ($field in $requiredFields) {
      if (-not $json.data.PSObject.Properties.Name.Contains($field)) {
        Write-Host ("SCHEDULER_GUARD_FAIL metrics_field_missing=" + $field)
        exit 1
      }
    }
    return $json.data
  } catch {
    Write-Host ("SCHEDULER_GUARD_FAIL request_error=" + $_.Exception.Message)
    exit 1
  }
}

$checkIncrementFlag = Parse-BoolFlag -Name "CheckIncrement" -Value $CheckIncrement
$base = $BaseUrl.TrimEnd("/")
$prefix = $ApiPrefix
if (-not $prefix.StartsWith("/")) {
  $prefix = "/" + $prefix
}
$url = $base + $prefix + "/admin/system/metrics/scheduler"

$first = Get-MetricsSnapshot -Url $url -Token $AdminToken
Write-Host ("SCHEDULER_GUARD_OK snapshot_totalRuns=" + [int64]$first.totalRuns)

if (-not $checkIncrementFlag) {
  Write-Host "SCHEDULER_GUARD_ALL_OK"
  exit 0
}

Start-Sleep -Seconds $WaitSeconds
$second = Get-MetricsSnapshot -Url $url -Token $AdminToken
$firstRuns = [int64]$first.totalRuns
$secondRuns = [int64]$second.totalRuns
if ($secondRuns -le $firstRuns) {
  Write-Host ("SCHEDULER_GUARD_FAIL totalRuns_not_incremented first=" + $firstRuns + " second=" + $secondRuns)
  exit 1
}

Write-Host ("SCHEDULER_GUARD_OK totalRuns_incremented first=" + $firstRuns + " second=" + $secondRuns)
Write-Host "SCHEDULER_GUARD_ALL_OK"
exit 0
