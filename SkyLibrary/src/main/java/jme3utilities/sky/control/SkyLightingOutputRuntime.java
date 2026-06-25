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
import com.jme3.math.FastMath;
import jme3utilities.math.MyColor;
import jme3utilities.math.MyMath;
import jme3utilities.sky.SkyAtmosphere;

/**
 * Runtime helper for assembling final sky lighting output.
 *
 * @author Take Some
 */
final public class SkyLightingOutputRuntime {
    /**
     * Hidden constructor.
     */
    private SkyLightingOutputRuntime() {
        // do nothing
    }

    /**
     * Compute final directional, ambient, shadow, and bloom lighting values.
     *
     * @param input lighting output input (not null)
     * @return new immutable result
     */
    public static Result compute(Input input) {
        assert input != null;

        ColorRGBA mainColor = computeMainColor(input);
        ColorRGBA ambientColor = computeAmbientColor(
                input.atmosphere, input.cloudsColor, mainColor);
        float shadowIntensity = computeShadowIntensity(
                input.atmosphere, mainColor, ambientColor);
        float bloomIntensity = computeBloomIntensity(
                input.atmosphere, input.sineSolarAltitude);

        Result result = new Result(
                mainColor, ambientColor, shadowIntensity, bloomIntensity);
        return result;
    }

    /**
     * Compute the ambient color.
     *
     * @param atmosphere lighting profile (not null)
     * @param cloudsColor cloud color (not null, unaffected)
     * @param mainColor main directional color (not null, unaffected)
     * @return new color
     */
    private static ColorRGBA computeAmbientColor(SkyAtmosphere atmosphere,
            ColorRGBA cloudsColor, ColorRGBA mainColor) {
        assert atmosphere != null;
        assert cloudsColor != null;
        assert mainColor != null;

        float slack = 1f - MyMath.max(mainColor.r, mainColor.g, mainColor.b);
        assert slack >= 0f : slack;
        ColorRGBA result = cloudsColor.mult(
                slack * atmosphere.getAmbientScale());
        return result;
    }

    /**
     * Compute the recommended bloom intensity using the sun's altitude.
     *
     * @param atmosphere lighting profile (not null)
     * @param sineSolarAltitude sine of the solar altitude
     * @return bloom intensity
     */
    private static float computeBloomIntensity(SkyAtmosphere atmosphere,
            float sineSolarAltitude) {
        assert atmosphere != null;

        float result = 6f * atmosphere.getBloomScale() * sineSolarAltitude;
        result = FastMath.clamp(
                result, 0f, atmosphere.getMaxBloomIntensity());
        return result;
    }

    /**
     * Compute the color and intensity of the main directional light.
     *
     * @param input lighting output input (not null)
     * @return new color
     */
    private static ColorRGBA computeMainColor(Input input) {
        assert input != null;

        ColorRGBA result;
        if (input.sunUp) {
            float altitudeFactor = MyMath.cubeRoot(
                    FastMath.saturate(input.sineSolarAltitude));
            float sunFactor = input.transmit * altitudeFactor;
            result = input.sunColor.mult(sunFactor);

        } else if (input.moonUp) {
            float lunarAltitudeFactor = MyMath.cubeRoot(
                    FastMath.saturate(input.sineLunarAltitude));
            float moonFactor = input.transmit * input.moonWeight
                    * lunarAltitudeFactor;
            result = MyColor.interpolateLinear(
                    moonFactor, input.starColor, input.moonColor);

        } else {
            result = input.starColor.clone();
        }

        return result;
    }

    /**
     * Compute the recommended shadow intensity.
     *
     * @param atmosphere lighting profile (not null)
     * @param mainColor main directional color (not null, unaffected)
     * @param ambientColor ambient color (not null, unaffected)
     * @return shadow intensity
     */
    private static float computeShadowIntensity(SkyAtmosphere atmosphere,
            ColorRGBA mainColor, ColorRGBA ambientColor) {
        assert atmosphere != null;
        assert mainColor != null;
        assert ambientColor != null;

        float mainAmount = mainColor.r + mainColor.g + mainColor.b;
        float ambientAmount = ambientColor.r + ambientColor.g + ambientColor.b;
        float totalAmount = mainAmount + ambientAmount;
        assert totalAmount > 0f : totalAmount;
        float result = FastMath.saturate(mainAmount / totalAmount);
        result = FastMath.saturate(result * atmosphere.getShadowContrast());
        return result;
    }

