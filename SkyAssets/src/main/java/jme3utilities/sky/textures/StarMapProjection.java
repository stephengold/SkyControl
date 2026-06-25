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
package jme3utilities.sky.textures;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import jme3utilities.MyAsset;
import jme3utilities.math.MyQuaternion;
import jme3utilities.math.MyVector3f;

/**
 * Coordinate conversion helpers for star-map generation.
 *
 * @author Take Some
 */
final class StarMapProjection {
    /**
     * Hidden constructor.
     */
    private StarMapProjection() {
        // do nothing
    }

    /**
     * Calculate texture coordinates for a direction on one cube face.
     *
     * @param direction direction from the cube center (length &gt;0,
     * unaffected)
     * @param faceIndex cube face index (&ge;0, &lt;6)
     * @return new vector, or null when outside the face
     */
    static Vector2f cubeUV(Vector3f direction, int faceIndex) {
        assert direction != null;
        assert !MyVector3f.isZero(direction);
        assert faceIndex >= 0 : faceIndex;
        assert faceIndex < 6 : faceIndex;

        Vector3f faceDir
                = MyAsset.copyFaceDirection(faceIndex);
        Vector3f norm = direction.normalize();
        float dot = faceDir.dot(norm);
        if (dot < 0.5f) {
            return null;
        }

        norm.divideLocal(dot);
        Vector3f uDir = MyAsset.copyUDirection(faceIndex);
        Vector3f vDir = MyAsset.copyVDirection(faceIndex);
        float u = 0.5f * (1f + uDir.dot(norm));
        float v = 0.5f * (1f + vDir.dot(norm));
        Vector2f result = new Vector2f(u, v);

        return result;
    }

    /**
     * Convert a star's equatorial coordinates to world coordinates.
     *
     * @param star star to convert (not null)
     * @param latitude radians north of the equator (&le;Pi/2, &ge;-Pi/2)
     * @param siderealTime radians since sidereal midnight (&lt;2*Pi, &ge;0)
     * @return new unit vector in world coordinates
     */
    static Vector3f worldDirection(
            Star star, float latitude, float siderealTime) {
        assert star != null;
        assert latitude >= -FastMath.HALF_PI : latitude;
        assert latitude <= FastMath.HALF_PI : latitude;
        assert siderealTime >= 0f : siderealTime;
        assert siderealTime < FastMath.TWO_PI : siderealTime;

        Vector3f equatorial = star.getEquatorialLocation(siderealTime);
        float coLatitude = FastMath.HALF_PI - latitude;
        Quaternion rotation = new Quaternion();
        rotation.fromAngleNormalAxis(-coLatitude, Vector3f.UNIT_Y);
        Vector3f rotated = MyQuaternion.rotate(rotation, equatorial, null);
        assert rotated.isUnitVector() : rotated;
        Vector3f result = new Vector3f(-rotated.x, rotated.z, rotated.y);

        return result;
    }
}
