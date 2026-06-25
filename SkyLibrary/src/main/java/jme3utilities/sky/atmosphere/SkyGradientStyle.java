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

/**
 * Strength presets for atmospheric gradients and astronomical halos.
 *
 * @author Take Some
 */
public enum SkyGradientStyle {
    /** Conservative real-world inspired gradients. */
    REALISTIC(0.70f, 0.65f, 0.65f, 0.75f, 0.75f, 0.65f),
    /** Expressive cinematic gradients for game visuals. */
    CINEMATIC(1.00f, 1.00f, 1.00f, 1.25f, 1.15f, 1.10f),
    /** Strong fantasy gradients for unreal skies. */
    FANTASY(1.55f, 1.40f, 1.45f, 1.80f, 1.65f, 1.55f);

    /** Color-shift multiplier. */
    final private float colorScale;
    /** Preset moon halo intensity. */
    final private float presetMoonHalo;
    /** Preset sun halo intensity. */
    final private float presetSunHalo;
    /** Preset sunset intensity. */
    final private float presetSunset;
    /** Halo/glow multiplier. */
    final private float haloScale;
    /** Horizon gradient multiplier. */
    final private float horizonScale;

    /**
     * Instantiate a preset.
     *
     * @param horizonScale horizon multiplier
     * @param colorScale color multiplier
     * @param haloScale halo multiplier
     * @param presetSunset preset sunset intensity
     * @param presetSunHalo preset sun halo intensity
     * @param presetMoonHalo preset moon halo intensity
     */
    SkyGradientStyle(float horizonScale, float colorScale, float haloScale,
            float presetSunset, float presetSunHalo,
            float presetMoonHalo) {
        this.horizonScale = horizonScale;
        this.colorScale = colorScale;
        this.haloScale = haloScale;
        this.presetSunset = presetSunset;
        this.presetSunHalo = presetSunHalo;
        this.presetMoonHalo = presetMoonHalo;
    }


    /**
     * Return preset moon halo intensity.
     *
     * @return multiplier
     */
    public float presetMoonHalo() {
        return presetMoonHalo;
    }

    /**
     * Return preset sun halo intensity.
     *
     * @return multiplier
     */
    public float presetSunHalo() {
        return presetSunHalo;
    }

    /**
     * Return preset sunset intensity.
     *
     * @return multiplier
     */
    public float presetSunset() {
        return presetSunset;
    }

    /**
     * Return the color-shift multiplier.
     *
     * @return multiplier
     */
    public float colorScale() {
        return colorScale;
    }

    /**
     * Return the halo/glow multiplier.
     *
     * @return multiplier
     */
    public float haloScale() {
        return haloScale;
    }

    /**
     * Return the horizon gradient multiplier.
     *
     * @return multiplier
     */
    public float horizonScale() {
        return horizonScale;
    }
}
