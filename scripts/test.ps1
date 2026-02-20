$ErrorActionPreference = 'Stop'
$root = (Resolve-Path "$PSScriptRoot\..").Path
$backend = Join-Path $root 'laidekuai-backend'
$frontend = Join-Path $root 'laidekuai-frontend'
$env:PORT = if ($env:PORT) { $env:PORT } else { '9090' }
Set-Location $backend
./mvnw -q test
Set-Location $frontend
$hasTest = npm run -s | Select-String -Pattern " test" -SimpleMatch
if ($hasTest) { npm run test } else { Write-Host 'No frontend test script configured.' }