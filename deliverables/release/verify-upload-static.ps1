param(
  [string]$BaseUrl = "http://127.0.0.1:9090",
  [string]$ApiPrefix = "/api",
  [string]$UserToken = ""
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Net.Http

if ([string]::IsNullOrWhiteSpace($UserToken)) {
  Write-Host "UPLOAD_STATIC_FAIL missing_user_token"
  exit 1
}

function Parse-Json {
  param([string]$Text)
  try {
    return $Text | ConvertFrom-Json
  } catch {
    return $null
  }
}

function Upload-File {
  param(
    [string]$Url,
    [string]$Token,
    [string]$FilePath
  )
  $handler = New-Object System.Net.Http.HttpClientHandler
  $client = New-Object System.Net.Http.HttpClient($handler)
  $client.Timeout = [TimeSpan]::FromSeconds(10)
  $client.DefaultRequestHeaders.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", $Token)

  $bytes = [System.IO.File]::ReadAllBytes($FilePath)
  $content = New-Object System.Net.Http.MultipartFormDataContent
  $fileContent = New-Object System.Net.Http.ByteArrayContent -ArgumentList (,[byte[]]$bytes)
  $fileContent.Headers.ContentType = New-Object System.Net.Http.Headers.MediaTypeHeaderValue("application/octet-stream")
  $filename = [System.IO.Path]::GetFileName($FilePath)
  $content.Add($fileContent, "file", $filename)

  $resp = $client.PostAsync($Url, $content).Result
  $body = $resp.Content.ReadAsStringAsync().Result
  $result = [PSCustomObject]@{
    StatusCode = [int]$resp.StatusCode
    Content = $body
  }
  $client.Dispose()
  return $result
}

$base = $BaseUrl.TrimEnd("/")
$prefix = $ApiPrefix
if (-not $prefix.StartsWith("/")) {
  $prefix = "/" + $prefix
}
$uploadUrl = $base + $prefix + "/files/upload"

$workDir = Join-Path ([System.IO.Path]::GetTempPath()) ("upload-static-" + [Guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Path $workDir | Out-Null

$ok = $true
try {
  $pngPath = Join-Path $workDir "smoke-ok.png"
  [System.IO.File]::WriteAllBytes($pngPath, [byte[]](137,80,78,71,13,10,26,10,0,0,0,0))
  $uploadResp = Upload-File -Url $uploadUrl -Token $UserToken -FilePath $pngPath
  if ([int]$uploadResp.StatusCode -ne 200) {
    Write-Host ("UPLOAD_STATIC_FAIL upload_status=" + $uploadResp.StatusCode)
    exit 1
  }

  $uploadJson = Parse-Json -Text $uploadResp.Content
  if ($null -eq $uploadJson -or [int]$uploadJson.code -ne 0) {
    Write-Host ("UPLOAD_STATIC_FAIL upload_business_code=" + ($uploadJson.code))
    exit 1
  }
  $url = [string]$uploadJson.data
  if ([string]::IsNullOrWhiteSpace($url) -or -not $url.StartsWith("/static/files/")) {
    Write-Host ("UPLOAD_STATIC_FAIL upload_url_invalid=" + $url)
    exit 1
  }

  $staticResp = Invoke-WebRequest -UseBasicParsing -Method Get -Uri ($base + $url) -TimeoutSec 10
  if ([int]$staticResp.StatusCode -ne 200) {
    Write-Host ("UPLOAD_STATIC_FAIL static_status=" + $staticResp.StatusCode)
    exit 1
  }
  Write-Host "UPLOAD_STATIC_OK upload_and_static_access"

  $badPath = Join-Path $workDir "smoke-invalid.exe"
  Set-Content -Path $badPath -Value "invalid" -Encoding UTF8
  $badResp = Upload-File -Url $uploadUrl -Token $UserToken -FilePath $badPath
  if ([int]$badResp.StatusCode -ne 200) {
    Write-Host ("UPLOAD_STATIC_FAIL invalid_upload_status=" + $badResp.StatusCode)
    exit 1
  }
  $badJson = Parse-Json -Text $badResp.Content
  if ($null -eq $badJson -or [int]$badJson.code -ne 40001) {
    Write-Host ("UPLOAD_STATIC_FAIL invalid_upload_business_code=" + ($badJson.code))
    exit 1
  }
  Write-Host "UPLOAD_STATIC_OK invalid_type_rejected"
} finally {
  Remove-Item -Path $workDir -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Host "UPLOAD_STATIC_ALL_OK"
exit 0
