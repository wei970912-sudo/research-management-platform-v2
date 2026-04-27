$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendScript = Join-Path $root "backend\start-backend.ps1"

if (-not (Test-Path $backendScript)) {
    throw "Backend start script not found: $backendScript"
}

& $backendScript
