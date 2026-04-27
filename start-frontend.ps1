$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontendRoot = Join-Path $root "frontend"
$logDir = Join-Path $root "logs"
$stdoutLogPath = Join-Path $logDir "frontend.out.log"
$stderrLogPath = Join-Path $logDir "frontend.err.log"

if (-not (Test-Path $frontendRoot)) {
    throw "Frontend directory not found: $frontendRoot"
}

if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}

Start-Process -FilePath "npm.cmd" `
    -ArgumentList "run", "dev" `
    -WorkingDirectory $frontendRoot `
    -RedirectStandardOutput $stdoutLogPath `
    -RedirectStandardError $stderrLogPath `
    -WindowStyle Hidden

Write-Host "Frontend started. Dev URL: http://localhost:5174"
Write-Host "Frontend output log: $stdoutLogPath"
Write-Host "Frontend error log: $stderrLogPath"
