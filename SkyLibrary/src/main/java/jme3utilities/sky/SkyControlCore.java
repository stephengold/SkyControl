/*
 Copyright (c) 2014-2024, Stephen Gold

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
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.clone.Cloner;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.MySpatial;
import jme3utilities.SubtreeControl;
import jme3utilities.Validate;
import jme3utilities.math.MyColor;
import jme3utilities.mesh.DomeMesh;
import jme3utilities.sky.cloud.SkyCloudPreset;
import jme3utilities.sky.cloud.SkyCloudPresetDefinition;
import jme3utilities.sky.control.SkyCelestialState;
import jme3utilities.sky.control.SkyCloudRuntime;
import jme3utilities.sky.material.SkyMaterialFactory;
import jme3utilities.sky.scene.SkyDomeFactory;
import jme3utilities.sky.scene.SkySceneLookup;

/**
 * Core fields and methods of a SubtreeControl to simulate a dynamic sky.
 * <p>
 * The Control is disabled at creation. When enabled, it attaches a "sky" node
 * to the controlled spatial, which must be a scene-graph node.
 * <p>
 * The "top" dome is oriented so that its rim is parallel to the horizon. The
 * top dome implements the sun, moon, clear sky color, and horizon haze.
 * <p>
 * This Control simulates up to 6 layers of clouds. The cloud density may be
 * adjusted by invoking setCloudiness(). The rate of cloud motion may be
 * adjusted by invoking setCloudsRate(). Flatten the clouds for best results;
 * this puts them on a translucent "clouds only" dome.
 * <p>
 * To simulate star motion, additional geometries are added: either 2 domes or a
 * cube.
 * <p>
 * For scenes with low horizons, an optional "bottom" dome can also be added.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class SkyControlCore extends SubtreeControl {
    // *************************************************************************
    // constants and loggers

    /**
     * maximum number of cloud layers
     */
    final public static int numCloudLayers = 6;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SkyControlCore.class.getName());
    /**
     * local copy of {@link com.jme3.math.Quaternion#IDENTITY}
     */
    final private static Quaternion rotationIdentity = new Quaternion();
    // *************************************************************************
    // fields

    /**
     * asset manager for loading textures and material definitions: set by
     * constructor
     */
    private AssetManager assetManager;
    /**
     * true to create a material and geometry for the hemisphere below the
     * horizon, false to leave this region to background color (if
     * starsOption==TopDome) or stars only (if starsOption!=TopDome): set by
     * constructor
     */
    final private boolean bottomDomeFlag;
    /**
     * true to counteract rotation of the controlled node, false to allow
     * rotation
     */
    private boolean stabilizeFlag = false;
    /**
     * which camera to track: set by constructor or
     * {@link #setCamera(com.jme3.renderer.Camera)} or {@link
     * #render(com.jme3.renderer.RenderManager, com.jme3.renderer.ViewPort)}
     */
    private Camera camera;
    /**
     * Runtime state and update logic for cloud layers.
     */
    private SkyCloudRuntime cloudRuntime;
    /**
     * Runtime state for the moon's latitude and phase angle.
     */
    private SkyCelestialState celestialState = new SkyCelestialState();
    /**
     * how stars are rendered: set by constructor
     */
    protected StarsOption starsOption; // TODO privatize
    // *************************************************************************
    // constructors

    /**
     * No-argument constructor needed by SavableClassUtil.
     */
    protected SkyControlCore() {
        this.assetManager = null;
        this.bottomDomeFlag = false;
        this.starsOption = StarsOption.TopDome;
        this.camera = null;
        this.cloudRuntime = null;
        this.celestialState = new SkyCelestialState();
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
     * clouds (&ge;0, &lt;1, 0 &rarr; no flattening (hemisphere), 1 &rarr;
     * maximum flattening
     * @param starsOption how stars are rendered (not null)
     * @param bottomDomeFlag true to create a bottom dome, false to leave this
     * region to background color (if starsOption==TopDome) or stars (if
     * starsOption!=TopDome)
     */
    public SkyControlCore(
            AssetManager assetManager, Camera camera, float cloudFlattening,
            StarsOption starsOption, boolean bottomDomeFlag) {
        Validate.nonNull(assetManager, "asset manager");
        Validate.nonNull(camera, "camera");
        if (!(cloudFlattening >= 0f && cloudFlattening < 1f)) {
            logger.log(Level.SEVERE, "cloudFlattening={0}", cloudFlattening);
            throw new IllegalArgumentException(
                    "flattening should be between 0 and 1");
        }
        Validate.nonNull(starsOption, "stars option");

        this.assetManager = assetManager;
        this.camera = camera;
        this.starsOption = starsOption;
        this.bottomDomeFlag = bottomDomeFlag;

        boolean separateCloudDome = cloudFlattening != 0f;
        SkyMaterial topMaterial = SkyMaterialFactory.createTop(
                assetManager, separateCloudDome, starsOption);
        SkyMaterial cloudsMaterial = SkyMaterialFactory.createClouds(
                assetManager, separateCloudDome, topMaterial);
        CloudLayer[] cloudLayers
                = SkyCloudLayerFactory.createLayers(cloudsMaterial);
        this.cloudRuntime = new SkyCloudRuntime(cloudLayers);
        Material bottomMaterial = SkyMaterialFactory.createBottom(
                assetManager, bottomDomeFlag);
        Node subtreeNode = SkyDomeFactory.createSubtree(cloudFlattening,
                bottomDomeFlag, topMaterial, bottomMaterial, cloudsMaterial);
        setSubtree(subtreeNode);
        Node starsNode = SkyDomeFactory.createDefaultStars(
                assetManager, starsOption);
        if (starsNode != null) {
            SkyDomeFactory.attachStars(subtreeNode, starsNode);
        }

        assert !isEnabled();
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Clear the star maps.
     */
    public void clearStarMaps() {
        switch (starsOption) {
            case Cube:
            case TwoDomes:
                removeStarsNode();
                break;

            case TopDome:
                SkyMaterial topMaterial = getTopMaterial();
                topMaterial.removeStars();
                break;

            default:
                throw new IllegalStateException("option = " + starsOption);
        }
    }

    /**
     * Access the indexed cloud layer.
     *
     * @param layerIndex (&lt;numCloudLayers, &ge;0)
     * @return pre-existing instance
     */
    public CloudLayer getCloudLayer(int layerIndex) {
        Validate.inRange(
                layerIndex, "cloud layer index", 0, numCloudLayers - 1);
        CloudLayer result = cloudRuntime.getLayer(layerIndex);
        return result;
    }

    /**
     * Return the speed and direction of cloud motion (all layers).
     *
     * @return multiple of the default rate (may be negative)
     */
    public float getCloudsRate() {
        float result = cloudRuntime.rate();
        return result;
    }

    /**
     * Return the vertical offset of the clouds-only dome.
     *
     * @return vertical offset as a fraction of the dome height
     */
    public float getCloudsYOffset() {
        Spatial cloudsOnlyDome = getCloudsOnlyDome();
        float result = 0f;
        if (cloudsOnlyDome != null) {
            float deltaY = cloudsOnlyDome.getLocalTranslation().y;
            result = -deltaY / cloudsOnlyDome.getLocalScale().y;
        }

        return result;
    }

    /**
     * Return the difference in celestial longitude (lambda) between the moon
     * and the sun.
     *
     * @return radians east of the sun
     */
    public float getLongitudeDifference() {
        float result = celestialState.longitudeDifference();
        return result;
    }

    /**
     * Return the lunar latitude (beta).
     *
     * @return radians north of the ecliptic (&ge;-Pi/2, &le;Pi/2)
     */
    public float getLunarLatitude() {
        float result = celestialState.lunarLatitude();
        return result;
    }

    /**
     * Compute the contribution of the moon to the nighttime illumination mix
     * using its phase, assuming it is above the horizon.
     *
     * @return fraction (&le;1, &ge;0) 1 &rarr; full moon, 0 &rarr; no
     * contribution
     */
    public float getMoonIllumination() {
        float result = celestialState.moonIllumination();
        return result;
    }

    /**
     * Return the vertical angle of the top dome.
     *
     * @return angle from the zenith to the rim of the top dome (in radians,
     * &lt;1.785, &gt;0)
     * @see #setTopVerticalAngle(float)
     */
    public float getTopVerticalAngle() {
        DomeMesh topMesh = getTopMesh();
        float result = topMesh.getVerticalAngle();

        return result;
    }

    /**
     * Alter which camera to track.
     *
     * @param camera which camera to track (not null, alias created)
     */
    public void setCamera(Camera camera) {
        Validate.nonNull(camera, "camera");
        this.camera = camera;
    }

    /**
     * Alter the opacity of all cloud layers.
     *
     * @param newAlpha desired opacity of the cloud layers (&le;1, &ge;0)
     */
    public void setCloudiness(float newAlpha) {
        Validate.fraction(newAlpha, "alpha");

        cloudRuntime.setCloudiness(newAlpha);
    }

    /**
     * Alter the speed and/or direction of cloud motion (all layers).
     *
     * @param newRate multiple of the default rate (may be negative, default=1)
     */
    public void setCloudsRate(float newRate) {
        cloudRuntime.setRate(newRate);
    }

    /**
     * Transition cloud layers to a weather preset.
     * <p>
     * Textures are swapped only after the affected layers fade out, then the
     * target layers fade back in.
     *
     * @param preset target cloud preset (not null)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void setCloudPreset(SkyCloudPreset preset, float seconds) {
        Validate.nonNull(preset, "preset");
        if (!(seconds >= 0f)) {
            throw new IllegalArgumentException("duration must be non-negative");
        }

        cloudRuntime.transitionTo(preset, seconds);
    }

    /**
     * Transition cloud layers to a data-driven weather preset.
     * <p>
     * Textures are swapped only after the affected layers fade out, then the
     * target layers fade back in.
     *
     * @param definition target cloud preset definition (not null)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void setCloudPreset(SkyCloudPresetDefinition definition,
            float seconds) {
        Validate.nonNull(definition, "definition");
        if (!(seconds >= 0f)) {
            throw new IllegalArgumentException("duration must be non-negative");
        }

        cloudRuntime.transitionTo(definition, seconds);
    }

    /**
     * Alter the vertical offset of the clouds-only dome. When the scene's
     * horizon lies below the astronomical horizon, it may help to depress the
     * clouds-only dome.
     *
     * @param newYOffset desired vertical offset as a fraction of the dome
     * height (&lt;1, &ge;0 when flattening&gt;0; 0 when flattening=0)
     */
    public void setCloudsYOffset(float newYOffset) {
        Spatial cloudsOnlyDome = getCloudsOnlyDome();
        if (cloudsOnlyDome == null) {
            if (newYOffset != 0f) {
                logger.log(Level.SEVERE, "offset={0}", newYOffset);
                throw new IllegalArgumentException("offset should be 0");
            }
            return;
        }
        if (!(newYOffset >= 0f && newYOffset < 1f)) {
            logger.log(Level.SEVERE, "offset={0}", newYOffset);
            throw new IllegalArgumentException(
                    "offset should be between 0 and 1");
        }

        float deltaY = -newYOffset * cloudsOnlyDome.getLocalScale().y;
        cloudsOnlyDome.setLocalTranslation(0f, deltaY, 0f);
    }

    /**
     * Alter an object's color map texture.
     *
     * @param objectIndex which object (&ge;0)
     * @param newColorMap texture to apply (not null)
     */
    public void setObjectTexture(int objectIndex, Texture newColorMap) {
        Validate.nonNegative(objectIndex, "index");
        Validate.nonNull(newColorMap, "texture");

        SkyMaterial topMaterial = getTopMaterial();
        topMaterial.addObject(objectIndex, newColorMap);
    }

    /**
     * Alter the stabilize flag.
     *
     * @param newState true to counteract rotation of the controlled node, false
     * to allow rotation
     */
    public void setStabilizeFlag(boolean newState) {
        this.stabilizeFlag = newState;
    }

    /**
     * Alter the star map.
     *
     * @param assetName if starsOption==Cube: name of a cube-map folder in
     * Textures/skies/star-maps (not null, not empty)<br>
     * if starsOption==TopDome: path to texture asset (not null, not empty)<br>
     * if starsOption==TwoDomes: path to an asset folder containing northern.png
     * and southern.png (not null, not empty)
     */
    final public void setStarMaps(String assetName) {
        Validate.nonEmpty(assetName, "asset name");

        switch (starsOption) {
            case Cube:
            case TwoDomes:
                removeStarsNode();
                Node starNode = SkyDomeFactory.createStars(
                        assetManager, starsOption, assetName);
                SkyDomeFactory.attachStars((Node) getSubtree(), starNode);
                break;

            case TopDome:
                SkyMaterial topMaterial = getTopMaterial();
                topMaterial.addStars(assetName);
                break;

            default:
                throw new IllegalStateException("option = " + starsOption);
        }
    }

    /**
     * Alter the vertical angle of the top dome, which is Pi/2 by default. If
     * the terrain's horizon lies below the horizontal, increase this angle (to
     * values greater than Pi/2) to avoid clipping the sun and moon when they
     * are near the horizontal.
     *
     * @param newAngle desired angle from the zenith to the rim of the top dome
     * (in radians, &lt;1.785, &gt;0)
     */
    public void setTopVerticalAngle(float newAngle) {
        if (!(newAngle > 0f && newAngle < 1.785f)) {
            logger.log(Level.SEVERE, "angle={0}", newAngle);
            throw new IllegalArgumentException(
                    "angle should be between 0 and 1.785");
        }

        DomeMesh topMesh = getTopMesh();
        topMesh.setVerticalAngle(newAngle);
        if (bottomDomeFlag) {
            DomeMesh bottomMesh = getBottomMesh();
            bottomMesh.setVerticalAngle(FastMath.PI - newAngle);
        }
    }
    // *************************************************************************
    // protected methods

    /**
     * Access the bottom dome geometry.
     *
     * @return the pre-existing geometry (or null if none)
     */
    protected Geometry getBottomDome() {
        Node subtreeNode = (Node) getSubtree();
        Geometry bottomDome = SkySceneLookup.bottomDome(subtreeNode);

        return bottomDome;
    }

    /**
     * Access the bottom dome material.
     *
     * @return the pre-existing instance (or null if none)
     */
    protected Material getBottomMaterial() {
        Geometry bottomDome = getBottomDome();
        Material bottomMaterial = SkySceneLookup.material(bottomDome);

        return bottomMaterial;
    }

    /**
     * Access the bottom dome mesh.
     *
     * @return the pre-existing instance (or null if none)
     */
    protected DomeMesh getBottomMesh() {
        Geometry bottomDome = getBottomDome();
        DomeMesh bottomMesh = SkySceneLookup.domeMesh(bottomDome);

        return bottomMesh;
    }

    /**
     * Access the clouds-only dome geometry.
     *
     * @return the pre-existing geometry (or null if none)
     */
    protected Geometry getCloudsOnlyDome() {
        Node subtreeNode = (Node) getSubtree();
        Geometry cloudsOnlyDome = SkySceneLookup.cloudsOnlyDome(subtreeNode);

        return cloudsOnlyDome;
    }

    /**
     * Access the clouds material.
     *
     * @return the pre-existing instance (not null)
     */
    protected SkyMaterial getCloudsMaterial() {
        Geometry cloudsOnlyDome = getCloudsOnlyDome();
        SkyMaterial cloudsMaterial;
        if (cloudsOnlyDome == null) {
            cloudsMaterial = getTopMaterial();
        } else {
            cloudsMaterial = (SkyMaterial) cloudsOnlyDome.getMaterial();
        }

        assert cloudsMaterial != null;
        return cloudsMaterial;
    }

    /**
     * Access the clouds mesh.
     *
     * @return the pre-existing instance (not null)
     */
    protected DomeMesh getCloudsMesh() {
        Geometry cloudsOnlyDome = getCloudsOnlyDome();
        DomeMesh cloudsMesh;
        if (cloudsOnlyDome == null) {
            cloudsMesh = getTopMesh();
        } else {
            cloudsMesh = (DomeMesh) cloudsOnlyDome.getMesh();
        }

        assert cloudsMesh != null;
        return cloudsMesh;
    }

    /**
     * Access the stars node. For starsOption==Cube or TwoDomes, this is the
     * node that parents the star geometries.
     *
     * @return the pre-existing node (or null if none)
     */
    protected Node getStarsNode() {
        Node subtreeNode = (Node) getSubtree();
        Node starsNode = SkySceneLookup.starsNode(subtreeNode);

        return starsNode;
    }

    /**
     * Access the top dome geometry.
     *
     * @return the pre-existing geometry (not null)
     */
    protected Geometry getTopDome() {
        Node subtreeNode = (Node) getSubtree();
        Geometry topDome = SkySceneLookup.topDome(subtreeNode);

        assert topDome != null;
        return topDome;
    }

    /**
     * Access the top dome material.
     *
     * @return the pre-existing instance (not null)
     */
    protected SkyMaterial getTopMaterial() {
        Geometry topDome = getTopDome();
        SkyMaterial topMaterial = SkySceneLookup.skyMaterial(topDome);

        return topMaterial;
    }

    /**
     * Access the top dome mesh.
     *
     * @return the pre-existing instance (not null)
     */
    protected DomeMesh getTopMesh() {
        Geometry topDome = getTopDome();
        DomeMesh topMesh = SkySceneLookup.domeMesh(topDome);

        assert topMesh != null;
        return topMesh;
    }

    /**
     * Apply a modified version of the base color to each cloud layer.
     * <p>
     * The return value is used in calculating ambient light intensity.
     *
     * @param baseColor (not null, unaffected, alpha is ignored)
     * @param sunUp true if sun is above the horizon, otherwise false
     * @param moonUp true if moon is above the horizon, otherwise false
     * @return new instance (alpha is undefined)
     */
    protected ColorRGBA updateCloudsColor(
            ColorRGBA baseColor, boolean sunUp, boolean moonUp) {
        assert baseColor != null;

        ColorRGBA cloudsColor = MyColor.saturate(baseColor);
        if (!sunUp) {
            // At night, darken the clouds by 15%-75%.
            float cloudBrightness = 0.25f;
            if (moonUp) {
                cloudBrightness += 0.6f * getMoonIllumination();
            }
            cloudsColor.multLocal(cloudBrightness);
        }
        setCloudLayersColor(cloudsColor);

        return cloudsColor;
    }

    /**
     * Apply a color to all cloud layers.
     *
     * @param color desired cloud color (not null, unaffected)
     */
    protected void setCloudLayersColor(ColorRGBA color) {
        cloudRuntime.setColor(color);
    }

    /**
     * Return the moon's longitude difference.
     *
     * @return radians east of the sun
     */
    protected float moonLongitudeDifference() {
        float result = celestialState.longitudeDifference();
        return result;
    }

    /**
     * Return the moon's lunar latitude.
     *
     * @return radians north of the ecliptic
     */
    protected float moonLatitude() {
        float result = celestialState.lunarLatitude();
        return result;
    }

    /**
     * Alter the moon's longitude difference.
     *
     * @param longitudeDifference radians east of the sun
     */
    protected void setMoonLongDiff(float longitudeDifference) {
        celestialState.setLongitudeDifference(longitudeDifference);
    }

    /**
     * Alter the moon's lunar latitude and phase angle.
     *
     * @param longitudeDifference radians east of the sun
     * @param lunarLatitude radians north of the ecliptic
     */
    protected void setCelestialPhase(float longitudeDifference,
            float lunarLatitude) {
        celestialState.setPhase(longitudeDifference, lunarLatitude);
    }
    // *************************************************************************
    // SubtreeControl methods

    /**
     * Create a shallow copy of this control.
     *
     * @return a new control, equivalent to this one
     * @throws CloneNotSupportedException if superclass isn't cloneable
     */
    @Override
    public SkyControlCore clone() throws CloneNotSupportedException {
        SkyControlCore clone = (SkyControlCore) super.clone();
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

        this.camera = cloner.clone(camera);
        this.cloudRuntime.cloneFields(cloner);
        this.celestialState = celestialState.copy();
    }

    /**
     * Callback invoked when the sky node's geometric state is about to be
     * updated, once per frame while attached and enabled.
     *
     * @param updateInterval time interval between updates (in seconds, &ge;0)
     */
    @Override
    public void controlUpdate(float updateInterval) {
        super.controlUpdate(updateInterval);

        if (camera == null) {
            return;
        }

        cloudRuntime.update(updateInterval);

        // Translate the sky node to center the sky on the camera.
        Vector3f cameraLocation = camera.getLocation();
        Spatial subtree = getSubtree();
        MySpatial.setWorldLocation(subtree, cameraLocation);
        /*
         * Scale the sky node so that its furthest geometries are midway
         * between the near and far planes of the view frustum.
         */
        float far = camera.getFrustumFar();
        float near = camera.getFrustumNear();
        float radius = (near + far) / 2f;
        assert getSubtree().getParent() == spatial;
        MySpatial.setWorldScale(subtree, radius);

        if (stabilizeFlag) {
            // Counteract rotation of the controlled node.
            MySpatial.setWorldOrientation(subtree, rotationIdentity);
        }
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

        this.assetManager = importer.getAssetManager();
        this.stabilizeFlag = ic.readBoolean("stabilizeFlag", false);
        /* for backward compatibility with version 0.9.11 and earlier: */
        boolean starMotionFlag = ic.readBoolean("starMotionFlag", false);
        this.starsOption
                = starMotionFlag ? StarsOption.Cube : StarsOption.TopDome;
        this.starsOption
                = ic.readEnum("starsOption", StarsOption.class, starsOption);
        /* camera not serialized */
        this.cloudRuntime = SkyCloudRuntime.read(ic);
        this.celestialState = SkyCelestialState.read(ic);
    }

    /**
     * Callback invoked when the controlled spatial is about to be rendered to a
     * viewport.
     *
     * @param renderManager (not null)
     * @param viewPort viewport where the spatial will be rendered (not null)
     */
    @Override
    public void render(RenderManager renderManager, ViewPort viewPort) {
        super.render(renderManager, viewPort);
        this.camera = viewPort.getCamera();
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

        oc.write(stabilizeFlag, "stabilizeFlag", false);
        oc.write(starsOption, "starsOption", StarsOption.TopDome);
        /* camera not serialized */
        cloudRuntime.write(oc);
        celestialState.write(oc);
    }
    // *************************************************************************
    // private methods

    /**
     * Remove the stars node (if it exists) from the scene graph.
     */
    private void removeStarsNode() {
        Node starsNode = getStarsNode();
        if (starsNode != null) {
            int index = ((Node) getSubtree()).detachChild(starsNode);
            assert index == 0 : index;
        }
    }

}
