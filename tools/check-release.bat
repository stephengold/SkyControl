@echo off
setlocal

set SKY_SIMULATION_VERSION=%~1
if "%SKY_SIMULATION_VERSION%"=="" set SKY_SIMULATION_VERSION=1.4.4

echo Running SkySimulation release check for %SKY_SIMULATION_VERSION%
call gradlew.bat --no-daemon clean build packageLocal --console=plain --stacktrace -PskySimulationVersion=%SKY_SIMULATION_VERSION%
