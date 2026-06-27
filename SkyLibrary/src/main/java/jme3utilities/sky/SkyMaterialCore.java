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

import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.texture.Texture;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.Validate;
import jme3utilities.sky.material.SkyCloudMaterialSlots;
import jme3utilities.sky.material.SkyDdsTextureLoader;
import jme3utilities.sky.material.SkyMaterialParamNames;
import jme3utilities.sky.material.SkyObjectMaterialSlots;
import jme3utilities.sky.material.SkyObjectTransform;

/**
 * Core fields and methods of a material for a dynamic sky dome.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class SkyMaterialCore extends Material {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SkyMaterialCore.class.getName());
    /**
     * special texture coordinates for hidden objects
     */
    final private static Vector2f hidden = new Vector2f(0f, 0f);
    // *************************************************************************
    // fields

    /**
     * asset manager used to load textures and material definitions: set by
     * constructor
     */
    protected AssetManager assetManager; // TODO privatize
    /**
     * state and raster cache for cloud material slots: set by constructor or
     * read().
     */
    private SkyCloudMaterialSlots cloudSlots;
    /**
     * state for astronomical object material slots: set by constructor or
     * read().
     */
    private SkyObjectMaterialSlots objectSlots;
    /**
     * maximum number of cloud layers (&ge;0)
     */
    protected int maxCloudLayers; // TODO privatize
    /**
     * maximum number of astronomical objects (&ge;0)
     */
    protected int maxObjects; // TODO privatize
    // *************************************************************************
    // constructors

    /**
     * No-argument constructor needed by SavableClassUtil.
     */
    protected SkyMaterialCore() {
        this.assetManager = null;
        this.cloudSlots = null;
        this.maxCloudLayers = 0;
        this.maxObjects = 0;

        this.objectSlots = null;
    }

    /**
     * Instantiate sky material from a specified asset path. The first method
     * invoked should be initialize().
     *
     * @param assetManager asset manager for loading textures and material
     * definitions (not null)
     * @param assetPath pathname to the material-definitions asset (not null)
     * @param maxObjects number of astronomical objects allowed (&ge;0)
     * @param maxCloudLayers number of cloud layers allowed (&ge;0)
     */
    public SkyMaterialCore(AssetManager assetManager, String assetPath,
            int maxObjects, int maxCloudLayers) {
        super(assetManager, assetPath);
        Validate.nonNull(assetManager, "asset manager");
        Validate.nonNegative(maxObjects, "limit");
        Validate.nonNegative(maxCloudLayers, "limit");

        this.assetManager = assetManager;
        this.maxObjects = maxObjects;
        this.maxCloudLayers = maxCloudLayers;

        this.cloudSlots = new SkyCloudMaterialSlots(maxCloudLayers);

        this.objectSlots = new SkyObjectMaterialSlots(maxObjects);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a cloud layer to this material using the specified alpha map asset
     * path.
     *
     * @param layerIndex (&lt;maxCloudLayers, &ge;0)
     * @param assetPath the asset path to the alpha map (not null, not empty)
     */
    public void addClouds(int layerIndex, String assetPath) {
        validateLayerIndex(layerIndex);
        Validate.nonEmpty(assetPath, "asset path");

        boolean mipmaps = false;
        Texture alphaMap
                = MyAsset.loadTexture(assetManager, assetPath, mipmaps);
        alphaMap.setWrap(Texture.WrapMode.Repeat);
        String parameterName
                = SkyMaterialParamNames.cloudAlphaMap(layerIndex);
        setTexture(parameterName, alphaMap);

        boolean firstTime = cloudSlots.addAlphaMap(layerIndex, alphaMap);

        if (firstTime) {
            setCloudsColor(layerIndex, ColorRGBA.White);
            setCloudsOffset(layerIndex, 0f, 0f);
            setCloudsScale(layerIndex, 1f);
        }
    }


    /**
     * Add, replace, or clear a cloud-layer normal map.
     *
     * @param layerIndex (&lt;maxCloudLayers, &ge;0)
     * @param assetPath asset path to the normal map, or null to clear it
     */
    public void setCloudsNormalMap(int layerIndex, String assetPath) {
        validateLayerIndex(layerIndex);
        requireCloudLayerAdded(layerIndex);

        String parameterName
                = SkyMaterialParamNames.cloudNormalMap(layerIndex);
        if (assetPath == null) {
            clearParam(parameterName);
            logger.log(Level.FINE,
                    "cloud normal map cleared: layer={0}, parameter={1}",
                    new Object[]{layerIndex, parameterName});
        } else {
            Validate.nonEmpty(assetPath, "asset path");
            Texture normalMap = loadNormalMap(assetPath);
            normalMap.setWrap(Texture.WrapMode.Repeat);
            setTexture(parameterName, normalMap);
            logger.log(Level.FINE,
                    "cloud normal map applied: layer={0}, parameter={1}, path={2}, image={3}",
                    new Object[]{layerIndex, parameterName, assetPath,
                        normalMap.getImage()});
        }
    }

    /**
     * Add an astronomical object to this material using the specified color
     * map. If the object already exists, its color map is updated.
     *
     * @param objectIndex (&lt;maxObjects, &ge;0)
     * @param colorMap color map to use (not null)
     */
    public void addObject(int objectIndex, Texture colorMap) {
        validateObjectIndex(objectIndex);
        Validate.nonNull(colorMap, "texture");

        String parameterName
                = SkyMaterialParamNames.objectColorMap(objectIndex);
        setTexture(parameterName, colorMap);

        if (objectSlots.addObject(objectIndex)) {
            setObjectColor(objectIndex, ColorRGBA.White);
            setObjectGlow(objectIndex, ColorRGBA.Black);
            setObjectTransform(objectIndex, Constants.topUV, 1f, null);
        }
    }

    /**
     * Copy the color of the specified cloud layer.
     *
     * @param layerIndex (&lt;maxCloudLayers, &ge;0)
     * @return a new instance
     * @see #setCloudsColor(int, com.jme3.math.ColorRGBA)
     */
    public ColorRGBA copyCloudsColor(int layerIndex) {
        validateLayerIndex(layerIndex);
        requireCloudLayerAdded(layerIndex);

        String parameterName
                = SkyMaterialParamNames.cloudColor(layerIndex);
        ColorRGBA color = copyColor(parameterName);
        color.a = cloudSlots.alpha(layerIndex);

        return color;
    }

    /**
     * Copy the glow color of the specified cloud layer.
     *
     * @param layerIndex (&lt;maxCloudLayers, &ge;0)
     * @return a new instance
     * @see #setCloudsGlow(int, com.jme3.math.ColorRGBA)
     */
    public ColorRGBA copyCloudsGlow(int layerIndex) {
        validateLayerIndex(layerIndex);
        requireCloudLayerAdded(layerIndex);

        String parameterName
                = SkyMaterialParamNames.cloudGlow(layerIndex);
        ColorRGBA color = copyColor(parameterName);

        return color;
    }

    /**
     * Copy the texture offset of the specified cloud layer.
     *
     * @param layerIndex (&lt;maxCloudLayers, &ge;0)
     * @return a new instance
     * @see #setCloudsOffset(int, float, float)
     */
    public Vector2f copyCloudsOffset(int layerIndex) {
        validateLayerIndex(layerIndex);
        requireCloudLayerAdded(layerIndex);

        Vector2f result = cloudSlots.copyOffset(layerIndex);
        return result;
    }

    /**
     * Copy the value of the specified color parameter.
     *
     * @param name name of the color parameter
     * @return a new instance
     * @see com.jme3.material.Material#setColor(java.lang.String,
     * com.jme3.math.ColorRGBA)
     */
    public ColorRGBA copyColor(String name) {
        MatParam parameter = getParam(name);
        ColorRGBA color = (ColorRGBA) parameter.getValue();

        return color.clone();
    }

    /**
     * Copy the color of the specified astronomical object.
     *
     * @param objectIndex (&lt;maxObjects, &ge;0)
     * @return a new instance
     * @see #setObjectColor(int, com.jme3.math.ColorRGBA)
     */
    public ColorRGBA copyObjectColor(int objectIndex) {
        validateObjectIndex(objectIndex);
        requireObjectAdded(objectIndex);

        String parameterName
                = SkyMaterialParamNames.objectColor(objectIndex);
        ColorRGBA color = copyColor(parameterName);

        return color;
    }

    /**
     * Copy the glow color of the specified astronomical object.
     *
     * @param objectIndex (&lt;maxObjects, &ge;0)
     * @return a new instance
     * @see #setObjectGlow(int, com.jme3.math.ColorRGBA)
     */
    public ColorRGBA copyObjectGlow(int objectIndex) {
        validateObjectIndex(objectIndex);
        requireObjectAdded(objectIndex);

        String parameterName
                = SkyMaterialParamNames.objectGlow(objectIndex);
        ColorRGBA color = copyColor(parameterName);

        return color;
    }

    /**
     * Copy the texture offset of the specified astronomical object.
     *
     * @param objectIndex (&lt;maxObjects, &ge;0)
     * @return a new instance
     * @see #setObjectTransform(int, com.jme3.math.Vector2f, float,
     * com.jme3.math.Vector2f)
     */
    public Vector2f copyObjectOffset(int objectIndex) {
        validateObjectIndex(objectIndex);
        requireObjectAdded(objectIndex);

        Vector2f result = objectSlots.copyCenter(objectIndex);
        return result;
    }

    /**
     * Copy the texture rotation vector of the specified astronomical object.
     *
     * @param objectIndex (&lt;maxObjects, &ge;0)
     * @return a new instance
     * @see #setObjectTransform(int, com.jme3.math.Vector2f, float,
     * com.jme3.math.Vector2f)
     */
    public Vector2f copyObjectRotation(int objectIndex) {
        validateObjectIndex(objectIndex);
        requireObjectAdded(objectIndex);

        Vector2f result = objectSlots.copyRotation(objectIndex);
        return result;
    }

    /**
     * Copy the value of the specified vector2 parameter.
     *
     * @param name name of the parameter
     * @return a new instance
     * @see com.jme3.material.Material#setVector2(java.lang.String,
     * com.jme3.math.Vector2f)
     */
    public Vector2f copyVector2(String name) {
        MatParam parameter = getParam(name);
        Vector2f vector = (Vector2f) parameter.getValue();

        return vector.clone();
    }

    /**
     * Return the scale of the specified cloud layer.
     *
     * @param layerIndex (&lt;maxCloudLayers, &ge;0)
     * @return scale factor (&gt;0)
     */
    public float getCloudsScale(int layerIndex) {
        validateLayerIndex(layerIndex);
        requireCloudLayerAdded(layerIndex);

        String parameterName
                = SkyMaterialParamNames.cloudScale(layerIndex);
        float result = getFloat(parameterName);

        assert result > 0f : result;
        return result;
    }

    /**
     * Return the scale of the specified astronomical object.
     *
     * @param objectIndex (&lt;maxObjects, &ge;0)
     * @return scale factor (&gt;0)
     * @see #setObjectTransform(int, com.jme3.math.Vector2f, float,
     * com.jme3.math.Vector2f)
     */
    public float getObjectScale(int objectIndex) {
        validateObjectIndex(objectIndex);
        requireObjectAdded(objectIndex);

        float result = objectSlots.scale(objectIndex);

        assert result > 0f : result;
        return result;
    }

    /**
     * Return the value of the specified float parameter.
     *
     * @param name name of the parameter
     * @return value
     * @see com.jme3.material.Material#setFloat(java.lang.String, float)
     */
    public float getFloat(String name) {
        MatParam parameter = getParam(name);
        float result = (float) parameter.getValue();

        return result;
    }

    /**
     * Estimate how much of an object's light is transmitted through the clouds.
     *
     * @param objectIndex (&lt;maxObjects, &ge;0)
     * @return fraction of light transmitted (&lt;1, &ge;0)
     */
    public float getTransmission(int objectIndex) {
        validateObjectIndex(objectIndex);
        requireObjectAdded(objectIndex);

        Vector2f center = objectSlots.copyCenter(objectIndex);
        float result = getTransmission(center);

        return result;
    }

    /**
     * Estimate how much light is transmitted through the clouds at the
     * specified texture coordinates.
     *
     * @param skyCoordinates (unaffected, not null)
     * @return fraction of light transmitted (&le;1, &ge;0)
     */
    public float getTransmission(Vector2f skyCoordinates) {
        Validate.nonNull(skyCoordinates, "coordinates");

        float result = cloudSlots.transmission(skyCoordinates);
        return result;
    }

    /**
     * Hide an astronomical object temporarily.
     * <p>
     * Use {@link
     * #setObjectTransform(int, com.jme3.math.Vector2f, float,
     * com.jme3.math.Vector2f)} to reveal an object that has been hidden.
     *
     * @param objectIndex (&lt;maxObjects, &ge;0)
     */
    public void hideObject(int objectIndex) {
        validateObjectIndex(objectIndex);
        requireObjectAdded(objectIndex);

        String objectParameterName
                = SkyMaterialParamNames.objectCenter(objectIndex);
        setVector2(objectParameterName, hidden);
        objectSlots.hide(objectIndex, hidden);

        // Scale down the object to occupy only a few pixels in texture space.
        float scale = 1000f;
        String transformUParameterName
                = SkyMaterialParamNames.objectTransformU(objectIndex);
        setVector2(transformUParameterName, new Vector2f(scale, scale));
        String transformVParameterName
                = SkyMaterialParamNames.objectTransformV(objectIndex);
        setVector2(transformVParameterName, new Vector2f(scale, scale));
    }

    /**
     * Alter the color of a cloud layer.
     *
     * @param layerIndex (&lt;maxCloudLayers, &ge;0)
     * @param newColor (not null, unaffected)
     */
    public void setCloudsColor(int layerIndex, ColorRGBA newColor) {
        validateLayerIndex(layerIndex);
        Validate.nonNull(newColor, "color");
        requireCloudLayerAdded(layerIndex);

        String parameterName
                = SkyMaterialParamNames.cloudColor(layerIndex);
        setColor(parameterName, newColor.clone());
        cloudSlots.setAlpha(layerIndex, newColor.a);
    }

    /**
     * Alter the glow color of a cloud layer.
     *
     * @param layerIndex (&lt;maxCloudLayers, &ge;0)
     * @param newColor (not null, unaffected)
     */
    public void setCloudsGlow(int layerIndex, ColorRGBA newColor) {
        validateLayerIndex(layerIndex);
        Validate.nonNull(newColor, "color");
        requireCloudLayerAdded(layerIndex);

        String parameterName
                = SkyMaterialParamNames.cloudGlow(layerIndex);
        setColor(parameterName, newColor.clone());
    }

    /**
     * Alter the texture offset of a cloud layer.
     *
     * @param layerIndex (&lt;maxCloudLayers, &ge;0)
     * @param newU first component of the new offset
     * @param newV 2nd component of the new offset
     */
    public void setCloudsOffset(int layerIndex, float newU, float newV) {
        validateLayerIndex(layerIndex);
        requireCloudLayerAdded(layerIndex);

        Vector2f offset = cloudSlots.setOffset(layerIndex, newU, newV);

        String parameterName
                = SkyMaterialParamNames.cloudOffset(layerIndex);
        setVector2(parameterName, offset);
    }

    /**
     * Alter the texture scale of a cloud layer.
     *
     * @param layerIndex (&lt;maxCloudLayers, &ge;0)
     * @param newScale scale factor (&gt;0)
     */
    public void setCloudsScale(int layerIndex, float newScale) {
        validateLayerIndex(layerIndex);
        Validate.positive(newScale, "scale");
        requireCloudLayerAdded(layerIndex);

        String parameterName
                = SkyMaterialParamNames.cloudScale(layerIndex);
        setFloat(parameterName, newScale);
        cloudSlots.setScale(layerIndex, newScale);
    }

    /**
     * Alter the color of the specified astronomical object.
     *
     * @param objectIndex (&lt;maxObjects, &ge;0)
     * @param newColor (not null, unaffected)
     */
    public void setObjectColor(int objectIndex, ColorRGBA newColor) {
        validateObjectIndex(objectIndex);
        Validate.nonNull(newColor, "color");
        requireObjectAdded(objectIndex);

        String parameterName
                = SkyMaterialParamNames.objectColor(objectIndex);
        setColor(parameterName, newColor.clone());
    }

    /**
     * Alter the glow color of the specified astronomical object.
     *
     * @param objectIndex (&lt;maxObjects, &ge;0)
     * @param newColor (not null, unaffected)
     */
    public void setObjectGlow(int objectIndex, ColorRGBA newColor) {
        validateObjectIndex(objectIndex);
        Validate.nonNull(newColor, "color");
        requireObjectAdded(objectIndex);

        String parameterName
                = SkyMaterialParamNames.objectGlow(objectIndex);
        setColor(parameterName, newColor.clone());
    }

    /**
     * Alter the position and scaling of the specified astronomical object.
     *
     * @param objectIndex (&lt;maxObjects, &ge;0)
     * @param centerUV sky texture coordinates for the center of the object (not
     * null, each component &le;1 and &ge;0, unaffected)
     * @param newScale ratio of the sky's texture scale to that of the object
     * (&ge;0, usually &lt;1)
     * @param newRotate (cos, sin) of clockwise rotation angle (length&gt;0,
     * unaffected) or null if rotation doesn't matter
     */
    public void setObjectTransform(int objectIndex, Vector2f centerUV,
            float newScale, Vector2f newRotate) {
        validateObjectIndex(objectIndex);
        Validate.nonNull(centerUV, "coordinates");
        Validate.positive(newScale, "scale");
        if (newRotate != null) {
            Validate.nonZero(newRotate, "rotation vector");
        }
        requireObjectAdded(objectIndex);

        // Record transform parameters for save().
        objectSlots.setTransform(objectIndex, centerUV, newScale, newRotate);

        String objectParameterName
                = SkyMaterialParamNames.objectCenter(objectIndex);
        setVector2(objectParameterName, centerUV);

        SkyObjectTransform transform
                = SkyObjectTransform.from(centerUV, newScale, newRotate);

        String transformUParameterName
                = SkyMaterialParamNames.objectTransformU(objectIndex);
        setVector2(transformUParameterName, transform.copyTransformU());

        String transformVParameterName
                = SkyMaterialParamNames.objectTransformV(objectIndex);
        setVector2(transformVParameterName, transform.copyTransformV());
    }
    // *************************************************************************
    // new protected methods

    /**
     * Validate a cloud layer index used as a method argument.
     *
     * @param layerIndex the index of a cloud layer
     * @throws IllegalArgumentException if the index is out of range
     */
    protected void validateLayerIndex(int layerIndex) {
        Validate.inRange(
                layerIndex, "cloud layer index", 0, maxCloudLayers - 1);
    }

    /**
     * Validate an object index used as a method argument.
     *
     * @param objectIndex the index of an astronomical object
     * @throws IllegalArgumentException if the index is out of range
     */
    protected void validateObjectIndex(int objectIndex) {
        Validate.inRange(
                objectIndex, "object index", 0, maxObjects - 1);
    }
    // *************************************************************************
    // Savable methods

    /**
     * De-serialize this instance when loading.
     *
     * @param importer (not null)
     * @throws IOException from importer
     */
    @Override
    public void read(JmeImporter importer) throws IOException {
        super.read(importer);
        InputCapsule capsule = importer.getCapsule(this);

        // cloud layers
        this.cloudSlots = SkyCloudMaterialSlots.read(capsule);

        // astronomical objects
        this.objectSlots = SkyObjectMaterialSlots.read(capsule);

        // cached values
        this.assetManager = importer.getAssetManager();
        this.maxCloudLayers = cloudSlots.count();
        this.maxObjects = objectSlots.count();

    }

    /**
     * Serialize this instance when saving.
     *
     * @param exporter (not null)
     * @throws IOException from exporter
     */
    @Override
    public void write(JmeExporter exporter) throws IOException {
        super.write(exporter);

        OutputCapsule capsule = exporter.getCapsule(this);

        cloudSlots.write(capsule);

        objectSlots.write(capsule);
    }

    /**
     * Load a cloud normal map, including BC5 DDS fallback.
     *
     * @param assetPath texture asset path (not null, not empty)
     * @return new texture
     */
    private Texture loadNormalMap(String assetPath) {
        assert assetPath != null;

        boolean mipmaps = false;
        Texture result;
        try {
            logger.log(Level.FINER,
                    "loading cloud normal map through AssetManager: {0}",
                    assetPath);
            result = MyAsset.loadTexture(assetManager, assetPath, mipmaps);
        } catch (AssetLoadException exception) {
            if (!assetPath.toLowerCase(java.util.Locale.ROOT)
                    .endsWith(".dds")) {
                throw exception;
            }
            logger.log(Level.INFO,
                    "using internal compressed texture reader for cloud normal map: {0}",
                    assetPath);
            result = SkyDdsTextureLoader.loadTexture(assetManager, assetPath);
        }

        return result;
    }

    // *************************************************************************
    // private methods

    /**
     * Verify that the specified cloud layer has been added.
     *
     * @param layerIndex cloud layer index
     * @throws IllegalStateException if the layer has not been added
     */
    private void requireCloudLayerAdded(int layerIndex) {
        cloudSlots.requireAdded(layerIndex);
    }

    /**
     * Verify that the specified astronomical object has been added.
     *
     * @param objectIndex astronomical object index
     * @throws IllegalStateException if the object has not been added
     */
    private void requireObjectAdded(int objectIndex) {
        objectSlots.requireAdded(objectIndex);
    }



}
