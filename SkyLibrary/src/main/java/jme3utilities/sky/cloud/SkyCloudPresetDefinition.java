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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jme3utilities.Validate;
import jme3utilities.sky.SkyControlCore;
import jme3utilities.sky.runtime.SkyWeatherMetrics;

/**
 * Data-driven cloud/weather preset definition.
 *
 * @author Take Some
 */
final public class SkyCloudPresetDefinition {
    /** Built-in enum alias, or null for ABI/custom presets. */
    final private SkyCloudPreset builtIn;
    /** Human-readable description. */
    final private String description;
    /** Default transition duration. */
    final private float defaultSeconds;
    /** Stable preset id. */
    final private String id;
    /** Cloud layer targets. */
    final private List<SkyCloudLayerSpec> layers;
    /** Game-facing weather metrics. */
    final private SkyWeatherMetrics metrics;

    /**
     * Instantiate a preset definition.
     *
     * @param id stable preset id (not null, not empty)
     * @param description human-readable description (not null)
     * @param defaultSeconds default transition duration (&ge;0)
     * @param layers cloud layer targets (not null, copied)
     * @param metrics game-facing weather metrics (not null, unaffected)
     */
    public SkyCloudPresetDefinition(String id, String description,
            float defaultSeconds, List<SkyCloudLayerSpec> layers,
            SkyWeatherMetrics metrics) {
        this(id, description, defaultSeconds, layers, metrics, null);
    }

    /**
     * Instantiate a preset definition.
     *
     * @param id stable preset id (not null, not empty)
     * @param description human-readable description (not null)
     * @param defaultSeconds default transition duration (&ge;0)
     * @param layers cloud layer targets (not null, copied)
     * @param metrics game-facing weather metrics (not null, unaffected)
     * @param builtIn built-in enum alias, or null
     */
    private SkyCloudPresetDefinition(String id, String description,
            float defaultSeconds, List<SkyCloudLayerSpec> layers,
            SkyWeatherMetrics metrics, SkyCloudPreset builtIn) {
        Validate.nonEmpty(id, "id");
        Validate.nonNull(description, "description");
        Validate.nonNegative(defaultSeconds, "seconds");
        Validate.nonNull(layers, "layers");
        Validate.nonNull(metrics, "metrics");

        this.id = id;
        this.description = description;
        this.defaultSeconds = defaultSeconds;
        this.layers = Collections.unmodifiableList(
                new ArrayList<SkyCloudLayerSpec>(layers));
        this.metrics = metrics.copy();
        this.builtIn = builtIn;
    }

    /**
     * Create a definition for a built-in preset.
     *
     * @param preset built-in preset (not null)
     * @return new definition
     */
    public static SkyCloudPresetDefinition fromPreset(SkyCloudPreset preset) {
        Validate.nonNull(preset, "preset");

        List<SkyCloudLayerSpec> specs = new ArrayList<SkyCloudLayerSpec>();
        for (int layerI = 0;
                layerI < SkyControlCore.numCloudLayers; ++layerI) {
            specs.add(preset.layer(layerI));
        }
        SkyWeatherMetrics metrics = builtinMetrics(preset);
        SkyCloudPresetDefinition result = new SkyCloudPresetDefinition(
                preset.name(), preset.name(), 60f, specs, metrics, preset);
        return result;
    }

    /**
     * Return the built-in enum alias, if any.
     *
     * @return built-in preset, or null
     */
    public SkyCloudPreset builtIn() {
        return builtIn;
    }

    /**
     * Return the default transition duration.
     *
     * @return duration in seconds
     */
    public float defaultSeconds() {
        return defaultSeconds;
    }

    /**
     * Return the description.
     *
     * @return description
     */
    public String description() {
        return description;
    }

    /**
     * Return the preset id.
     *
     * @return id
     */
    public String id() {
        return id;
    }

    /**
     * Access the target state for a layer.
     *
     * @param layerIndex cloud layer index
     * @return layer target, or clear if no layer is defined there
     */
    public SkyCloudLayerSpec layer(int layerIndex) {
        SkyCloudLayerSpec result = SkyCloudLayerSpec.clear;
        if (layerIndex < layers.size()) {
            result = layers.get(layerIndex);
        }
        return result;
    }

    /**
     * Return the number of explicitly defined layers.
     *
     * @return layer count
     */
    public int layerCount() {
        return layers.size();
    }

    /**
     * Copy the game-facing weather metrics.
     *
     * @return copied metrics
     */
    public SkyWeatherMetrics metrics() {
        return metrics.copy();
    }

    /**
     * Return game-facing metrics for a built-in preset.
     *
     * @param preset built-in preset
     * @return new metrics
     */
    private static SkyWeatherMetrics builtinMetrics(SkyCloudPreset preset) {
        SkyWeatherMetrics result;
        switch (preset) {
            case CLEAR:
                result = new SkyWeatherMetrics(0f, 1f, 0f, 0.05f, 0f);
                break;
            case FAIR:
                result = new SkyWeatherMetrics(0.35f, 0.95f, 0f, 0.10f, 0f);
                break;
            case OVERCAST:
                result = new SkyWeatherMetrics(0.72f, 0.75f, 0f, 0.25f, 0f);
                break;
            case WISPY:
                result = new SkyWeatherMetrics(0.28f, 0.92f, 0f, 0.20f, 0f);
                break;
            case CLOUDY:
                result = new SkyWeatherMetrics(0.55f, 0.78f, 0f, 0.35f, 0f);
                break;
            case RAIN:
                result = new SkyWeatherMetrics(
                        0.75f, 0.55f, 0.75f, 0.55f, 0.02f);
                break;
            case STORM:
                result = new SkyWeatherMetrics(
                        0.90f, 0.35f, 0.95f, 0.90f, 0.20f);
                break;
            case NIMBUS:
                result = new SkyWeatherMetrics(
                        0.82f, 0.45f, 0.80f, 0.70f, 0.08f);
                break;
            default:
                throw new IllegalStateException("preset = " + preset);
        }
        return result;
    }
}
