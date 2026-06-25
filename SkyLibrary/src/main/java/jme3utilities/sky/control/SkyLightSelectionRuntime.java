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

import com.jme3.math.Vector3f;

/**
 * Runtime helper for selecting the main sky light source.
 *
 * @author Take Some
 */
final public class SkyLightSelectionRuntime {
    /**
     * Hidden constructor.
     */
    private SkyLightSelectionRuntime() {
        // do nothing
    }

    /**
     * Select the main light direction and expose altitude flags.
     *
     * @param sunDirection world direction to the sun (not null, length=1)
     * @param moonDirection world direction to the moon, or null if hidden
     * @param moonWeight moonlight contribution (&le;1, &ge;0)
     * @param fallbackDirection fallback direction (not null, length=1)
     * @return new immutable result
     */
    public static Result select(Vector3f sunDirection, Vector3f moonDirection,
            float moonWeight, Vector3f fallbackDirection) {
        assert sunDirection != null;
        assert sunDirection.isUnitVector() : sunDirection;
        assert moonWeight >= 0f : moonWeight;
        assert moonWeight <= 1f : moonWeight;
        assert fallbackDirection != null;
        assert fallbackDirection.isUnitVector() : fallbackDirection;
        assert fallbackDirection.y >= 0f : fallbackDirection;
        if (moonDirection != null) {
            assert moonDirection.isUnitVector() : moonDirection;
        }

        float sineSolarAltitude = sunDirection.y;
        float sineLunarAltitude;
        if (moonDirection != null) {
            sineLunarAltitude = moonDirection.y;
        } else {
            sineLunarAltitude = -1f;
        }

        boolean moonUp = sineLunarAltitude >= 0f;
        boolean sunUp = sineSolarAltitude >= 0f;
        Vector3f mainDirection;
        if (sunUp) {
            mainDirection = sunDirection;
        } else if (moonUp && moonWeight > 0f) {
            assert moonDirection != null;
            mainDirection = moonDirection;
        } else {
            mainDirection = fallbackDirection;
        }
        assert mainDirection.isUnitVector() : mainDirection;
        assert mainDirection.y >= 0f : mainDirection;

        Result result = new Result(mainDirection, sineSolarAltitude,
                sineLunarAltitude, sunUp, moonUp);
        return result;
    }

    /**
     * Main-light selection result.
     */
    final public static class Result {
        /** Main light direction. */
        final private Vector3f mainDirection;
        /** True if moon is above horizon. */
        final private boolean moonUp;
        /** Sine of the lunar altitude. */
        final private float sineLunarAltitude;
        /** Sine of the solar altitude. */
        final private float sineSolarAltitude;
        /** True if sun is above horizon. */
        final private boolean sunUp;

        /**
         * Instantiate a result.
         *
         * @param mainDirection main light direction (not null, alias created)
         * @param sineSolarAltitude sine of the solar altitude
         * @param sineLunarAltitude sine of the lunar altitude
         * @param sunUp true if sun is above horizon
         * @param moonUp true if moon is above horizon
         */
        private Result(Vector3f mainDirection, float sineSolarAltitude,
                float sineLunarAltitude, boolean sunUp, boolean moonUp) {
            assert mainDirection != null;

            this.mainDirection = mainDirection;
            this.sineSolarAltitude = sineSolarAltitude;
            this.sineLunarAltitude = sineLunarAltitude;
            this.sunUp = sunUp;
            this.moonUp = moonUp;
        }

        /**
         * Return the selected main light direction.
         *
         * @return pre-existing vector
         */
        public Vector3f mainDirection() {
            return mainDirection;
        }

        /**
         * Test whether the moon is above the horizon.
         *
         * @return true if above the horizon
         */
        public boolean moonUp() {
            return moonUp;
        }

        /**
         * Return the sine of the lunar altitude.
         *
         * @return sine value
         */
        public float sineLunarAltitude() {
            return sineLunarAltitude;
        }

        /**
         * Return the sine of the solar altitude.
         *
         * @return sine value
         */
        public float sineSolarAltitude() {
            return sineSolarAltitude;
        }

        /**
         * Test whether the sun is above the horizon.
         *
         * @return true if above the horizon
         */
        public boolean sunUp() {
            return sunUp;
        }
    }
}
