// copy-backend-assets.js
// Copies backend JAR and runtime from jpackage output to Electron's backend/ and runtime/ folders for packaging

const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const jpackageApp = path.join(root, 'dist', 'FixSimulator', 'app');
const jpackageRuntime = path.join(root, 'dist', 'FixSimulator', 'runtime');
const electronBackend = path.join(__dirname, 'backend');
const electronRuntime = path.join(__dirname, 'runtime');

// Ensure backend/ exists
if (!fs.existsSync(electronBackend)) fs.mkdirSync(electronBackend);
if (!fs.existsSync(electronRuntime)) fs.mkdirSync(electronRuntime);

// Copy backend JAR(s)
const jars = fs.readdirSync(jpackageApp).filter(f => f.endsWith('.jar'));
for (const jar of jars) {
  fs.copyFileSync(path.join(jpackageApp, jar), path.join(electronBackend, jar));
}

// Copy runtime directory recursively
function copyDir(src, dest) {
  if (!fs.existsSync(dest)) fs.mkdirSync(dest);
  for (const entry of fs.readdirSync(src)) {
    const srcPath = path.join(src, entry);
    const destPath = path.join(dest, entry);
    if (fs.lstatSync(srcPath).isDirectory()) {
      copyDir(srcPath, destPath);
    } else {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}
copyDir(jpackageRuntime, electronRuntime);

console.log('Backend JAR and runtime copied to Electron app.');
