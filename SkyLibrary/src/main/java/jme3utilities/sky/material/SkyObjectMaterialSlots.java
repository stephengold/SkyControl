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
import java.io.IOException;

/**
 * Serializable astronomical-object state for sky materials.
 *
 * @author Take Some
 */
public final class SkyObjectMaterialSlots {
    // *************************************************************************
    // fields

    /**
     * Sky texture coordinates of each astronomical object's center.
     */
    final private Vector2f[] centers;
    /**
     * Rotation vector of each astronomical object, or null when not meaningful.
     */
    final private Vector2f[] rotations;
    /**
     * Scale factor of each astronomical object.
     */
    final private float[] scales;
    // *************************************************************************
    // constructors

    /**
     * Instantiate empty astronomical-object slots.
     *
     * @param numObjects number of supported astronomical objects (&ge;0)
     */
    public SkyObjectMaterialSlots(int numObjects) {
        this.centers = new Vector2f[numObjects];
        this.rotations = new Vector2f[numObjects];
        this.scales = new float[numObjects];
    }

    /**
     * Instantiate astronomical-object slots from serialized state.
     *
     * @param centers serialized center array (not null, aliased)
     * @param rotations serialized rotation array (not null, aliased)
     * @param scales serialized scale array (not null, aliased)
     */
    private SkyObjectMaterialSlots(Vector2f[] centers, Vector2f[] rotations,
            float[] scales) {
        assert centers != null;
        assert rotations != null;
        assert scales != null;

        this.centers = centers;
        this.rotations = rotations;
        this.scales = scales;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add an astronomical object slot if it has not been added yet.
     *
     * @param objectIndex object index
     * @return true if this was the first addition for the object
     */
    public boolean addObject(int objectIndex) {
        boolean result = !isAdded(objectIndex);
        if (result) {
            this.centers[objectIndex] = new Vector2f();
            this.rotations[objectIndex] = new Vector2f();
        }

        return result;
    }

    /**
     * Copy the center coordinates for an astronomical object.
     *
     * @param objectIndex object index
     * @return a new vector
     */
    public Vector2f copyCenter(int objectIndex) {
        Vector2f result = centers[objectIndex].clone();
        return result;
    }

    /**
     * Copy the rotation vector for an astronomical object.
     *
     * @param objectIndex object index
     * @return a new vector, or null if rotation is not meaningful
     */
    public Vector2f copyRotation(int objectIndex) {
        Vector2f vector = rotations[objectIndex];
        if (vector == null) {
            return null;
        } else {
            Vector2f result = vector.clone();
            return result;
        }
    }

    /**
     * Count supported astronomical object slots.
     *
     * @return number of slots (&ge;0)
     */
    public int count() {
        int result = centers.length;
        return result;
    }

    /**
     * Hide an astronomical object by moving its center to hidden coordinates.
     *
     * @param objectIndex object index
     * @param hiddenCoordinates hidden texture coordinates (not null,
     * unaffected)
     */
    public void hide(int objectIndex, Vector2f hiddenCoordinates) {
        assert hiddenCoordinates != null;
        centers[objectIndex].set(hiddenCoordinates);
    }

    /**
     * Test whether an astronomical object slot has been added.
     *
     * @param objectIndex object index
     * @return true if added, otherwise false
     */
    public boolean isAdded(int objectIndex) {
        boolean result = centers[objectIndex] != null;
        return result;
    }

    /**
     * Read object slots from an input capsule.
     *
     * @param capsule input capsule (not null)
     * @return a new instance
     * @throws IOException from capsule
     */
    public static SkyObjectMaterialSlots read(InputCapsule capsule)
            throws IOException {
        assert capsule != null;

        Savable[] sav = capsule.readSavableArray("objectCenters", null);
        Vector2f[] centers = new Vector2f[sav.length];
        System.arraycopy(sav, 0, centers, 0, sav.length);

        sav = capsule.readSavableArray("objectRotations", null);
        Vector2f[] rotations = new Vector2f[sav.length];
        System.arraycopy(sav, 0, rotations, 0, sav.length);

        float[] scales = capsule.readFloatArray("objectScales", null);

        SkyObjectMaterialSlots result
                = new SkyObjectMaterialSlots(centers, rotations, scales);
        return result;
    }

    /**
     * Verify that an astronomical object slot has been added.
     *
     * @param objectIndex object index
     * @throws IllegalStateException if the object has not been added
     */
    public void requireAdded(int objectIndex) {
        if (!isAdded(objectIndex)) {
            throw new IllegalStateException("object not yet added");
        }
    }

    /**
     * Access the scale for an astronomical object.
     *
     * @param objectIndex object index
     * @return scale factor
     */
    public float scale(int objectIndex) {
        float result = scales[objectIndex];
        return result;
    }

    /**
     * Record transform state for an astronomical object.
     *
     * @param objectIndex object index
     * @param centerUV center coordinates (not null, unaffected)
     * @param scale desired scale
     * @param rotation rotation vector, or null if rotation is not meaningful
     */
    public void setTransform(int objectIndex, Vector2f centerUV, float scale,
            Vector2f rotation) {
        assert centerUV != null;

        this.centers[objectIndex] = centerUV.clone();
        if (rotation == null) {
            this.rotations[objectIndex] = null;
        } else {
            this.rotations[objectIndex] = rotation.clone();
        }
        this.scales[objectIndex] = scale;
    }

    /**
     * Serialize astronomical-object slots to an output capsule.
     *
     * @param capsule output capsule (not null)
     * @throws IOException from capsule
     */
    public void write(OutputCapsule capsule) throws IOException {
        assert capsule != null;

        capsule.write(centers, "objectCenters", null);
        capsule.write(rotations, "objectRotations", null);
        capsule.write(scales, "objectScales", null);
    }
}
