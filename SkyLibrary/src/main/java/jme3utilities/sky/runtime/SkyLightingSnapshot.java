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

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import jme3utilities.Validate;

/**
 * Immutable copy of the latest sky lighting output.
 * <p>
 * Game code can read this snapshot without coupling directly to the internal
 * {@code Updater} pipeline.
 *
 * @author Take Some
 */
final public class SkyLightingSnapshot {
    /** Shared empty snapshot used before the first lighting update. */
    final private static SkyLightingSnapshot emptySnapshot
            = new SkyLightingSnapshot(
                    ColorRGBA.Black, ColorRGBA.Black, ColorRGBA.Black,
                    Vector3f.UNIT_Y, new SkyLightingState(0f, 0f, false,
                            false));

    /** Ambient light color. */
    final private ColorRGBA ambientColor;
    /** Viewport/background color derived from the sky. */
    final private ColorRGBA backgroundColor;
    /** Main directional light color. */
    final private ColorRGBA mainLightColor;
    /** Main light direction. */
    final private Vector3f mainDirection;
    /** Scalar lighting state and horizon flags. */
    final private SkyLightingState state;

    /**
     * Instantiate a snapshot.
     *
     * @param ambientColor ambient light color (not null, unaffected)
     * @param backgroundColor viewport/background color (not null, unaffected)
     * @param mainLightColor main light color (not null, unaffected)
     * @param mainDirection main light direction (not null, unaffected)
     * @param state scalar lighting state (not null, unaffected)
     */
    public SkyLightingSnapshot(ColorRGBA ambientColor,
            ColorRGBA backgroundColor, ColorRGBA mainLightColor,
            Vector3f mainDirection, SkyLightingState state) {
        Validate.nonNull(ambientColor, "ambient color");
        Validate.nonNull(backgroundColor, "background color");
        Validate.nonNull(mainLightColor, "main color");
        Validate.nonNull(mainDirection, "main direction");
        Validate.nonNull(state, "state");

        this.ambientColor = ambientColor.clone();
        this.backgroundColor = backgroundColor.clone();
        this.mainLightColor = mainLightColor.clone();
        this.mainDirection = mainDirection.clone();
        this.state = state.copy();
    }

    /**
     * Return an empty pre-update snapshot.
     *
     * @return shared immutable snapshot
     */
    public static SkyLightingSnapshot empty() {
        return emptySnapshot;
    }

    /**
     * Copy the ambient light color.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return copied color
     */
    public ColorRGBA ambientColor(ColorRGBA storeResult) {
        return copyColor(ambientColor, storeResult);
    }

    /**
     * Copy the background color.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return copied color
     */
    public ColorRGBA backgroundColor(ColorRGBA storeResult) {
        return copyColor(backgroundColor, storeResult);
    }

    /**
     * Return the recommended bloom intensity.
     *
     * @return bloom intensity
     */
    public float bloomIntensity() {
        return state.bloomIntensity();
    }

    /**
     * Copy this snapshot.
     *
     * @return a new equivalent snapshot
     */
    public SkyLightingSnapshot copy() {
        SkyLightingSnapshot result = new SkyLightingSnapshot(ambientColor,
                backgroundColor, mainLightColor, mainDirection, state);
        return result;
    }

    /**
     * Copy the main directional light color.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return copied color
     */
    public ColorRGBA mainDirectionalColor(ColorRGBA storeResult) {
        return copyColor(mainLightColor, storeResult);
    }

    /**
     * Copy the main light direction.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return copied direction
     */
    public Vector3f mainDirection(Vector3f storeResult) {
        Vector3f result = storeResult == null ? new Vector3f() : storeResult;
        result.set(mainDirection);
        return result;
    }

    /**
     * Test whether the moon is above the horizon.
     *
     * @return true if the moon is up
     */
    public boolean moonUp() {
        return state.moonUp();
    }

    /**
     * Return the recommended shadow intensity.
     *
     * @return shadow intensity
     */
    public float shadowIntensity() {
        return state.shadowIntensity();
    }

    /**
     * Test whether the sun is above the horizon.
     *
     * @return true if the sun is up
     */
    public boolean sunUp() {
        return state.sunUp();
    }

    /**
     * Copy a color.
     *
     * @param source source color (not null)
     * @param storeResult storage for the result (modified if not null)
     * @return copied color
     */
    private static ColorRGBA copyColor(
            ColorRGBA source, ColorRGBA storeResult) {
        ColorRGBA result = storeResult == null ? new ColorRGBA() : storeResult;
        result.set(source);
        return result;
    }
}
