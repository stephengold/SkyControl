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
import jme3utilities.sky.cloud.SkyCloudPresetDefinition;
import jme3utilities.sky.cloud.SkyCloudPresetLoader;
import jme3utilities.sky.cloud.SkyCloudPresetRegistry;
import jme3utilities.sky.runtime.SkyEnvironmentRuntime;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the Lua weather ABI loader.
 *
 * @author Take Some
 */
public class TestSkyCloudPresetLoader {
    /**
     * Verify the default Lua ABI loads as a typed registry.
     */
    @Test
    public void testLuaWeatherAbi() {
        AssetManager assetManager = new DesktopAssetManager();
        assetManager.registerLocator(null, ClasspathLocator.class);

        SkyCloudPresetRegistry registry
                = SkyCloudPresetLoader.loadDefault(assetManager);
        SkyCloudPresetDefinition storm = registry.require("STORM");

        Assert.assertEquals("STORM", storm.id());
        Assert.assertEquals(3, storm.layerCount());
        Assert.assertEquals(90f, storm.defaultSeconds(), 0f);
        Assert.assertEquals(0.35f, storm.metrics().visibility(), 0.0001f);
        Assert.assertEquals(0.95f,
                storm.metrics().precipitation(), 0.0001f);
        Assert.assertTrue(storm.layer(0).alphaMap().contains("stormclouds"));
        Assert.assertNotNull(storm.layer(0).normalMap());

        SkyEnvironmentRuntime runtime = new SkyEnvironmentRuntime();
        runtime.setWeather(storm, storm.defaultSeconds());
        Assert.assertTrue(runtime.isStorm());
        Assert.assertEquals(0.90f, runtime.windStrength(), 0.0001f);
        Assert.assertEquals("STORM", runtime.weather().presetId());
    }
}
