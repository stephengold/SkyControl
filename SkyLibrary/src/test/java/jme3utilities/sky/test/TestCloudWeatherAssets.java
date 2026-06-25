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
package jme3utilities.sky.test;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.material.plugins.J3MLoader;
import com.jme3.texture.plugins.AWTLoader;
import com.jme3.texture.plugins.DDSLoader;
import java.io.InputStream;
import jme3utilities.sky.SkyControlCore;
import jme3utilities.sky.SkyMaterial;
import jme3utilities.sky.cloud.SkyCloudAssets;
import jme3utilities.sky.cloud.SkyCloudLayerSpec;
import jme3utilities.sky.cloud.SkyCloudPreset;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cloud weather assets and material parameters without opening a window.
 *
 * @author Take Some
 */
public class TestCloudWeatherAssets {
    /** Asset manager for classpath resources. */
    final private static AssetManager assetManager = new DesktopAssetManager();

    /**
     * Test loading weather preset alpha and normal maps.
     */
    @Test
    public void testCloudWeatherAssets() {
        registerLoaders();

        assertRegistryExists();
        SkyMaterial material = new SkyMaterial(
                assetManager, 0, SkyControlCore.numCloudLayers);
        for (SkyCloudPreset preset : SkyCloudPreset.values()) {
            applyPreset(material, preset);
        }
    }

    /**
     * Apply a preset to a material.
     *
     * @param material material to mutate (not null)
     * @param preset preset to apply (not null)
     */
    private static void applyPreset(
            SkyMaterial material, SkyCloudPreset preset) {
        assert material != null;
        assert preset != null;

        for (int layerIndex = 0;
                layerIndex < SkyControlCore.numCloudLayers; ++layerIndex) {
            SkyCloudLayerSpec spec = preset.layer(layerIndex);
            assetManager.loadTexture(spec.alphaMap());
            material.addClouds(layerIndex, spec.alphaMap());
            if (spec.normalMap() == null) {
                material.setCloudsNormalMap(layerIndex, null);
            } else {
                material.setCloudsNormalMap(layerIndex, spec.normalMap());
            }
        }
    }

    /** Register asset locators and loaders. */
    private static void registerLoaders() {
        assetManager.registerLoader(AWTLoader.class, "jpg", "png");
        assetManager.registerLoader(DDSLoader.class, "dds");
        assetManager.registerLoader(J3MLoader.class, "j3m", "j3md");
        assetManager.registerLocator(null, ClasspathLocator.class);
    }

    /** Assert the registry exists on the classpath. */
    private static void assertRegistryExists() {
        String resourceName = "/" + SkyCloudAssets.registry;
        try (InputStream stream
                = TestCloudWeatherAssets.class.getResourceAsStream(
                        resourceName)) {
            Assert.assertNotNull(resourceName, stream);
        } catch (java.io.IOException exception) {
            throw new AssertionError(exception);
        }
    }
}
