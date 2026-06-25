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

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import jme3utilities.math.MyMath;
import jme3utilities.mesh.DomeMesh;
import jme3utilities.sky.SkyMaterial;

/**
 * Runtime helper for cloud transmission sampling.
 *
 * @author Take Some
 */
final public class SkyCloudTransmissionRuntime {
    /**
     * Hidden constructor.
     */
    private SkyCloudTransmissionRuntime() {
        // do nothing
    }

    /**
     * Determine what fraction of the main light passes through clouds.
     *
     * @param input lighting state (not null)
     * @param resources cloud resources (not null)
     * @return transmission factor
     */
    public static float transmission(Input input, Resources resources) {
        assert input != null;
        assert resources != null;

        if (!input.modulationApplies()) {
            return 1f;
        }

        Vector3f intersection = intersectCloudDome(
                input.mainDirection, resources.cloudsOnlyDome);
        Vector2f texCoord = resources.cloudsMesh.directionUV(intersection);
        float result = resources.cloudsMaterial.getTransmission(texCoord);
        return result;
    }

    /**
     * Compute where a direction intersects the local cloud dome.
     *
     * @param mainDirection unit vector with non-negative y-component
     * @param cloudsOnlyDome optional clouds-only dome geometry
     * @return new unit vector
     */
    private static Vector3f intersectCloudDome(
            Vector3f mainDirection, Geometry cloudsOnlyDome) {
        assert mainDirection != null;
        assert mainDirection.isUnitVector() : mainDirection;
        assert mainDirection.y >= 0f : mainDirection;

        double cosSquared
                = MyMath.sumOfSquares(mainDirection.x, mainDirection.z);
        if (cosSquared == 0.0) {
            return new Vector3f(0f, 1f, 0f);
        }

        float deltaY;
        float semiMinorAxis;
        if (cloudsOnlyDome == null) {
            deltaY = 0f;
            semiMinorAxis = 1f;
        } else {
            Vector3f offset = cloudsOnlyDome.getLocalTranslation();
            assert offset.x == 0f : offset;
            assert offset.y <= 0f : offset;
            assert offset.z == 0f : offset;
            deltaY = offset.y;

            Vector3f scale = cloudsOnlyDome.getLocalScale();
            assert scale.x == 1f : scale;
            assert scale.y > 0f : scale;
            assert scale.z == 1f : scale;
            semiMinorAxis = scale.y;
        }

        double cosAltitude = Math.sqrt(cosSquared);
        double tanAltitude = mainDirection.y / cosAltitude;
        double smaSquared = semiMinorAxis * semiMinorAxis;
        double a = tanAltitude * tanAltitude + smaSquared;
        assert a > 0.0 : a;
        double b = -2.0 * deltaY * tanAltitude;
        double c = deltaY * deltaY - smaSquared;
        double discriminant = MyMath.discriminant(a, b, c);
        assert discriminant >= 0.0 : discriminant;
        double w = (-b + Math.sqrt(discriminant)) / (2.0 * a);

        double distance = w / cosAltitude;
        if (distance > 1.0) {
            distance = 1.0;
        }
        float x = (float) (mainDirection.x * distance);
        float y = (float) MyMath.circle(w);
        float z = (float) (mainDirection.z * distance);
        Vector3f result = new Vector3f(x, y, z);

        assert result.isUnitVector() : result;
        return result;
    }

    /**
     * Lighting state for cloud transmission.
     */
    final public static class Input {
        /** True if cloud modulation is enabled. */
        final private boolean cloudModulationFlag;
        /** Main light direction. */
        final private Vector3f mainDirection;
        /** True if moon is above the horizon. */
        final private boolean moonUp;
        /** Moonlight contribution. */
        final private float moonWeight;
        /** True if sun is above the horizon. */
        final private boolean sunUp;

        /**
         * Instantiate an input.
         *
         * @param cloudModulationFlag true if cloud modulation is enabled
         * @param sunUp true if the sun is above the horizon
         * @param moonUp true if the moon is above the horizon
         * @param moonWeight moonlight contribution (&le;1, &ge;0)
         * @param mainDirection main light direction (not null, alias created)
         */
        public Input(boolean cloudModulationFlag, boolean sunUp,
                boolean moonUp, float moonWeight, Vector3f mainDirection) {
            assert moonWeight >= 0f : moonWeight;
            assert moonWeight <= 1f : moonWeight;
            assert mainDirection != null;
            assert mainDirection.isUnitVector() : mainDirection;
            assert mainDirection.y >= 0f : mainDirection;

            this.cloudModulationFlag = cloudModulationFlag;
            this.sunUp = sunUp;
            this.moonUp = moonUp;
            this.moonWeight = moonWeight;
            this.mainDirection = mainDirection;
        }

        /**
         * Test whether cloud modulation applies.
         *
         * @return true if transmission should be sampled
         */
        private boolean modulationApplies() {
            boolean result = cloudModulationFlag
                    && (sunUp || moonUp && moonWeight > 0f);
            return result;
        }
    }

    /**
     * Cloud resources for transmission sampling.
     */
    final public static class Resources {
        /** Optional clouds-only dome. */
        final private Geometry cloudsOnlyDome;
        /** Clouds material. */
        final private SkyMaterial cloudsMaterial;
        /** Clouds mesh. */
        final private DomeMesh cloudsMesh;

        /**
         * Instantiate resources.
         *
         * @param cloudsOnlyDome optional clouds-only dome
         * @param cloudsMesh clouds mesh (not null, alias created)
         * @param cloudsMaterial clouds material (not null, alias created)
         */
        public Resources(Geometry cloudsOnlyDome, DomeMesh cloudsMesh,
                SkyMaterial cloudsMaterial) {
            assert cloudsMesh != null;
            assert cloudsMaterial != null;

            this.cloudsOnlyDome = cloudsOnlyDome;
            this.cloudsMesh = cloudsMesh;
            this.cloudsMaterial = cloudsMaterial;
        }
    }
}
