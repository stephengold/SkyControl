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
import com.jme3.texture.plugins.DDSLoader;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.command.SkyCommandBus;
import jme3utilities.sky.command.SkyCommandIds;
import jme3utilities.sky.command.SkyCommandResult;
import jme3utilities.sky.config.SkySimulationConfig;
import jme3utilities.sky.config.SkySimulationConfigLoader;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the Java executor for the Sky command ABI.
 *
 * @author Take Some
 */
public class TestSkyCommandAbi {
    /**
     * Verify supported command ids mutate and query sky state.
     */
    @Test
    public void testSkyCommandBus() {
        AssetManager assetManager = new DesktopAssetManager();
        assetManager.registerLoader(J3MLoader.class, "j3m", "j3md");
        assetManager.registerLoader(AWTLoader.class, "jpg", "png");
        assetManager.registerLoader(DDSLoader.class, "dds");
        assetManager.registerLocator(null, ClasspathLocator.class);

        SkySimulationConfig config
                = SkySimulationConfigLoader.loadDefault(assetManager);
        SkyControl skyControl = config.createControl(
                assetManager, new Camera(640, 480));
        SkyCommandBus commandBus = new SkyCommandBus(
                assetManager, skyControl);

        SkyCommandResult list = commandBus.execute(SkyCommandIds.weatherList);
        Assert.assertTrue(list.succeeded());
        Assert.assertTrue(list.values().contains("STORM"));

        SkyCommandResult weather = commandBus.execute(
                SkyCommandIds.weatherSet, "STORM", "0");
        Assert.assertTrue(weather.succeeded());
        Assert.assertTrue(skyControl.environment().isStorm());
        Assert.assertEquals("STORM", skyControl.environment().weather().id());

        SkyCommandResult setTime = commandBus.execute(
                SkyCommandIds.clockSetTime, "18.5");
        Assert.assertTrue(setTime.succeeded());
        Assert.assertEquals(18.5f,
                skyControl.getSunAndStars().getHour(), 0.0001f);

        SkyCommandResult advance = commandBus.execute(
                SkyCommandIds.clockAdvance, "3600", "86400");
        Assert.assertTrue(advance.succeeded());
        Assert.assertEquals(19.5f,
                skyControl.getSunAndStars().getHour(), 0.0001f);

        SkyCommandResult snapshot = commandBus.execute(
                SkyCommandIds.environmentSnapshot);
        Assert.assertTrue(snapshot.succeeded());
        Assert.assertNotNull(snapshot.snapshot());
        Assert.assertEquals("STORM", snapshot.snapshot().weatherId());

        SkyCommandResult reload = commandBus.execute(
                SkyCommandIds.configReload);
        Assert.assertTrue(reload.succeeded());
        Assert.assertEquals("FAIR", skyControl.environment().weather().id());
        Assert.assertTrue(skyControl.getCloudModulation());
    }
}
