# Requirements — FIX Simulator

This document summarizes the project's tasks, scope, and acceptance criteria for the FIX Simulator project (backend + UI + packaging).

## Project Goal
Provide a lightweight FIX simulator for Windows that allows users to create and manage FIX sessions dynamically via a REST API, view session status/messages/logs in a UI, and package the app for Windows Store distribution (MSIX).

## Checklist

- [x] Dynamic session lifecycle: create/list/inspect/stop sessions via REST API
- [x] REST API endpoints implemented: `POST /fix/session`, `GET /fix/sessions`, `GET /fix/sessions/{id}/messages`, `POST /fix/sessions/{id}/stop`
- [x] QuickFIX/J integration (engine) with default `defaultApplVerID: FIX.5.0SP2`
- [x] Backend unit/integration tests and Spotless formatting applied
- [x] Basic React UI scaffold for session management (sidebar + details)
- [x] Docs reorganized into `docs/` and `docs/requirement.md` created
- [ ] Improve session state detection (`isLoggedOn()` heuristic)
- [ ] Add live push updates to UI (WebSocket or Server-Sent Events)
- [ ] Create MSIX packaging scripts and CI release steps

## Core Requirements (detailed)

- Dynamic session lifecycle:
  - Create, list, inspect, and stop sessions via REST API.
  - Support initiator and acceptor session types.
- Protocol support:
  - Use QuickFIX/J as the FIX engine; default application version `FIX.5.0SP2`.
- REST API endpoints:
  - `POST /fix/session` — create session
  - `GET /fix/sessions` — list sessions with metadata
  - `GET /fix/sessions/{id}/messages` — fetch recent messages
  - `POST /fix/sessions/{id}/stop` — stop session
- UI:
  - React UI (works in browser and Electron shell) listing sessions, showing details, and allowing session creation.
  - Default fields include `defaultApplVerID` set to `FIX.5.0SP2`.
- Tests & Quality:
  - Unit and integration tests for backend endpoints (MockMvc/JUnit).
  - Spotless formatting applied to Java sources.
- Packaging:
  - Document packaging steps for MSIX with `jpackage` and bundling a JRE.

## Acceptance Criteria
- Backend builds and tests pass (`./gradlew.bat clean build`).
- REST API supports dynamic session creation and lifecycle operations.
- UI displays session metadata (sender/target, host, port, heartBtInt, defaultApplVerID, loggedOn, startedAt, messageCount).
- Helper scripts demonstrate create → messages → stop flow.
- README and docs updated with Quickstart and dev instructions.

## Non-functional Requirements
- Target platform: Windows 10/11.
- Use Java 21 and Spring Boot 3.3.
- Keep the backend as a child process of Electron (avoid Windows Service in packaged app).

## Open Questions / Next Work
- Improve `isLoggedOn()` heuristic for accurate session state detection.
- Add live push updates to UI (WebSocket or Server-Sent Events) instead of polling.
- Create MSIX packaging scripts and CI steps for release builds.

## Quick dev commands
- Build & test:
```powershell
.\gradlew.bat clean build
```
- Run backend in dev:
```powershell
.\gradlew.bat bootRun
```
- Smoke test (example):
```powershell
.\scripts\run_session_flow.ps1
```

---

Add more details to this file as requirements become stable.

## High-level architecture

- Spring Boot backend runs QuickFIX/J service and provides REST endpoints for session management and logs.
- Electron wrapper (desktop) is responsible for:
  - Starting the backend process when the app launches (or using bundled JAR in production).
  - Stopping the backend when the app quits.
  - Hosting the React UI (or loading it from the backend URL).
- Packaging: produce an MSIX bundle using `jpackage` + bundled JRE (or MSIX Packaging Tool).

## API contract (backend & UI)

- `POST /fix/session` — Create a session. JSON body example:
  ```json
  {
    "type": "initiator",
    "senderCompID": "MY_SENDER",
    "targetCompID": "THEIR_TARGET",
    "host": "127.0.0.1",
    "port": "9876",
    "heartBtInt": "30"
  }
  ```
  Responses: 200 OK on success, 4xx on bad input
- `GET /fix/sessions` — Lists active sessions and statuses (returns metadata: senderCompID, targetCompID, host, port, heartBtInt, defaultApplVerID, type, loggedOn, startedAt, messageCount)
- `GET /fix/sessions/{sessionId}/messages` — Returns recent messages for the session
- `POST /fix/sessions/{sessionId}/stop` — Stops the requested session

UI contract:
- Sidebar lists sessions and status
- Main panel shows session details and message history
- Sessions update automatically (polling or push in future)

## Useful commands (development)
- Build backend (Gradle):
  - Windows PowerShell
    ```powershell
    .\gradlew.bat clean build
    ```
- Run backend (dev):
  - Windows PowerShell
    ```powershell
    .\gradlew.bat bootRun
    ```

## Acceptance criteria (summary)
- Able to start the backend and create/stop sessions via REST API
- Session status updates and messages are available via endpoints
- All endpoints covered by integration tests
- React UI displays sessions and details, updates automatically

