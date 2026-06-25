# SkySimulation v1.4.0

SkySimulation 1.4.0 turns the package into a Lua-driven sky runtime bridge for Take Some / Helix / NOESIS style engine integration.

## Highlights

- Added a game-facing runtime layer with environment snapshots, weather metrics, lighting snapshots, and world clock support.
- Added Lua weather ABI support through `helix/lua/sky/weather.lua`.
- Added typed data-driven weather presets through `SkyCloudPresetDefinition`, `SkyCloudPresetRegistry`, and `SkyCloudPresetLoader`.
- Added Lua boot/config ABI support through `helix/lua/sky/default-sky.lua`.
- Added typed `SkySimulationConfig` loading and application for atmosphere profile, initial weather, clock, stars mode, lower dome, cloud flattening, cloud Y offset, and integration defaults.
- Added command ABI support through `helix/lua/sky/commands.lua` and `SkyCommandBus`.
- Added commands: `sky.weather.set`, `sky.weather.list`, `sky.clock.setTime`, `sky.clock.advance`, `sky.environment.snapshot`, and `sky.config.reload`.
- Added `weatherId()` to `SkyEnvironmentSnapshot` for stable external runtime snapshots.
- Preserved the existing `jme3utilities.sky` public namespace and direct Java APIs while adding Lua ABI control paths.
- Strengthened release validation: the release workflow now runs `clean build packageLocal` before publishing.

## Runtime ABI resources

```text
helix/lua/sky/weather.lua
helix/lua/sky/default-sky.lua
helix/lua/sky/commands.lua
```

## Java entry points

```java
SkySimulationConfig config =
        SkySimulationConfigLoader.loadDefault(assetManager);

SkyControl skyControl = config.createControl(assetManager, camera);

SkyCommandBus commandBus = new SkyCommandBus(assetManager, skyControl);
commandBus.execute(SkyCommandIds.weatherSet, "STORM", "90");
commandBus.execute(SkyCommandIds.clockSetTime, "18.5");
SkyCommandResult snapshot =
        commandBus.execute(SkyCommandIds.environmentSnapshot);
```

## Package coordinates

```kotlin
implementation("dev.takesome:sky-simulation:1.4.0")
```

For local development snapshots:

```kotlin
implementation("dev.takesome:sky-simulation:1.4.0-SNAPSHOT")
```

## Validation

Release validation was run with:

```bat
gradlew.bat clean build packageLocal -PskySimulationVersion=1.4.0
```
