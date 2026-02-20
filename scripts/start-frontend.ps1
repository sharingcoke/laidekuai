$ErrorActionPreference = 'Stop'
$root = (Resolve-Path "$PSScriptRoot\..").Path
$frontend = Join-Path $root 'laidekuai-frontend'
$env:PORT = if ($env:PORT) { $env:PORT } else { '5173' }
$env:VITE_API_TARGET = if ($env:VITE_API_TARGET) { $env:VITE_API_TARGET } else { 'http://localhost:9090' }
$env:VITE_API_BASE = if ($env:VITE_API_BASE) { $env:VITE_API_BASE } else { '/api' }
Set-Location $frontend
if (-not (Test-Path node_modules)) { npm install }
npm run dev -- --host 0.0.0.0 --port $env:PORT