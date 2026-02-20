$ErrorActionPreference = 'Stop'
$root = (Resolve-Path "$PSScriptRoot\..").Path
$backend = Join-Path $root 'laidekuai-backend'
$env:PORT = if ($env:PORT) { $env:PORT } else { '9090' }
Set-Location $backend
./mvnw -q spring-boot:run