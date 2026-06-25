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
package jme3utilities.sky;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;

/**
 * Physically-inspired sky lighting calculations shared by SkyControl.
 *
 * @author Take Some
 */
final class SkyLightingModel {
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
    static float airMassTransmission(
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
    static ColorRGBA daylightColor(
            SkyAtmosphere atmosphere, float sineSolarAltitude) {
        ColorRGBA result = atmosphere.copySunLight(null);
        float transmission = airMassTransmission(atmosphere, sineSolarAltitude);
        float warmWeight = 1f - smoothStep(
                sineSolarAltitude / atmosphere.getColorShiftAltitude());
        float shift = warmWeight * atmosphere.getSunsetWarmth();

        result.g *= 1f - 0.35f * shift;
        result.b *= 1f - 0.75f * shift;
        result.multLocal(transmission);

        return result;
    }

    /**
     * Smoothly remap a value from 0..1 to 0..1.
     *
     * @param input input value
     * @return smoothed fraction
     */
    static float smoothStep(float input) {
        float x = FastMath.saturate(input);
        float result = x * x * (3f - 2f * x);

        return result;
    }
}
