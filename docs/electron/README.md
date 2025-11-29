Electron wrapper (prototype)

This folder contains a minimal Electron scaffold to host the web UI (served by the Spring Boot backend) inside a desktop shell.

Goals
- Provide a small desktop shell which opens the backend UI at `http://localhost:8080`.
- In production, the Electron app should start a bundled backend JAR; in development, you can run `./gradlew.bat bootRun` and then start Electron.

Dev instructions
1. Install Node.js (16+ recommended) and npm.
2. From this folder run `npm install` to install Electron.
3. Start the backend in dev mode in the project root:
   - PowerShell: `..\gradlew.bat bootRun`
4. Start Electron from this folder:
   - `npm start`

Production notes
- Set the `BACKEND_CMD` environment variable to the command that starts the bundled backend JAR. Example:
  - `set BACKEND_CMD=java -jar ./backend/backend.jar`
- The packaged Electron app will run `BACKEND_CMD` to start the backend. Alternatively, the backend can be started in the same JVM when switching to a pure Java desktop app.

Packaging to MSIX
- packaging will be a two-step process:
  1. Package the backend into a single runnable JAR and bundle a trimmed JRE (use `jpackage` or `jlink`).
  2. Package the Electron app into an MSIX/AppX (use electron-builder or MSIX Packaging Tool) and include the backend and JRE in the package.

Notes and caveats
- This prototype uses `BrowserWindow.loadURL(BACKEND_URL)` and assumes the backend serves the UI at that URL.
- Consider selecting a random free port dynamically in production and passing it to Electron via env var or args to avoid conflicts.
- Ensure the Appx manifest includes required network capabilities for TCP socket operations.
