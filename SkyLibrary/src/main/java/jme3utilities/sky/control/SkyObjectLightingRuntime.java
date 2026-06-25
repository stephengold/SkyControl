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
import jme3utilities.sky.SkyAtmosphere;
import jme3utilities.sky.SkyMaterial;
import jme3utilities.sky.atmosphere.SkyLightingModel;

/**
 * Runtime helper for sun/moon material colors.
 *
 * @author Take Some
 */
public final class SkyObjectLightingRuntime {
    // *************************************************************************
    // constants and loggers

    /**
     * Sky-material object index for the sun.
     */
    final private static int sunObjectIndex = 0;
    /**
     * Sky-material object index for the moon.
     */
    final private static int moonObjectIndex = 1;
    /**
     * Maximum material alpha.
     */
    final private static float maxAlpha = 1f;

    /**
     * Hidden constructor.
     */
    private SkyObjectLightingRuntime() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Update the sun and moon material colors using their altitudes.
     *
     * @param material top-dome material (not null)
     * @param atmosphere lighting profile (not null)
     * @param sineSolarAltitude sine of the solar altitude (&le;1, &ge;-1)
     * @param sineLunarAltitude sine of the lunar altitude (&le;1, &ge;-1)
     */
    public static void updateObjects(SkyMaterial material,
            SkyAtmosphere atmosphere, float sineSolarAltitude,
            float sineLunarAltitude) {
        assert material != null;
        assert atmosphere != null;
        assert sineSolarAltitude <= 1f : sineSolarAltitude;
        assert sineSolarAltitude >= -1f : sineSolarAltitude;
        assert sineLunarAltitude <= 1f : sineLunarAltitude;
        assert sineLunarAltitude >= -1f : sineLunarAltitude;

        updateSun(material, atmosphere, sineSolarAltitude);
        updateMoon(material, atmosphere, sineLunarAltitude);
    }

    /**
     * Update the moon material color.
     *
     * @param material top-dome material (not null)
     * @param atmosphere lighting profile (not null)
     * @param sineLunarAltitude sine of the lunar altitude
     */
    private static void updateMoon(SkyMaterial material,
            SkyAtmosphere atmosphere, float sineLunarAltitude) {
        float moonVisibility = FastMath.saturate(
                2f * sineLunarAltitude + 0.6f);
        float moonWarmth = 1f - SkyLightingModel.smoothStep(
                (sineLunarAltitude + 0.02f) / 0.25f);
        float moonShift = moonWarmth * atmosphere.getSunsetWarmth();
        ColorRGBA moonColor = atmosphere.copyMoonLight(null);
        moonColor.g *= 1f - 0.15f * moonShift;
        moonColor.b *= 1f - 0.35f * moonShift;
        moonColor.multLocal(moonVisibility);
        moonColor.a = maxAlpha;
        material.setObjectColor(moonObjectIndex, moonColor);
    }

    /**
     * Update the sun material color and glow.
     *
     * @param material top-dome material (not null)
     * @param atmosphere lighting profile (not null)
     * @param sineSolarAltitude sine of the solar altitude
     */
    private static void updateSun(SkyMaterial material,
            SkyAtmosphere atmosphere, float sineSolarAltitude) {
        float sunVisibility = FastMath.saturate(
                1f + sineSolarAltitude / atmosphere.getTwilightLimit());
        ColorRGBA sunColor = SkyLightingModel.daylightColor(
                atmosphere, sineSolarAltitude);
        sunColor.a = maxAlpha * sunVisibility;
        material.setObjectColor(sunObjectIndex, sunColor);
        material.setObjectGlow(sunObjectIndex, sunColor);
    }
}
