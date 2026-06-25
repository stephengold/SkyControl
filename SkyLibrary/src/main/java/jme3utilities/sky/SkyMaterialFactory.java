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

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import jme3utilities.MyAsset;

/**
 * Factory for sky materials and cloud-layer runtime objects.
 *
 * @author Take Some
 */
final class SkyMaterialFactory {
    /**
     * Hidden constructor.
     */
    private SkyMaterialFactory() {
        // do nothing
    }

    /**
     * Create the optional bottom-dome material.
     *
     * @param assetManager asset manager (not null)
     * @param enabled true to create a material, otherwise false
     * @return material, or null when disabled
     */
    static Material createBottom(AssetManager assetManager, boolean enabled) {
        Material result = enabled
                ? MyAsset.createUnshadedMaterial(assetManager) : null;
        return result;
    }

    /**
     * Create and initialize the cloud material.
     *
     * @param assetManager asset manager (not null)
     * @param separateDome true for a separate cloud dome
     * @param topMaterial initialized top material (not null)
     * @return initialized cloud material (not null)
     */
    static SkyMaterial createClouds(AssetManager assetManager,
            boolean separateDome, SkyMaterial topMaterial) {
        SkyMaterial result;
        if (separateDome) {
            int numObjects = 0;
            result = new SkyMaterial(
                    assetManager, numObjects, SkyControlCore.numCloudLayers);
            result.initialize();
            result.getAdditionalRenderState().setDepthWrite(false);
            result.setClearColor(ColorRGBA.BlackNoAlpha);
        } else {
            result = topMaterial;
        }

        return result;
    }

    /**
     * Create cloud-layer runtime objects.
     *
     * @param material cloud material (not null)
     * @return new cloud-layer array
     */
    static CloudLayer[] createLayers(SkyMaterial material) {
        CloudLayer[] result = new CloudLayer[SkyControlCore.numCloudLayers];
        for (int layer = 0; layer < SkyControlCore.numCloudLayers; ++layer) {
            result[layer] = new CloudLayer(material, layer);
        }

        return result;
    }

    /**
     * Create and initialize the top sky material.
     *
     * @param assetManager asset manager (not null)
     * @param separateCloudDome true when clouds use a separate dome
     * @param starsOption star rendering option (not null)
     * @return initialized top material (not null)
     */
    static SkyMaterial createTop(AssetManager assetManager,
            boolean separateCloudDome, StarsOption starsOption) {
        int topObjects = 2;
        int topClouds = separateCloudDome ? 0 : SkyControlCore.numCloudLayers;
        SkyMaterial result = new SkyMaterial(assetManager, topObjects,
                topClouds);
        result.initialize();
        result.addHaze();
        if (starsOption == StarsOption.TopDome) {
            result.addStars();
        }

        return result;
    }
}
