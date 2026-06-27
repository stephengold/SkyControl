# SkySimulation v1.4.3

SkySimulation 1.4.3 improves the runtime architecture around game-facing weather events and prepares the package for safer growth behind the protected `master` branch.

## Highlights

- Extracted weather subscription management from `SkyEnvironmentRuntime` into `SkyWeatherSubscriptionRegistry`.
- Reduced weather-event dispatch overhead by avoiding per-event `ArrayList` snapshots and active-list `contains()` checks.
- Preserved the existing typed weather subscription API, cancellable handles, listener isolation, and current-state replay behavior.
- Kept runtime weather observability for gameplay, UI, AI, fog-of-war, and audio systems.
- Added release documentation for the `1.4.3` package coordinates.

## Coordinates

```kotlin
implementation("dev.takesome:sky-simulation:1.4.3")
```

Local development snapshot:

```kotlin
implementation("dev.takesome:sky-simulation:1.4.3-SNAPSHOT")
```

## Validation

- `gradlew.bat :SkyLibrary:test --no-daemon --console=plain --stacktrace`
