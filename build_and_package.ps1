# Windows PowerShell build script for packaging FIX Simulator as MSIX
# Save as build_and_package.ps1 in the project root

# 1. Build backend JAR
Write-Host "Building backend JAR..."
cd "E:\Code\fix-simulator"
./gradlew.bat clean bootJar

# 2. Build React UI
Write-Host "Building React UI..."
cd "E:\Code\fix-simulator\ui"
npm install
npm run build

# 3. Bundle Java runtime with jpackage
Write-Host "Bundling Java runtime with jpackage..."
cd "E:\Code\fix-simulator"
$jars = Get-ChildItem -Path .\build\libs -Filter "fix-simulator-*.jar" | Select-Object -First 1
if ($null -eq $jars) { throw "JAR not found!" }
jpackage --type app-image --input .\build\libs --main-jar $jars.Name --name FixSimulator --dest dist

# 4. Build Electron app
Write-Host "Building Electron app..."
cd "E:\Code\fix-simulator\electron"
npm install
npm run build

# 5. Package Electron app as MSIX
Write-Host "Packaging Electron app as MSIX..."
npm install electron-builder --save-dev
npx electron-builder --win --msix

Write-Host "Build and packaging complete! Check the 'electron/dist' folder for the MSIX package."
