$ErrorActionPreference = 'Stop'
Push-Location (Join-Path (Split-Path $PSScriptRoot -Parent) 'frontend')
try {
    if (-not (Test-Path node_modules)) {
        & npm.cmd ci --no-audit --no-fund
        if ($LASTEXITCODE -ne 0) { throw 'No fue posible instalar el frontend.' }
    }
    & npm.cmd run dev
} finally { Pop-Location }
