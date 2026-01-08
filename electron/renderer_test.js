const runBtn = document.getElementById('runBtn');
const output = document.getElementById('output');

runBtn.addEventListener('click', async () => {
  output.textContent = 'Running...';
  const exePath = document.getElementById('exePath').value;
  let args = [];
  try {
    args = JSON.parse(document.getElementById('exeArgs').value);
  } catch (e) {
    output.textContent = 'Invalid args JSON: ' + e.message;
    return;
  }

  try {
    const res = await window.electronAPI.runExe(exePath, args, {});
    output.textContent = JSON.stringify(res, null, 2);
  } catch (err) {
    output.textContent = 'Error: ' + err;
  }
});
