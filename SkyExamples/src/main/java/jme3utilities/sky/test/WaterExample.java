/*
 Copyright (c) 2014-2023 Stephen Gold
 All rights reserved.

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
import com.jme3.light.Light;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.water.WaterFilter;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyCamera;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.StarsOption;
import jme3utilities.sky.Updater;

/**
 * An example that combines a SkyControl with a WaterFilter.
 * <p>
 * See issue #4.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class WaterExample extends SimpleApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(WaterExample.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = WaterExample.class.getSimpleName();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        SimpleApplication application = new WaterExample();
        Heart.parseAppArgs(application, arguments);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setTitle(applicationName);
        application.setSettings(settings);
        application.setShowSettings(false);
        application.start();
    }
    // *************************************************************************
    // SimpleApplication methods

    /**
     * Initialize this application.
     */
    @Override
    public void simpleInitApp() {
        initializeCamera();
        initializeLandscape();
        initializeLights();
        initializeSky();
    }
    // *************************************************************************
    // private methods

    /**
     * Configure the camera, including flyCam.
     */
    private void initializeCamera() {
        cam.setLocation(new Vector3f(177f, 17f, 326f));
        Vector3f direction = new Vector3f(0.3503f, -0.3145f, -0.8823f);
        MyCamera.look(cam, direction);

        flyCam.setDragToRotate(true);
        flyCam.setRotationSpeed(2f);
        flyCam.setMoveSpeed(20f);
        flyCam.setZoomSpeed(20f);
    }

    /**
     * Create, configure, add, and enable the landscape.
     */
    private void initializeLandscape() {
        // textures
        Texture alphaMap = assetManager.loadTexture(
                "Textures/Terrain/splat/alphamap.png");
        Texture dirt = loadSplatTexture("dirt.jpg");
        Texture dirtNormal = loadSplatTexture("dirt_normal.png");
        Texture heights = assetManager.loadTexture(
                "Textures/Terrain/splat/mountains512.png");

        // material
        Material terrainMaterial = new Material(assetManager,
                "Common/MatDefs/Terrain/TerrainLighting.j3md");
        terrainMaterial.setBoolean("useTriPlanarMapping", false);
        terrainMaterial.setBoolean("WardIso", true);
        terrainMaterial.setFloat("DiffuseMap_0_scale", 64f);
        terrainMaterial.setFloat("DiffuseMap_1_scale", 64f);
        terrainMaterial.setFloat("DiffuseMap_2_scale", 64f);
        terrainMaterial.setTexture("AlphaMap", alphaMap);
        terrainMaterial.setTexture("DiffuseMap", dirt);
        terrainMaterial.setTexture("DiffuseMap_1", dirt);
        terrainMaterial.setTexture("DiffuseMap_2", dirt);
        terrainMaterial.setTexture("NormalMap", dirtNormal);
        terrainMaterial.setTexture("NormalMap_1", dirtNormal);
        terrainMaterial.setTexture("NormalMap_2", dirtNormal);

        // spatials
        Image image = heights.getImage();
        ImageBasedHeightMap heightMap = new ImageBasedHeightMap(image);
        heightMap.load();
        float[] heightArray = heightMap.getHeightMap();
        TerrainQuad terrain = new TerrainQuad("terrain", 65, 513, heightArray);
        rootNode.attachChild(terrain);
        terrain.setLocalScale(2f, 0.25f, 2f);
        terrain.setMaterial(terrainMaterial);
    }

    /**
     * Create, configure, and add light sources.
     */
    private void initializeLights() {
        DirectionalLight mainLight = new DirectionalLight();
        Vector3f lightDirection = new Vector3f(-2f, -5f, 4f).normalize();
        mainLight.setColor(ColorRGBA.White.mult(1f));
        mainLight.setDirection(lightDirection);
        mainLight.setName("main");
        rootNode.addLight(mainLight);

        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(0.2f));
        ambientLight.setName("ambient");
        rootNode.addLight(ambientLight);

        // water filter
        WaterFilter water = new WaterFilter(rootNode, lightDirection);
        water.setCausticsIntensity(0.4f);
        water.setColorExtinction(new Vector3f(30f, 50f, 70f));
        ColorRGBA deepWaterColor = new ColorRGBA()
                .setAsSrgb(0.0039f, 0.00196f, 0.145f, 1f);
        water.setDeepWaterColor(deepWaterColor);
        water.setFoamExistence(new Vector3f(0.8f, 8f, 1f));
        water.setFoamHardness(0.3f);
        water.setFoamIntensity(0.04f);
        water.setMaxAmplitude(0.3f);
        water.setReflectionDisplace(1f);
        water.setRefractionConstant(0.25f);
        water.setRefractionStrength(0.2f);
        water.setUnderWaterFogDistance(80f);
        ColorRGBA waterColor
                = new ColorRGBA().setAsSrgb(0.0078f, 0.3176f, 0.5f, 1f);
        water.setWaterColor(waterColor);
        water.setWaterHeight(10f);
        water.setWaterTransparency(0.4f);
        water.setWaveScale(0.03f);
        Texture2D foamTexture = (Texture2D) assetManager.loadTexture(
                "Common/MatDefs/Water/Textures/foam2.jpg");
        water.setFoamTexture(foamTexture);

        int numSamples = getContext().getSettings().getSamples();
        FilterPostProcessor fpp
                = Heart.getFpp(viewPort, assetManager, numSamples);
        fpp.addFilter(water);
        viewPort.addProcessor(fpp);
    }

    /**
     * Create and attach the sky.
     */
    private void initializeSky() {
        float cloudFlattening = 0.8f;
        boolean bottomDome = true;
        SkyControl skyControl = new SkyControl(assetManager, cam,
                cloudFlattening, StarsOption.Cube, bottomDome);
        rootNode.addControl(skyControl);
        skyControl.setCloudiness(0.2f);
        skyControl.setCloudsYOffset(0.4f);
        //skyControl.getSunAndStars().setHour(12f);
        skyControl.setEnabled(true);

        Updater updater = skyControl.getUpdater();
        for (Light light : rootNode.getLocalLightList()) {
            String lightName = light.getName();
            switch (lightName) {
                case "ambient":
                    updater.setAmbientLight((AmbientLight) light);
                    break;
                case "main":
                    updater.setMainLight((DirectionalLight) light);
                    break;
                default:
            }
        }
    }

    /**
     * Load an inverted splat texture asset in "repeat" mode.
     *
     * @param fileName (not null)
     */
    private Texture loadSplatTexture(String fileName) {
        assert fileName != null;

        String path = String.format("Textures/Terrain/splat/%s", fileName);
        Texture result = assetManager.loadTexture(path);
        result.setWrap(Texture.WrapMode.Repeat);

        return result;
    }
}
