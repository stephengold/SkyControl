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
package jme3utilities.sky.cloud;

import com.jme3.math.FastMath;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.sky.CloudLayer;

/**
 * Runtime transition that lets cloud presets fade in and out.
 * <p>
 * Texture swaps happen only while layers are invisible, so changes
 * do not pop instantly on screen.
 *
 * @author Take Some
 */
final public class SkyCloudTransitionRuntime {
    /** Message logger for this class. */
    final private static Logger logger
            = Logger.getLogger(SkyCloudTransitionRuntime.class.getName());

    /** Cloud layers being driven. */
    private CloudLayer[] layers;
    /** Opacities at transition start. */
    private float[] startOpacities;
    /** True while a transition is active. */
    private boolean active;
    /** True after target textures have been applied. */
    private boolean swapped;
    /** Elapsed transition time. */
    private float elapsed;
    /** Total transition duration. */
    private float duration;
    /** Target preset. */
    private SkyCloudPresetDefinition target;

    /**
     * Instantiate a transition runtime.
     *
     * @param layers cloud layers (not null, aliased)
     */
    public SkyCloudTransitionRuntime(CloudLayer[] layers) {
        resetLayers(layers);
        cancel();
    }

    /** Cancel any active transition. */
    public void cancel() {
        if (active && elapsed < duration) {
            logger.log(Level.FINE,
                    "cloud weather transition cancelled: target={0}, elapsed={1}, duration={2}",
                    new Object[]{target == null ? null : target.id(),
                        elapsed, duration});
        }
        this.active = false;
        this.swapped = false;
        this.elapsed = 0f;
        this.duration = 0f;
        this.target = null;
    }

    /**
     * Reset the driven layer array.
     *
     * @param layers cloud layers (not null, aliased)
     */
    public void resetLayers(CloudLayer[] layers) {
        Validate.nonNull(layers, "layers");

        this.layers = layers;
        this.startOpacities = new float[layers.length];
    }

    /**
     * Start a transition to the specified built-in preset.
     *
     * @param preset target preset (not null)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void transitionTo(SkyCloudPreset preset, float seconds) {
        Validate.nonNull(preset, "preset");

        SkyCloudPresetDefinition definition
                = SkyCloudPresetDefinition.fromPreset(preset);
        transitionTo(definition, seconds);
    }

    /**
     * Start a transition to the specified data preset.
     *
     * @param definition target preset definition (not null)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void transitionTo(SkyCloudPresetDefinition definition,
            float seconds) {
        Validate.nonNull(definition, "definition");
        if (!(seconds >= 0f)) {
            throw new IllegalArgumentException("duration must be non-negative");
        }

        this.target = definition;
        this.duration = seconds;
        this.elapsed = 0f;
        this.swapped = false;

        for (int layerI = 0; layerI < layers.length; ++layerI) {
            startOpacities[layerI] = layers[layerI].getOpacity();
        }

        logger.log(Level.INFO,
                "cloud weather transition started: id={0}, seconds={1}, layers={2}",
                new Object[]{definition.id(), seconds, definition.layerCount()});
        if (seconds == 0f) {
            applyTargetTextures();
            applyTargetOpacities();
            logger.log(Level.FINE,
                    "cloud weather transition applied immediately: id={0}",
                    definition.id());
            cancel();
        } else {
            this.active = true;
        }
    }

    /**
     * Update the active transition.
     *
     * @param interval update interval in seconds (&ge;0)
     */
    public void update(float interval) {
        assert interval >= 0f : interval;
        if (!active) {
            return;
        }

        elapsed += interval;
        float halfDuration = duration * 0.5f;
        if (elapsed < halfDuration) {
            float weight = smooth(elapsed / halfDuration);
            fadeOut(weight);
            return;
        }

        if (!swapped) {
            applyTargetTextures();
            swapped = true;
            logger.log(Level.FINE,
                    "cloud weather transition textures swapped: id={0}",
                    target.id());
        }

        float weight = smooth((elapsed - halfDuration) / halfDuration);
        fadeIn(weight);

        if (elapsed >= duration) {
            applyTargetOpacities();
            logger.log(Level.INFO,
                    "cloud weather transition completed: id={0}, duration={1}",
                    new Object[]{target.id(), duration});
            cancel();
        }
    }

    /** Apply target opacities. */
    private void applyTargetOpacities() {
        for (int layerI = 0; layerI < layers.length; ++layerI) {
            SkyCloudLayerSpec spec = target.layer(layerI);
            layers[layerI].setOpacity(spec.opacity());
        }
    }

    /** Apply target textures, scales, and motions with zero opacity. */
    private void applyTargetTextures() {
        for (int layerI = 0; layerI < layers.length; ++layerI) {
            SkyCloudLayerSpec spec = target.layer(layerI);
            CloudLayer layer = layers[layerI];
            layer.setOpacity(0f);
            layer.setTexture(spec.alphaMap(), spec.scale());
            layer.setNormalMap(spec.normalMap());
            layer.setMotion(0f, spec.uRate(), 0f, spec.vRate());
            logger.log(Level.FINER,
                    "cloud layer target applied: index={0}, alpha={1}, normal={2}, scale={3}, opacity={4}, uRate={5}, vRate={6}",
                    new Object[]{layerI, spec.alphaMap(), spec.normalMap(),
                        spec.scale(), spec.opacity(), spec.uRate(), spec.vRate()});
        }
    }

    /**
     * Fade current layers out.
     *
     * @param weight transition weight
     */
    private void fadeOut(float weight) {
        for (int layerI = 0; layerI < layers.length; ++layerI) {
            float opacity = startOpacities[layerI] * (1f - weight);
            layers[layerI].setOpacity(opacity);
        }
    }

    /**
     * Fade target layers in.
     *
     * @param weight transition weight
     */
    private void fadeIn(float weight) {
        for (int layerI = 0; layerI < layers.length; ++layerI) {
            SkyCloudLayerSpec spec = target.layer(layerI);
            float opacity = spec.opacity() * weight;
            layers[layerI].setOpacity(opacity);
        }
    }

    /**
     * Smooth a transition weight.
     *
     * @param input unsmoothed weight
     * @return smoothed weight
     */
    private static float smooth(float input) {
        float t = FastMath.saturate(input);
        float result = t * t * (3f - 2f * t);
        return result;
    }
}
