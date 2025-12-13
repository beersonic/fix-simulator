# FIX Simulator — Requirements & Design

This document is formatted as Markdown so it can be pasted into Confluence. It combines the project requirements, acceptance criteria, and a concise design overview for implementation and review.

---

## Overview
The FIX Simulator is a lightweight application for Windows that lets users create and manage FIX protocol sessions dynamically. It consists of a Spring Boot backend using QuickFIX/J to manage FIX connections and a React UI (also run inside Electron in production) for session management, monitoring, and message viewing.

Primary objectives:
- Allow users to create, start, inspect, and stop FIX sessions on demand (initiator & acceptor).
- Provide a REST API for session lifecycle, status, and message retrieval.
- Provide a minimal UI for session management and viewing logs/messages.
- Enable packaging for Windows Store (MSIX) with a bundled runtime.

---

## Checklist (Quick Status)
- [x] Dynamic session lifecycle (create/list/stop)
- [x] REST API endpoints for session and messages
- [x] QuickFIX/J integration with default `defaultApplVerID: FIX.5.0SP2`
- [x] Basic React UI (session list + details)
- [x] Project docs consolidated under `docs/`
- [ ] Improve `isLoggedOn()` session detection
- [ ] Add live push updates (WebSocket/SSE)
- [ ] MSIX packaging & CI release steps

---

## Requirements

### Functional
1. Create new FIX sessions on demand via `POST /fix/session`.
2. List active sessions and metadata via `GET /fix/sessions`.
3. View recent messages for a session via `GET /fix/sessions/{id}/messages`.
4. Stop a session via `POST /fix/sessions/{id}/stop`.
5. Prevent duplicate session names/IDs.
6. Expose session metadata: `senderCompID`, `targetCompID`, `host`, `port`, `heartBtInt`, `defaultApplVerID`, `type`, `loggedOn`, `startedAt`, `messageCount`.

### Non-functional
- Platform: Windows 10/11.
- Java 21, Spring Boot 3.3.x.
- QuickFIX/J for FIX protocol support.
- Tests: unit & integration tests (JUnit + MockMvc).
- Code style: Spotless for Java formatting.
- Packaging: MSIX bundle (using `jpackage` or MSIX Packaging Tool) with a bundled runtime.

---

## API Contract

- `POST /fix/session` — Create session
  - Body (example):
  ```json
  {
    "type": "initiator",
    "senderCompID": "MY_SENDER",
    "targetCompID": "THEIR_TARGET",
    "host": "127.0.0.1",
    "port": 9876,
    "heartBtInt": 30,
    "defaultApplVerID": "FIX.5.0SP2"
  }
  ```
  - Returns: 200 OK + `{ "id": "s1", ... }` on success, 4xx on invalid input.

- `GET /fix/sessions` — List sessions
  - Returns array of session metadata objects (fields listed in Requirements).

- `GET /fix/sessions/{sessionId}/messages` — Get recent messages
  - Returns: `{ "id": "s1", "messages": [ ... ] }`

- `POST /fix/sessions/{sessionId}/stop` — Stop session
  - Returns: `{ "stopped": true, "id": "s1" }`

---

## Design Overview

### High-level architecture
- Backend (Spring Boot)
  - Exposes REST API used by the UI and scripts.
  - Hosts `QuickFixService` to manage dynamic session lifecycle.
  - Stores runtime session state in `SessionHolder` objects (settings, engine, application, store, logs).
  - Uses QuickFIX/J `SocketInitiator` / `SocketAcceptor` depending on `type`.

- UI (React + optional Electron shell)
  - Session list (sidebar) with status badge (online/offline), and selected session details.
  - Add Session form (includes `defaultApplVerID`, defaults to `FIX.5.0SP2`).

- Packaging: Electron app bundles the UI and a backend JAR, starts backend as a child process.

### Session lifecycle
1. Client POSTs to `/fix/session` with session parameters.
2. Backend validates input and constructs `SessionSettings` programmatically.
3. `QuickFixService` creates `SessionHolder`:
   - Build a QuickFIX `SessionID` and `SessionSettings`.
   - Create `Application` implementation (collects messages, stores metadata).
   - Create `MessageStoreFactory`, `LogFactory`, `MessageFactory`.
   - Start initiator/acceptor using QuickFIX classes.
4. Backend returns session id and metadata.
5. Backend collects messages in-memory (bounded) for `GET /messages`.
6. Client can `POST /stop` to stop the session and free resources.

Notes: session `loggedOn` detection is provided by the `Application` implementation but should be hardened (currently heuristic-based).

### Data model (session metadata)
- id: string
- name: string (optional)
- type: `initiator` | `acceptor`
- senderCompID, targetCompID: string
- host: string
- port: int
- heartBtInt: int
- defaultApplVerID: string
- startedAt: timestamp
- loggedOn: boolean
- messageCount: int

---

## Configuration & QuickFIX details
- Default `DefaultApplVerID` is `FIX.5.0SP2` for compatibility with common servers.
- Log files and message stores are created per-session under `log/` and `store/` folders.
- `quickfixj.cfg` is used as a base for configurable defaults; programmatic `SessionSettings` override per-session properties.

---

## UI Design Notes
- Sidebar: sessions with `sender-target` label and status dot.
- Main panel: session details, message list (latest-first), controls to Stop session and fetch messages.
- Add Session Form: fields for `type`, `senderCompID`, `targetCompID`, `host`, `port`, `heartBtInt`, `defaultApplVerID`.
- Polling: UI polls `/fix/sessions` every few seconds; migrate to WebSocket/SSE for push updates.

---

## Testing
- Unit tests: `FixControllerTest` (create/list/stop) using MockMvc.
- Integration: start a test backend and create a session against a local QuickFIX test acceptor (or a loopback initiator).
- Smoke: `scripts/run_session_flow.ps1` demonstrates create→messages→stop flow.

---

## Acceptance Criteria
- `./gradlew.bat clean build` completes with tests passing.
- REST API endpoints operate as specified and return the required metadata.
- UI displays sessions and metadata, and can create/stop sessions.
- Packaging documentation exists and MSIX packaging steps documented.

---

## Next Steps
1. Harden `isLoggedOn()` logic and reliable session state detection.
2. Improve message persistence and log rotation.
3. Replace polling with push updates (SSE / WebSocket) for real-time UI.
4. Create MSIX packaging script and CI workflow.

---

## Contact / Notes
- Repo root: `e:\Code\fix-simulator`
- Key files:
  - Backend: `src/main/java/com/beersonic/QuickFixService.java`
  - Controller: `src/main/java/com/beersonic/FixController.java`
  - Tests: `src/test/java/com/beersonic/FixControllerTest.java`
  - UI: `ui/src/` (React app)
  - Smoke script: `scripts/run_session_flow.ps1`


---

*Copy and paste this file into a Confluence page using the Markdown macro (or paste directly if your Confluence accepts Markdown).*