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

import jme3utilities.Validate;

/**
 * Target state for one cloud layer in a weather preset.
 *
 * @author Take Some
 */
final public class SkyCloudLayerSpec {
    /** A transparent clear layer. */
    final public static SkyCloudLayerSpec clear
            = new SkyCloudLayerSpec(SkyCloudAssets.clear, 0f, 1f, 0f, 0f);

    /** Asset path to the alpha map. */
    final private String alphaMap;
    /** Optional asset path to the normal map. */
    final private String normalMap;
    /** Target opacity. */
    final private float opacity;
    /** Target texture scale. */
    final private float scale;
    /** U-axis motion rate. */
    final private float uRate;
    /** V-axis motion rate. */
    final private float vRate;

    /**
     * Instantiate a layer target.
     *
     * @param alphaMap asset path to the alpha map (not null, not empty)
     * @param opacity target opacity (&le;1, &ge;0)
     * @param scale texture scale (&gt;0)
     * @param uRate U-axis motion rate
     * @param vRate V-axis motion rate
     */
    public SkyCloudLayerSpec(String alphaMap, float opacity, float scale,
            float uRate, float vRate) {
        this(alphaMap, null, opacity, scale, uRate, vRate);
    }

    /**
     * Instantiate a layer target with a normal map.
     *
     * @param alphaMap asset path to the alpha map (not null, not empty)
     * @param normalMap asset path to the normal map, or null
     * @param opacity target opacity (&le;1, &ge;0)
     * @param scale texture scale (&gt;0)
     * @param uRate U-axis motion rate
     * @param vRate V-axis motion rate
     */
    public SkyCloudLayerSpec(String alphaMap, String normalMap, float opacity,
            float scale, float uRate, float vRate) {
        Validate.nonEmpty(alphaMap, "alpha map");
        if (normalMap != null) {
            Validate.nonEmpty(normalMap, "normal map");
        }
        Validate.fraction(opacity, "opacity");
        Validate.positive(scale, "scale");

        this.alphaMap = alphaMap;
        this.normalMap = normalMap;
        this.opacity = opacity;
        this.scale = scale;
        this.uRate = uRate;
        this.vRate = vRate;
    }

    /**
     * Return the alpha-map asset path.
     *
     * @return asset path
     */
    public String alphaMap() {
        return alphaMap;
    }

    /**
     * Return the optional normal-map asset path.
     *
     * @return asset path, or null
     */
    public String normalMap() {
        return normalMap;
    }

    /**
     * Return the target opacity.
     *
     * @return opacity fraction
     */
    public float opacity() {
        return opacity;
    }

    /**
     * Return the texture scale.
     *
     * @return scale factor
     */
    public float scale() {
        return scale;
    }

    /**
     * Return the U-axis motion rate.
     *
     * @return motion rate in cycles per second
     */
    public float uRate() {
        return uRate;
    }

    /**
     * Return the V-axis motion rate.
     *
     * @return motion rate in cycles per second
     */
    public float vRate() {
        return vRate;
    }
}
