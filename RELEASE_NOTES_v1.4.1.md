# SkySimulation v1.4.1

SkySimulation 1.4.1 adds runtime-controllable atmospheric gradient styling for Helix / NOESIS driven worlds.

## Highlights

- Add physically-inspired horizon gradient weighting for sunrise, sunset, and low-altitude moonlight.
- Add `SkyGradientStyle` presets: `REALISTIC`, `CINEMATIC`, and `FANTASY`.
- Add `SkyAtmosphere` runtime controls for `gradientStyle`, `sunsetIntensity`, `sunHaloIntensity`, and `moonHaloIntensity`.
- Add Lua config ABI fields under `atmosphere` for gradient style and halo/sunset intensities.
- Add Lua gradient presets in `helix/lua/sky/default-sky.lua`.
- Add runtime atmosphere command ABI entries in `helix/lua/sky/commands.lua`:
  - `sky.atmosphere.setGradient`
  - `sky.atmosphere.setSunsetIntensity`
  - `sky.atmosphere.setSunHaloIntensity`
  - `sky.atmosphere.setMoonHaloIntensity`
- Extend `SkyCommandBus` so NOESIS / Helix can change atmospheric style at runtime without rebuilding Java.
- Improve GitHub CI for push, pull request, and manual validation on Java 17 Linux and Windows.
- Harden release workflow by using `--no-daemon` for Gradle commands.

## Runtime command example

```lua
sky.atmosphere.setGradient("FANTASY")
sky.atmosphere.setSunsetIntensity(2.0)
sky.atmosphere.setSunHaloIntensity(1.7)
sky.atmosphere.setMoonHaloIntensity(1.8)
```

## Package coordinates

```kotlin
implementation("dev.takesome:sky-simulation:1.4.1")
```

For local development builds:

```kotlin
implementation("dev.takesome:sky-simulation:1.4.1-SNAPSHOT")
```

## Validation

Validated with:

```bat
gradlew.bat --no-daemon clean build packageLocal --console=plain --stacktrace -PskySimulationVersion=1.4.1
```
