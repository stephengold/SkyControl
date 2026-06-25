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

import com.jme3.math.Vector2f;
import jme3utilities.sky.Constants;

/**
 * Immutable UV transform vectors for an astronomical object projected onto a
 * sky dome.
 *
 * @author Take Some
 */
public final class SkyObjectTransform {
    // *************************************************************************
    // fields

    /**
     * Horizontal texture-coordinate transform vector.
     */
    final private Vector2f transformU;
    /**
     * Vertical texture-coordinate transform vector.
     */
    final private Vector2f transformV;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an immutable transform from calculated vectors.
     *
     * @param transformU horizontal transform vector (not null, unaffected)
     * @param transformV vertical transform vector (not null, unaffected)
     */
    private SkyObjectTransform(Vector2f transformU, Vector2f transformV) {
        assert transformU != null;
        assert transformV != null;

        this.transformU = transformU.clone();
        this.transformV = transformV.clone();
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Calculate transform vectors for an astronomical object.
     *
     * @param centerUV sky texture coordinates for the object center (not null,
     * unaffected)
     * @param scale ratio of the sky's texture scale to that of the object
     * (&gt;0)
     * @param rotation (cos, sin) of clockwise rotation angle (unaffected) or
     * null if rotation doesn't matter
     * @return a new immutable transform
     */
    public static SkyObjectTransform from(Vector2f centerUV, float scale,
            Vector2f rotation) {
        assert centerUV != null;
        assert scale > 0f : scale;

        Vector2f transformU = new Vector2f();
        Vector2f transformV = new Vector2f();

        Vector2f offset = centerUV.subtract(Constants.topUV);
        float topDist = offset.length();
        if (topDist > 0f) {
            applyDomeDistortion(
                    topDist, offset, rotation, transformU, transformV);

        } else {
            // No UV distortion at the top of the dome.
            transformU.set(1f, 0f);
            transformV.set(0f, 1f);
        }

        if (rotation != null) {
            applyObjectRotation(rotation, transformU, transformV);
        }

        // Scale by object scale.
        transformU.divideLocal(scale);
        transformV.divideLocal(scale);

        SkyObjectTransform result
                = new SkyObjectTransform(transformU, transformV);
        return result;
    }

    /**
     * Copy the horizontal texture-coordinate transform vector.
     *
     * @return a new vector
     */
    public Vector2f copyTransformU() {
        Vector2f result = transformU.clone();
        return result;
    }

    /**
     * Copy the vertical texture-coordinate transform vector.
     *
     * @return a new vector
     */
    public Vector2f copyTransformV() {
        Vector2f result = transformV.clone();
        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Apply dome UV distortion compensation near the horizon.
     *
     * @param topDist UV distance from the top of the dome (&gt;0)
     * @param offset UV offset from the top of the dome (not null, unaffected)
     * @param rotation rotation vector, or null
     * @param transformU storage for horizontal transform (not null, modified)
     * @param transformV storage for vertical transform (not null, modified)
     */
    private static void applyDomeDistortion(float topDist, Vector2f offset,
            Vector2f rotation, Vector2f transformU, Vector2f transformV) {
        assert topDist > 0f : topDist;
        assert offset != null;
        assert transformU != null;
        assert transformV != null;

        Vector2f tU = new Vector2f();
        Vector2f tV = new Vector2f();

        /*
         * Stretch the image horizontally to compensate for UV distortion near
         * the horizon.
         */
        float a = offset.x / topDist;
        float b = offset.y / topDist;
        tU.set(b, -a);
        tV.set(a, b);

        float stretchFactor
                = 1f + Constants.stretchCoefficient * topDist * topDist;
        tU.divideLocal(stretchFactor);

        if (rotation != null) {
            transformU.set(tU.x * b + tV.x * a, tU.y * b + tV.y * a);
            transformV.set(tV.x * b - tU.x * a, tV.y * b - tU.y * a);
        } else {
            transformU.set(tU);
            transformV.set(tV);
        }
    }

    /**
     * Apply object-local clockwise rotation.
     *
     * @param rotation rotation vector (not null, unaffected)
     * @param transformU storage for horizontal transform (not null, modified)
     * @param transformV storage for vertical transform (not null, modified)
     */
    private static void applyObjectRotation(Vector2f rotation,
            Vector2f transformU, Vector2f transformV) {
        assert rotation != null;
        assert transformU != null;
        assert transformV != null;

        Vector2f tU = new Vector2f();
        Vector2f tV = new Vector2f();

        // Rotate so top is toward the north horizon.
        tU.set(transformV);
        tV.set(-transformU.x, -transformU.y);

        // Rotate by the requested rotation vector.
        Vector2f norm = rotation.normalize();
        transformU.set(tU.x * norm.x + tV.x * norm.y,
                tU.y * norm.x + tV.y * norm.y);
        transformV.set(tV.x * norm.x - tU.x * norm.y,
                tV.y * norm.x - tU.y * norm.y);
    }
}
