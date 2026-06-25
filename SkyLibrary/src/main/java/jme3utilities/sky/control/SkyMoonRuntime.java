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
package jme3utilities.sky.control;

import com.jme3.asset.AssetNotFoundException;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture;
import jme3utilities.math.MyMath;
import jme3utilities.mesh.DomeMesh;
import jme3utilities.sky.GlobeRenderer;
import jme3utilities.sky.LunarPhase;
import jme3utilities.sky.SkyMaterial;
import jme3utilities.sky.SunAndStars;

/**
 * Runtime helper for moon direction, phase texture, and dome projection.
 *
 * @author Take Some
 */
public final class SkyMoonRuntime {
    /**
     * Hidden constructor.
     */
    private SkyMoonRuntime() {
        // do nothing
    }

    /**
     * Immutable arguments for one moon update.
     */
    final public static class MoonUpdateState {
        /** Off-screen moon renderer, or null. */
        final private GlobeRenderer renderer;
        /** Moon object index in the sky material. */
        final private int objectIndex;
        /** Difference in longitude between the moon and sun. */
        final private float longitudeDifference;
        /** Lunar latitude north of the ecliptic. */
        final private float lunarLatitude;
        /** Moon texture scale. */
        final private float moonScale;

        /**
         * Instantiate update arguments.
         *
         * @param renderer off-screen moon renderer, or null
         * @param objectIndex moon object index
         * @param longitudeDifference radians east of the sun
         * @param lunarLatitude radians north of the ecliptic
         * @param moonScale moon texture scale
         */
        public MoonUpdateState(GlobeRenderer renderer, int objectIndex,
                float longitudeDifference, float lunarLatitude,
                float moonScale) {
            this.renderer = renderer;
            this.objectIndex = objectIndex;
            this.longitudeDifference = longitudeDifference;
            this.lunarLatitude = lunarLatitude;
            this.moonScale = moonScale;
        }
    }

    /**
     * Apply a custom moon texture if one has been specified.
     *
     * @param material top-dome material (not null)
     * @param objectIndex moon object index
     * @param assetPath custom texture asset path, or null
     * @param colorMap custom texture object, or null
     * @return true if a custom texture was applied, otherwise false
     */
    public static boolean applyTexture(SkyMaterial material, int objectIndex,
            String assetPath, Texture colorMap) {
        assert material != null;

        if (colorMap != null) {
            material.addObject(objectIndex, colorMap);
            return true;
        } else if (assetPath != null) {
            material.addObject(objectIndex, assetPath);
            return true;
        }

        return false;
    }

    /**
     * Apply the moon texture for a phase preset.
     *
     * @param material top-dome material (not null)
     * @param objectIndex moon object index
     * @param preset phase preset (not null)
     */
    public static void applyPresetTexture(SkyMaterial material, int objectIndex,
            LunarPhase preset) {
        assert material != null;
        assert preset != null;

        String assetPath = preset.imagePath("");
        try {
            material.addObject(objectIndex, assetPath);
        } catch (AssetNotFoundException exception) {
            assetPath = preset.imagePath("-nonviral");
            material.addObject(objectIndex, assetPath);
        }
    }

    /**
     * Calculate the direction to the center of the moon.
     *
     * @param sunAndStars orientation model (not null)
     * @param longitudeDifference radians east of the sun
     * @param lunarLatitude radians north of the ecliptic
     * @param storeResult storage for the result, or null
     * @return a unit vector in world coordinates
     */
    public static Vector3f direction(SunAndStars sunAndStars,
            float longitudeDifference, float lunarLatitude,
            Vector3f storeResult) {
        assert sunAndStars != null;

        float solarLongitude = sunAndStars.getSolarLongitude();
        float celestialLongitude = solarLongitude + longitudeDifference;
        celestialLongitude = MyMath.modulo(celestialLongitude, FastMath.TWO_PI);
        Vector3f result = sunAndStars.convertToWorld(
                lunarLatitude, celestialLongitude, storeResult);

        return result;
    }

    /**
     * Compute the clockwise rotation of the moon texture relative to the sky.
     *
     * @param sunAndStars orientation model (not null)
     * @param topMesh top dome mesh (not null)
     * @param lunarLatitude radians north of the ecliptic
     * @param longitude moon celestial longitude
     * @param uvCenter texture coordinates of the moon center (not null)
     * @return new unit vector
     */
    public static Vector2f rotation(SunAndStars sunAndStars, DomeMesh topMesh,
            float lunarLatitude, float longitude, Vector2f uvCenter) {
        assert sunAndStars != null;
        assert topMesh != null;
        assert uvCenter != null;

        float latitude = lunarLatitude + 0.01f;
        if (latitude <= FastMath.HALF_PI) {
            Vector3f north
                    = sunAndStars.convertToWorld(latitude, longitude, null);
            Vector2f uvNorth = topMesh.directionUV(north);
            if (uvNorth != null) {
                Vector2f offset = uvNorth.subtract(uvCenter);
                assert offset.length() > 0f : offset;
                Vector2f result = offset.normalize();
                return result;
            }
        }

        latitude = lunarLatitude - 0.01f;
        assert latitude >= -FastMath.HALF_PI : lunarLatitude;
        Vector3f south = sunAndStars.convertToWorld(latitude, longitude, null);
        Vector2f uvSouth = topMesh.directionUV(south);
        if (uvSouth != null) {
            Vector2f offset = uvCenter.subtract(uvSouth);
            assert offset.length() > 0f : offset;
            Vector2f result = offset.normalize();
            return result;
        }

        assert false : south;
        return null;
    }

    /**
     * Update the moon's position and size.
     *
     * @param material top-dome material (not null)
     * @param topMesh top dome mesh (not null)
     * @param sunAndStars orientation model (not null)
     * @param phase current phase, or null to hide the moon
     * @param state update arguments (not null)
     * @return world direction to the moon, or null if hidden
     */
    public static Vector3f updateMoon(SkyMaterial material, DomeMesh topMesh,
            SunAndStars sunAndStars, LunarPhase phase,
            MoonUpdateState state) {
        assert material != null;
        assert topMesh != null;
        assert sunAndStars != null;
        assert state != null;

        if (phase == null) {
            material.hideObject(state.objectIndex);
            return null;
        }

        if (phase == LunarPhase.CUSTOM) {
            assert state.renderer != null;
            float intensity = 2f + FastMath.abs(
                    state.longitudeDifference - FastMath.PI);
            state.renderer.setLightIntensity(intensity);
            state.renderer.setPhase(
                    state.longitudeDifference, state.lunarLatitude);
        }

        float solarLongitude = sunAndStars.getSolarLongitude();
        float celestialLongitude
                = solarLongitude + state.longitudeDifference;
        celestialLongitude = MyMath.modulo(celestialLongitude, FastMath.TWO_PI);
        Vector3f worldDirection = sunAndStars.convertToWorld(
                state.lunarLatitude, celestialLongitude, null);
        Vector2f uvCenter = topMesh.directionUV(worldDirection);

        if (uvCenter != null) {
            Vector2f rotation = rotation(sunAndStars, topMesh,
                    state.lunarLatitude, celestialLongitude, uvCenter);
            material.setObjectTransform(
                    state.objectIndex, uvCenter, state.moonScale, rotation);
        } else {
            material.hideObject(state.objectIndex);
        }

        return worldDirection;
    }
}
