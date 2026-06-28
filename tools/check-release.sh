#!/usr/bin/env bash
set -euo pipefail

SKY_SIMULATION_VERSION="${1:-1.4.4}"

echo "Running SkySimulation release check for ${SKY_SIMULATION_VERSION}"
./gradlew --no-daemon clean build packageLocal --console=plain --stacktrace -PskySimulationVersion="${SKY_SIMULATION_VERSION}"
