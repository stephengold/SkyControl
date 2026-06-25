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

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.system.AppSettings;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.StarsOption;
import jme3utilities.sky.Updater;
import jme3utilities.sky.cloud.SkyCloudPreset;

/**
 * Visual smoke test for cloud weather presets and generated cloud shaders.
 *
 * @author Take Some
 */
final class SkyCloudWeatherSmoke extends SimpleApplication {
    /** Message logger for this class. */
    final private static Logger logger
            = Logger.getLogger(SkyCloudWeatherSmoke.class.getName());
    /** Transition duration between presets. */
    final private static float transitionSeconds = 1.2f;
    /** Application time accumulator. */
    private float elapsed;
    /** Next transition slot. */
    private int transitionIndex;
    /** Sky control under test. */
    private SkyControl skyControl;

    /**
     * Main entry point.
     *
     * @param arguments ignored command-line arguments
     */
    public static void main(String[] arguments) {
        logger.setLevel(Level.INFO);
        SkyCloudWeatherSmoke application = new SkyCloudWeatherSmoke();
        application.setPauseOnLostFocus(false);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setTitle("SkyCloudWeatherSmoke");
        settings.setResolution(960, 540);
        application.setSettings(settings);
        application.setShowSettings(false);
        application.start();
    }

    /** Initialize the scene. */
    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(25f);
        cam.setLocation(new Vector3f(0f, 3f, 8f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        AmbientLight ambientLight = new AmbientLight();
        DirectionalLight mainLight = new DirectionalLight();
        rootNode.addLight(ambientLight);
        rootNode.addLight(mainLight);

        float cloudFlattening = 0.9f;
        boolean bottomDome = true;
        this.skyControl = new SkyControl(
                assetManager, cam, cloudFlattening, StarsOption.TwoDomes,
                bottomDome);
        rootNode.addControl(skyControl);
        skyControl.setEnabled(true);
        skyControl.setCloudModulation(true);
        skyControl.getSunAndStars().setHour(14f);
        skyControl.setCloudPreset(SkyCloudPreset.WISPY, 0f);

        Updater updater = skyControl.getUpdater();
        updater.addViewPort(viewPort);
        updater.setAmbientLight(ambientLight);
        updater.setMainLight(mainLight);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
        bloom.setBloomIntensity(1.5f);
        fpp.addFilter(bloom);
        viewPort.addProcessor(fpp);
        updater.addBloomFilter(bloom);

        logger.info("Started WISPY preset");
    }

    /**
     * Update the smoke-test timeline.
     *
     * @param tpf time per frame in seconds
     */
    @Override
    public void simpleUpdate(float tpf) {
        elapsed += tpf;
        if (transitionIndex == 0 && elapsed >= 2f) {
            skyControl.setCloudPreset(SkyCloudPreset.RAIN, transitionSeconds);
            logger.info("Transition to RAIN");
            ++transitionIndex;
        } else if (transitionIndex == 1 && elapsed >= 5f) {
            skyControl.setCloudPreset(SkyCloudPreset.STORM, transitionSeconds);
            logger.info("Transition to STORM");
            ++transitionIndex;
        } else if (transitionIndex == 2 && elapsed >= 8f) {
            skyControl.setCloudPreset(SkyCloudPreset.WISPY, transitionSeconds);
            logger.info("Transition back to WISPY");
            ++transitionIndex;
        } else if (transitionIndex == 3 && elapsed >= 12f) {
            logger.info("Cloud weather visual smoke completed.");
            stop(true);
            ++transitionIndex;
        }
    }
}
