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
package jme3utilities.sky.config;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import jme3utilities.Validate;
import jme3utilities.sky.StarsOption;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * Loader for Lua SkySimulation configuration ABI.
 *
 * @author Take Some
 */
final public class SkySimulationConfigLoader {
    /** Default Lua config ABI resource. */
    final public static String defaultConfig
            = "helix/lua/sky/default-sky.lua";

    /** Hidden constructor. */
    private SkySimulationConfigLoader() {
        // do nothing
    }

    /**
     * Load a Lua sky simulation configuration.
     *
     * @param assetManager asset manager (not null)
     * @param assetPath Lua asset path (not null, not empty)
     * @return loaded configuration
     */
    public static SkySimulationConfig load(AssetManager assetManager,
            String assetPath) {
        Validate.nonNull(assetManager, "asset manager");
        Validate.nonEmpty(assetPath, "asset path");

        String source = readText(assetManager, assetPath);
        Globals globals = JsePlatform.standardGlobals();
        LuaValue root;
        try {
            root = globals.load(source, assetPath).call();
        } catch (LuaError exception) {
            throw new IllegalArgumentException(
                    "Invalid sky config Lua ABI: " + assetPath, exception);
        }
        SkySimulationConfig result = parseRoot(root, assetPath);
        return result;
    }

    /**
     * Load the default bundled Lua config ABI.
     *
     * @param assetManager asset manager (not null)
     * @return loaded configuration
     */
    public static SkySimulationConfig loadDefault(AssetManager assetManager) {
        SkySimulationConfig result = load(assetManager, defaultConfig);
        return result;
    }

    /**
     * Parse clock section.
     *
     * @param value Lua clock table
     * @return parsed clock config
     */
    private static SkyClockConfig parseClock(LuaValue value) {
        requireTable(value, "clock");

        SkyClockConfig result = new SkyClockConfig(
                requiredFloat(value, "hour"),
                requiredFloat(value, "observerLatitudeDegrees"),
                requiredInt(value, "solarMonth"),
                requiredInt(value, "solarDay"));
        return result;
    }

    /**
     * Parse integration and weather sections.
     *
     * @param root root Lua table
     * @return parsed integration config
     */
    private static SkyIntegrationConfig parseIntegration(LuaValue root) {
        LuaValue atmosphere = root.get("atmosphere");
        LuaValue weather = root.get("weather");
        LuaValue integration = root.get("integration");
        requireTable(atmosphere, "atmosphere");
        requireTable(weather, "weather");
        requireTable(integration, "integration");

        SkyIntegrationConfig result = new SkyIntegrationConfig(
                requiredString(atmosphere, "profile"),
                requiredString(weather, "registry"),
                requiredString(weather, "initial"),
                requiredFloat(weather, "transitionSeconds"),
                requiredBoolean(integration, "cloudModulation"));
        return result;
    }

    /**
     * Parse render section.
     *
     * @param value Lua render table
     * @return parsed render config
     */
    private static SkyRenderConfig parseRender(LuaValue value) {
        requireTable(value, "rendering");

        StarsOption starsOption = StarsOption.valueOf(
                requiredString(value, "stars"));
        SkyRenderConfig result = new SkyRenderConfig(starsOption,
                requiredFloat(value, "cloudFlattening"),
                requiredFloat(value, "cloudsYOffset"),
                requiredBoolean(value, "lowerDome"));
        return result;
    }

    /**
     * Parse a root Lua ABI table.
     *
     * @param root root table
     * @param assetPath asset path for diagnostics
     * @return parsed config
     */
    private static SkySimulationConfig parseRoot(
            LuaValue root, String assetPath) {
        requireTable(root, assetPath);

        SkyClockConfig clock = parseClock(root.get("clock"));
        SkyRenderConfig render = parseRender(root.get("rendering"));
        SkyIntegrationConfig integration = parseIntegration(root);
        SkySimulationConfig result = new SkySimulationConfig(
                clock, render, integration);
        return result;
    }

    /**
     * Read a text asset.
     *
     * @param assetManager asset manager
     * @param assetPath asset path
     * @return asset contents
     */
    private static String readText(AssetManager assetManager,
            String assetPath) {
        AssetKey<Object> key = new AssetKey<Object>(assetPath);
        AssetInfo info = assetManager.locateAsset(key);
        if (info == null) {
            throw new AssetNotFoundException(assetPath);
        }

        StringBuilder builder = new StringBuilder();
        try (InputStream input = info.openStream();
                InputStreamReader reader = new InputStreamReader(
                        input, StandardCharsets.UTF_8);
                BufferedReader buffered = new BufferedReader(reader)) {
            String line;
            while ((line = buffered.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Failed to read sky config Lua ABI: " + assetPath,
                    exception);
        }
        String result = builder.toString();
        return result;
    }

    /**
     * Read a required boolean field.
     *
     * @param table Lua table
     * @param name field name
     * @return parsed value
     */
    private static boolean requiredBoolean(LuaValue table, String name) {
        LuaValue value = table.get(name);
        if (value.isnil()) {
            throw new IllegalArgumentException(
                    "Missing boolean field: " + name);
        }
        boolean result = value.checkboolean();
        return result;
    }

    /**
     * Read a required float field.
     *
     * @param table Lua table
     * @param name field name
     * @return parsed value
     */
    private static float requiredFloat(LuaValue table, String name) {
        LuaValue value = table.get(name);
        if (value.isnil()) {
            throw new IllegalArgumentException("Missing float field: " + name);
        }
        float result = (float) value.checkdouble();
        return result;
    }

    /**
     * Read a required integer field.
     *
     * @param table Lua table
     * @param name field name
     * @return parsed value
     */
    private static int requiredInt(LuaValue table, String name) {
        LuaValue value = table.get(name);
        if (value.isnil()) {
            throw new IllegalArgumentException("Missing int field: " + name);
        }
        int result = value.checkint();
        return result;
    }

    /**
     * Read a required string field.
     *
     * @param table Lua table
     * @param name field name
     * @return parsed value
     */
    private static String requiredString(LuaValue table, String name) {
        LuaValue value = table.get(name);
        if (value.isnil()) {
            throw new IllegalArgumentException("Missing string field: " + name);
        }
        String result = value.checkjstring();
        return result;
    }

    /**
     * Require a Lua table value.
     *
     * @param value Lua value
     * @param label diagnostic label
     */
    private static void requireTable(LuaValue value, String label) {
        if (value == null || !value.istable()) {
            throw new IllegalArgumentException("Expected table: " + label);
        }
    }
}
