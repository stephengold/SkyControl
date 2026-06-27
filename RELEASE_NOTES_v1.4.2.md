# SkySimulation v1.4.2

SkySimulation 1.4.2 improves runtime control and observability for atmosphere and weather systems.

## Highlights

- Added smooth runtime atmosphere transitions for gradient style, sunset intensity, sun halo intensity, and moon halo intensity.
- Extended the atmosphere command ABI with optional `transitionSeconds` arguments for live cinematic changes.
- Added typed game-facing weather subscriptions through `SkyEnvironmentRuntime`.
- Added subscription filters for exact weather ids, built-in presets, storm-like states, precipitation thresholds, cloudiness thresholds, wind thresholds, and all weather changes.
- Added cancellable `SkyWeatherSubscription` handles and immutable `SkyWeatherEvent` payloads.
- Expanded logging for Lua weather ABI loading, preset parsing, weather state changes, cloud transition lifecycle, generated sky material creation, and cloud normal-map application.
- Added compressed DDS/BC texture diagnostics for cloud normals: dimensions, mip levels, FourCC/code, jME image format, and block byte size.
- Added tests for atmosphere transitions and weather subscriptions.

## Coordinates

```kotlin
implementation("dev.takesome:sky-simulation:1.4.2")
```

Local development snapshot:

```kotlin
implementation("dev.takesome:sky-simulation:1.4.2-SNAPSHOT")
```

## Validation

```bat
gradlew.bat :SkyLibrary:test --no-daemon
```
