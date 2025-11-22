const { app, BrowserWindow } = require('electron');
const { spawn } = require('child_process');
const path = require('path');

// Configuration: change BACKEND_CMD as needed in production to point to bundled jar
// Example (prod): BACKEND_CMD = "java -jar ./backend/backend.jar"
// For dev, you can run backend separately with `./gradlew.bat bootRun` and skip spawning it.
const BACKEND_CMD = process.env.BACKEND_CMD || null;
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

let backendProcess = null;
let mainWindow = null;

function startBackend() {
    if (!BACKEND_CMD) {
        console.log('BACKEND_CMD not set — assuming backend is started externally (dev mode).');
        return;
    }

    // BACKEND_CMD can be a full shell command; split into executable + args
    const parts = BACKEND_CMD.split(' ');
    const exe = parts[0];
    const args = parts.slice(1);

    console.log('Starting backend:', exe, args);
    backendProcess = spawn(exe, args, { shell: true, cwd: path.resolve(__dirname, '..') });

    backendProcess.stdout.on('data', (data) => {
        console.log('[backend]', data.toString());
    });
    backendProcess.stderr.on('data', (data) => {
        console.error('[backend]', data.toString());
    });

    backendProcess.on('exit', (code, signal) => {
        console.log('Backend exited with', code, signal);
        backendProcess = null;
    });
}

function stopBackend() {
    if (backendProcess) {
        console.log('Stopping backend process...');
        try {
            backendProcess.kill();
        } catch (e) {
            console.error('Failed to kill backend process:', e);
        }
    }
}

function createWindow() {
    mainWindow = new BrowserWindow({
        width: 1200,
        height: 800,
        webPreferences: {
            nodeIntegration: false,
            contextIsolation: true
        }
    });

    mainWindow.loadURL(BACKEND_URL);

    mainWindow.on('closed', function() {
        mainWindow = null;
    });
}

app.whenReady().then(() => {
    startBackend();
    createWindow();

    app.on('activate', function() {
        if (BrowserWindow.getAllWindows().length === 0) createWindow();
    });
});

app.on('window-all-closed', function() {
    // On Windows it's common to quit the app when all windows are closed
    stopBackend();
    if (process.platform !== 'darwin') {
        app.quit();
    }
});

app.on('quit', () => {
    stopBackend();
});