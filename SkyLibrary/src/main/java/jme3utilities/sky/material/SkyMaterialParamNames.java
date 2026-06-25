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
package jme3utilities.sky.material;

import java.util.Locale;

/**
 * Shader parameter-name factory for sky materials.
 *
 * @author Take Some
 */
public final class SkyMaterialParamNames {
    /**
     * Hidden constructor.
     */
    private SkyMaterialParamNames() {
        // do nothing
    }

    /**
     * Build the alpha-map parameter name for a cloud layer.
     *
     * @param layerIndex cloud layer index
     * @return material parameter name
     */
    public static String cloudAlphaMap(int layerIndex) {
        String result = cloud(layerIndex, "AlphaMap");
        return result;
    }

    /**
     * Build the color parameter name for a cloud layer.
     *
     * @param layerIndex cloud layer index
     * @return material parameter name
     */
    public static String cloudColor(int layerIndex) {
        String result = cloud(layerIndex, "Color");
        return result;
    }

    /**
     * Build the glow parameter name for a cloud layer.
     *
     * @param layerIndex cloud layer index
     * @return material parameter name
     */
    public static String cloudGlow(int layerIndex) {
        String result = cloud(layerIndex, "Glow");
        return result;
    }

    /**
     * Build the normal-map parameter name for a cloud layer.
     *
     * @param layerIndex cloud layer index
     * @return material parameter name
     */
    public static String cloudNormalMap(int layerIndex) {
        String result = cloud(layerIndex, "NormalMap");
        return result;
    }

    /**
     * Build the offset parameter name for a cloud layer.
     *
     * @param layerIndex cloud layer index
     * @return material parameter name
     */
    public static String cloudOffset(int layerIndex) {
        String result = cloud(layerIndex, "Offset");
        return result;
    }

    /**
     * Build the scale parameter name for a cloud layer.
     *
     * @param layerIndex cloud layer index
     * @return material parameter name
     */
    public static String cloudScale(int layerIndex) {
        String result = cloud(layerIndex, "Scale");
        return result;
    }

    /**
     * Build the center parameter name for an astronomical object.
     *
     * @param objectIndex astronomical object index
     * @return material parameter name
     */
    public static String objectCenter(int objectIndex) {
        String result = object(objectIndex, "Center");
        return result;
    }

    /**
     * Build the color parameter name for an astronomical object.
     *
     * @param objectIndex astronomical object index
     * @return material parameter name
     */
    public static String objectColor(int objectIndex) {
        String result = object(objectIndex, "Color");
        return result;
    }

    /**
     * Build the color-map parameter name for an astronomical object.
     *
     * @param objectIndex astronomical object index
     * @return material parameter name
     */
    public static String objectColorMap(int objectIndex) {
        String result = object(objectIndex, "ColorMap");
        return result;
    }

    /**
     * Build the glow parameter name for an astronomical object.
     *
     * @param objectIndex astronomical object index
     * @return material parameter name
     */
    public static String objectGlow(int objectIndex) {
        String result = object(objectIndex, "Glow");
        return result;
    }

    /**
     * Build the horizontal transform parameter name for an object.
     *
     * @param objectIndex astronomical object index
     * @return material parameter name
     */
    public static String objectTransformU(int objectIndex) {
        String result = object(objectIndex, "TransformU");
        return result;
    }

    /**
     * Build the vertical transform parameter name for an object.
     *
     * @param objectIndex astronomical object index
     * @return material parameter name
     */
    public static String objectTransformV(int objectIndex) {
        String result = object(objectIndex, "TransformV");
        return result;
    }

    /**
     * Build a cloud-layer parameter name.
     *
     * @param layerIndex cloud layer index
     * @param suffix parameter suffix (not null)
     * @return material parameter name
     */
    private static String cloud(int layerIndex, String suffix) {
        assert suffix != null;
        String result = String.format(
                Locale.ROOT, "Clouds%d%s", layerIndex, suffix);
        return result;
    }

    /**
     * Build an astronomical-object parameter name.
     *
     * @param objectIndex astronomical object index
     * @param suffix parameter suffix (not null)
     * @return material parameter name
     */
    private static String object(int objectIndex, String suffix) {
        assert suffix != null;
        String result = String.format(
                Locale.ROOT, "Object%d%s", objectIndex, suffix);
        return result;
    }
}
