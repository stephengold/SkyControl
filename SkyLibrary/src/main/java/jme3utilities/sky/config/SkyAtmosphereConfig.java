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
package jme3utilities.sky.config;

import jme3utilities.Validate;
import jme3utilities.sky.SkyAtmosphere;
import jme3utilities.sky.atmosphere.SkyGradientStyle;

/**
 * Atmosphere profile and gradient configuration from Lua ABI.
 *
 * @author Take Some
 */
final public class SkyAtmosphereConfig {
    /** Gradient strength style. */
    final private SkyGradientStyle gradientStyle;
    /** Moon halo intensity multiplier. */
    final private float moonHaloIntensity;
    /** Atmosphere profile path. */
    final private String profilePath;
    /** Sun halo intensity multiplier. */
    final private float sunHaloIntensity;
    /** Sunset gradient intensity multiplier. */
    final private float sunsetIntensity;

    /**
     * Instantiate atmosphere configuration.
     *
     * @param profilePath atmosphere profile path (not null, not empty)
     * @param gradientStyle gradient style (not null)
     * @param sunsetIntensity sunset intensity (&ge;0)
     * @param sunHaloIntensity sun halo intensity (&ge;0)
     * @param moonHaloIntensity moon halo intensity (&ge;0)
     */
    public SkyAtmosphereConfig(String profilePath,
            SkyGradientStyle gradientStyle, float sunsetIntensity,
            float sunHaloIntensity, float moonHaloIntensity) {
        Validate.nonEmpty(profilePath, "profile path");
        Validate.nonNull(gradientStyle, "gradient style");
        Validate.nonNegative(sunsetIntensity, "sunset intensity");
        Validate.nonNegative(sunHaloIntensity, "sun halo intensity");
        Validate.nonNegative(moonHaloIntensity, "moon halo intensity");

        this.profilePath = profilePath;
        this.gradientStyle = gradientStyle;
        this.sunsetIntensity = sunsetIntensity;
        this.sunHaloIntensity = sunHaloIntensity;
        this.moonHaloIntensity = moonHaloIntensity;
    }

    /**
     * Apply gradient settings to an atmosphere profile.
     *
     * @param atmosphere target atmosphere (not null)
     */
    public void applyTo(SkyAtmosphere atmosphere) {
        Validate.nonNull(atmosphere, "atmosphere");

        atmosphere.setGradientStyle(gradientStyle);
        atmosphere.setSunsetIntensity(sunsetIntensity);
        atmosphere.setSunHaloIntensity(sunHaloIntensity);
        atmosphere.setMoonHaloIntensity(moonHaloIntensity);
    }

    /**
     * Return gradient style.
     *
     * @return gradient style
     */
    public SkyGradientStyle gradientStyle() {
        return gradientStyle;
    }

    /**
     * Return moon halo intensity.
     *
     * @return multiplier
     */
    public float moonHaloIntensity() {
        return moonHaloIntensity;
    }

    /**
     * Return atmosphere profile path.
     *
     * @return asset path
     */
    public String profilePath() {
        return profilePath;
    }

    /**
     * Return sun halo intensity.
     *
     * @return multiplier
     */
    public float sunHaloIntensity() {
        return sunHaloIntensity;
    }

    /**
     * Return sunset intensity.
     *
     * @return multiplier
     */
    public float sunsetIntensity() {
        return sunsetIntensity;
    }
}
