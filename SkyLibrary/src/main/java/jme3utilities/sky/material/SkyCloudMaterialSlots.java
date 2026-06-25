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
package jme3utilities.sky.material;

import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Vector2f;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.image.ImageRaster;
import java.io.IOException;
import jme3utilities.math.MyMath;
import jme3utilities.sky.Constants;

/**
 * Serializable cloud-layer state and raster cache for sky materials.
 *
 * @author Take Some
 */
public final class SkyCloudMaterialSlots {
    // *************************************************************************
    // constants and loggers

    /**
     * Maximum value for opacity/transmission.
     */
    final private static float maxAlpha = 1f;
    // *************************************************************************
    // fields

    /**
     * Maximum opacity of each cloud layer.
     */
    final private float[] alphas;
    /**
     * Image of each cloud layer, retained for serialization.
     */
    final private Image[] images;
    /**
     * Cached rasterization of each cloud layer.
     */
    final private ImageRaster[] rasters;
    /**
     * Scale factors of cloud layers.
     */
    final private float[] scales;
    /**
     * UV offset of each cloud layer.
     */
    final private Vector2f[] offsets;
    // *************************************************************************
    // constructors

    /**
     * Instantiate empty cloud-layer slots.
     *
     * @param numLayers number of supported cloud layers (&ge;0)
     */
    public SkyCloudMaterialSlots(int numLayers) {
        this.alphas = new float[numLayers];
        this.images = new Image[numLayers];
        this.offsets = new Vector2f[numLayers];
        this.rasters = new ImageRaster[numLayers];
        this.scales = new float[numLayers];
    }

