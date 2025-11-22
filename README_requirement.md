
# FIX Simulator — Backend Requirements and Architecture

This document captures the requirements, architecture decisions, acceptance criteria, and next steps for the FIX Simulator backend (Spring Boot + QuickFIX/J) and Electron wrapper. Use this as the single-point reference for backend and packaging work.

## Goals
- Provide a backend service (Windows) that lets users create and manage FIX sessions on the fly
- Use QuickFIX/J for FIX protocol handling (initiator & acceptor modes)
- Expose REST API for session management, status, messages, and logs
- Enable packaging for Windows Store via Electron shell (UI implementation pending)

## High-level architecture
- Spring Boot backend runs QuickFIX/J service and provides REST endpoints for session management and logs
- Electron wrapper (desktop) is responsible for:
  - Starting the backend process when the app launches (or using bundled JAR in production)
  - Stopping the backend when the app quits
- Packaging: produce an MSIX bundle using jpackage + bundled JRE (or MSIX Packaging Tool)

## API contract (backend)
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

- `GET /fix/sessions` — Lists active sessions and statuses
- `GET /fix/sessions/{sessionId}/messages` — Returns recent messages for the session
- `POST /fix/sessions/{sessionId}/stop` — Stops the requested session

See `backend_test.rest` for example requests and payloads.

## Acceptance criteria
- Able to start the backend and create/stop sessions via REST API
- Session status updates and messages are available via endpoints
- All endpoints covered by integration tests
- The packaged MSIX starts and runs the backend on a clean Windows machine (JRE bundled or requirement documented)

## Constraints & non-goals
- No advanced FIX message crafting UI (order entry) in MVP — basic message logging and display only
- No Windows services; backend should run as child process to comply with Store policy

## Security & Privacy
- Do not ship any credentials by default. Users enter their own Sender/Target credentials
- Document any telemetry or logging if added later

## Implementation notes & decisions
- Backend uses `QuickFixService` refactored for dynamic, multi-session lifecycle (create/start/stop per session)
- Electron wrapper supports two run modes:
  1. Development: run backend with `./gradlew.bat bootRun`
  2. Production: use a bundled `backend.jar` shipped inside app and start it with `java -jar backend.jar`
- Provide a mechanism to choose a free port at runtime and pass it to Electron via env var or CLI args to avoid conflicts

## Next steps
1. Refactor `QuickFixService` to support programmatic, multi-session lifecycle (done)
2. Add endpoints to manage sessions and messages (done)
3. Add backend unit and integration tests (done)
4. Add packaging scripts for MSIX using `jpackage` and document platform requirements (Windows SDK, code-signing cert, WebView2 runtime)

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

## References
- QuickFIX/J docs: https://www.quickfixj.org
- Spring Boot docs: https://spring.io/projects/spring-boot
- Microsoft Store packaging: MSIX Packaging Tool, jpackage docs

---

Keep this file as the canonical backend requirements doc for the next session. Adjust acceptance criteria and API contract as implementation decisions are made.