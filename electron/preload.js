// Preload file: expose safe APIs to the renderer.
const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
    // Runs an external executable. Returns a promise that resolves with
    // { code, signal, stdout, stderr } or { error } on failure.
    runExe: (exePath, args = [], options = {}) => ipcRenderer.invoke('run-exe', exePath, args, options),
    // Subscribe to backend status messages while loading
    onBackendStatus: (cb) => {
        if (typeof cb !== 'function') return;
        ipcRenderer.on('backend-status', (event, msg) => cb(msg));
    }
});