$ErrorActionPreference = "Stop"

$appRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$platformRoot = Split-Path -Parent $appRoot
$jarPath = Join-Path $appRoot "app.jar"
$configPath = Join-Path $appRoot "app-prod.yml"
$logDir = Join-Path $platformRoot "logs"
$stdoutLogPath = Join-Path $logDir "backend.out.log"
$stderrLogPath = Join-Path $logDir "backend.err.log"

if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}

Set-Location $appRoot

Start-Process -FilePath "java.exe" `
    -ArgumentList "-jar `"$jarPath`" --spring.config.location=`"$configPath`"" `
    -RedirectStandardOutput $stdoutLogPath `
    -RedirectStandardError $stderrLogPath `
    -WindowStyle Hidden

Write-Host "Backend started. Output log: $stdoutLogPath"
Write-Host "Backend error log: $stderrLogPath"
