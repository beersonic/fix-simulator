<#
Starts backend (Gradle bootRun), the React dev server, then Electron (pointed at the dev server).
Each component is started in its own new PowerShell window so logs are visible.

Usage:
  ./scripts/run-dev.ps1            # runs install, dev server, and electron
  ./scripts/run-dev.ps1 -SkipInstall # skips `npm install` in ui/
#>
param(
    [switch]$SkipInstall
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$root = Split-Path -Parent $scriptDir
Write-Host "Repository root: $root"

function Start-WindowedProcess($workingDir, $commands, $title="Process") {
     # Start a new PowerShell process with the provided commands
     $fullCmd = "cd `"$workingDir`"; $commands"
     Write-Host "Launching $title in new window: powershell -NoExit -Command $fullCmd"
     Start-Process -FilePath powershell -ArgumentList @("-NoExit", "-Command", $fullCmd) -WorkingDirectory $workingDir -WindowStyle Normal
}

# 1) Start backend
Write-Host "\n[1/3] Starting backend (Gradle bootRun)..."
$backendCmd = '.\\gradlew bootRun'
Start-WindowedProcess -workingDir $root -commands $backendCmd -title 'Backend (gradle)'

Start-Sleep -Seconds 3

# 2) Start UI dev server
Write-Host "\n[2/3] Starting UI dev server (npm start) ..."
$uiDir = Join-Path $root 'ui'
if (-not $SkipInstall) {
    # run install then start so fresh dev environments work
    $uiCmd = "npm install --legacy-peer-deps; npm start"
} else {
    $uiCmd = "npm start"
}
Start-WindowedProcess -workingDir $uiDir -commands $uiCmd -title 'UI (npm start)'

Start-Sleep -Seconds 3

function Wait-ForUrl($url, $timeoutSeconds=120, $intervalSeconds=2) {
    $end = (Get-Date).AddSeconds($timeoutSeconds)
    while ((Get-Date) -lt $end) {
        try {
            Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop | Out-Null
            return $true
        } catch {
            Start-Sleep -Seconds $intervalSeconds
        }
    }
    return $false
}

# 3) Start Electron (wait for UI first)
Write-Host "\n[3/3] Waiting for UI at http://localhost:3000 before launching Electron..."

$uiUrl = 'http://localhost:3000'
if (Wait-ForUrl $uiUrl 120 2) {
    Write-Host "UI is available at $uiUrl - launching Electron."
}
else {
    Write-Host "Timeout waiting for $uiUrl (120s). Launching Electron anyway."
}

$electronDir = Join-Path $root 'electron'
$electronCmd = '$env:FRONTEND_URL=''http://localhost:3000''; npm start'
Start-WindowedProcess -workingDir $electronDir -commands $electronCmd -title 'Electron'

Write-Host "\nLaunched backend, UI and Electron. Check the opened windows for logs."