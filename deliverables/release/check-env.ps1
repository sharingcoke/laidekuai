param(
  [string]$RequireK6 = "true",
  [string]$RequireMySql = "false",
  [string]$RequireMaven = "false",
  [string[]]$ExtraRequired = @()
)

$ErrorActionPreference = "Stop"
$nativeCommandErrFlagVar = Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue
if ($null -ne $nativeCommandErrFlagVar) {
  $script:__nativeCommandErrFlagBackup = [bool]$nativeCommandErrFlagVar.Value
  $global:PSNativeCommandUseErrorActionPreference = $false
}

function Test-CommandExists {
  param([string]$Name)
  return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

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
  Write-Host ("ENV_CHECK_FAIL invalid " + $Name + " value, use true/false")
  exit 1
}

function Get-MajorVersion {
  param([string]$RawVersion)
  $trimmed = $RawVersion.Trim()
  if ($trimmed -match "(\d+)(\.\d+)*") {
    return [int]$matches[1]
  }
  throw "version_parse_failed:" + $RawVersion
}

function Assert-MinVersion {
  param(
    [string]$Name,
    [int]$MinMajor,
    [scriptblock]$VersionCommand
  )
  $output = (& $VersionCommand 2>&1 | Out-String).Trim()
  if ($output -eq "") {
    Write-Host ("ENV_CHECK_FAIL " + $Name + " version_empty")
    exit 1
  }
  $major = Get-MajorVersion -RawVersion $output
  if ($major -lt $MinMajor) {
    Write-Host ("ENV_CHECK_FAIL " + $Name + " version_too_low current=" + $output + " required>=" + $MinMajor)
    exit 1
  }
  return $output
}

$requireK6Flag = Parse-BoolFlag -Name "RequireK6" -Value $RequireK6
$requireMySqlFlag = Parse-BoolFlag -Name "RequireMySql" -Value $RequireMySql
$requireMavenFlag = Parse-BoolFlag -Name "RequireMaven" -Value $RequireMaven

$required = @("java", "node", "npm")
if ($requireK6Flag) {
  $required += "k6"
}
if ($requireMySqlFlag) {
  $required += "mysql"
}
if ($requireMavenFlag) {
  $required += "mvn"
}
if ($ExtraRequired.Count -gt 0) {
  $required += $ExtraRequired
}

$required = $required | Select-Object -Unique

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

$javaVersion = Assert-MinVersion -Name "java" -MinMajor 17 -VersionCommand { java --version }
$nodeVersion = Assert-MinVersion -Name "node" -MinMajor 18 -VersionCommand { node --version }
$npmVersion = Assert-MinVersion -Name "npm" -MinMajor 9 -VersionCommand { npm --version }

Write-Host ("ENV_CHECK_OK required commands: " + ($required -join ", "))
Write-Host ("ENV_CHECK_OK versions: java=" + $javaVersion + "; node=" + $nodeVersion + "; npm=" + $npmVersion)
if ($null -ne $nativeCommandErrFlagVar) {
  $global:PSNativeCommandUseErrorActionPreference = $script:__nativeCommandErrFlagBackup
}
exit 0
