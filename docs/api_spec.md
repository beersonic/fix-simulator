# FIX Simulator — API Specification

This API specification is formatted for Confluence. It documents the REST API used by the FIX Simulator backend for creating and managing FIX sessions.

Base URL (development)
- http://localhost:8080

Authentication
- None for the MVP. If you add auth later, document header/cookie schemes here.

Common response structure (errors)
- HTTP 4xx/5xx responses return JSON:
```
{
  "error": "Human readable message",
  "code": 400
}
```

Notes & conventions
- Timestamps: ISO 8601 (UTC) e.g. `2025-11-29T07:47:31Z`.
- `defaultApplVerID` default: `FIX.5.0SP2` (used by UI and backend when omitted).
- Session `type`: `initiator` | `acceptor`.
- `loggedOn` is a boolean provided by the backend; the implementation uses a heuristic and may be improved later.

---

## Endpoints

### POST /fix/session
Create a new FIX session (initiator or acceptor).

Request body (JSON)
- type: string, required, `initiator` or `acceptor`
- senderCompID: string, required
- targetCompID: string, required
- host: string, required for initiator (tcp host/IP)
- port: integer, required for initiator (tcp port)
- heartBtInt: integer (seconds), optional (default depends on QuickFIX defaults)
- defaultApplVerID: string, optional (default `FIX.5.0SP2`)

Example (curl)
```
curl -X POST http://localhost:8080/fix/session \
  -H "Content-Type: application/json" \
  -d '{
    "type":"initiator",
    "senderCompID":"MY_SENDER",
    "targetCompID":"THEIR_TARGET",
    "host":"127.0.0.1",
    "port":9876,
    "heartBtInt":30,
    "defaultApplVerID":"FIX.5.0SP2"
  }'
```

Success response (200)
```
{
  "id": "s1" // short alias returned by the server (use this id for subsequent calls)
}
```

Errors
- 400 Bad Request — missing/invalid fields
- 409 Conflict — duplicate session name/ID
- 500 Internal Server Error — backend failure starting the QuickFIX engine

Notes
- The call returns immediately after the backend attempts to start the session; `loggedOn` indicates connectivity but may change soon after creation.

---

### GET /fix/sessions
List all sessions with metadata.

Query parameters (optional)
- none in MVP; future: filter by `type`, `loggedOn` etc.

Success response (200)
```
[
  {
    "id":"s1",
    "fixSessionKey":"FIXT.1.1:MY_SENDER->THEIR_TARGET@127.0.0.1:9876",
    "type":"initiator",
    "senderCompID":"MY_SENDER",
    "targetCompID":"THEIR_TARGET",
    "host":"127.0.0.1",
    "port":9876,
    "heartBtInt":30,
    "defaultApplVerID":"FIX.5.0SP2",
    "startedAt":"2025-11-29T07:47:31Z",
    "loggedOn":true,
    "messageCount": 42
  },
  { ... }
]
```

Field descriptions
- `id`: internal session identifier (string)
- `type`: `initiator` or `acceptor`
- `senderCompID`, `targetCompID`: FIX identifiers
- `host`, `port`: TCP endpoint for initiator sessions
- `heartBtInt`: heartbeat interval in seconds
- `defaultApplVerID`: FIX application version
- `startedAt`: ISO8601 timestamp when session was started
- `loggedOn`: boolean, indicates currently connected (best-effort)
- `messageCount`: number of messages recorded in-memory/store

---

### GET /fix/sessions/{sessionId}/messages
Get recent messages for a session.

Path params
- `sessionId` (string) — id returned by `POST /fix/session` or listed in `GET /fix/sessions`.

Query params
- `limit` (int, optional) — max number of messages to return, default 100
- `since` (ISO8601, optional) — return messages after this timestamp

Success response (200)
```
{
  "id":"s1",
  "messages": [
    {
      "timestamp":"2025-11-29T07:48:01Z",
      "direction":"inbound", // or "outbound"
      "raw":"8=FIXT.1.1\u00019=...",
      "summary":"Logon, MsgType=A"
    },
    ...
  ]
}
```

Notes
- Message storage is bounded in memory for the MVP; longer-term store persistence and pagination will be added.

---

### POST /fix/sessions/{sessionId}/stop
Stop a running session and free its resources.

Path params
- `sessionId` (string)

Success response (200)
```
{ "stopped": true, "id": "s1" }
```

Errors
- 404 Not Found — session does not exist
- 409 Conflict — session already stopping/stopped

---

## Examples (PowerShell)
Create session
```powershell
$body = @{ 
  type = 'initiator';
  senderCompID = 'MY_SENDER';
  targetCompID = 'THEIR_TARGET';
  host = '127.0.0.1';
  port = 9876;
  heartBtInt = 30;
  defaultApplVerID = 'FIX.5.0SP2'
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:8080/fix/session -Body $body -ContentType 'application/json'
```

List sessions
```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/fix/sessions
```

Stop session
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/fix/sessions/s1/stop
```

---

## Future / Notes
- Authentication & authorization (API keys / OAuth) may be added for production.
- Consider SSE or WebSocket for session status and message push to the UI.
- Add pagination and persistent message store for longer histories.

---

## Contact
- Backend entrypoints: `src/main/java/com/beersonic/FixController.java` and `QuickFixService.java`
- Smoke test script: `scripts/run_session_flow.ps1`


*Paste this file into Confluence using the Markdown macro (or paste the raw Markdown if your Confluence instance supports it).*