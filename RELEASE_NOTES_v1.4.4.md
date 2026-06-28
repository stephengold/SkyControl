# SkySimulation v1.4.4

SkySimulation 1.4.4 is a release-hardening patch focused on predictable CI validation, protected-branch release discipline, and weather runtime test coverage.

## Highlights

- Added shared release-check scripts for Windows and Unix-like shells.
- Aligned CI and release workflows around the full `clean build packageLocal` path.
- Documented the protected branch release order: branch, PR, green CI, merge, tag, release workflow.
- Added a pull request checklist for release hygiene.
- Expanded weather runtime tests for listener removal, failure isolation, and cancellation during dispatch.
- Updated package coordinates and documentation for `dev.takesome:sky-simulation:1.4.4`.

## Validation

```bat
tools\check-release.bat 1.4.4
```

or:

```bash
./tools/check-release.sh 1.4.4
```

Expected result:

```text
BUILD SUCCESSFUL
installed locally as dev.takesome:sky-simulation:1.4.4
```

## Coordinates

```kotlin
implementation("dev.takesome:sky-simulation:1.4.4")
```

Local development snapshot:

```kotlin
implementation("dev.takesome:sky-simulation:1.4.4-SNAPSHOT")
```

## Compatibility

No runtime API-breaking changes are intended in 1.4.4.
