param(
  [string]$BaseUrl = "http://127.0.0.1:9090",
  [string]$ApiPrefix = "/api",
  [string]$EnableStateChecks = "false",
  [string]$BuyerToken = "",
  [string]$SellerToken = "",
  [string]$IllegalPayOrderId = "",
  [string]$IllegalCancelOrderId = "",
  [string]$IllegalRefundOrderId = "",
  [string]$IllegalShipOrderItemId = "",
  [string]$ProbeOrderItemId = "1",
  [string]$ProbeOrderId = "1",
  [string]$ProbeReviewId = "1",
  [string]$ProbeMessageId = "1"
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
  Write-Host ("RBAC_STATE_FAIL invalid " + $Name + " value, use true/false")
  exit 1
}

function Convert-ResponseToCode {
  param([string]$Content)
  if ([string]::IsNullOrWhiteSpace($Content)) {
    return $null
  }
  try {
    $json = $Content | ConvertFrom-Json
    if ($null -ne $json.code) {
      return [int]$json.code
    }
  } catch {
    return $null
  }
  return $null
}

function Invoke-ApiCheck {
  param(
    [string]$Name,
    [string]$Method,
    [string]$Url,
    [int[]]$ExpectedStatus,
    [int[]]$ExpectedCodes = @(),
    [hashtable]$Headers = @{},
    [string]$Body = ""
  )
  $status = 0
  $content = ""
  try {
    if ($Body -ne "") {
      $response = Invoke-WebRequest -UseBasicParsing -Method $Method -Uri $Url -Headers $Headers -ContentType "application/json" -Body $Body -TimeoutSec 8
    } else {
      $response = Invoke-WebRequest -UseBasicParsing -Method $Method -Uri $Url -Headers $Headers -TimeoutSec 8
    }
    $status = [int]$response.StatusCode
    $content = $response.Content
  } catch {
    if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
      $webResponse = $_.Exception.Response
      $status = [int]$webResponse.StatusCode
      try {
        $stream = $webResponse.GetResponseStream()
        if ($null -ne $stream) {
          $reader = New-Object System.IO.StreamReader($stream)
          $content = $reader.ReadToEnd()
          $reader.Dispose()
        }
      } catch {
        $content = ""
      }
    } else {
      Write-Host ("RBAC_STATE_FAIL " + $Name + " request_error=" + $_.Exception.Message)
      return $false
    }
  }

  if (-not ($ExpectedStatus -contains $status)) {
    Write-Host ("RBAC_STATE_FAIL " + $Name + " status expected=" + ($ExpectedStatus -join "/") + " actual=" + $status)
    return $false
  }

  if ($ExpectedCodes.Count -gt 0) {
    $code = Convert-ResponseToCode -Content $content
    if ($null -eq $code -or -not ($ExpectedCodes -contains $code)) {
      Write-Host ("RBAC_STATE_FAIL " + $Name + " business_code expected=" + ($ExpectedCodes -join "/") + " actual=" + $code)
      return $false
    }
  }

  Write-Host ("RBAC_STATE_OK " + $Name + " status=" + $status)
  return $true
}

$enableStateChecksFlag = Parse-BoolFlag -Name "EnableStateChecks" -Value $EnableStateChecks
$base = $BaseUrl.TrimEnd("/")
$prefix = $ApiPrefix
if (-not $prefix.StartsWith("/")) {
  $prefix = "/" + $prefix
}

$shipPayload = '{"shipCompany":"AUTO","trackingNo":"AUTO-TRACK-001"}'
$ok = $true

# Access control regression probes from requirement chapter 18.
$ok = (Invoke-ApiCheck -Name "goods_list_public" -Method "GET" -Url ($base + $prefix + "/goods?page=1&size=1") -ExpectedStatus @(200)) -and $ok
$ok = (Invoke-ApiCheck -Name "admin_disputes_anonymous_denied" -Method "GET" -Url ($base + $prefix + "/admin/disputes?page=1&size=1") -ExpectedStatus @(401, 403)) -and $ok
$ok = (Invoke-ApiCheck -Name "seller_ship_anonymous_denied" -Method "POST" -Url ($base + $prefix + "/seller/order-items/" + $ProbeOrderItemId + "/ship") -ExpectedStatus @(401, 403) -Body $shipPayload) -and $ok
$ok = (Invoke-ApiCheck -Name "admin_ship_anonymous_denied" -Method "POST" -Url ($base + $prefix + "/admin/order-items/" + $ProbeOrderItemId + "/ship") -ExpectedStatus @(401, 403) -Body $shipPayload) -and $ok
$ok = (Invoke-ApiCheck -Name "admin_refund_approve_anonymous_denied" -Method "POST" -Url ($base + $prefix + "/admin/orders/" + $ProbeOrderId + "/refund/approve") -ExpectedStatus @(401, 403)) -and $ok
$ok = (Invoke-ApiCheck -Name "admin_review_hide_anonymous_denied" -Method "POST" -Url ($base + $prefix + "/admin/reviews/" + $ProbeReviewId + "/hide") -ExpectedStatus @(401, 403)) -and $ok
$ok = (Invoke-ApiCheck -Name "admin_message_hide_anonymous_denied" -Method "POST" -Url ($base + $prefix + "/admin/messages/" + $ProbeMessageId + "/hide") -ExpectedStatus @(401, 403)) -and $ok

if ($enableStateChecksFlag) {
  if ([string]::IsNullOrWhiteSpace($BuyerToken) -or [string]::IsNullOrWhiteSpace($SellerToken) -or [string]::IsNullOrWhiteSpace($IllegalPayOrderId) -or [string]::IsNullOrWhiteSpace($IllegalCancelOrderId) -or [string]::IsNullOrWhiteSpace($IllegalRefundOrderId) -or [string]::IsNullOrWhiteSpace($IllegalShipOrderItemId)) {
    Write-Host "RBAC_STATE_FAIL missing required token/id args for state checks"
    exit 1
  }

  $buyerHeaders = @{ "Authorization" = "Bearer " + $BuyerToken }
  $sellerHeaders = @{ "Authorization" = "Bearer " + $SellerToken }
  $ok = (Invoke-ApiCheck -Name "illegal_pay_returns_40901" -Method "POST" -Url ($base + $prefix + "/orders/" + $IllegalPayOrderId + "/pay") -ExpectedStatus @(200, 409) -ExpectedCodes @(40901) -Headers $buyerHeaders) -and $ok
  $ok = (Invoke-ApiCheck -Name "illegal_cancel_returns_40901" -Method "POST" -Url ($base + $prefix + "/orders/" + $IllegalCancelOrderId + "/cancel?reason=SMOKE_ILLEGAL") -ExpectedStatus @(200, 409) -ExpectedCodes @(40901) -Headers $buyerHeaders) -and $ok
  $ok = (Invoke-ApiCheck -Name "illegal_refund_returns_40901" -Method "POST" -Url ($base + $prefix + "/orders/" + $IllegalRefundOrderId + "/refund?reason=SMOKE_ILLEGAL") -ExpectedStatus @(200, 409) -ExpectedCodes @(40901) -Headers $buyerHeaders) -and $ok
  $ok = (Invoke-ApiCheck -Name "illegal_ship_returns_40901" -Method "POST" -Url ($base + $prefix + "/seller/order-items/" + $IllegalShipOrderItemId + "/ship") -ExpectedStatus @(200, 409) -ExpectedCodes @(40901) -Headers $sellerHeaders -Body $shipPayload) -and $ok
}

if ($ok) {
  Write-Host "RBAC_STATE_ALL_OK"
  exit 0
}

exit 1
