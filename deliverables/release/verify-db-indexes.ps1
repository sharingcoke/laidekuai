param(
  [string]$DbHost = "127.0.0.1",
  [int]$DbPort = 3306,
  [string]$DbUser = "root",
  [string]$DbPassword = "",
  [string]$DbName = "laidekuai",
  [string]$MysqlCommand = "mysql",
  [string]$IndexSnapshotPath = ""
)

$ErrorActionPreference = "Stop"

$required = @{
  "orders" = @("idx_orders_buyer", "idx_orders_seller", "idx_orders_status_created")
  "order_item" = @("idx_order_item_order_id", "idx_order_item_seller_id")
  "goods" = @("idx_goods_category_id", "idx_goods_status_created")
  "dispute" = @("idx_dispute_order", "idx_dispute_status")
}

function Parse-Snapshot {
  param([string]$Path)
  if (-not (Test-Path -Path $Path -PathType Leaf)) {
    Write-Host ("DB_INDEX_FAIL snapshot_missing=" + $Path)
    exit 1
  }
  $raw = Get-Content -Path $Path -Raw
  try {
    $obj = $raw | ConvertFrom-Json
    $map = @{}
    foreach ($prop in $obj.PSObject.Properties) {
      $map[$prop.Name] = @($prop.Value)
    }
    return $map
  } catch {
    Write-Host ("DB_INDEX_FAIL snapshot_invalid_json=" + $Path)
    exit 1
  }
}

function Load-FromMySql {
  param(
    [string]$Command,
    [string]$Host,
    [int]$Port,
    [string]$User,
    [string]$Password,
    [string]$Schema
  )
  $query = "SELECT TABLE_NAME, INDEX_NAME FROM information_schema.statistics WHERE TABLE_SCHEMA='${Schema}' ORDER BY TABLE_NAME, INDEX_NAME;"
  $args = @("-N", "-B", "-h", $Host, "-P", $Port, "-u", $User, "--password=$Password", "-e", $query)
  $result = & $Command @args 2>&1
  if ($LASTEXITCODE -ne 0) {
    Write-Host ("DB_INDEX_FAIL mysql_query_failed=" + ($result | Out-String).Trim())
    exit 1
  }
  $indexMap = @{}
  foreach ($line in $result) {
    $parts = "$line".Split("`t")
    if ($parts.Count -lt 2) {
      continue
    }
    $tableName = $parts[0].Trim()
    $indexName = $parts[1].Trim()
    if (-not $indexMap.ContainsKey($tableName)) {
      $indexMap[$tableName] = @()
    }
    $indexMap[$tableName] += $indexName
  }
  return $indexMap
}

$indexMap = @{}
if (-not [string]::IsNullOrWhiteSpace($IndexSnapshotPath)) {
  $indexMap = Parse-Snapshot -Path $IndexSnapshotPath
} else {
  $indexMap = Load-FromMySql -Command $MysqlCommand -Host $DbHost -Port $DbPort -User $DbUser -Password $DbPassword -Schema $DbName
}

$missing = @()
foreach ($table in $required.Keys) {
  if (-not $indexMap.ContainsKey($table)) {
    foreach ($idx in $required[$table]) {
      $missing += ($table + "." + $idx)
    }
    continue
  }
  $existing = @($indexMap[$table] | ForEach-Object { "$_".Trim() })
  foreach ($idx in $required[$table]) {
    if (-not ($existing -contains $idx)) {
      $missing += ($table + "." + $idx)
    }
  }
}

if ($missing.Count -gt 0) {
  Write-Host ("DB_INDEX_FAIL missing=" + ($missing -join ","))
  exit 1
}

Write-Host "DB_INDEX_OK"
Write-Host ("DB_INDEX_OK checked_tables=" + ($required.Keys -join ","))
exit 0
