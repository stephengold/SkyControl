/*
 Copyright (c) 2026, Take Some

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3utilities.sky.command;

import com.jme3.asset.AssetManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import jme3utilities.Validate;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.atmosphere.SkyGradientStyle;
import jme3utilities.sky.cloud.SkyCloudPresetDefinition;
import jme3utilities.sky.cloud.SkyCloudPresetLoader;
import jme3utilities.sky.cloud.SkyCloudPresetRegistry;
import jme3utilities.sky.config.SkySimulationConfig;
import jme3utilities.sky.config.SkySimulationConfigLoader;
import jme3utilities.sky.runtime.SkyEnvironmentSnapshot;

/**
 * Java executor for the SkySimulation command ABI.
 *
 * @author Take Some
 */
final public class SkyCommandBus {
    /** Asset manager used for Lua ABI reloads. */
    final private AssetManager assetManager;
    /** Lua config ABI asset path. */
    final private String configPath;
    /** Target sky control. */
    final private SkyControl skyControl;
    /** Current parsed config. */
    private SkySimulationConfig config;
    /** Current parsed weather registry. */
    private SkyCloudPresetRegistry weatherRegistry;

    /**
     * Instantiate a command bus using the default config ABI.
     *
     * @param assetManager asset manager (not null)
     * @param skyControl target control (not null)
     */
    public SkyCommandBus(AssetManager assetManager, SkyControl skyControl) {
        this(assetManager, skyControl, SkySimulationConfigLoader.defaultConfig);
    }

    /**
     * Instantiate a command bus.
     *
     * @param assetManager asset manager (not null)
     * @param skyControl target control (not null)
     * @param configPath Lua config ABI path (not null, not empty)
     */
    public SkyCommandBus(AssetManager assetManager, SkyControl skyControl,
            String configPath) {
        Validate.nonNull(assetManager, "asset manager");
        Validate.nonNull(skyControl, "control");
        Validate.nonEmpty(configPath, "config path");

        this.assetManager = assetManager;
        this.skyControl = skyControl;
        this.configPath = configPath;
        reloadConfig(false);
    }

    /**
     * Execute a command ABI entry.
     *
     * @param commandId command id (not null, not empty)
     * @param arguments command arguments, or none
     * @return command result
     */
    public SkyCommandResult execute(String commandId, String... arguments) {
        Validate.nonEmpty(commandId, "command id");

        String[] args = arguments == null ? new String[0] : arguments;
        SkyCommandResult result;
        if (SkyCommandIds.atmosphereSetGradient.equals(commandId)) {
            result = execAtmoGradient(args);
        } else if (SkyCommandIds.atmosphereSetSunsetIntensity.equals(
                commandId)) {
            result = execAtmoSunset(args);
        } else if (SkyCommandIds.atmosphereSetSunHaloIntensity.equals(
                commandId)) {
            result = execAtmoSunHalo(args);
        } else if (SkyCommandIds.atmosphereSetMoonHaloIntensity.equals(
                commandId)) {
            result = execAtmoMoonHalo(args);
        } else if (SkyCommandIds.weatherSet.equals(commandId)) {
            result = executeWeatherSet(args);
        } else if (SkyCommandIds.weatherList.equals(commandId)) {
            result = executeWeatherList();
        } else if (SkyCommandIds.clockSetTime.equals(commandId)) {
            result = executeClockSetTime(args);
        } else if (SkyCommandIds.clockAdvance.equals(commandId)) {
            result = executeClockAdvance(args);
        } else if (SkyCommandIds.environmentSnapshot.equals(commandId)) {
            result = executeEnvSnapshot();
        } else if (SkyCommandIds.configReload.equals(commandId)) {
            result = executeConfigReload();
        } else {
            throw new IllegalArgumentException("Unknown sky command: "
                    + commandId);
        }
        return result;
    }

    /**
     * Return current parsed config.
     *
     * @return config
     */
    public SkySimulationConfig config() {
        return config;
    }

    /**
     * Return current weather registry.
     *
     * @return weather registry
     */
    public SkyCloudPresetRegistry weatherRegistry() {
        return weatherRegistry;
    }

    /**
     * Execute atmosphere set-gradient command.
     *
     * @param args command arguments
     * @return command result
     */
    private SkyCommandResult execAtmoGradient(String[] args) {
        String styleId = requireArgument(args, 0,
                SkyCommandIds.atmosphereSetGradient);
        SkyGradientStyle style = parseGradientStyle(styleId);
        float seconds = optionalFloat(args, 1, 0f, "seconds");

        skyControl.setGradientStyle(style, seconds);
        SkyCommandResult result = SkyCommandResult.success(
                SkyCommandIds.atmosphereSetGradient,
                "atmosphere gradient set: " + style.name());
        return result;
    }

    /**
     * Execute atmosphere set-moon-halo-intensity command.
     *
     * @param args command arguments
     * @return command result
     */
    private SkyCommandResult execAtmoMoonHalo(
            String[] args) {
        float intensity = parseFloat(requireArgument(args, 0,
                SkyCommandIds.atmosphereSetMoonHaloIntensity),
                "moon halo intensity");
        float seconds = optionalFloat(args, 1, 0f, "seconds");

        skyControl.setMoonHaloIntensity(intensity, seconds);
        SkyCommandResult result = SkyCommandResult.success(
                SkyCommandIds.atmosphereSetMoonHaloIntensity,
                "moon halo intensity set");
        return result;
    }

