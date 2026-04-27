$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

& (Join-Path $root "start-backend.ps1")
Start-Sleep -Seconds 2
& (Join-Path $root "start-frontend.ps1")

Write-Host "Backend URL: http://localhost:8080"
Write-Host "Frontend URL: http://localhost:5174"
