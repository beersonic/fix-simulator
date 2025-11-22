# FIX Simulator — Requirements and Next-Session Notes

This document captures the requirements, architecture decisions, acceptance criteria, and next steps for the FIX Simulator project (web UI + local backend packaged for Windows Store via an Electron wrapper). Use this as the single-point reference for the next AI or human session.

## Goals
- Provide a desktop app (Windows) that lets users create and manage FIX sessions on the fly.
- Use QuickFIX/J for FIX protocol handling (initiator & acceptor modes).
- Provide a web-based UI (React/Vue/vanilla) hosted by a local Spring Boot backend.
- Wrap the frontend+backend in an Electron-based desktop shell for Windows Store packaging (MSIX).

## High-level architecture
- Spring Boot backend (existing project) runs QuickFIX/J service and provides REST endpoints for session management and logs.
- Web frontend (hosted by backend) presents UI to create sessions, view status, messages, and logs.
- Electron wrapper (desktop) is responsible for:
  - Starting the backend process when the app launches (or using bundled JAR in production).
  - Providing a BrowserWindow (Edge WebView2 under the hood) to show the UI (http://localhost:PORT).
  - Stopping the backend when the app quits.
- Packaging: produce an MSIX bundle using jpackage + bundled JRE (or MSIX Packaging Tool). Ensure WebView2 is available or use System-installed WebView2.

## User stories
1. As a user, I can open the app and see the UI that allows me to enter FIX session parameters (SenderCompID, TargetCompID, Host, Port, HeartBtInt, SessionType).
2. As a user, I can click "Add" to create and start a session which uses QuickFIX/J.
3. As a user, I can see session status (Connected, Disconnected, Logon/Logout timestamps).
4. As a user, I can view inbound and outbound FIX messages and download logs.
5. As a user, I can stop a session.
6. As a user, I can package the app and distribute via Microsoft Store.

## API contract (backend)
- POST /fix/session
  - Creates a session. JSON body example:
    {
      "type": "initiator",
      "senderCompID": "MY_SENDER",
      "targetCompID": "THEIR_TARGET",
      "host": "127.0.0.1",
      "port": "9876",
      "heartBtInt": "30"
    }
  - Responses: 200 OK on success, 4xx on bad input.

- GET /fix/sessions
  - Lists active sessions and statuses.

- GET /fix/sessions/{sessionId}/messages
  - Returns recent messages for the session.

- POST /fix/sessions/{sessionId}/stop
  - Stops the requested session.

Notes: These endpoints may already partially exist (current `FixController` is a starting point). Ensure endpoints return JSON and status codes appropriate for UI consumption.

## UI screens
1. Session List — shows active sessions, status badges, Start/Stop buttons, and an "Add" button.
2. Add Session modal/form — collects required FIX fields and a Save/Add button.
3. Session Detail — shows live message feed (inbound/outbound), and the session log (file-backed). Include filters and a download log action.

## Acceptance criteria
- Able to start the desktop Electron app which launches the backend and shows the UI.
- The UI can call backend endpoints to create an initiator session and the backend starts QuickFIX/J for that session.
- Session status updates are pushed (polling acceptable initially) and messages appear in the UI.
- The packaged MSIX starts and runs the same behavior on a clean Windows machine (jre bundled or jre requirement documented).

## Constraints & non-goals
- We will not implement advanced FIX message crafting UI (order entry) in the MVP — basic message logging and display is enough.
- We will not implement Windows services; backend should run as child process to comply with Store policy.

## Security & Privacy
- Do not ship any credentials by default. Users enter their own Sender/Target credentials.
- The app will need network capabilities declared in the Appx manifest.
- Document any telemetry or logging if added later.

## Implementation notes & decisions
- Use the existing `QuickFixService` but refactor to allow multiple dynamic session settings programmatically (create SessionSettings from JSON map and start a separate initiator/acceptor instance per session).
- For the Electron wrapper, support two run modes:
  1. Development: run backend with `./gradlew bootRun` and start electron with `npm start`.
  2. Production: use a bundled `backend.jar` shipped inside app and start it with `java -jar backend.jar`.
- Provide a mechanism to choose a free port at runtime and pass it to the UI via environment variables or CLI args to avoid conflicts.

## Next steps (for the next AI/human session)
1. Implement Electron scaffold and a minimal redirect UI that opens the backend URL. (Done in this session: prototype files added.)
2. Refactor `QuickFixService` to support programmatic, multi-session lifecycle (create/start/stop per session). Add endpoints to manage them.
3. Implement frontend pages and wire to backend endpoints (session form, list, messages).
4. Add simple tests: backend unit tests for session creation, and a UI smoke test. 
5. Add packaging scripts for MSIX using `jpackage` and document platform requirements (Windows SDK, code-signing cert, WebView2 runtime).

## Useful commands (development)
- Build backend (Gradle):
  - Windows PowerShell
    .\gradlew.bat clean build
  - Run backend (dev):
    .\gradlew.bat bootRun
- Electron dev:
  - Install dependencies: `npm install` inside `electron` folder
  - Start: `npm start` (ensure backend is running first or configure `electron` to start backend)

## Contacts & references
- QuickFIX/J docs: https://www.quickfixj.org
- Electron docs: https://www.electronjs.org
- Microsoft Store packaging: MSIX Packaging Tool, jpackage docs


---

Keep this file as the canonical requirements doc for the next session. Adjust acceptance criteria and API contract as implementation decisions are made.