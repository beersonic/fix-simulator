# FIX Simulator

## Project Structure

This project is a Windows-focused FIX protocol simulator with:
- **Spring Boot backend** (Java, QuickFIX/J) for dynamic FIX session management and REST API.
- **React UI** (in `ui/`) for session creation, listing, and live status display.
- **Electron shell** (in `electron/`) for desktop packaging and Windows Store delivery.
- **Scripts** (in `scripts/`) for development and orchestration.
- **Documentation** (in `docs/`) for requirements, API, and design.

### Folder Overview
- `src/main/java/` — Spring Boot backend source
- `ui/` — React frontend
- `electron/` — Electron desktop shell
- `scripts/` — PowerShell scripts for dev and packaging
- `docs/` — Requirements, API spec, and design docs

## How to Use

### Development Workflow
1. **Start all services (recommended):**
   Use the PowerShell launcher to start backend, UI, and Electron:
   ```powershell
   cd E:\Code\fix-simulator
   .\scripts\run-dev.ps1
   ```
   This opens three windows: backend, React UI, and Electron (desktop shell).

2. **Manual steps (if needed):**
   - Backend:
     ```powershell
     .\gradlew.bat bootRun
     ```
   - UI:
     ```powershell
     cd ui
     npm install --legacy-peer-deps
     npm start
     ```
   - Electron:
     ```powershell
     cd electron
     npm install
     $env:FRONTEND_URL='http://localhost:3000'; npm start
     ```

### REST API Endpoints
- `POST /fix/session` — Create a new FIX session
- `GET /fix/sessions` — List all active sessions
- `GET /fix/sessions/{id}/messages` — Get session messages
- `POST /fix/sessions/{id}/stop` — Stop a session

See `docs/api_spec.md` for full API details and example payloads.

### UI Features
- Sidebar: session list and status
- Main panel: session details
- Add session form
- Live updates via polling

### Electron Shell
- Loads the React UI in a desktop window
- In dev, points to the React dev server (`FRONTEND_URL`)
- In production, can package backend and UI for Windows Store

### Testing
- Backend: `./gradlew.bat test`
- UI: interact with the app in browser or Electron

### Formatting & Dependency Management
- Format code: `./gradlew.bat spotlessApply`
- Check dependencies: `./gradlew.bat dependencyUpdates`

## References
- QuickFIX/J: https://www.quickfixj.org
- Spring Boot: https://spring.io/projects/spring-boot
- React: https://react.dev

## For More Details
- Requirements: `docs/requirement.md`
- API Spec: `docs/api_spec.md`
- Design: `docs/confluence_requirement_design.md`

---
For questions or contributions, see the docs folder or open an issue.
