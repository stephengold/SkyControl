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
package jme3utilities.sky.cloud;

/**
 * Cloud weather presets built from up to six cloud layers.
 *
 * @author Take Some
 */
public enum SkyCloudPreset {
    /** No visible clouds. */
    CLEAR(SkyCloudLayerSpec.clear),
    /** Existing procedural fair-weather cloud setup. */
    FAIR(
            layer(SkyCloudAssets.fbm, 0.35f, 1.50f, -0.0005f, 0.0030f),
            layer(SkyCloudAssets.fbm, 0.18f, 2.15f, 0.0003f, 0.0010f)),
    /** Existing overcast fallback. */
    OVERCAST(
            layer(SkyCloudAssets.overcast, 0.72f, 1.00f, 0.0000f, 0.0003f),
            layer(SkyCloudAssets.fbm, 0.20f, 2.40f, 0.0002f, 0.0010f)),
    /** Optional high-altitude wispy cloud setup. */
    WISPY(
            layer(SkyCloudAssets.wispyCirrocumulus,
                    SkyCloudAssets.wispyCirrocumulusNormal, 0.28f, 1.30f,
                    0.0001f, 0.0012f),
            layer(SkyCloudAssets.wisps, SkyCloudAssets.wispsNormal, 0.16f,
                    2.20f, -0.0002f, 0.0020f)),
    /** Optional varied cloudy setup. */
    CLOUDY(
            layer(SkyCloudAssets.cloudyBase,
                    SkyCloudAssets.cloudyBaseNormal, 0.52f, 1.20f,
                    -0.0003f, 0.0018f),
            layer(SkyCloudAssets.marbleDetail,
                    SkyCloudAssets.cloudyDetailNormal, 0.22f, 3.00f,
                    0.0004f, 0.0024f)),
    /** Optional rainy setup. */
    RAIN(
            layer(SkyCloudAssets.rain, SkyCloudAssets.rainNormal, 0.68f,
                    1.10f, -0.0003f, 0.0026f),
            layer(SkyCloudAssets.cloudyBase,
                    SkyCloudAssets.cloudyBaseNormal, 0.35f, 2.00f,
                    0.0002f, 0.0018f),
            layer(SkyCloudAssets.marbleDetail,
                    SkyCloudAssets.cloudyDetailNormal, 0.18f, 3.50f,
                    0.0004f, 0.0030f)),
    /** Optional storm setup. */
    STORM(
            layer(SkyCloudAssets.storm, SkyCloudAssets.stormNormal, 0.85f,
                    1.00f, -0.0004f, 0.0032f),
            layer(SkyCloudAssets.rain, SkyCloudAssets.stormRainNormal, 0.42f,
                    1.80f, 0.0002f, 0.0026f),
            layer(SkyCloudAssets.marbleDetail,
                    SkyCloudAssets.cloudyDetailNormal, 0.26f, 4.00f,
                    0.0005f, 0.0035f)),
    /** Optional nimbus setup. */
    NIMBUS(
            layer(SkyCloudAssets.nimbus, SkyCloudAssets.nimbusNormal, 0.76f,
                    1.10f, -0.0004f, 0.0020f),
            layer(SkyCloudAssets.marbleDetail,
                    SkyCloudAssets.cloudyDetailNormal, 0.22f, 3.50f,
                    0.0003f, 0.0024f));

    /** Layer targets, in material-layer order. */
    final private SkyCloudLayerSpec[] layers;

    /**
     * Instantiate a preset.
     *
     * @param layers layer targets (not null, aliased)
     */
    SkyCloudPreset(SkyCloudLayerSpec... layers) {
        this.layers = layers;
    }

    /**
     * Access the target state for a layer.
     *
     * @param layerIndex cloud layer index
     * @return layer target, or clear if the preset has no layer there
     */
    public SkyCloudLayerSpec layer(int layerIndex) {
        SkyCloudLayerSpec result = SkyCloudLayerSpec.clear;
        if (layerIndex < layers.length) {
            result = layers[layerIndex];
        }

        return result;
    }

    /**
     * Create a layer target.
     *
     * @param alphaMap asset path to the alpha map
     * @param opacity target opacity
     * @param scale texture scale
     * @param uRate U-axis motion rate
     * @param vRate V-axis motion rate
     * @return new layer target
     */
    private static SkyCloudLayerSpec layer(String alphaMap, float opacity,
            float scale, float uRate, float vRate) {
        SkyCloudLayerSpec result
                = new SkyCloudLayerSpec(alphaMap, opacity, scale, uRate, vRate);
        return result;
    }

    /**
     * Create a layer target with a normal map.
     *
     * @param alphaMap asset path to the alpha map
     * @param normalMap asset path to the normal map
     * @param opacity target opacity
     * @param scale texture scale
     * @param uRate U-axis motion rate
     * @param vRate V-axis motion rate
     * @return new layer target
     */
    private static SkyCloudLayerSpec layer(String alphaMap, String normalMap,
            float opacity, float scale, float uRate, float vRate) {
        SkyCloudLayerSpec result = new SkyCloudLayerSpec(
                alphaMap, normalMap, opacity, scale, uRate, vRate);
        return result;
    }
}
