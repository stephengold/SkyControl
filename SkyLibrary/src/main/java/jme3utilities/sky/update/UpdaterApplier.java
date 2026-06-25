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
package jme3utilities.sky.update;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.ViewPort;
import com.jme3.shadow.AbstractShadowFilter;
import com.jme3.shadow.AbstractShadowRenderer;
import java.util.List;

/**
 * Applies Updater state to live scene objects.
 *
 * @author Take Some
 */
public final class UpdaterApplier {
    /**
     * Hidden constructor.
     */
    private UpdaterApplier() {
        // do nothing
    }

    /**
     * Apply ambient light color.
     *
     * @param light target light, or null
     * @param color source color (not null, unaffected)
     * @param multiplier intensity multiplier
     */
    public static void applyAmbient(
            AmbientLight light, ColorRGBA color, float multiplier) {
        if (light != null) {
            ColorRGBA applied = color.mult(multiplier);
            light.setColor(applied);
        }
    }

    /**
     * Apply bloom intensity to known filters.
     *
     * @param filters target filters (not null)
     * @param intensity bloom intensity
     */
    public static void applyBloom(List<BloomFilter> filters, float intensity) {
        for (BloomFilter filter : filters) {
            filter.setBloomIntensity(intensity);
        }
    }

    /**
     * Apply directional light color and direction.
     *
     * @param light target light, or null
     * @param color source color (not null, unaffected)
     * @param multiplier intensity multiplier
     * @param direction direction to the light source (not null, unaffected)
     */
    public static void applyMain(DirectionalLight light, ColorRGBA color,
            float multiplier, Vector3f direction) {
        if (light != null) {
            ColorRGBA applied = color.mult(multiplier);
            light.setColor(applied);
            Vector3f propagationDirection = direction.negate();
            light.setDirection(propagationDirection);
        }
    }

    /**
     * Apply shadow intensity to known filters.
     *
     * @param filters target filters (not null)
     * @param intensity shadow intensity
     */
    public static void applyShadowFilters(
            List<AbstractShadowFilter<?>> filters, float intensity) {
        for (AbstractShadowFilter<?> filter : filters) {
            filter.setShadowIntensity(intensity);
        }
    }

    /**
     * Apply shadow intensity to known renderers.
     *
     * @param renderers target renderers (not null)
     * @param intensity shadow intensity
     */
    public static void applyShadowRenderers(
            List<AbstractShadowRenderer> renderers, float intensity) {
        for (AbstractShadowRenderer renderer : renderers) {
            renderer.setShadowIntensity(intensity);
        }
    }

    /**
     * Apply background color to known viewports.
     *
     * @param viewPorts target viewports (not null)
     * @param color background color (not null, unaffected)
     */
    public static void applyViewPorts(
            List<ViewPort> viewPorts, ColorRGBA color) {
        for (ViewPort viewPort : viewPorts) {
            viewPort.setBackgroundColor(color);
        }
    }
}
