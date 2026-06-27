/*
 Copyright (c) 2013-2024, Stephen Gold

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
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.jme3.util.clone.Cloner;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyColor;
import jme3utilities.mesh.DomeMesh;
import jme3utilities.sky.atmosphere.SkyAtmosphereTransitionRuntime;
import jme3utilities.sky.atmosphere.SkyGradientStyle;
import jme3utilities.sky.cloud.SkyCloudPreset;
import jme3utilities.sky.cloud.SkyCloudPresetDefinition;
import jme3utilities.sky.control.SkyBaseColorRuntime;
import jme3utilities.sky.control.SkyCloudTransmissionRuntime;
import jme3utilities.sky.control.SkyLightSelectionRuntime;
import jme3utilities.sky.control.SkyLightingOutputRuntime;
import jme3utilities.sky.control.SkyMoonRuntime;
import jme3utilities.sky.control.SkyObjectLightingRuntime;
import jme3utilities.sky.runtime.SkyEnvironmentRuntime;
import jme3utilities.sky.runtime.SkyLightingSnapshot;
import jme3utilities.sky.runtime.SkyLightingState;
import jme3utilities.sky.runtime.SkyWorldClock;

/**
 * Simple control to simulate a dynamic sky using assets and techniques derived
 * from
 * http://code.google.com/p/jmonkeyplatform-contributions/source/browse/trunk/SkyDome
 * <p>
 * While not astronomically accurate, the simulation approximates the motion of
 * the sun and moon as seen from Earth. The coordinate system is: +X=north
 * horizon, +Y=zenith (up), and +Z=east horizon. The sun crosses the meridian at
 * noon (12:00 hours).
 * <p>
 * The control is disabled at creation. When enabled, it attaches a "sky" node
 * to the controlled spatial, which must be a scene-graph node. For best
 * results, place the scene's main light, ambient light, and shadow
 * filters/renderers under simulation control by adding them to the Updater.
 * <p>
 * The "top" dome is oriented so that its rim is parallel to the horizon. The
 * top dome implements the sun, moon, clear sky color, and horizon haze. Object
 * 0 is the sun, and object 1 is the moon.
 * <p>
 * This control simulates up to six layers of clouds. The cloud density may be
 * adjusted by invoking setCloudiness(). The rate of cloud motion may be
 * adjusted by invoking setCloudsSpeed(). Flatten the clouds for best results;
 * this puts them on a translucent "clouds only" dome.
 * <p>
 * To simulate star motion, additional geometries are added: either 2 domes or a
 * cube.
 * <p>
 * For scenes with low horizons, an optional "bottom" dome can also be added.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class SkyControl extends SkyControlCore {
    // *************************************************************************
    // constants and loggers

    /**
     * object index for the moon
     */
    final public static int moonIndex = 1;
    /**
     * object index for the sun
     */
    final public static int sunIndex = 0;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SkyControl.class.getName());
    /**
     * light direction for starlight: don't make this perfectly vertical because
     * that might cause shadow map aliasing
     */
    final private static Vector3f starlightDirection
            = new Vector3f(1f, 9f, 1f).normalizeLocal();
    // *************************************************************************
    // fields

    /**
     * true if clouds modulate the main light, false for steady light (the
     * default)
     */
    private boolean cloudModulationFlag = false;
    /**
     * base color of the daytime sky: pale blue
     */
    private ColorRGBA colorDay
            = new ColorRGBA(0.4f, 0.6f, 1f, Constants.alphaMax);
    /**
     * texture scale for moon images; larger value gives a larger moon
     * <p>
     * The default value (0.02) exaggerates the moon's size by a factor of 8.
     */
    private float moonScale = 0.02f;
    /**
     * texture scale for sun images; larger value would give a larger sun
     * <p>
     * The default value (0.08) exaggerates the sun's size by a factor of 8.
     */
    private float sunScale = 0.08f;
    /**
     * off-screen renderer for the moon
     */
    private GlobeRenderer moonRenderer = null;
    /**
     * phase-of-the-moon preset (default is FULL)
     */
    private LunarPhase phase = LunarPhase.FULL;
    /**
     * orientations of the sun and stars relative to the observer
     */
    private SunAndStars sunAndStars = null;
    /**
     * lights, shadows, and viewports to update
     */
    private Updater updater = null;
    /**
     * Game-facing environment runtime.
     */
    private SkyEnvironmentRuntime environmentRuntime = null;
    /** atmospheric lighting and realism profile */
    private SkyAtmosphere atmosphere = new SkyAtmosphere();
    /** Smooth runtime atmosphere gradient transition. */
    private SkyAtmosphereTransitionRuntime atmoTransition
            = new SkyAtmosphereTransitionRuntime();
    /** custom moon texture asset path, or null to use the phase preset */
    private String moonAssetPath = null;
    /** custom moon texture object, or null for phase preset or path */
    private Texture moonColorMap = null;
    /** Latest lighting snapshot exposed to game/runtime code. */
    private SkyLightingSnapshot lastLightSnapshot
            = SkyLightingSnapshot.empty();
    // *************************************************************************
    // constructors

    /**
     * No-argument constructor needed by SavableClassUtil.
     */
    protected SkyControl() {
        super();
        this.environmentRuntime = createEnvRuntime();
    }

    /**
     * Instantiate a disabled control for no clouds, full moon, no cloud
     * modulation, no lights, no shadows, and no viewports. For a visible sky,
     * the control must be (1) added to a node of the scene graph and (2)
     * enabled.
     *
     * @param assetManager for loading textures and material definitions (not
     * null)
     * @param camera the application's camera (not null)
     * @param cloudFlattening the oblateness (ellipticity) of the dome with the
     * clouds: 0 &rarr; no flattening (hemisphere), 1 &rarr; maximum flattening
     * @param starsOption how stars are rendered (not null)
     * @param bottomDome true to create a material and geometry for the
     * hemisphere below the horizon, false to leave this region to background
     * color (if starsOption==TopDome) or stars (if starsOption!=TopDome)
     */
    public SkyControl(
            AssetManager assetManager, Camera camera, float cloudFlattening,
            StarsOption starsOption, boolean bottomDome) {
        super(assetManager, camera, cloudFlattening, starsOption, bottomDome);

        this.sunAndStars = new SunAndStars();
        this.updater = new Updater();
        this.environmentRuntime = createEnvRuntime();
        setPhase(phase);
        setSunStyle("Textures/skies/suns/hazy-disc.png");

        assert !isEnabled();
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Copy the daytime clear-sky color.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a new object
     */
    public ColorRGBA colorDay(ColorRGBA storeResult) {
        ColorRGBA result
                = (storeResult == null) ? new ColorRGBA() : storeResult;
        result.set(colorDay);

        return result;
    }

    /**
     * Test the cloud modulation flag.
     *
     * @return true if clouds modulate the main light, false otherwise
     */
    public boolean getCloudModulation() {
        return cloudModulationFlag;
    }

    /**
     * Return the phase-of-the-moon preset.
     *
     * @return preset value (may be null)
     */
    public LunarPhase getPhase() {
        return phase;
    }

    /**
     * Access the orientations of the sun and stars.
     *
     * @return the pre-existing instance (not null)
     */
    public SunAndStars getSunAndStars() {
        assert sunAndStars != null;
        return sunAndStars;
    }

    /**
     * Access the updater.
     *
     * @return the pre-existing instance
     */
    public Updater getUpdater() {
        assert updater != null;
        return updater;
    }

    /**
     * Access the game-facing sky environment runtime.
     *
     * @return pre-existing runtime
     */
    public SkyEnvironmentRuntime environment() {
        ensureEnvRuntime();
        return environmentRuntime;
    }

    /**
     * Access the atmospheric tuning profile.
     *
     * @return the pre-existing mutable profile (not null)
     */
    public SkyAtmosphere getAtmosphere() {
        assert atmosphere != null;
        return atmosphere;
    }

    /**
     * Replace the atmospheric tuning profile.
     * <p>
     * The specified profile is copied, so later caller-side mutation will not
     * alter this control.
     *
     * @param newAtmosphere desired profile (not null, unaffected)
     */
    public void setAtmosphere(SkyAtmosphere newAtmosphere) {
        Validate.nonNull(newAtmosphere, "atmosphere");
        this.atmosphere = newAtmosphere.copy();
        this.atmoTransition = new SkyAtmosphereTransitionRuntime();
    }

    /**
     * Transition the atmosphere gradient style.
     *
     * @param style target style (not null)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void setGradientStyle(SkyGradientStyle style, float seconds) {
        atmoTransition.transitionStyle(atmosphere, style, seconds);
    }

    /**
     * Transition moon halo intensity.
     *
     * @param intensity target intensity (&ge;0)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void setMoonHaloIntensity(float intensity, float seconds) {
        atmoTransition.transitionMoon(atmosphere, intensity, seconds);
    }

    /**
     * Transition sun halo intensity.
     *
     * @param intensity target intensity (&ge;0)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void setSunHaloIntensity(float intensity, float seconds) {
        atmoTransition.transitionSun(atmosphere, intensity, seconds);
    }

    /**
     * Transition sunset intensity.
     *
     * @param intensity target intensity (&ge;0)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void setSunsetIntensity(float intensity, float seconds) {
        atmoTransition.transitionSunset(atmosphere, intensity, seconds);
    }

    /**
     * Copy the latest lighting output snapshot.
     *
     * @return copied lighting snapshot
     */
    public SkyLightingSnapshot lightingSnapshot() {
        ensureEnvRuntime();
        return lastLightSnapshot.copy();
    }

    /**
     * Calculate the angular diameter of the moon.
     *
     * @return diameter (in radians, &lt;Pi, &gt;0)
     */
    public float lunarDiameter() {
        float result = moonScale * FastMath.HALF_PI / Constants.uvScale;

        assert result > 0f : result;
        assert result < FastMath.PI : result;
        return result;
    }

    /**
     * Calculate the direction to the center of the moon.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a unit vector in world (horizontal) coordinates (either
     * storeResult or a new vector)
     */
    public Vector3f moonDirection(Vector3f storeResult) {
        float longitudeDifference = moonLongitudeDifference();
        float lunarLatitude = moonLatitude();
        Vector3f result = SkyMoonRuntime.direction(sunAndStars,
                longitudeDifference, lunarLatitude, storeResult);

        return result;
    }

    /**
     * Alter the cloud modulation flag.
     *
     * @param newValue true for clouds to modulate the main light, false for a
     * steady main light
     */
    public void setCloudModulation(boolean newValue) {
        this.cloudModulationFlag = newValue;
    }

    /**
     * Transition cloud layers to a weather preset and update environment state.
     *
     * @param preset target cloud preset (not null)
     * @param seconds transition duration in seconds (&ge;0)
     */
    @Override
    public void setCloudPreset(SkyCloudPreset preset, float seconds) {
        ensureEnvRuntime();
        environmentRuntime.setWeather(preset, seconds);
    }

    /**
     * Transition cloud layers to a data-driven weather preset and update state.
     *
     * @param definition target cloud preset definition (not null)
     * @param seconds transition duration in seconds (&ge;0)
     */
    @Override
    public void setCloudPreset(SkyCloudPresetDefinition definition,
            float seconds) {
        ensureEnvRuntime();
        environmentRuntime.setWeather(definition, seconds);
    }

    /**
     * Alter the daytime clear-sky color.
     *
     * @param newColor the desired color (not null, unaffected,
     * default=(0.4,0.6,1))
     */
    public void setColorDay(ColorRGBA newColor) {
        colorDay.set(newColor);
    }

    /**
     * Alter the angular diameter of the moon.
     *
     * @param newDiameter (in radians, &lt;Pi, &gt;0)
     */
    public void setLunarDiameter(float newDiameter) {
        if (!(newDiameter > 0f && newDiameter < FastMath.PI)) {
            logger.log(Level.SEVERE, "diameter={0}", newDiameter);
            throw new IllegalArgumentException(
                    "diameter should be between 0 and Pi");
        }

        moonScale = newDiameter * Constants.uvScale / FastMath.HALF_PI;
    }

    /**
     * Specify a globe renderer for the moon.
     *
     * @param newRenderer (not null)
     */
    public void setMoonRenderer(GlobeRenderer newRenderer) {
        Validate.nonNull(newRenderer, "renderer");

        if (moonRenderer != null) {
            boolean enabledFlag = moonRenderer.isEnabled();
            newRenderer.setEnabled(enabledFlag);
        }
        this.moonRenderer = newRenderer;

        if (moonRenderer.isEnabled()) {
            this.moonAssetPath = null;
            this.moonColorMap = null;
            Texture dynamicTexture = moonRenderer.getTexture();
            SkyMaterial topMaterial = getTopMaterial();
            topMaterial.addObject(moonIndex, dynamicTexture);
        }
    }

    /** Remove any custom moon texture and return to phase-preset images. */
    public void clearMoonTexture() {
        this.moonAssetPath = null;
        this.moonColorMap = null;
        setPhase(phase);
    }

    /**
     * Alter the moon's color map texture using an asset path.
     *
     * @param assetPath path to the texture asset (not null, not empty)
     */
    public void setMoonTexture(String assetPath) {
        Validate.nonEmpty(assetPath, "asset path");
        this.moonAssetPath = assetPath;
        this.moonColorMap = null;
        if (moonRenderer != null) {
            moonRenderer.setEnabled(false);
        }
        applyMoonTexture();
    }

    /**
     * Alter the moon's color map texture using a pre-loaded texture.
     *
     * @param colorMap texture to apply (not null)
     */
    public void setMoonTexture(Texture colorMap) {
        Validate.nonNull(colorMap, "texture");
        this.moonAssetPath = null;
        this.moonColorMap = colorMap;
        if (moonRenderer != null) {
            moonRenderer.setEnabled(false);
        }
        applyMoonTexture();
    }

    /**
     * Alter the phase of the moon to a pre-set value.
     *
     * @param newPreset (or null to hide the moon)
     */
    final public void setPhase(LunarPhase newPreset) {
        if (newPreset == LunarPhase.CUSTOM) {
            setPhase(moonLongitudeDifference(), moonLatitude());
            return;
        }

        if (moonRenderer != null) {
            moonRenderer.setEnabled(false);
        }
        this.phase = newPreset;
        if (newPreset != null) {
            setMoonLongDiff(newPreset.longitudeDifference());
            if (!applyMoonTexture()) {
                SkyMaterial topMaterial = getTopMaterial();
                SkyMoonRuntime.applyPresetTexture(
                        topMaterial, moonIndex, newPreset);
            }
        }
    }

    /**
     * Customize the phase of the moon for off-screen rendering.
     *
     * @param longitudeDifference radians east of the sun (&le;2*Pi, &ge;0)
     * @param lunarLatitude radians north of the ecliptic (&le;Pi/2, &ge;-Pi/2)
     */
    public void setPhase(float longitudeDifference, float lunarLatitude) {
        Validate.inRange(longitudeDifference, "longitude difference",
                0f, FastMath.TWO_PI);
        Validate.inRange(lunarLatitude, "lunar latitude",
                -FastMath.HALF_PI, FastMath.HALF_PI);
        if (moonRenderer == null) {
            throw new IllegalStateException("moon renderer not yet added");
        }

        moonRenderer.setEnabled(true);
        this.phase = LunarPhase.CUSTOM;
        setCelestialPhase(longitudeDifference, lunarLatitude);
        this.moonAssetPath = null;
        this.moonColorMap = null;

        Texture dynamicTexture = moonRenderer.getTexture();
        SkyMaterial topMaterial = getTopMaterial();
        topMaterial.addObject(moonIndex, dynamicTexture);
    }

    /**
     * Alter the angular diameter of the sun.
     *
     * @param newDiameter (in radians, &lt;Pi, &gt;0)
     */
    public void setSolarDiameter(float newDiameter) {
        if (!(newDiameter > 0f && newDiameter < FastMath.PI)) {
            logger.log(Level.SEVERE, "diameter={0}", newDiameter);
            throw new IllegalArgumentException(
                    "diameter should be between 0 and Pi");
        }

        this.sunScale = newDiameter * Constants.uvScale
                / (Constants.discDiameter * FastMath.HALF_PI);
    }

    /**
     * Alter the sun's color map.
     *
     * @param assetPath path to the texture asset (not null, not empty)
     */
    final public void setSunStyle(String assetPath) {
        setSunTexture(assetPath);
    }

    /**
     * Alter the sun's color map texture using an asset path.
     *
     * @param assetPath path to the texture asset (not null, not empty)
     */
    public void setSunTexture(String assetPath) {
        Validate.nonEmpty(assetPath, "asset path");
        SkyMaterial topMaterial = getTopMaterial();
        topMaterial.addObject(sunIndex, assetPath);
    }

    /**
     * Alter the sun's color map texture using a pre-loaded texture.
     *
     * @param colorMap texture to apply (not null)
     */
    public void setSunTexture(Texture colorMap) {
        Validate.nonNull(colorMap, "texture");
        setObjectTexture(sunIndex, colorMap);
    }

    /**
     * Calculate the angular diameter of the sun.
     *
     * @return diameter (in radians, &lt;Pi, &gt;0)
     */
    public float solarDiameter() {
        float result = sunScale * Constants.discDiameter * FastMath.HALF_PI
                / Constants.uvScale;

        assert result > 0f : result;
        assert result < FastMath.PI : result;
        return result;
    }

    /**
     * Update cloud colors using the atmospheric profile.
     * <p>
     * Subclasses overriding this method should preserve the contract of
     * returning the color used for ambient-light estimation and should update
     * every configured cloud layer.
     *
     * @param baseColor source color (not null, unaffected)
     * @param sunUp true if the sun is above the horizon, otherwise false
     * @param moonUp true if the moon is above the horizon, otherwise false
     * @return new color used for ambient-light estimation
     */
    @Override
    protected ColorRGBA updateCloudsColor(
            ColorRGBA baseColor, boolean sunUp, boolean moonUp) {
        assert baseColor != null;

        ColorRGBA cloudsColor = MyColor.saturate(baseColor);
        float brightness = atmosphere.getCloudDayBrightness();
        if (!sunUp) {
            brightness = atmosphere.getCloudNight();
            if (moonUp) {
                brightness += atmosphere.getCloudMoonBoost()
                        * getMoonIllumination();
            }
        }
        cloudsColor.multLocal(brightness);
        setCloudLayersColor(cloudsColor);

        return cloudsColor;
    }

    // *************************************************************************
    // SkyControlCore methods

    /**
     * Create a shallow copy of this control.
     *
     * @return a new control, equivalent to this one
     * @throws CloneNotSupportedException if superclass isn't cloneable
     */
    @Override
    public SkyControl clone() throws CloneNotSupportedException {
        SkyControl clone = (SkyControl) super.clone();
        return clone;
    }

    /**
     * Convert this shallow-cloned control into a deep-cloned one, using the
     * specified cloner and original to resolve copied fields.
     *
     * @param cloner the cloner currently cloning this control
     * @param original the control from which this control was shallow-cloned
     */
    @Override
    public void cloneFields(Cloner cloner, Object original) {
        super.cloneFields(cloner, original);

        final SkyControl originalControl = (SkyControl) original;
        this.colorDay = cloner.clone(colorDay);
        this.moonRenderer = cloner.clone(moonRenderer);
        this.moonColorMap = cloner.clone(moonColorMap);
        this.atmosphere = cloner.clone(atmosphere);
        this.sunAndStars = cloner.clone(sunAndStars);
        this.updater = cloner.clone(updater);
        this.lastLightSnapshot
                = originalControl.lastLightSnapshot.copy();
        this.environmentRuntime = createEnvRuntime();
        if (originalControl.environmentRuntime != null) {
            this.environmentRuntime.restoreWeather(
                    originalControl.environmentRuntime.weather());
            this.environmentRuntime.updateLighting(lastLightSnapshot);
        }
    }

    /**
     * Callback to update this control prior to rendering.
     *
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void controlUpdate(float tpf) {
        super.controlUpdate(tpf);
        atmoTransition.update(atmosphere, tpf);
        updateAll();
    }

    /**
     * De-serialize this instance, for example when loading from a J3O file.
     *
     * @param importer (not null)
     * @throws IOException from importer
     */
    @Override
    public void read(JmeImporter importer) throws IOException {
        super.read(importer);
        InputCapsule ic = importer.getCapsule(this);

        this.cloudModulationFlag = ic.readBoolean("cloudModulationFlag", false);
        this.colorDay = (ColorRGBA) ic.readSavable(
                "colorDay", new ColorRGBA(0.4f, 0.6f, 1f, Constants.alphaMax));
        this.moonScale = ic.readFloat("moonScale", 0.02f);
        this.moonAssetPath = ic.readString("moonAssetPath", null);
        this.moonColorMap = (Texture) ic.readSavable("moonColorMap", null);
        this.atmosphere = (SkyAtmosphere) ic.readSavable(
                "atmosphere", new SkyAtmosphere());
        this.sunScale = ic.readFloat("sunScale", 0.08f);
        // moon renderer not serialized
        this.phase = ic.readEnum("phase", LunarPhase.class, LunarPhase.FULL);
        this.sunAndStars = (SunAndStars) ic.readSavable("sunAndStars", null);
        this.updater = (Updater) ic.readSavable("updater", null);
        this.lastLightSnapshot = SkyLightingSnapshot.empty();
        this.environmentRuntime = createEnvRuntime();
        if (sunAndStars != null) {
            environmentRuntime.clock().setTimeOfDay(sunAndStars.getHour());
        }
    }

    /**
     * Serialize this instance, for example when saving to a J3O file.
     *
     * @param exporter (not null)
     * @throws IOException from exporter
     */
    @Override
    public void write(JmeExporter exporter) throws IOException {
        super.write(exporter);
        OutputCapsule oc = exporter.getCapsule(this);

        oc.write(cloudModulationFlag, "cloudModulationFlag", false);
        oc.write(colorDay, "colorDay",
                new ColorRGBA(0.4f, 0.6f, 1f, Constants.alphaMax));
        oc.write(moonScale, "moonScale", 0.02f);
        oc.write(moonAssetPath, "moonAssetPath", null);
        oc.write(moonColorMap, "moonColorMap", null);
        oc.write(atmosphere, "atmosphere", null);
        oc.write(sunScale, "sunScale", 0.08f);
        // moon renderer not serialized
        oc.write(phase, "phase", LunarPhase.FULL);
        oc.write(sunAndStars, "sunAndStars", null);
        oc.write(updater, "updater", null);
    }
    // *************************************************************************
    // private methods

    /**
     * Apply a cloud preset using the inherited visual cloud runtime.
     *
     * @param preset target cloud preset
     * @param seconds transition duration in seconds
     */
    private void applyCloudPresetToCore(SkyCloudPreset preset, float seconds) {
        super.setCloudPreset(preset, seconds);
    }

    /**
     * Apply a data-driven cloud preset using the inherited runtime.
     *
     * @param definition target cloud preset definition
     * @param seconds transition duration in seconds
     */
    private void applyCloudPresetToCore(SkyCloudPresetDefinition definition,
            float seconds) {
        super.setCloudPreset(definition, seconds);
    }

    /**
     * Create the game-facing environment runtime.
     *
     * @return new runtime
     */
    private SkyEnvironmentRuntime createEnvRuntime() {
        SkyEnvironmentRuntime result = new SkyEnvironmentRuntime(
                new SkyWorldClock.TimeApplier() {
                    @Override
                    public void applyTimeOfDay(float timeOfDayHours) {
                        if (sunAndStars != null) {
                            sunAndStars.setHour(timeOfDayHours);
                        }
                    }
                },
                new SkyEnvironmentRuntime.WeatherApplier() {
                    @Override
                    public void applyWeather(
                            SkyCloudPreset preset, float seconds) {
                        applyCloudPresetToCore(preset, seconds);
                    }

                    @Override
                    public void applyWeather(
                            SkyCloudPresetDefinition definition,
                            float seconds) {
                        applyCloudPresetToCore(definition, seconds);
                    }
                });
        return result;
    }

    /**
     * Ensure the environment runtime exists.
     */
    private void ensureEnvRuntime() {
        if (environmentRuntime == null) {
            environmentRuntime = createEnvRuntime();
        }
    }

    /**
     * Apply the custom moon texture, if one has been specified.
     *
     * @return true if a custom texture was applied, otherwise false
     */
    private boolean applyMoonTexture() {
        SkyMaterial topMaterial = getTopMaterial();
        boolean result = SkyMoonRuntime.applyTexture(
                topMaterial, moonIndex, moonAssetPath, moonColorMap);
        return result;
    }

    /**
     * Update astronomical objects, sky color, lighting, and stars.
     */
    private void updateAll() {
        /*
         * Daytime sky color is phased in during the twilight periods
         * before sunrise and after sunset. Update the sky material's
         * clear color accordingly.
         */
        Vector3f sunDirection = updateSun();
        ColorRGBA clearColor = colorDay.clone();
        float twilightLimit = atmosphere.getTwilightLimit();
        clearColor.a = FastMath.saturate(1f + sunDirection.y / twilightLimit);
        SkyMaterial topMaterial = getTopMaterial();
        topMaterial.setClearColor(clearColor);

        Vector3f moonDirection = updateMoon();
        updateLighting(sunDirection, moonDirection);

        Node starsNode = getStarsNode();
        if (starsNode != null) {
            sunAndStars.orientEquatorialSky(starsNode, false);
        }
    }

    /**
     * Update background colors, cloud colors, haze color, sun color, lights,
     * and shadows.
     *
     * @param sunDirection world direction to the sun (length=1)
     * @param moonDirection world direction to the moon (length=1 or null)
     */
    private void updateLighting(Vector3f sunDirection, Vector3f moonDirection) {
        float moonWeight = getMoonIllumination();
        SkyLightSelectionRuntime.Result lightSelection
                = SkyLightSelectionRuntime.select(
                        sunDirection, moonDirection, moonWeight,
                        starlightDirection);
        float sineSolarAltitude = lightSelection.sineSolarAltitude();
        float sineLunarAltitude = lightSelection.sineLunarAltitude();
        boolean moonUp = lightSelection.moonUp();
        boolean sunUp = lightSelection.sunUp();
        Vector3f mainDirection = lightSelection.mainDirection();

        SkyMaterial topMaterial = getTopMaterial();
        SkyObjectLightingRuntime.updateObjects(
                topMaterial, atmosphere, sineSolarAltitude, sineLunarAltitude);
        /*
         * Determine the base color (applied to horizon haze, bottom dome, and
         * viewport backgrounds) using the sun's altitude.
         */
        SkyBaseColorRuntime.Result baseResult
                = SkyBaseColorRuntime.compute(
                        atmosphere, sineSolarAltitude, moonUp, moonWeight);
        ColorRGBA baseColor = baseResult.baseColor();
        topMaterial.setHazeColor(baseColor);
        Material bottomMaterial = getBottomMaterial();
        if (bottomMaterial != null) {
            bottomMaterial.setColor("Color", baseColor.clone());
        }

        ColorRGBA cloudsColor = updateCloudsColor(baseColor, sunUp, moonUp);

        SkyCloudTransmissionRuntime.Input transmissionInput
                = new SkyCloudTransmissionRuntime.Input(
                        cloudModulationFlag, sunUp, moonUp, moonWeight,
                        mainDirection);
        SkyCloudTransmissionRuntime.Resources transmissionResources
                = new SkyCloudTransmissionRuntime.Resources(
                        getCloudsOnlyDome(), getCloudsMesh(),
                        getCloudsMaterial());
        float transmit = SkyCloudTransmissionRuntime.transmission(
                transmissionInput, transmissionResources);

        SkyLightingOutputRuntime.Input outputInput
                = new SkyLightingOutputRuntime.Input(
                        atmosphere, lightSelection, baseResult, cloudsColor,
                        moonWeight, transmit);
        SkyLightingOutputRuntime.Result output
                = SkyLightingOutputRuntime.compute(outputInput);
        ColorRGBA ambient = output.ambientColor();
        ColorRGBA main = output.mainDirectionalColor();
        float bloomIntensity = output.bloomIntensity();
        float shadowIntensity = output.shadowIntensity();

        SkyLightingState lightingState = new SkyLightingState(
                bloomIntensity, shadowIntensity, sunUp, moonUp);
        lastLightSnapshot = new SkyLightingSnapshot(
                ambient, baseColor, main, mainDirection, lightingState);
        ensureEnvRuntime();
        environmentRuntime.updateLighting(lastLightSnapshot);

        updater.update(ambient, baseColor, main, bloomIntensity,
                shadowIntensity, mainDirection);
    }

    /**
     * Update the moon's position and size.
     *
     * @return world direction to the moon (new unit vector) or null if the moon
     * is hidden
     */
    private Vector3f updateMoon() {
        SkyMaterial topMaterial = getTopMaterial();
        DomeMesh topMesh = getTopMesh();
        float longitudeDifference = moonLongitudeDifference();
        float lunarLatitude = moonLatitude();
        SkyMoonRuntime.MoonUpdateState state
                = new SkyMoonRuntime.MoonUpdateState(moonRenderer, moonIndex,
                        longitudeDifference, lunarLatitude, moonScale);
        Vector3f result = SkyMoonRuntime.updateMoon(
                topMaterial, topMesh, sunAndStars, phase, state);

        return result;
    }

    /**
     * Update the sun's position and size.
     *
     * @return world direction to the sun (new unit vector)
     */
    private Vector3f updateSun() {
        // Calculate the UV coordinates of the center of the sun.
        Vector3f worldDirection = sunAndStars.sunDirection(null);
        DomeMesh topMesh = getTopMesh();
        Vector2f uv = topMesh.directionUV(worldDirection);
        SkyMaterial topMaterial = getTopMaterial();
        if (uv == null) { // The sun is below the horizon, so hide it.
            topMaterial.hideObject(sunIndex);
        } else {
            topMaterial.setObjectTransform(sunIndex, uv, sunScale, null);
        }

        return worldDirection;
    }
}