    /**
     * Instantiate cloud-layer slots from serialized state.
     *
     * @param alphas serialized opacity array (not null, aliased)
     * @param images serialized images array (not null, aliased)
     * @param offsets serialized offset array (not null, aliased)
     * @param scales serialized scale array (not null, aliased)
     */
    private SkyCloudMaterialSlots(float[] alphas, Image[] images,
            Vector2f[] offsets, float[] scales) {
        assert alphas != null;
        assert images != null;
        assert offsets != null;
        assert scales != null;

        this.alphas = alphas;
        this.images = images;
        this.offsets = offsets;
        this.rasters = new ImageRaster[images.length];
        this.scales = scales;
        rebuildRasters();
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add or replace a cloud layer texture.
     *
     * @param layerIndex cloud layer index
     * @param alphaMap alpha-map texture (not null, unaffected)
     * @return true if this was the first texture for the layer
     */
    public boolean addAlphaMap(int layerIndex, Texture alphaMap) {
        assert alphaMap != null;

        boolean result = !isAdded(layerIndex);
        Image image = alphaMap.getImage();
        this.images[layerIndex] = image;
        this.rasters[layerIndex] = createRaster(image);
        if (result) {
            this.offsets[layerIndex] = new Vector2f();
        }

        return result;
    }

    /**
     * Access the recorded alpha for a cloud layer.
     *
     * @param layerIndex cloud layer index
     * @return opacity alpha
     */
    public float alpha(int layerIndex) {
        float result = alphas[layerIndex];
        return result;
    }

    /**
     * Count supported cloud layers.
     *
     * @return number of slots (&ge;0)
     */
    public int count() {
        int result = images.length;
        return result;
    }

    /**
     * Copy the recorded offset for a cloud layer.
     *
     * @param layerIndex cloud layer index
     * @return a new vector
     */
    public Vector2f copyOffset(int layerIndex) {
        Vector2f result = offsets[layerIndex].clone();
        return result;
    }

    /**
     * Test whether a cloud layer has a texture.
     *
     * @param layerIndex cloud layer index
     * @return true if added, otherwise false
     */
    public boolean isAdded(int layerIndex) {
        boolean result = images[layerIndex] != null;
        return result;
    }

    /**
     * Read cloud-layer slots from an input capsule.
     *
     * @param capsule input capsule (not null)
     * @return a new instance
     * @throws IOException from capsule
     */
    public static SkyCloudMaterialSlots read(InputCapsule capsule)
            throws IOException {
        assert capsule != null;

        float[] alphas = capsule.readFloatArray("cloudAlphas", null);

        Savable[] sav = capsule.readSavableArray("cloudImages", null);
        Image[] images = new Image[sav.length];
        System.arraycopy(sav, 0, images, 0, sav.length);

        sav = capsule.readSavableArray("cloudOffsets", null);
        Vector2f[] offsets = new Vector2f[sav.length];
        System.arraycopy(sav, 0, offsets, 0, sav.length);

        float[] scales = capsule.readFloatArray("cloudScales", null);

        SkyCloudMaterialSlots result
                = new SkyCloudMaterialSlots(alphas, images, offsets, scales);
        return result;
    }

    /**
     * Verify that a cloud layer has been added.
     *
     * @param layerIndex cloud layer index
     * @throws IllegalStateException if the layer has not been added
     */
    public void requireAdded(int layerIndex) {
        if (!isAdded(layerIndex)) {
            throw new IllegalStateException("layer not yet added");
        }
    }

    /**
     * Record the alpha for a cloud layer.
     *
     * @param layerIndex cloud layer index
     * @param alpha desired alpha
     */
    public void setAlpha(int layerIndex, float alpha) {
        this.alphas[layerIndex] = alpha;
    }

    /**
     * Record the offset for a cloud layer.
     *
     * @param layerIndex cloud layer index
     * @param newU first component of the new offset
     * @param newV second component of the new offset
     * @return the normalized offset vector
     */
    public Vector2f setOffset(int layerIndex, float newU, float newV) {
        float uOffset = MyMath.modulo(newU, 1f);
        float vOffset = MyMath.modulo(newV, 1f);
        Vector2f result = new Vector2f(uOffset, vOffset);
        offsets[layerIndex].set(result);

        return result;
    }

    /**
     * Record the scale for a cloud layer.
     *
     * @param layerIndex cloud layer index
     * @param scale desired scale
     */
    public void setScale(int layerIndex, float scale) {
        this.scales[layerIndex] = scale;
    }

    /**
     * Estimate how much light is transmitted through the cloud layers.
     *
     * @param skyCoordinates texture coordinates (not null, unaffected)
     * @return fraction of light transmitted (&le;1, &ge;0)
     */
    public float transmission(Vector2f skyCoordinates) {
        assert skyCoordinates != null;

        float result = 1f;
        for (int layerIndex = 0; layerIndex < count(); ++layerIndex) {
            if (isAdded(layerIndex)) {
                float transparency = transparency(layerIndex, skyCoordinates);
                result *= transparency;
            }
        }

        assert result >= 0f : result;
        assert result <= maxAlpha : result;
        return result;
    }

    /**
     * Serialize cloud-layer slots to an output capsule.
     *
     * @param capsule output capsule (not null)
     * @throws IOException from capsule
     */
    public void write(OutputCapsule capsule) throws IOException {
        assert capsule != null;

        capsule.write(alphas, "cloudAlphas", null);
        capsule.write(images, "cloudImages", null);
        capsule.write(offsets, "cloudOffsets", null);
        capsule.write(scales, "cloudScales", null);
    }
    // *************************************************************************
    // private methods

    /**
     * Create a raster cache for an image, if supported.
     *
     * @param image source image (not null, unaffected)
     * @return new raster, or null if the image format is unsupported
     */
    private static ImageRaster createRaster(Image image) {
        assert image != null;

        ImageRaster result;
        try {
            result = ImageRaster.create(image);
        } catch (UnsupportedOperationException exception) {
            result = null;
        }

        return result;
    }

    /**
     * Rebuild raster cache from serialized images.
     */
    private void rebuildRasters() {
        for (int layerIndex = 0; layerIndex < count(); ++layerIndex) {
            Image image = images[layerIndex];
            if (image == null) {
                this.rasters[layerIndex] = null;
            } else {
                this.rasters[layerIndex] = createRaster(image);
            }
        }
    }

    /**
     * Estimate how much light is transmitted through an indexed cloud layer at
     * the specified texture coordinates.
     *
     * @param layerIndex cloud layer index
     * @param skyCoordinates texture coordinates (not null, unaffected)
     * @return fraction of light transmitted (&le;1, &ge;0)
     */
    private float transparency(int layerIndex, Vector2f skyCoordinates) {
        assert layerIndex >= 0 : layerIndex;
        assert layerIndex < count() : layerIndex;
        assert skyCoordinates != null;
        assert isAdded(layerIndex) : layerIndex;

        ImageRaster raster = rasters[layerIndex];
        if (raster == null) {
            return maxAlpha;
        }

        Vector2f coord = skyCoordinates.mult(scales[layerIndex]);
        coord.addLocal(offsets[layerIndex]);
        coord.x = MyMath.modulo(coord.x, Constants.uvMax);
        coord.y = MyMath.modulo(coord.y, Constants.uvMax);
        float opacity = SkyTextureSampler.sampleRed(raster, coord);
        opacity *= alphas[layerIndex];
        float result = maxAlpha - opacity;

        assert result >= 0f : result;
        assert result <= maxAlpha : result;
        return result;
    }
}
