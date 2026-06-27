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

import java.util.Locale;
import jme3utilities.Validate;
import jme3utilities.sky.cloud.SkyCloudPreset;

/**
 * Factory methods for common weather subscription filters.
 *
 * @author Take Some
 */
final public class SkyWeatherFilters {
    /** Filter that accepts all weather states. */
    final private static SkyWeatherFilter any = new SkyWeatherFilter() {
        @Override
        public boolean matches(SkyWeatherState state) {
            return true;
        }

        @Override
        public String toString() {
            return "any-weather";
        }
    };

    /** Hidden constructor. */
    private SkyWeatherFilters() {
        // do nothing
    }

    /**
     * Match any weather state.
     *
     * @return reusable filter
     */
    public static SkyWeatherFilter any() {
        return any;
    }

    /**
     * Match states with at least the specified cloudiness.
     *
     * @param minCloudiness minimum cloudiness fraction (&le;1, &ge;0)
     * @return new filter
     */
    public static SkyWeatherFilter cloudy(float minCloudiness) {
        Validate.fraction(minCloudiness, "minimum cloudiness");
        return new SkyWeatherFilter() {
            @Override
            public boolean matches(SkyWeatherState state) {
                return state.cloudiness() >= minCloudiness;
            }

            @Override
            public String toString() {
                return "cloudiness>=" + minCloudiness;
            }
        };
    }

    /**
     * Match a stable weather id case-insensitively.
     *
     * @param weatherId weather id (not null, not empty)
     * @return new filter
     */
    public static SkyWeatherFilter id(String weatherId) {
        Validate.nonEmpty(weatherId, "weather id");
        final String normalized = weatherId.trim().toLowerCase(Locale.ROOT);
        return new SkyWeatherFilter() {
            @Override
            public boolean matches(SkyWeatherState state) {
                return state.id().toLowerCase(Locale.ROOT).equals(normalized)
                        || state.presetId().toLowerCase(Locale.ROOT)
                                .equals(normalized);
            }

            @Override
            public String toString() {
                return "weather-id=" + normalized;
            }
        };
    }

    /**
     * Match states with at least the specified precipitation.
     *
     * @param minIntensity minimum precipitation fraction (&le;1, &ge;0)
     * @return new filter
     */
    public static SkyWeatherFilter precipitating(float minIntensity) {
        Validate.fraction(minIntensity, "minimum precipitation");
        return new SkyWeatherFilter() {
            @Override
            public boolean matches(SkyWeatherState state) {
                return state.precipitation() >= minIntensity;
            }

            @Override
            public String toString() {
                return "precipitation>=" + minIntensity;
            }
        };
    }

    /**
     * Match a built-in cloud preset.
     *
     * @param preset built-in preset (not null)
     * @return new filter
     */
    public static SkyWeatherFilter preset(SkyCloudPreset preset) {
        Validate.nonNull(preset, "preset");
        return new SkyWeatherFilter() {
            @Override
            public boolean matches(SkyWeatherState state) {
                return state.cloudPreset() == preset;
            }

            @Override
            public String toString() {
                return "preset=" + preset.name();
            }
        };
    }

    /**
     * Match storm-like weather.
     *
     * @return new filter
     */
    public static SkyWeatherFilter storm() {
        return new SkyWeatherFilter() {
            @Override
            public boolean matches(SkyWeatherState state) {
                return state.isStorm();
            }

            @Override
            public String toString() {
                return "storm-like";
            }
        };
    }

    /**
     * Match states with at least the specified wind strength.
     *
     * @param minStrength minimum wind strength fraction (&le;1, &ge;0)
     * @return new filter
     */
    public static SkyWeatherFilter windy(float minStrength) {
        Validate.fraction(minStrength, "minimum wind strength");
        return new SkyWeatherFilter() {
            @Override
            public boolean matches(SkyWeatherState state) {
                return state.windStrength() >= minStrength;
            }

            @Override
            public String toString() {
                return "wind>=" + minStrength;
            }
        };
    }
}
