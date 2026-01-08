const statusEl = document.getElementById('status');
const detailsEl = document.getElementById('details');

function pushDetail(line) {
  detailsEl.textContent = (detailsEl.textContent + '\n' + line).trim();
}

if (window.electronAPI && window.electronAPI.onBackendStatus) {
  window.electronAPI.onBackendStatus((msg) => {
    statusEl.textContent = msg;
    pushDetail(msg);
  });
} else {
  pushDetail('electronAPI not available in preload (contextBridge).');
}

// Provide a timeout message if nothing comes through
setTimeout(() => {
  pushDetail('Still waiting... If nothing happens, check the backend logs or run `./gradlew.bat bootRun`.');
}, 15000);
