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
package jme3utilities.sky.config;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.renderer.Camera;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import jme3utilities.Validate;
import jme3utilities.sky.SkyAtmosphere;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.cloud.SkyCloudPresetDefinition;
import jme3utilities.sky.cloud.SkyCloudPresetLoader;
import jme3utilities.sky.cloud.SkyCloudPresetRegistry;

/**
 * Typed SkySimulation configuration loaded from Lua ABI.
 *
 * @author Take Some
 */
final public class SkySimulationConfig {
    /** Clock configuration. */
    final private SkyClockConfig clockConfig;
    /** Integration configuration. */
    final private SkyIntegrationConfig integrationConfig;
    /** Rendering configuration. */
    final private SkyRenderConfig renderConfig;

    /**
     * Instantiate sky simulation configuration.
     *
     * @param clockConfig clock configuration (not null)
     * @param renderConfig render configuration (not null)
     * @param integrationConfig integration configuration (not null)
     */
    public SkySimulationConfig(SkyClockConfig clockConfig,
            SkyRenderConfig renderConfig,
            SkyIntegrationConfig integrationConfig) {
        Validate.nonNull(clockConfig, "clock config");
        Validate.nonNull(renderConfig, "render config");
        Validate.nonNull(integrationConfig, "integration config");

        this.clockConfig = clockConfig;
        this.renderConfig = renderConfig;
        this.integrationConfig = integrationConfig;
    }

    /**
     * Apply this configuration to an existing sky control.
     *
     * @param assetManager asset manager (not null)
     * @param skyControl target control (not null)
     */
    public void applyTo(AssetManager assetManager, SkyControl skyControl) {
        Validate.nonNull(assetManager, "asset manager");
        Validate.nonNull(skyControl, "control");

        SkyAtmosphere atmosphere = loadAtmosphere(assetManager,
                integrationConfig.atmospherePath());
        skyControl.setAtmosphere(atmosphere);
        clockConfig.applyTo(skyControl);
        renderConfig.applyTo(skyControl);
        skyControl.setCloudModulation(integrationConfig.cloudModulation());

        SkyCloudPresetRegistry registry = SkyCloudPresetLoader.load(
                assetManager, integrationConfig.weatherPath());
        SkyCloudPresetDefinition definition = registry.require(
                integrationConfig.initialWeatherId());
        skyControl.setCloudPreset(
                definition, integrationConfig.transitionSec());
    }

    /**
     * Return clock configuration.
     *
     * @return clock configuration
     */
    public SkyClockConfig clock() {
        return clockConfig;
    }

    /**
     * Create and configure a sky control.
     *
     * @param assetManager asset manager (not null)
     * @param camera camera to track (not null)
     * @return configured control
     */
    public SkyControl createControl(AssetManager assetManager, Camera camera) {
        Validate.nonNull(assetManager, "asset manager");
        Validate.nonNull(camera, "camera");

        SkyControl result = new SkyControl(assetManager, camera,
                renderConfig.cloudFlattening(), renderConfig.starsOption(),
                renderConfig.lowerDome());
        applyTo(assetManager, result);
        return result;
    }

    /**
     * Return integration configuration.
     *
     * @return integration configuration
     */
    public SkyIntegrationConfig integration() {
        return integrationConfig;
    }

    /**
     * Return render configuration.
     *
     * @return render configuration
     */
    public SkyRenderConfig rendering() {
        return renderConfig;
    }

    /**
     * Load an atmosphere profile from Java properties.
     *
     * @param assetManager asset manager
     * @param assetPath properties asset path
     * @return loaded profile
     */
    private static SkyAtmosphere loadAtmosphere(AssetManager assetManager,
            String assetPath) {
        AssetKey<Object> key = new AssetKey<Object>(assetPath);
        AssetInfo info = assetManager.locateAsset(key);
        if (info == null) {
            throw new AssetNotFoundException(assetPath);
        }

        Properties properties = new Properties();
        try (InputStream input = info.openStream()) {
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Failed to read atmosphere profile: " + assetPath,
                    exception);
        }
        SkyAtmosphere result = new SkyAtmosphere();
        result.apply(properties);
        return result;
    }
}
