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

import com.jme3.math.ColorRGBA;
import jme3utilities.Validate;
import jme3utilities.sky.cloud.SkyCloudPreset;

/**
 * Immutable game-facing snapshot of sky, weather, and lighting state.
 *
 * @author Take Some
 */
final public class SkyEnvironmentSnapshot {
    /** Ambient light color. */
    final private ColorRGBA ambient;
    /** Recommended bloom intensity. */
    final private float bloom;
    /** Current cloud preset. */
    final private SkyCloudPreset cloudPreset;
    /** Approximate cloud coverage. */
    final private float cloudiness;
    /** Main directional light color. */
    final private ColorRGBA mainLight;
    /** Precipitation intensity. */
    final private float precipitation;
    /** Recommended shadow intensity. */
    final private float shadowIntensity;
    /** Time of day in hours. */
    final private float timeOfDayHours;
    /** Visibility multiplier. */
    final private float visibility;
    /** Wind intensity. */
    final private float windStrength;
    /** Stable weather id. */
    final private String weatherId;

    /**
     * Instantiate an environment snapshot.
     *
     * @param timeOfDayHours time of day in hours
     * @param weather weather state (not null, unaffected)
     * @param lighting lighting snapshot (not null, unaffected)
     */
    public SkyEnvironmentSnapshot(float timeOfDayHours,
            SkyWeatherState weather, SkyLightingSnapshot lighting) {
        Validate.nonNull(weather, "weather");
        Validate.nonNull(lighting, "lighting");

        this.timeOfDayHours = timeOfDayHours;
        this.cloudPreset = weather.cloudPreset();
        this.weatherId = weather.id();
        this.cloudiness = weather.cloudiness();
        this.visibility = weather.visibility();
        this.precipitation = weather.precipitation();
        this.windStrength = weather.windStrength();
        this.ambient = lighting.ambientColor(null);
        this.mainLight = lighting.mainDirectionalColor(null);
        this.bloom = lighting.bloomIntensity();
        this.shadowIntensity = lighting.shadowIntensity();
    }

    /**
     * Copy the ambient light color.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return copied color
     */
    public ColorRGBA ambient(ColorRGBA storeResult) {
        return copyColor(ambient, storeResult);
    }

    /**
     * Return the recommended bloom intensity.
     *
     * @return bloom intensity
     */
    public float bloom() {
        return bloom;
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
     * Return approximate cloud coverage.
     *
     * @return cloudiness fraction
     */
    public float cloudiness() {
        return cloudiness;
    }

    /**
     * Copy the main light color.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return copied color
     */
    public ColorRGBA mainLight(ColorRGBA storeResult) {
        return copyColor(mainLight, storeResult);
    }

    /**
     * Return precipitation intensity.
     *
     * @return precipitation fraction
     */
    public float precipitation() {
        return precipitation;
    }

    /**
     * Return the recommended shadow intensity.
     *
     * @return shadow intensity
     */
    public float shadowIntensity() {
        return shadowIntensity;
    }

    /**
     * Return time of day in hours.
     *
     * @return hours since midnight
     */
    public float timeOfDayHours() {
        return timeOfDayHours;
    }

    /**
     * Return the visibility multiplier.
     *
     * @return visibility fraction
     */
    public float visibility() {
        return visibility;
    }

    /**
     * Return the stable weather id.
     *
     * @return weather id
     */
    public String weatherId() {
        return weatherId;
    }

    /**
     * Return wind intensity.
     *
     * @return wind strength fraction
     */
    public float windStrength() {
        return windStrength;
    }

    /**
     * Copy a color.
     *
     * @param source source color (not null)
     * @param storeResult storage for the result (modified if not null)
     * @return copied color
     */
    private static ColorRGBA copyColor(
            ColorRGBA source, ColorRGBA storeResult) {
        ColorRGBA result = storeResult == null ? new ColorRGBA() : storeResult;
        result.set(source);
        return result;
    }
}
