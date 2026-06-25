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
package jme3utilities.sky.atmosphere;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import jme3utilities.sky.SkyAtmosphere;

/**
 * Physically-inspired sky lighting calculations shared by SkyControl.
 *
 * @author Take Some
 */
public final class SkyLightingModel {
    /**
     * Hidden constructor.
     */
    private SkyLightingModel() {
        // do nothing
    }

    /**
     * Approximate solar transmission through the atmosphere.
     *
     * @param atmosphere active atmospheric profile (not null)
     * @param sineSolarAltitude sine of the solar altitude
     * @return transmission fraction between 0 and 1
     */
    public static float airMassTransmission(
            SkyAtmosphere atmosphere, float sineSolarAltitude) {
        float altitude = FastMath.clamp(sineSolarAltitude, 0.01f, 1f);
        float opticalMass = 1f / (altitude + 0.15f
                * (float) Math.pow(altitude + 0.1f, 1.25));
        float extinction = (float) Math.exp(-0.18f
                * atmosphere.getAirMassStrength() * (opticalMass - 1f));
        float result = FastMath.clamp(extinction,
                atmosphere.getMinSunTransmit(), 1f);

        return result;
    }

    /**
     * Compute daylight color after approximate atmospheric extinction.
     *
     * @param atmosphere active atmospheric profile (not null)
     * @param sineSolarAltitude sine of the solar altitude
     * @return new color
     */
    public static ColorRGBA daylightColor(
            SkyAtmosphere atmosphere, float sineSolarAltitude) {
        ColorRGBA result = atmosphere.copySunLight(null);
        float transmission = airMassTransmission(atmosphere, sineSolarAltitude);
        float warmWeight = 1f - smoothStep(
                sineSolarAltitude / atmosphere.getColorShiftAltitude());
        float horizonWeight = horizonWeight(
                sineSolarAltitude, atmosphere.getTwilightLimit());
        float styleScale = atmosphere.getGradientStyle().colorScale();
        float shift = warmWeight * atmosphere.getSunsetWarmth()
                * atmosphere.getSunsetIntensity() * styleScale;
        float amber = horizonWeight * atmosphere.getHazeStrength()
                * atmosphere.getSunsetWarmth()
                * atmosphere.getSunsetIntensity() * styleScale;

        result.r *= 1f + 0.28f * amber;
        result.g *= 1f - 0.28f * shift + 0.10f * amber;
        result.b *= 1f - 0.70f * shift - 0.22f * amber;
        result.multLocal(transmission);

        return result;
    }

    /**
     * Compute a horizon/twilight gradient color.
     *
     * @param atmosphere active atmospheric profile (not null)
     * @param sineSolarAltitude sine of the solar altitude
     * @return new color
     */
    public static ColorRGBA horizonColor(
            SkyAtmosphere atmosphere, float sineSolarAltitude) {
        ColorRGBA result = atmosphere.copyTwilightColor(null);
        float horizonWeight = horizonWeight(
                sineSolarAltitude, atmosphere.getTwilightLimit());
        float duskWeight = smoothStep(
                -sineSolarAltitude / atmosphere.getTwilightLimit());
        float strength = atmosphere.getSunsetWarmth()
                * atmosphere.getSunsetIntensity()
                * atmosphere.getHazeStrength()
                * atmosphere.getGradientStyle().horizonScale();
        float amber = horizonWeight * strength;
        float violet = duskWeight * horizonWeight * strength;

        result.r *= 1f + 0.35f * amber + 0.12f * violet;
        result.g *= 1f + 0.06f * amber - 0.18f * violet;
        result.b *= 1f - 0.26f * amber + 0.42f * violet;

        return result;
    }

    /**
     * Compute the bell-shaped strength of horizon gradients.
     *
     * @param sineAltitude sine of altitude above horizon
     * @param twilightLimit twilight reach below horizon
     * @return gradient strength
     */
    public static float horizonWeight(float sineAltitude, float twilightLimit) {
        float distance = Math.abs(sineAltitude) / twilightLimit;
        float result = 1f - smoothStep(distance);
        return result;
    }

    /**
     * Compute lunar color after low-altitude horizon tint.
     *
     * @param atmosphere active atmospheric profile (not null)
     * @param sineLunarAltitude sine of lunar altitude
     * @return new color
     */
    public static ColorRGBA lunarColor(
            SkyAtmosphere atmosphere, float sineLunarAltitude) {
        ColorRGBA result = atmosphere.copyMoonLight(null);
        float horizonWeight = horizonWeight(
                sineLunarAltitude, atmosphere.getTwilightLimit());
        float shift = horizonWeight * atmosphere.getSunsetWarmth()
                * atmosphere.getSunsetIntensity()
                * atmosphere.getGradientStyle().colorScale();

        result.r *= 1f + 0.10f * shift;
        result.g *= 1f - 0.10f * shift;
        result.b *= 1f - 0.25f * shift;

        return result;
    }

    /**
     * Compute moon halo/glow color.
     *
     * @param atmosphere active atmospheric profile (not null)
     * @param sineLunarAltitude sine of lunar altitude
     * @return new color
     */
    public static ColorRGBA moonGlowColor(
            SkyAtmosphere atmosphere, float sineLunarAltitude) {
        ColorRGBA result = lunarColor(atmosphere, sineLunarAltitude);
        float horizonWeight = horizonWeight(
                sineLunarAltitude, atmosphere.getTwilightLimit());
        float glow = 0.45f + 0.35f * horizonWeight
                * atmosphere.getGradientStyle().haloScale();
        result.multLocal(glow * atmosphere.getMoonHaloIntensity());
        return result;
    }

    /**
     * Compute sun halo/glow color.
     *
     * @param atmosphere active atmospheric profile (not null)
     * @param sineSolarAltitude sine of solar altitude
     * @return new color
     */
    public static ColorRGBA sunGlowColor(
            SkyAtmosphere atmosphere, float sineSolarAltitude) {
        ColorRGBA result = daylightColor(atmosphere, sineSolarAltitude);
        float horizonWeight = horizonWeight(
                sineSolarAltitude, atmosphere.getTwilightLimit());
        float glow = 1.15f + 1.10f * horizonWeight
                * atmosphere.getSunsetWarmth()
                * atmosphere.getSunsetIntensity()
                * atmosphere.getGradientStyle().haloScale();
        result.multLocal(glow * atmosphere.getSunHaloIntensity());
        return result;
    }

    /**
     * Smoothly remap a value from 0..1 to 0..1.
     *
     * @param input input value
     * @return smoothed fraction
     */
    public static float smoothStep(float input) {
        float x = FastMath.saturate(input);
        float result = x * x * (3f - 2f * x);

        return result;
    }
}
