param(
  [string]$DbHost = "127.0.0.1",
  [int]$DbPort = 3306,
  [string]$DbUser = "root",
  [string]$DbPassword = "",
  [string]$DbName = "laidekuai",
  [string]$MysqlCommand = "mysql",
  [string]$MigrationDir = "laidekuai-backend/src/main/resources/db/migration",
  [string]$HistorySnapshotPath = ""
)

$ErrorActionPreference = "Stop"

function Normalize-Version {
  param([string]$Version)
  if ([string]::IsNullOrWhiteSpace($Version)) {
    return ""
  }
  return $Version.Trim().Replace("_", ".")
}

function Load-ExpectedVersions {
  param([string]$Dir)
  if (-not (Test-Path -Path $Dir -PathType Container)) {
    Write-Host ("FLYWAY_GUARD_FAIL migration_dir_missing=" + $Dir)
    exit 1
  }
  $versions = @()
  $files = Get-ChildItem -Path $Dir -Filter "V*__*.sql" -File
  foreach ($file in $files) {
    if ($file.BaseName -match "^V(.+)__") {
      $versions += (Normalize-Version -Version $matches[1])
    }
  }
  $versions = $versions | Sort-Object -Unique
  if ($versions.Count -eq 0) {
    Write-Host "FLYWAY_GUARD_FAIL expected_versions_empty"
    exit 1
  }
  return $versions
}

function Parse-Snapshot {
  param([string]$Path)
  if (-not (Test-Path -Path $Path -PathType Leaf)) {
    Write-Host ("FLYWAY_GUARD_FAIL snapshot_missing=" + $Path)
    exit 1
  }
  try {
    $obj = (Get-Content -Path $Path -Raw) | ConvertFrom-Json
  } catch {
    Write-Host ("FLYWAY_GUARD_FAIL snapshot_invalid_json=" + $Path)
    exit 1
  }
  $tableExists = [bool]$obj.tableExists
  $rows = @()
  foreach ($row in $obj.rows) {
    $rows += [PSCustomObject]@{
      version = "$($row.version)"
      success = [int]$row.success
    }
  }
  return [PSCustomObject]@{
    tableExists = $tableExists
    rows = $rows
  }
}

function Query-FlywayHistory {
  param(
    [string]$Command,
    [string]$Host,
    [int]$Port,
    [string]$User,
    [string]$Password,
    [string]$Schema
  )
  $existsQuery = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${Schema}' AND table_name='flyway_schema_history';"
  $argsBase = @("-N", "-B", "-h", $Host, "-P", $Port, "-u", $User, "--password=$Password")
  $existsRaw = & $Command @argsBase -e $existsQuery 2>&1
  if ($LASTEXITCODE -ne 0) {
    Write-Host ("FLYWAY_GUARD_FAIL mysql_query_failed=" + ($existsRaw | Out-String).Trim())
    exit 1
  }
  $exists = [int]("$existsRaw".Trim()) -gt 0
  if (-not $exists) {
    return [PSCustomObject]@{
      tableExists = $false
      rows = @()
    }
  }

  $historyQuery = "SELECT version, success FROM flyway_schema_history ORDER BY installed_rank;"
  $historyRaw = & $Command @argsBase -D $Schema -e $historyQuery 2>&1
  if ($LASTEXITCODE -ne 0) {
    Write-Host ("FLYWAY_GUARD_FAIL mysql_history_query_failed=" + ($historyRaw | Out-String).Trim())
    exit 1
  }
  $rows = @()
  foreach ($line in $historyRaw) {
    $parts = "$line".Split("`t")
    if ($parts.Count -lt 2) {
      continue
    }
    $rows += [PSCustomObject]@{
      version = $parts[0].Trim()
      success = [int]$parts[1].Trim()
    }
  }
  return [PSCustomObject]@{
    tableExists = $true
    rows = $rows
  }
}

$expectedVersions = Load-ExpectedVersions -Dir $MigrationDir
$history = $null
if (-not [string]::IsNullOrWhiteSpace($HistorySnapshotPath)) {
  $history = Parse-Snapshot -Path $HistorySnapshotPath
} else {
  $history = Query-FlywayHistory -Command $MysqlCommand -Host $DbHost -Port $DbPort -User $DbUser -Password $DbPassword -Schema $DbName
}

if (-not $history.tableExists) {
  Write-Host "FLYWAY_GUARD_FAIL history_table_missing"
  exit 1
}

$successfulVersions = @()
foreach ($row in $history.rows) {
  if ($row.success -eq 1) {
    $v = Normalize-Version -Version $row.version
    if (-not [string]::IsNullOrWhiteSpace($v)) {
      $successfulVersions += $v
    }
  }
}
$successfulVersions = $successfulVersions | Sort-Object -Unique

$missing = @()
foreach ($v in $expectedVersions) {
  if (-not ($successfulVersions -contains $v)) {
    $missing += $v
  }
}

if ($missing.Count -gt 0) {
  Write-Host ("FLYWAY_GUARD_FAIL missing_versions=" + ($missing -join ","))
  exit 1
}

Write-Host "FLYWAY_GUARD_OK"
Write-Host ("FLYWAY_GUARD_OK expected_versions=" + ($expectedVersions -join ","))
exit 0
