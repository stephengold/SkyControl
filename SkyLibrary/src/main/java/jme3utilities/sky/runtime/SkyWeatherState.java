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
package jme3utilities.sky.runtime;

import jme3utilities.Validate;
import jme3utilities.sky.cloud.SkyCloudPreset;
import jme3utilities.sky.cloud.SkyCloudPresetDefinition;

/**
 * Immutable weather state exported by the sky runtime.
 * <p>
 * This state intentionally describes game-facing conditions instead of only
 * cloud textures. It can drive visibility, precipitation, wind, and storm logic
 * outside the renderer.
 *
 * @author Take Some
 */
final public class SkyWeatherState {
    /** Built-in cloud preset, or null for ABI/custom presets. */
    final private SkyCloudPreset cloudPreset;
    /** Stable weather id. */
    final private String id;
    /** Game-facing weather metrics. */
    final private SkyWeatherMetrics metrics;

    /**
     * Instantiate weather state.
     *
     * @param id stable weather id (not null, not empty)
     * @param cloudPreset cloud preset (not null)
     * @param metrics game-facing metrics (not null, unaffected)
     */
    public SkyWeatherState(String id, SkyCloudPreset cloudPreset,
            SkyWeatherMetrics metrics) {
        Validate.nonEmpty(id, "id");
        Validate.nonNull(cloudPreset, "cloud preset");
        Validate.nonNull(metrics, "metrics");

        this.id = id;
        this.cloudPreset = cloudPreset;
        this.metrics = metrics.copy();
    }

    /**
     * Instantiate weather state from a data-driven definition.
     *
     * @param definition preset definition (not null, unaffected)
     */
    public SkyWeatherState(SkyCloudPresetDefinition definition) {
        Validate.nonNull(definition, "definition");

        this.id = definition.id();
        this.cloudPreset = definition.builtIn();
        this.metrics = definition.metrics();
    }

    /**
     * Return default fair-weather state.
     *
     * @return new state
     */
    public static SkyWeatherState fair() {
        return fromPreset(SkyCloudPreset.FAIR);
    }

    /**
     * Create game-facing weather state for a built-in cloud preset.
     *
     * @param preset built-in preset (not null)
     * @return new state
     */
    public static SkyWeatherState fromPreset(SkyCloudPreset preset) {
        Validate.nonNull(preset, "preset");

        SkyWeatherState result;
        switch (preset) {
            case CLEAR:
                result = state("clear", preset, metrics(0f, 1f, 0f, 0.05f, 0f));
                break;

            case FAIR:
                result = state("fair", preset, metrics(
                        0.35f, 0.95f, 0f, 0.10f, 0f));
                break;

            case OVERCAST:
                result = state(
                        "overcast", preset, metrics(
                                0.72f, 0.75f, 0f, 0.25f, 0f));
                break;

            case WISPY:
                result = state("wispy", preset, metrics(
                        0.28f, 0.92f, 0f, 0.20f, 0f));
                break;

            case CLOUDY:
                result = state(
                        "cloudy", preset, metrics(
                                0.55f, 0.78f, 0f, 0.35f, 0f));
                break;

            case RAIN:
                result = state(
                        "rain", preset, metrics(
                                0.75f, 0.55f, 0.75f, 0.55f, 0.02f));
                break;

            case STORM:
                result = state(
                        "storm", preset, metrics(
                                0.90f, 0.35f, 0.95f, 0.90f, 0.20f));
                break;

            case NIMBUS:
                result = state(
                        "nimbus", preset, metrics(
                                0.82f, 0.45f, 0.80f, 0.70f, 0.08f));
                break;

            default:
                throw new IllegalStateException("preset = " + preset);
        }

        return result;
    }


    /**
     * Create game-facing weather state for a data-driven definition.
     *
     * @param definition preset definition (not null, unaffected)
     * @return new state
     */
    public static SkyWeatherState fromDefinition(
            SkyCloudPresetDefinition definition) {
        SkyWeatherState result = new SkyWeatherState(definition);
        return result;
    }

    /**
     * Return the cloud preset.
     *
     * @return cloud preset
     */
    public SkyCloudPreset cloudPreset() {
        return cloudPreset;
    }

    /**
     * Return the stable preset id.
     *
     * @return preset id
     */
    public String presetId() {
        return id;
    }

    /**
     * Return approximate cloud coverage.
     *
     * @return cloudiness fraction
     */
    public float cloudiness() {
        return metrics.cloudiness();
    }

    /**
     * Copy this weather state.
     *
     * @return new equivalent state
     */
    public SkyWeatherState copy() {
        SkyWeatherState result;
        if (cloudPreset == null) {
            SkyCloudPresetDefinition definition = new SkyCloudPresetDefinition(
                    id, id, 0f, java.util.Collections.emptyList(), metrics);
            result = new SkyWeatherState(definition);
        } else {
            result = new SkyWeatherState(id, cloudPreset, metrics);
        }
        return result;
    }

    /**
     * Return the stable weather id.
     *
     * @return weather id
     */
    public String id() {
        return id;
    }

    /**
     * Test whether this state should be treated as storm weather.
     *
     * @return true if storm-like
     */
    public boolean isStorm() {
        return cloudPreset == SkyCloudPreset.STORM
                || "STORM".equals(id)
                || "storm".equals(id)
                || metrics.lightningChance() > 0.1f;
    }

    /**
     * Return lightning probability/intensity hint.
     *
     * @return lightning chance
     */
    public float lightningChance() {
        return metrics.lightningChance();
    }

    /**
     * Copy the full game-facing metrics.
     *
     * @return copied metrics
     */
    public SkyWeatherMetrics metrics() {
        return metrics.copy();
    }

    /**
     * Return precipitation intensity.
     *
     * @return precipitation fraction
     */
    public float precipitation() {
        return metrics.precipitation();
    }

    /**
     * Return visibility multiplier exposed to simulation code.
     *
     * @return visibility fraction
     */
    public float visibility() {
        return metrics.visibility();
    }

    /**
     * Return wind intensity.
     *
     * @return wind strength fraction
     */
    public float windStrength() {
        return metrics.windStrength();
    }

    /**
     * Describe this weather state.
     *
     * @return description string
     */
    @Override
    public String toString() {
        return "SkyWeatherState[id=" + id
                + ", cloudPreset=" + cloudPreset
                + ", metrics=" + metrics + "]";
    }

    /**
     * Create a weather state from raw metrics.
     *
     * @param cloudiness visible cloud coverage
     * @param visibility visibility multiplier
     * @param precipitation precipitation intensity
     * @param windStrength wind intensity
     * @param lightningChance lightning chance/intensity
     * @return new state
     */
    private static SkyWeatherMetrics metrics(float cloudiness,
            float visibility, float precipitation, float windStrength,
            float lightningChance) {
        SkyWeatherMetrics result = new SkyWeatherMetrics(cloudiness,
                visibility, precipitation, windStrength, lightningChance);
        return result;
    }

    /**
     * Create a weather state from metrics.
     *
     * @param id stable weather id
     * @param preset cloud preset
     * @param metrics game-facing metrics
     * @return new state
     */
    private static SkyWeatherState state(String id, SkyCloudPreset preset,
            SkyWeatherMetrics metrics) {
        SkyWeatherState result = new SkyWeatherState(id, preset, metrics);
        return result;
    }
}
