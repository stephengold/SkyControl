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
package jme3utilities.sky.control;

import com.jme3.math.ColorRGBA;
import jme3utilities.math.MyColor;
import jme3utilities.sky.SkyAtmosphere;
import jme3utilities.sky.atmosphere.SkyLightingModel;

/**
 * Runtime helper for sky base-color computation.
 *
 * @author Take Some
 */
final public class SkyBaseColorRuntime {
    /**
     * Hidden constructor.
     */
    private SkyBaseColorRuntime() {
        // do nothing
    }

    /**
     * Compute base and source colors for the current sky lighting state.
     *
     * @param atmosphere lighting profile (not null)
     * @param sineSolarAltitude sine of the solar altitude
     * @param moonUp true if moon is above horizon
     * @param moonWeight contribution of moonlight to nighttime lighting
     * @return new immutable result
     */
    public static Result compute(SkyAtmosphere atmosphere,
            float sineSolarAltitude, boolean moonUp, float moonWeight) {
        assert atmosphere != null;
        assert moonWeight >= 0f : moonWeight;
        assert moonWeight <= 1f : moonWeight;

        final ColorRGBA twilightColor = SkyLightingModel.horizonColor(
                atmosphere, sineSolarAltitude);
        final ColorRGBA sunColor = SkyLightingModel.daylightColor(
                atmosphere, sineSolarAltitude);
        final ColorRGBA moonColor = atmosphere.copyMoonLight(null);
        final ColorRGBA starColor = atmosphere.copyStarLight(null);
        Input input = new Input();
        input.atmosphere = atmosphere;
        input.sineSolarAltitude = sineSolarAltitude;
        input.moonUp = moonUp;
        input.moonWeight = moonWeight;
        input.twilightColor = twilightColor;
        input.sunColor = sunColor;
        input.moonColor = moonColor;
        input.starColor = starColor;
        ColorRGBA baseColor = baseColor(input);

        Result result = new Result(
                baseColor, sunColor, moonColor, starColor, twilightColor);
        return result;
    }

    /**
     * Compute the base color.
     *
     * @param input color-computation input (not null)
     * @return new color
     */
    private static ColorRGBA baseColor(Input input) {
        assert input != null;

        ColorRGBA result;
        boolean sunUp = input.sineSolarAltitude >= 0f;
        if (sunUp) {
            float dayWeight = SkyLightingModel.smoothStep(
                    input.sineSolarAltitude
                    / input.atmosphere.getFullDayAltitude());
            result = MyColor.interpolateLinear(
                    dayWeight, input.twilightColor, input.sunColor);
            float hazeWeight = input.atmosphere.getHazeStrength()
                    * (1f - dayWeight) * 0.5f;
            result = MyColor.interpolateLinear(
                    hazeWeight, result, input.twilightColor);

        } else {
            ColorRGBA blend;
            if (input.moonUp && input.moonWeight > 0f) {
                blend = MyColor.interpolateLinear(
                        input.moonWeight, input.starColor, input.moonColor);
            } else {
                blend = input.starColor;
            }
            float nightWeight = SkyLightingModel.smoothStep(
                    -input.sineSolarAltitude
                    / input.atmosphere.getTwilightLimit());
            result = MyColor.interpolateLinear(
                    nightWeight, input.twilightColor, blend);
        }

        return result;
    }

    /**
     * Color-computation input.
     */
    final private static class Input {
        /** Lighting profile. */
        private SkyAtmosphere atmosphere;
        /** True if the moon is above the horizon. */
        private boolean moonUp;
        /** Moonlight contribution to nighttime lighting. */
        private float moonWeight;
        /** Moonlight source color. */
        private ColorRGBA moonColor;
        /** Sine of the solar altitude. */
        private float sineSolarAltitude;
        /** Starlight source color. */
        private ColorRGBA starColor;
        /** Daylight source color. */
        private ColorRGBA sunColor;
        /** Twilight source color. */
        private ColorRGBA twilightColor;

    }

    /**
     * Base-color result and its source colors.
     */
    final public static class Result {
        /**
         * Base sky/haze/background color.
         */
        final private ColorRGBA baseColor;
        /**
         * Moonlight source color.
         */
        private ColorRGBA moonColor;
        /**
         * Starlight source color.
         */
        private ColorRGBA starColor;
        /**
         * Daylight source color.
         */
        private ColorRGBA sunColor;
        /**
         * Twilight source color.
         */
        private ColorRGBA twilightColor;

        /**
         * Instantiate a result.
         *
         * @param baseColor base color (not null, alias created)
         * @param sunColor sun color (not null, alias created)
         * @param moonColor moon color (not null, alias created)
         * @param starColor star color (not null, alias created)
         * @param twilightColor twilight color (not null, alias created)
         */
        private Result(ColorRGBA baseColor, ColorRGBA sunColor,
                ColorRGBA moonColor, ColorRGBA starColor,
                ColorRGBA twilightColor) {
            assert baseColor != null;
            assert sunColor != null;
            assert moonColor != null;
            assert starColor != null;
            assert twilightColor != null;

            this.baseColor = baseColor;
            this.sunColor = sunColor;
            this.moonColor = moonColor;
            this.starColor = starColor;
            this.twilightColor = twilightColor;
        }

        /**
         * Return the base sky/haze/background color.
         *
         * @return pre-existing instance
         */
        public ColorRGBA baseColor() {
            return baseColor;
        }

        /**
         * Return the moonlight color.
         *
         * @return pre-existing instance
         */
        public ColorRGBA moonColor() {
            return moonColor;
        }

        /**
         * Return the starlight color.
         *
         * @return pre-existing instance
         */
        public ColorRGBA starColor() {
            return starColor;
        }

        /**
         * Return the daylight color.
         *
         * @return pre-existing instance
         */
        public ColorRGBA sunColor() {
            return sunColor;
        }

        /**
         * Return the twilight color.
         *
         * @return pre-existing instance
         */
        public ColorRGBA twilightColor() {
            return twilightColor;
        }
    }
}
