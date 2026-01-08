const { app, BrowserWindow, ipcMain } = require('electron');
const http = require('http');
const https = require('https');
const { spawn } = require('child_process');
const path = require('path');


// Determine backend command for production (packaged) or dev
const isPackaged = app.isPackaged;
let BACKEND_CMD = process.env.BACKEND_CMD || null;
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';
// Allow overriding which frontend URL Electron should load (dev server)
const FRONTEND_URL = process.env.FRONTEND_URL || process.env.UI_URL || BACKEND_URL;

if (!BACKEND_CMD && isPackaged) {
    // Use the bundled Java runtime and backend JAR
    const runtimePath = path.join(__dirname, 'runtime', 'bin', 'java.exe');
    // Find the backend JAR in backend/
    const backendDir = path.join(__dirname, 'backend');
    const fs = require('fs');
    let jar = null;
    if (fs.existsSync(backendDir)) {
        const jars = fs.readdirSync(backendDir).filter(f => f.endsWith('.jar'));
        if (jars.length > 0) {
            jar = jars[0];
        }
    }
    if (jar) {
        // Store as array for direct use in spawn
        global.BACKEND_EXE = runtimePath;
        global.BACKEND_ARGS = ['-jar', path.join(backendDir, jar)];
        BACKEND_CMD = null; // Not used in this mode
    } else {
        console.error('No backend JAR found in backend/. Backend will not be started.');
    }
}

let backendProcess = null;
let mainWindow = null;

function startBackend() {
    let exe, args;
    if (global.BACKEND_EXE && global.BACKEND_ARGS) {
        exe = global.BACKEND_EXE;
        args = global.BACKEND_ARGS;
        console.log('Starting backend (packaged):', exe, args);
        try {
            backendProcess = spawn(exe, args, { shell: false, cwd: __dirname });
        } catch (err) {
            console.error('Failed to start backend process:', err);
        }
    } else if (BACKEND_CMD) {
        // BACKEND_CMD can be a full shell command; split into executable + args
        const parts = BACKEND_CMD.split(' ');
        exe = parts[0];
        args = parts.slice(1);
        console.log('Starting backend:', exe, args);
        try {
            backendProcess = spawn(exe, args, { shell: true, cwd: __dirname });
        } catch (err) {
            console.error('Failed to start backend process:', err);
        }
    } else {
        console.log('BACKEND_CMD not set — assuming backend is started externally (dev mode).');
        return;
    }

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
            contextIsolation: true,
            preload: path.join(__dirname, 'preload.js')
        }
    });

    // Forward renderer console messages to main process logs for easier debugging
    mainWindow.webContents.on('console-message', (event, level, message, line, sourceId) => {
        console.log(`Renderer[${level}] ${sourceId}:${line} ${message}`);
    });

    // Load a local loading page first while the backend starts
    mainWindow.loadFile(path.join(__dirname, 'loading.html'))
        .catch(err => console.error('Failed to load loading page:', err));

    // Start backend (if configured) and then poll the BACKEND_URL until it's ready.
    // When ready, load the backend UI.
    const pollIntervalMs = 1000;
    const pollTimeoutMs = 120000; // 2 minutes max

    function pollUrl(url, timeoutMs) {
        const start = Date.now();
        const isHttps = url.startsWith('https://');
        const lib = isHttps ? https : http;

        return new Promise((resolve) => {
            const attempt = () => {
                const req = lib.get(url, (res) => {
                    // Consider any 2xx/3xx as success
                    if (res.statusCode && res.statusCode < 400) {
                        resolve({ ok: true, status: res.statusCode });
                    } else {
                        sendStatus(`Waiting for backend (status ${res.statusCode})`);
                        finishOrRetry();
                    }
                    res.resume();
                });
                req.on('error', () => {
                    sendStatus('Waiting for backend (no response)');
                    finishOrRetry();
                });
                req.setTimeout(3000, () => {
                    req.abort();
                    sendStatus('Waiting for backend (timeout)');
                    finishOrRetry();
                });
            };

            function finishOrRetry() {
                if (Date.now() - start > timeoutMs) {
                    resolve({ ok: false, timeout: true });
                } else {
                    setTimeout(attempt, pollIntervalMs);
                }
            }

            attempt();
        });
    }

    function sendStatus(msg) {
        try {
            if (mainWindow && mainWindow.webContents) {
                mainWindow.webContents.send('backend-status', msg);
            }
        } catch (e) { /* ignore */ }
        console.log('[backend-status]', msg);
    }

    // Start backend (if applicable) and then poll
    startBackend();
    pollUrl(BACKEND_URL, pollTimeoutMs).then(result => {
        if (result.ok) {
            sendStatus('Backend is ready — loading UI');
            mainWindow.loadURL(FRONTEND_URL).catch(err => console.error('Failed to load frontend URL:', err));
        } else {
            sendStatus('Backend not available (timeout) — loading UI anyway');
            // still try to load the frontend so user can inspect errors
            mainWindow.loadURL(FRONTEND_URL).catch(err => console.error('Failed to load frontend URL:', err));
        }
    });

    mainWindow.on('closed', function() {
        mainWindow = null;
    });

        // Forward renderer console messages to the main process console for easier debugging.
        try {
            mainWindow.webContents.on('console-message', (event, level, message, line, sourceId) => {
                console.log(`[renderer console] [level ${level}] ${message} (${sourceId}:${line})`);
            });

            mainWindow.webContents.on('did-finish-load', () => {
                console.log('Renderer finished loading:', mainWindow.webContents.getURL());
            });
        } catch (e) {
            console.error('Failed to attach renderer console handlers:', e && e.message);
        }
}

// IPC handler to run an external executable and return its output when it exits.
ipcMain.handle('run-exe', async (event, exePath, args = [], options = {}) => {
    return new Promise((resolve) => {
        try {
            const child = spawn(exePath, args, { shell: false, cwd: options.cwd || __dirname });
            let stdout = '';
            let stderr = '';
            if (child.stdout) child.stdout.on('data', (d) => { stdout += d.toString(); });
            if (child.stderr) child.stderr.on('data', (d) => { stderr += d.toString(); });
            child.on('error', (err) => resolve({ error: err.message }));
            child.on('close', (code, signal) => resolve({ code, signal, stdout, stderr }));
        } catch (err) {
            resolve({ error: err.message });
        }
    });
});

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