/*
 Copyright (c) 2013-2024 Stephen Gold

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
import jme3utilities.math.MyMath;

/**
 * Enumerate the pre-set conditions for MakeStarMaps.
 *
 * @author Stephen Gold sgold@sonic.net
 */
enum StarMapPreset {
    // *************************************************************************
    // values

    /**
     * Equator 0h local sidereal time at 2048x2048 resolution (for cube)
     */
    EQUATOR_4M,
    /**
     * Equator 0h local sidereal time at 4096x4096 resolution (for cube)
     */
    EQUATOR_16M,
    /**
     * stars of the Northern Hemisphere at 2048x2048 resolution
     */
    NORTH_4M,
    /**
     * stars of the Northern Hemisphere at 4096x4096 resolution
     */
    NORTH_16M,
    /**
     * stars of the Southern Hemisphere at 2048x2048 resolution
     */
    SOUTH_4M,
    /**
     * stars of the Southern Hemisphere at 4096x4096 resolution
     */
    SOUTH_16M,
    /**
     * stars of Wiltshire 10h33m local sidereal time at 2048x2048 resolution
     */
    WILTSHIRE_4M,
    /**
     * stars of Wiltshire 10h33m local sidereal time at 4096x4096 resolution
     */
    WILTSHIRE_16M;
    // *************************************************************************
    // new methods exposed

    /**
     * Look up the textual description of this preset.
     *
     * @return a description (not null, not empty)
     */
    String describe() {
        switch (this) {
            case EQUATOR_4M:
                return "equator";
            case EQUATOR_16M:
                return "equator_16m";
            case NORTH_4M:
                return "north";
            case NORTH_16M:
                return "north_16m";
            case SOUTH_4M:
                return "south";
            case SOUTH_16M:
                return "south_16m";
            case WILTSHIRE_4M:
                return "wiltshire";
            case WILTSHIRE_16M:
                return "wiltshire_16m";
            default:
                return "?";
        }
    }

    /**
     * Find a preset value based on its textual description.
     *
     * @param description textual description of the desired preset
     * @return the preset value, or null if not found
     */
    static StarMapPreset fromDescription(String description) {
        for (StarMapPreset preset : values()) {
            String d = preset.describe();
            if (d.equals(description)) {
                return preset;
            }
        }

        return null;
    }

    /**
     * Look up the sidereal time for this preset.
     *
     * @return number of hours since midnight (&le;24, &ge;0)
     */
    float hour() {
        switch (this) {
            case EQUATOR_4M:
            case EQUATOR_16M:
            case NORTH_4M:
            case NORTH_16M:
            case SOUTH_4M:
            case SOUTH_16M:
                return 0f;
            case WILTSHIRE_4M:
            case WILTSHIRE_16M:
                /*
                 * At 10h33m, Orion is about to set in the west and the
                 * Pointers of the Big Dipper are near the meridian.
                 */
                return 10.55f;
            default:
                throw new IllegalStateException("preset = " + this);
        }
    }

    /**
     * Return the observer's latitude for this preset.
     *
     * @return radians north of the equator (&le;Pi/2, &ge;-Pi/2)
     */
    float latitude() {
        switch (this) {
            case EQUATOR_4M:
            case EQUATOR_16M:
                return 0f;
            case NORTH_4M:
            case NORTH_16M:
                return FastMath.HALF_PI;
            case SOUTH_4M:
            case SOUTH_16M:
                return -FastMath.HALF_PI;
            case WILTSHIRE_4M:
            case WILTSHIRE_16M:
                // Stonehenge
                return MyMath.toRadians(51.1788f);

            default:
                throw new IllegalStateException("preset = " + this);
        }
    }

    /**
     * Return the name of the texture asset file or folder for this preset.
     *
     * @return the name (not null, not empty)
     */
    String textureFileName() {
        switch (this) {
            case EQUATOR_4M:
                return "equator";
            case EQUATOR_16M:
                return "equator16m";
            case NORTH_4M:
                return "northern";
            case NORTH_16M:
                return "16m/northern";
            case SOUTH_4M:
                return "southern";
            case SOUTH_16M:
                return "16m/southern";
            case WILTSHIRE_4M:
                return "wiltshire";
            case WILTSHIRE_16M:
                return "16m/wiltshire";
            default:
                throw new IllegalStateException("preset = " + this);
        }
    }

    /**
     * Look up the texture resolution for this preset.
     *
     * @return size of each texture map (pixels per side)
     */
    int textureSize() {
        switch (this) {
            case EQUATOR_4M:
            case NORTH_4M:
            case SOUTH_4M:
            case WILTSHIRE_4M:
                return 2_048;
            case EQUATOR_16M:
            case NORTH_16M:
            case SOUTH_16M:
            case WILTSHIRE_16M:
                return 4_096;
            default:
                throw new IllegalStateException("preset = " + this);
        }
    }
}
