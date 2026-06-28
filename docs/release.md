# SkySimulation release process

SkySimulation releases must be validated through the same command path used by GitHub Actions.

## Required order

```text
branch -> PR -> green CI -> merge master -> tag -> release workflow
```

Do not push a release tag before the PR is green and merged into `master`.

## Local release validation

Windows:

```bat
tools\check-release.bat 1.4.4
```

Unix-like shells:

```bash
./tools/check-release.sh 1.4.4
```

Both scripts run the full release gate:

```text
clean build packageLocal
```

The release check must end with `BUILD SUCCESSFUL` and install the expected Maven Local coordinate before a tag is created.

## Tagging after merge

After the PR is merged into `master`, create and push the release tag from `master`:

```bat
git checkout master
git pull --ff-only origin master
git tag -a v1.4.4 -m "SkySimulation v1.4.4"
git push origin v1.4.4
```

The `Release` workflow validates the full build again before publishing to GitHub Packages and creating the GitHub Release.
