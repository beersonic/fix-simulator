# Run a full session flow: create -> get messages -> stop
# Usage: Open PowerShell in project root and run: .\scripts\run_session_flow.ps1

$baseUrl = 'http://localhost:8080'
$body = @{ 
    type = 'initiator'
    senderCompID = 'BeerClient05'
    targetCompID = 'BeerFIXServer'
    host = '127.0.0.1'
    port = '9878'
    heartBtInt = '30'
    defaultApplVerID = 'FIX.5.0SP2'
    resetOnLogon = 'Y'
} | ConvertTo-Json

Write-Host "Creating session..."
try {
    $sessionId = Invoke-RestMethod -Method Post -Uri "$baseUrl/fix/session" -Body $body -ContentType 'application/json'
} catch {
    Write-Error "Failed to create session: $_"
    exit 1
}

Write-Host "Session created: $sessionId"

Write-Host "Getting messages for session $sessionId..."
try {
    $msgs = Invoke-RestMethod -Method Get -Uri "$baseUrl/fix/sessions/$sessionId/messages"
    if ($msgs) { $msgs | ForEach-Object { Write-Host $_ } } else { Write-Host "No messages." }
} catch {
    Write-Warning "Could not fetch messages: $_"
}

Write-Host "Stopping session $sessionId..."
try {
    $stopResp = Invoke-RestMethod -Method Post -Uri "$baseUrl/fix/sessions/$sessionId/stop" -ContentType 'application/json'
    Write-Host "Stop response: $($stopResp | ConvertTo-Json -Depth 3)"
} catch {
    Write-Warning "Failed to stop session: $_"
}
