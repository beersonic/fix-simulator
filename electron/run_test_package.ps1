# PowerShell script to run the packaged Fix Simulator app for local testing
# Save as run_test_package.ps1 in the project root or electron/ folder

$electronAppPath = "E:\Code\fix-simulator\electron\dist\win-unpacked\Fix Simulator.exe"

if (-Not (Test-Path $electronAppPath)) {
    Write-Host "ERROR: Electron app not found at $electronAppPath"
    exit 1
}

Write-Host "Launching Fix Simulator packaged app..."
Start-Process -FilePath $electronAppPath

Write-Host "App launched. If the backend is bundled, it will start automatically."
Write-Host "Close the app window to stop the backend and UI."