    /**
     * Execute atmosphere set-sun-halo-intensity command.
     *
     * @param args command arguments
     * @return command result
     */
    private SkyCommandResult execAtmoSunHalo(
            String[] args) {
        float intensity = parseFloat(requireArgument(args, 0,
                SkyCommandIds.atmosphereSetSunHaloIntensity),
                "sun halo intensity");
        float seconds = optionalFloat(args, 1, 0f, "seconds");

        skyControl.setSunHaloIntensity(intensity, seconds);
        SkyCommandResult result = SkyCommandResult.success(
                SkyCommandIds.atmosphereSetSunHaloIntensity,
                "sun halo intensity set");
        return result;
    }

    /**
     * Execute atmosphere set-sunset-intensity command.
     *
     * @param args command arguments
     * @return command result
     */
    private SkyCommandResult execAtmoSunset(
            String[] args) {
        float intensity = parseFloat(requireArgument(args, 0,
                SkyCommandIds.atmosphereSetSunsetIntensity),
                "sunset intensity");
        float seconds = optionalFloat(args, 1, 0f, "seconds");

        skyControl.setSunsetIntensity(intensity, seconds);
        SkyCommandResult result = SkyCommandResult.success(
                SkyCommandIds.atmosphereSetSunsetIntensity,
                "sunset intensity set");
        return result;
    }

    /**
     * Execute clock advance command.
     *
     * @param args command arguments
     * @return command result
     */
    private SkyCommandResult executeClockAdvance(String[] args) {
        float seconds = parseFloat(requireArgument(args, 0,
                SkyCommandIds.clockAdvance), "seconds");
        float secondsPerDay = parseFloat(requireArgument(args, 1,
                SkyCommandIds.clockAdvance), "seconds per day");

        skyControl.environment().clock().advance(seconds, secondsPerDay);
        SkyCommandResult result = SkyCommandResult.success(
                SkyCommandIds.clockAdvance, "clock advanced");
        return result;
    }

    /**
     * Execute clock set-time command.
     *
     * @param args command arguments
     * @return command result
     */
    private SkyCommandResult executeClockSetTime(String[] args) {
        float hour = parseFloat(requireArgument(args, 0,
                SkyCommandIds.clockSetTime), "hour");

        skyControl.environment().clock().setTimeOfDay(hour);
        SkyCommandResult result = SkyCommandResult.success(
                SkyCommandIds.clockSetTime, "clock time set");
        return result;
    }

    /**
     * Execute config reload command.
     *
     * @return command result
     */
    private SkyCommandResult executeConfigReload() {
        reloadConfig(true);
        SkyCommandResult result = SkyCommandResult.success(
                SkyCommandIds.configReload, "config reloaded");
        return result;
    }

    /**
     * Execute environment snapshot command.
     *
     * @return command result
     */
    private SkyCommandResult executeEnvSnapshot() {
        SkyEnvironmentSnapshot snapshot = skyControl.environment().snapshot();
        SkyCommandResult result = SkyCommandResult.successSnapshot(
                SkyCommandIds.environmentSnapshot, "environment snapshot",
                snapshot);
        return result;
    }

    /**
     * Execute weather list command.
     *
     * @return command result
     */
    private SkyCommandResult executeWeatherList() {
        List<String> ids = new ArrayList<String>(weatherRegistry.ids());
        Collections.sort(ids);
        SkyCommandResult result = SkyCommandResult.successValues(
                SkyCommandIds.weatherList, "weather ids", ids);
        return result;
    }

    /**
     * Execute weather set command.
     *
     * @param args command arguments
     * @return command result
     */
    private SkyCommandResult executeWeatherSet(String[] args) {
        String weatherId = requireArgument(args, 0, SkyCommandIds.weatherSet);
        SkyCloudPresetDefinition definition = weatherRegistry.require(
                weatherId);
        float seconds = optionalFloat(args, 1, definition.defaultSeconds(),
                "seconds");

        skyControl.setCloudPreset(definition, seconds);
        SkyCommandResult result = SkyCommandResult.success(
                SkyCommandIds.weatherSet, "weather set: " + weatherId);
        return result;
    }

    /**
     * Parse an optional float command argument.
     *
     * @param args command arguments
     * @param index argument index
     * @param fallback fallback value
     * @param label diagnostic label
     * @return parsed value or fallback
     */
    private static float optionalFloat(String[] args, int index,
            float fallback, String label) {
        float result = index < args.length
                ? parseFloat(args[index], label) : fallback;
        return result;
    }

    /**
     * Parse a gradient style argument.
     *
     * @param text command argument text
     * @return parsed style
     */
    private static SkyGradientStyle parseGradientStyle(String text) {
        String key = text.trim().toUpperCase(Locale.ROOT);
        SkyGradientStyle result = SkyGradientStyle.valueOf(key);
        return result;
    }

    /**
     * Parse a float command argument.
     *
     * @param text argument text
     * @param label diagnostic label
     * @return parsed value
     */
    private static float parseFloat(String text, String label) {
        try {
            float result = Float.parseFloat(text);
            return result;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid " + label + ": " + text, exception);
        }
    }

    /**
     * Reload Lua config and weather registry.
     *
     * @param applyToControl true to apply config to the target control
     */
    private void reloadConfig(boolean applyToControl) {
        this.config = SkySimulationConfigLoader.load(assetManager, configPath);
        if (applyToControl) {
            config.applyTo(assetManager, skyControl);
        }
        this.weatherRegistry = SkyCloudPresetLoader.load(
                assetManager, config.integration().weatherPath());
    }

    /**
     * Require a positional command argument.
     *
     * @param args command arguments
     * @param index required index
     * @param commandId command id
     * @return argument value
     */
    private static String requireArgument(String[] args, int index,
            String commandId) {
        if (index >= args.length) {
            throw new IllegalArgumentException(
                    "Missing argument " + index + " for " + commandId);
        }
        String result = args[index];
        Validate.nonEmpty(result, "argument");
        return result;
    }
}
