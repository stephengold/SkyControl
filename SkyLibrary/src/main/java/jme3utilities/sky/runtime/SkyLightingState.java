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
package jme3utilities.sky.runtime;

import jme3utilities.Validate;

/**
 * Immutable scalar and horizon flags for a lighting snapshot.
 *
 * @author Take Some
 */
final public class SkyLightingState {
    /** Recommended bloom intensity. */
    final private float bloomIntensity;
    /** True if the moon is above the horizon. */
    final private boolean moonUp;
    /** Recommended shadow intensity. */
    final private float shadowIntensity;
    /** True if the sun is above the horizon. */
    final private boolean sunUp;

    /**
     * Instantiate lighting state.
     *
     * @param bloomIntensity recommended bloom intensity (&ge;0)
     * @param shadowIntensity recommended shadow intensity (&le;1, &ge;0)
     * @param sunUp true if the sun is above the horizon
     * @param moonUp true if the moon is above the horizon
     */
    public SkyLightingState(float bloomIntensity, float shadowIntensity,
            boolean sunUp, boolean moonUp) {
        Validate.nonNegative(bloomIntensity, "bloom intensity");
        Validate.fraction(shadowIntensity, "shadow intensity");

        this.bloomIntensity = bloomIntensity;
        this.shadowIntensity = shadowIntensity;
        this.sunUp = sunUp;
        this.moonUp = moonUp;
    }

    /**
     * Return the recommended bloom intensity.
     *
     * @return bloom intensity
     */
    public float bloomIntensity() {
        return bloomIntensity;
    }

    /**
     * Copy this state.
     *
     * @return new equivalent state
     */
    public SkyLightingState copy() {
        SkyLightingState result = new SkyLightingState(
                bloomIntensity, shadowIntensity, sunUp, moonUp);
        return result;
    }

    /**
     * Test whether the moon is above the horizon.
     *
     * @return true if the moon is up
     */
    public boolean moonUp() {
        return moonUp;
    }

    /**
     * Return the recommended shadow intensity.
     *
     * @return shadow intensity
     */
    public float shadowIntensity() {
        return shadowIntensity;
    }

    /**
     * Test whether the sun is above the horizon.
     *
     * @return true if the sun is up
     */
    public boolean sunUp() {
        return sunUp;
    }
}