    /**
     * Lighting output input.
     */
    final public static class Input {
        /** Lighting profile. */
        final private SkyAtmosphere atmosphere;
        /** Cloud color used for ambient-light estimation. */
        final private ColorRGBA cloudsColor;
        /** Moonlight source color. */
        final private ColorRGBA moonColor;
        /** True if the moon is above the horizon. */
        final private boolean moonUp;
        /** Moonlight contribution to nighttime lighting. */
        final private float moonWeight;
        /** Sine of the lunar altitude. */
        final private float sineLunarAltitude;
        /** Sine of the solar altitude. */
        final private float sineSolarAltitude;
        /** Starlight source color. */
        final private ColorRGBA starColor;
        /** Daylight source color. */
        final private ColorRGBA sunColor;
        /** True if the sun is above the horizon. */
        final private boolean sunUp;
        /** Cloud transmission factor for the main light. */
        final private float transmit;

        /**
         * Instantiate an input.
         *
         * @param atmosphere lighting profile (not null, alias created)
         * @param lightSelection selected light state (not null, unaffected)
         * @param baseResult base/source colors (not null, unaffected)
         * @param cloudsColor cloud color (not null, alias created)
         * @param moonWeight moonlight contribution (&le;1, &ge;0)
         * @param transmit cloud transmission factor for the main light
         */
        public Input(SkyAtmosphere atmosphere,
                SkyLightSelectionRuntime.Result lightSelection,
                SkyBaseColorRuntime.Result baseResult, ColorRGBA cloudsColor,
                float moonWeight, float transmit) {
            assert atmosphere != null;
            assert lightSelection != null;
            assert baseResult != null;
            assert cloudsColor != null;
            assert moonWeight >= 0f : moonWeight;
            assert moonWeight <= 1f : moonWeight;
            assert transmit >= 0f : transmit;

            this.atmosphere = atmosphere;
            this.cloudsColor = cloudsColor;
            this.sunColor = baseResult.sunColor();
            this.moonColor = baseResult.moonColor();
            this.starColor = baseResult.starColor();
            this.sunUp = lightSelection.sunUp();
            this.moonUp = lightSelection.moonUp();
            this.moonWeight = moonWeight;
            this.sineSolarAltitude = lightSelection.sineSolarAltitude();
            this.sineLunarAltitude = lightSelection.sineLunarAltitude();
            this.transmit = transmit;
        }
    }

    /**
     * Final lighting output result.
     */
    final public static class Result {
        /** Ambient light color. */
        final private ColorRGBA ambientColor;
        /** Recommended bloom intensity. */
        final private float bloomIntensity;
        /** Main directional light color. */
        final private ColorRGBA mainColor;
        /** Recommended shadow intensity. */
        final private float shadowIntensity;

        /**
         * Instantiate a result.
         *
         * @param mainColor main light color (not null, alias created)
         * @param ambientColor ambient light color (not null, alias created)
         * @param shadowIntensity recommended shadow intensity
         * @param bloomIntensity recommended bloom intensity
         */
        private Result(ColorRGBA mainColor, ColorRGBA ambientColor,
                float shadowIntensity, float bloomIntensity) {
            assert mainColor != null;
            assert ambientColor != null;

            this.mainColor = mainColor;
            this.ambientColor = ambientColor;
            this.shadowIntensity = shadowIntensity;
            this.bloomIntensity = bloomIntensity;
        }

        /**
         * Return the ambient light color.
         *
         * @return pre-existing instance
         */
        public ColorRGBA ambientColor() {
            return ambientColor;
        }

        /**
         * Return the recommended bloom intensity.
         *
         * @return bloom intensity
         */
        public float bloomIntensity() {
            return bloomIntensity;
        }

        /**
         * Return the main directional light color.
         *
         * @return pre-existing instance
         */
        public ColorRGBA mainDirectionalColor() {
            return mainColor;
        }

        /**
         * Return the recommended shadow intensity.
         *
         * @return shadow intensity
         */
        public float shadowIntensity() {
            return shadowIntensity;
        }
    }
}
