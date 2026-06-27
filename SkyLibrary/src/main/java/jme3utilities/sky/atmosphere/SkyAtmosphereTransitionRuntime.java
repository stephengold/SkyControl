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

import com.jme3.math.FastMath;
import jme3utilities.Validate;
import jme3utilities.sky.SkyAtmosphere;

/**
 * Runtime interpolator for atmosphere gradient styling.
 *
 * @author Take Some
 */
final public class SkyAtmosphereTransitionRuntime {
    /** True while a transition is active. */
    private boolean active;
    /** Transition duration in seconds. */
    private float duration;
    /** Elapsed transition time. */
    private float elapsed;
    /** Initial color style scale. */
    private float fromColor;
    /** Initial halo style scale. */
    private float fromHalo;
    /** Initial horizon style scale. */
    private float fromHorizon;
    /** Initial moon halo intensity. */
    private float fromMoon;
    /** Initial sun halo intensity. */
    private float fromSun;
    /** Initial sunset intensity. */
    private float fromSunset;
    /** Target color style scale. */
    private float toColor;
    /** Target halo style scale. */
    private float toHalo;
    /** Target horizon style scale. */
    private float toHorizon;
    /** Target moon halo intensity. */
    private float toMoon;
    /** Target sun halo intensity. */
    private float toSun;
    /** Target sunset intensity. */
    private float toSunset;

    /**
     * Test whether a transition is active.
     *
     * @return true if active, otherwise false
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Start a gradient preset transition.
     *
     * @param atmosphere target atmosphere (not null)
     * @param style target gradient style (not null)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void transitionStyle(SkyAtmosphere atmosphere,
            SkyGradientStyle style, float seconds) {
        Validate.nonNull(atmosphere, "atmosphere");
        Validate.nonNull(style, "style");
        Validate.nonNegative(seconds, "seconds");

        toHorizon = style.horizonScale();
        toColor = style.colorScale();
        toHalo = style.haloScale();
        toSunset = style.presetSunset();
        toSun = style.presetSunHalo();
        toMoon = style.presetMoonHalo();
        start(atmosphere, seconds);
        atmosphere.setGradientStyle(style);
        apply(atmosphere, active ? 0f : 1f);
    }

    /**
     * Start a moon halo intensity transition.
     *
     * @param atmosphere target atmosphere (not null)
     * @param intensity target intensity (&ge;0)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void transitionMoon(SkyAtmosphere atmosphere, float intensity,
            float seconds) {
        Validate.nonNull(atmosphere, "atmosphere");
        Validate.nonNegative(intensity, "intensity");
        Validate.nonNegative(seconds, "seconds");

        toHorizon = atmosphere.getHorizonScale();
        toColor = atmosphere.getColorScale();
        toHalo = atmosphere.getHaloScale();
        toSunset = atmosphere.getSunsetIntensity();
        toSun = atmosphere.getSunHaloIntensity();
        toMoon = intensity;
        start(atmosphere, seconds);
    }

    /**
     * Start a sun halo intensity transition.
     *
     * @param atmosphere target atmosphere (not null)
     * @param intensity target intensity (&ge;0)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void transitionSun(SkyAtmosphere atmosphere, float intensity,
            float seconds) {
        Validate.nonNull(atmosphere, "atmosphere");
        Validate.nonNegative(intensity, "intensity");
        Validate.nonNegative(seconds, "seconds");

        toHorizon = atmosphere.getHorizonScale();
        toColor = atmosphere.getColorScale();
        toHalo = atmosphere.getHaloScale();
        toSunset = atmosphere.getSunsetIntensity();
        toSun = intensity;
        toMoon = atmosphere.getMoonHaloIntensity();
        start(atmosphere, seconds);
    }

    /**
     * Start a sunset intensity transition.
     *
     * @param atmosphere target atmosphere (not null)
     * @param intensity target intensity (&ge;0)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void transitionSunset(SkyAtmosphere atmosphere, float intensity,
            float seconds) {
        Validate.nonNull(atmosphere, "atmosphere");
        Validate.nonNegative(intensity, "intensity");
        Validate.nonNegative(seconds, "seconds");

        toHorizon = atmosphere.getHorizonScale();
        toColor = atmosphere.getColorScale();
        toHalo = atmosphere.getHaloScale();
        toSunset = intensity;
        toSun = atmosphere.getSunHaloIntensity();
        toMoon = atmosphere.getMoonHaloIntensity();
        start(atmosphere, seconds);
    }

    /**
     * Advance the active transition.
     *
     * @param atmosphere target atmosphere (not null)
     * @param interval update interval in seconds (&ge;0)
     */
    public void update(SkyAtmosphere atmosphere, float interval) {
        Validate.nonNull(atmosphere, "atmosphere");
        Validate.nonNegative(interval, "interval");
        if (!active) {
            return;
        }

        elapsed += interval;
        float weight = FastMath.saturate(elapsed / duration);
        apply(atmosphere, weight);
        if (weight >= 1f) {
            active = false;
        }
    }

    /**
     * Apply an interpolation weight.
     *
     * @param atmosphere target atmosphere
     * @param weight interpolation weight
     */
    private void apply(SkyAtmosphere atmosphere, float weight) {
        float horizon = lerp(fromHorizon, toHorizon, weight);
        float color = lerp(fromColor, toColor, weight);
        float halo = lerp(fromHalo, toHalo, weight);
        atmosphere.setGradientScales(horizon, color, halo);
        atmosphere.setSunsetIntensity(lerp(fromSunset, toSunset, weight));
        atmosphere.setSunHaloIntensity(lerp(fromSun, toSun, weight));
        atmosphere.setMoonHaloIntensity(lerp(fromMoon, toMoon, weight));
    }

    /**
     * Linear interpolation.
     *
     * @param start start value
     * @param end end value
     * @param weight interpolation weight
     * @return interpolated value
     */
    private static float lerp(float start, float end, float weight) {
        float result = start + (end - start) * weight;
        return result;
    }

    /**
     * Start a transition to the current target fields.
     *
     * @param atmosphere target atmosphere
     * @param seconds transition duration
     */
    private void start(SkyAtmosphere atmosphere, float seconds) {
        fromHorizon = atmosphere.getHorizonScale();
        fromColor = atmosphere.getColorScale();
        fromHalo = atmosphere.getHaloScale();
        fromSunset = atmosphere.getSunsetIntensity();
        fromSun = atmosphere.getSunHaloIntensity();
        fromMoon = atmosphere.getMoonHaloIntensity();
        duration = seconds;
        elapsed = 0f;
        active = seconds > 0f;
        apply(atmosphere, active ? 0f : 1f);
    }
}
