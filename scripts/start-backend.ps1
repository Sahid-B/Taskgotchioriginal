param([switch]$Demo)
$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
Push-Location $project
try {
    if (Test-Path 'C:\Program Files\Java\jdk-21') { $env:JAVA_HOME = 'C:\Program Files\Java\jdk-21' }
    if ($env:JAVA_HOME) { $env:PATH = "$env:JAVA_HOME\bin;$env:PATH" }
    if ($Demo) {
        & .\mvnw.cmd --no-transfer-progress '-Dspring-boot.run.profiles=h2' spring-boot:run
    } else {
        if (-not $env:DB_PASSWORD) {
            $secret = Read-Host 'Clave local del contenedor PostgreSQL' -AsSecureString
            $env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $secret).Password
        }
        $env:DB_HOST = 'localhost'
        $env:DB_PORT = '5433'
        $env:DB_NAME = 'taskgotchidb'
        $env:DB_USER = 'taskgotchi'
        & docker compose up -d --wait
        if ($LASTEXITCODE -ne 0) { throw 'Enciende Docker Desktop y vuelve a intentar.' }
        & .\mvnw.cmd --no-transfer-progress spring-boot:run
    }
} finally { Pop-Location }
