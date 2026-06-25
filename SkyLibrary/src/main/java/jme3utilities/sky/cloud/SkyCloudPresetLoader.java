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
package jme3utilities.sky.cloud;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import jme3utilities.Validate;
import jme3utilities.sky.runtime.SkyWeatherMetrics;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * Loader for Lua weather ABI preset registries.
 *
 * @author Take Some
 */
final public class SkyCloudPresetLoader {
    /** Default Lua ABI registry resource. */
    final public static String defaultRegistry
            = SkyCloudAssets.luaRegistry;

    /** Hidden constructor. */
    private SkyCloudPresetLoader() {
        // do nothing
    }

    /**
     * Load a Lua weather ABI registry.
     *
     * @param assetManager asset manager (not null)
     * @param assetPath Lua asset path (not null, not empty)
     * @return loaded registry
     */
    public static SkyCloudPresetRegistry load(AssetManager assetManager,
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
                    "Invalid sky weather Lua ABI: " + assetPath, exception);
        }
        SkyCloudPresetRegistry result = parseRegistry(root, assetPath);
        return result;
    }

    /**
     * Load the default bundled Lua weather ABI registry.
     *
     * @param assetManager asset manager (not null)
     * @return loaded registry
     */
    public static SkyCloudPresetRegistry loadDefault(
            AssetManager assetManager) {
        SkyCloudPresetRegistry result = load(assetManager, defaultRegistry);
        return result;
    }

    /**
     * Parse one preset definition.
     *
     * @param id preset id
     * @param value Lua preset table
     * @return parsed definition
     */
    private static SkyCloudPresetDefinition parseDefinition(
            String id, LuaValue value) {
        requireTable(value, id);

        String description = optionalString(value, "description", id);
        float seconds = optionalFloat(value, "transitionSeconds", 60f);
        SkyWeatherMetrics metrics = parseMetrics(value.get("world"));
        List<SkyCloudLayerSpec> layers = parseLayers(value.get("layers"));
        SkyCloudPresetDefinition result = new SkyCloudPresetDefinition(
                id, description, seconds, layers, metrics);
        return result;
    }

    /**
     * Parse one cloud layer table.
     *
     * @param value Lua layer table
     * @return parsed layer spec
     */
    private static SkyCloudLayerSpec parseLayer(LuaValue value) {
        requireTable(value, "layer");

        String alphaMap = requiredString(value, "alphaMap");
        String normalMap = optionalString(value, "normalMap", null);
        float opacity = requiredFloat(value, "opacity");
        float scale = requiredFloat(value, "scale");
        float uRate = requiredFloat(value, "uRate");
        float vRate = requiredFloat(value, "vRate");
        SkyCloudLayerSpec result;
        if (normalMap == null) {
            result = new SkyCloudLayerSpec(
                    alphaMap, opacity, scale, uRate, vRate);
        } else {
            result = new SkyCloudLayerSpec(
                    alphaMap, normalMap, opacity, scale, uRate, vRate);
        }
        return result;
    }

    /**
     * Parse cloud layers.
     *
     * @param value Lua layers array
     * @return layer specs
     */
    private static List<SkyCloudLayerSpec> parseLayers(LuaValue value) {
        requireTable(value, "layers");

        List<SkyCloudLayerSpec> result = new ArrayList<SkyCloudLayerSpec>();
        int length = value.length();
        for (int layerI = 1; layerI <= length; ++layerI) {
            result.add(parseLayer(value.get(layerI)));
        }
        return result;
    }

    /**
     * Parse weather metrics.
     *
     * @param value Lua world table
     * @return parsed metrics
     */
    private static SkyWeatherMetrics parseMetrics(LuaValue value) {
        requireTable(value, "world");

        SkyWeatherMetrics result = new SkyWeatherMetrics(
                requiredFloat(value, "cloudiness"),
                requiredFloat(value, "visibility"),
                requiredFloat(value, "precipitation"),
                requiredFloat(value, "windStrength"),
                requiredFloat(value, "lightningChance"));
        return result;
    }

    /**
     * Parse a root Lua ABI table.
     *
     * @param root root table
     * @param assetPath asset path for diagnostics
     * @return parsed registry
     */
    private static SkyCloudPresetRegistry parseRegistry(
            LuaValue root, String assetPath) {
        requireTable(root, assetPath);

        LuaValue presets = root.get("presets");
        requireTable(presets, "presets");

        List<SkyCloudPresetDefinition> list
                = new ArrayList<SkyCloudPresetDefinition>();
        LuaValue key = LuaValue.NIL;
        while (true) {
            Varargs pair = presets.next(key);
            key = pair.arg1();
            if (key.isnil()) {
                break;
            }
            LuaValue value = pair.arg(2);
            list.add(parseDefinition(key.checkjstring(), value));
        }
        SkyCloudPresetRegistry result = new SkyCloudPresetRegistry(list);
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
                    "Failed to read sky weather Lua ABI: " + assetPath,
                    exception);
        }
        String result = builder.toString();
        return result;
    }

    /**
     * Read an optional float field.
     *
     * @param table Lua table
     * @param name field name
     * @param fallback fallback value
     * @return parsed value
     */
    private static float optionalFloat(LuaValue table, String name,
            float fallback) {
        LuaValue value = table.get(name);
        float result = value.isnil() ? fallback : (float) value.checkdouble();
        return result;
    }

    /**
     * Read an optional string field.
     *
     * @param table Lua table
     * @param name field name
     * @param fallback fallback value
     * @return parsed value
     */
    private static String optionalString(LuaValue table, String name,
            String fallback) {
        LuaValue value = table.get(name);
        String result = value.isnil() ? fallback : value.checkjstring();
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
