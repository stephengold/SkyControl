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
package jme3utilities.sky;

import com.jme3.math.ColorRGBA;
import java.util.Properties;

/**
 * Parsing and validation helpers for SkyAtmosphere property profiles.
 *
 * @author Take Some
 */
final class SkyAtmosphereProperties {
    /**
     * Hidden constructor.
     */
    private SkyAtmosphereProperties() {
        // do nothing
    }

    /**
     * Read a color override from properties.
     *
     * @param properties source properties (not null)
     * @param key property key (not null)
     * @param fallback fallback color (not null)
     * @return parsed or fallback color
     */
    static ColorRGBA readColor(
            Properties properties, String key, ColorRGBA fallback) {
        String text = properties.getProperty(key);
        if (text == null) {
            return fallback;
        }

        String[] components = text.split(",");
        if (components.length != 3 && components.length != 4) {
            throw new IllegalArgumentException(
                    key + " should contain r,g,b or r,g,b,a");
        }
        float r = Float.parseFloat(components[0].trim());
        float g = Float.parseFloat(components[1].trim());
        float b = Float.parseFloat(components[2].trim());
        float a = (components.length == 4)
                ? Float.parseFloat(components[3].trim()) : fallback.a;
        ColorRGBA result = new ColorRGBA(r, g, b, a);

        return result;
    }

    /**
     * Read a float override from properties.
     *
     * @param properties source properties (not null)
     * @param key property key (not null)
     * @param fallback fallback value
     * @return parsed or fallback value
     */
    static float readFloat(Properties properties, String key, float fallback) {
        String text = properties.getProperty(key);
        if (text == null) {
            return fallback;
        }
        float result = Float.parseFloat(text.trim());

        return result;
    }

    /**
     * Validate a positive fraction.
     *
     * @param value value to validate
     * @param description value description (not null)
     */
    static void validatePosFraction(float value, String description) {
        if (!(value > 0f && value <= 1f)) {
            throw new IllegalArgumentException(
                    description + " should be greater than 0"
                    + " and no more than 1");
        }
    }
}
