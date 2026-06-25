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
import com.jme3.renderer.Camera;
import com.jme3.texture.plugins.AWTLoader;
import jme3utilities.math.MyMath;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.StarsOption;
import jme3utilities.sky.atmosphere.SkyGradientStyle;
import jme3utilities.sky.config.SkySimulationConfig;
import jme3utilities.sky.config.SkySimulationConfigLoader;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the Lua SkySimulation config ABI loader.
 *
 * @author Take Some
 */
public class TestSkySimulationConfigLoader {
    /**
     * Verify the default config loads and can construct a control.
     */
    @Test
    public void testDefaultSkyConfig() {
        AssetManager assetManager = new DesktopAssetManager();
        assetManager.registerLoader(J3MLoader.class, "j3m", "j3md");
        assetManager.registerLoader(AWTLoader.class, "jpg", "png");
        assetManager.registerLocator(null, ClasspathLocator.class);

        SkySimulationConfig config
                = SkySimulationConfigLoader.loadDefault(assetManager);
        Assert.assertEquals(12f, config.clock().hour(), 0f);
        Assert.assertEquals(StarsOption.TwoDomes,
                config.rendering().starsOption());
        Assert.assertEquals(0.9f,
                config.rendering().cloudFlattening(), 0.0001f);
        Assert.assertEquals("FAIR",
                config.integration().initialWeatherId());
        Assert.assertEquals(SkyGradientStyle.CINEMATIC,
                config.atmosphere().gradientStyle());
        Assert.assertEquals(1.25f,
                config.atmosphere().sunsetIntensity(), 0.0001f);
        Assert.assertEquals(1.15f,
                config.atmosphere().sunHaloIntensity(), 0.0001f);
        Assert.assertEquals(1.10f,
                config.atmosphere().moonHaloIntensity(), 0.0001f);

        Camera camera = new Camera(640, 480);
        SkyControl skyControl = config.createControl(assetManager, camera);
        Assert.assertEquals(12f,
                skyControl.getSunAndStars().getHour(), 0f);
        Assert.assertEquals(MyMath.toRadians(51.1788f),
                skyControl.getSunAndStars().getObserverLatitude(), 0.0001f);
        Assert.assertEquals(0.4f, skyControl.getCloudsYOffset(), 0.0001f);
        Assert.assertTrue(skyControl.getCloudModulation());
        Assert.assertEquals(SkyGradientStyle.CINEMATIC,
                skyControl.getAtmosphere().getGradientStyle());
        Assert.assertEquals(1.25f,
                skyControl.getAtmosphere().getSunsetIntensity(), 0.0001f);
        Assert.assertEquals("FAIR", skyControl.environment().weather().id());
        Assert.assertEquals(0.95f,
                skyControl.environment().visibility(), 0.0001f);
    }
}
