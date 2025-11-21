# fix-simulator

Spring Boot 3 project with Gradle, Spotless, and dependency version checker.

## Features
- Spring Boot 3 REST API
- Spotless for code formatting
- Dependency version checker plugin
- All versions managed in `gradle.properties`

## Build & Run (PowerShell)

```powershell
cd C:\Users\beer_\Code\fix-simulator
.\gradlew.bat clean test
.\gradlew.bat bootRun
```

## Check for dependency updates
```powershell
.\gradlew.bat dependencyUpdates
```

## Format code
```powershell
.\gradlew.bat spotlessApply
```

## Version management
All plugin and dependency versions are managed in `gradle.properties`.

## REST API
- `GET /` returns a greeting string.

## Testing
Spring Boot test verifies the REST endpoint.
