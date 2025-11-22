# fix-simulator

FIX Simulator — Backend & React UI

Spring Boot 3 backend for dynamic FIX session management using QuickFIX/J. Includes a React UI for session management, listing, and status display. Supports REST API for creating, listing, and stopping FIX sessions, with robust test coverage and Gradle build tools.

## Features
- Dynamic FIX session creation (initiator/acceptor) via REST API
- Session uniqueness and status reporting
- Message and log retrieval per session
- Robust unit and integration tests (MockMvc/JUnit)
- Gradle wrapper, Spotless formatting, dependency version checker
- React UI (in `ui/` folder):
  - Displays session list and details
  - Polls backend every 3 seconds for live updates
  - Add new sessions via form

## Build & Run (PowerShell)
```powershell
cd E:\Code\fix-simulator
.\gradlew.bat clean test
.\gradlew.bat bootRun
cd ui
npm install
npm start
```

## REST API Endpoints
- `POST /fix/session` — Create a new FIX session (see `backend_test.rest` for example payloads)
- `GET /fix/sessions` — List all active sessions with status
- `GET /fix/sessions/{id}/messages` — Get messages for a session
- `POST /fix/sessions/{id}/stop` — Stop a session

## React UI
- Sidebar shows all sessions and status (online/offline)
- Main panel displays session details
- Sessions update automatically (polling)

## Testing
Run all backend tests:
```powershell
.\gradlew.bat test
```
All endpoints and session logic are covered by integration tests.
UI can be tested by running the React app and interacting with the sidebar and session form.

## Dependency Management
All plugin and dependency versions are managed in `gradle.properties`.

## Electron Wrapper (Prototype)
The backend can be packaged with an Electron shell for Windows Store delivery. See `README_requirement.md` for architecture and packaging notes. React UI is now implemented and working for session management.

## Useful Commands
- Check for dependency updates:
	```powershell
	.\gradlew.bat dependencyUpdates
	```
- Format code:
	```powershell
	.\gradlew.bat spotlessApply
	```

## References
- QuickFIX/J: https://www.quickfixj.org
- Spring Boot: https://spring.io/projects/spring-boot
- React: https://react.dev
