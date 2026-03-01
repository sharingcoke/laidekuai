param(
  [string]$BaseUrl = "http://127.0.0.1:9090",
  [string]$ApiPrefix = "/api",
  [string]$AdminToken = ""
)

$ErrorActionPreference = "Stop"

function Invoke-Check {
  param(
    [string]$Name,
    [string]$Url,
    [int[]]$ExpectedStatus,
    [hashtable]$Headers = @{}
  )
  try {
    $response = Invoke-WebRequest -UseBasicParsing -Method Get -Uri $Url -Headers $Headers -TimeoutSec 5
    $status = [int]$response.StatusCode
  } catch {
    if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
      $status = [int]$_.Exception.Response.StatusCode
    } else {
      Write-Host ("SMOKE_FAIL " + $Name + " request_error=" + $_.Exception.Message)
      return $false
    }
  }

  if ($ExpectedStatus -contains $status) {
    Write-Host ("SMOKE_OK " + $Name + " status=" + $status)
    return $true
  }

  Write-Host ("SMOKE_FAIL " + $Name + " expected=" + ($ExpectedStatus -join "/") + " actual=" + $status)
  return $false
}

$base = $BaseUrl.TrimEnd("/")
$prefix = $ApiPrefix
if (-not $prefix.StartsWith("/")) {
  $prefix = "/" + $prefix
}

$ok = $true
$ok = (Invoke-Check -Name "goods_list" -Url ($base + $prefix + "/goods?page=1&size=1") -ExpectedStatus @(200)) -and $ok

$adminHeaders = @{}
if ($AdminToken -ne "") {
  $adminHeaders["Authorization"] = "Bearer " + $AdminToken
  $ok = (Invoke-Check -Name "scheduler_metrics_admin" -Url ($base + $prefix + "/admin/system/metrics/scheduler") -ExpectedStatus @(200) -Headers $adminHeaders) -and $ok
} else {
  $ok = (Invoke-Check -Name "scheduler_metrics_anon" -Url ($base + $prefix + "/admin/system/metrics/scheduler") -ExpectedStatus @(401, 403)) -and $ok
}

if ($ok) {
  Write-Host "SMOKE_ALL_OK"
  exit 0
}

exit 1
