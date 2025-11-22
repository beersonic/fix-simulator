// Preload file (empty for now). Use this to expose safe APIs to renderer if needed.

const { contextBridge } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
    // Add safe wrappers here if needed in the future
});