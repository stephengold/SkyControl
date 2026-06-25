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

import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.ColorRGBA;
import com.jme3.util.clone.Cloner;
import java.io.IOException;
import jme3utilities.Validate;
import jme3utilities.sky.CloudLayer;
import jme3utilities.sky.cloud.SkyCloudPreset;
import jme3utilities.sky.cloud.SkyCloudTransitionRuntime;

/**
 * Runtime state and update logic for sky cloud layers.
 *
 * @author Take Some
 */
public final class SkyCloudRuntime {
    // *************************************************************************
    // fields

    /**
     * Cloud-layer runtime objects.
     */
    private CloudLayer[] layers;
    /**
     * Simulation time for cloud-layer animations.
     */
    private float animationTime;
    /**
     * Rate of motion for cloud-layer animations.
     */
    private float rate = 1f;
    /**
     * Runtime state for weather-preset transitions.
     */
    private SkyCloudTransitionRuntime transitionRuntime;
    // *************************************************************************
    // constructors

    /**
     * Instantiate runtime state around cloud layers.
     *
     * @param layers cloud-layer array (not null, aliased)
     */
    public SkyCloudRuntime(CloudLayer[] layers) {
        Validate.nonNull(layers, "layers");

        this.layers = layers;
        this.animationTime = 0f;
        this.transitionRuntime = new SkyCloudTransitionRuntime(layers);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Clone mutable fields using the specified cloner.
     *
     * @param cloner the cloner currently cloning the owner (not null)
     */
    public void cloneFields(Cloner cloner) {
        assert cloner != null;
        this.layers = cloner.clone(layers);
        this.transitionRuntime = new SkyCloudTransitionRuntime(layers);
    }

    /**
     * Access the indexed cloud layer.
     *
     * @param layerIndex cloud layer index
     * @return pre-existing layer
     */
    public CloudLayer getLayer(int layerIndex) {
        CloudLayer result = layers[layerIndex];

        assert result != null;
        return result;
    }

    /**
     * Return the speed and direction of cloud motion.
     *
     * @return multiple of the default rate
     */
    public float rate() {
        return rate;
    }

    /**
     * Read runtime state from an input capsule.
     *
     * @param capsule input capsule (not null)
     * @return a new instance
     * @throws IOException from capsule
     */
    public static SkyCloudRuntime read(InputCapsule capsule)
            throws IOException {
        assert capsule != null;

        Savable[] sav = capsule.readSavableArray("cloudLayers", null);
        CloudLayer[] layers = new CloudLayer[sav.length];
        System.arraycopy(sav, 0, layers, 0, sav.length);

        SkyCloudRuntime result = new SkyCloudRuntime(layers);
        result.animationTime = capsule.readFloat("cloudsAnimationTime", 0f);
        result.rate = capsule.readFloat("cloudsRelativeSpeed", 1f);

        return result;
    }

    /**
     * Alter the opacity of all cloud layers.
     *
     * @param alpha desired opacity of the cloud layers
     */
    public void setCloudiness(float alpha) {
        Validate.fraction(alpha, "alpha");

        transitionRuntime.cancel();
        for (CloudLayer layer : layers) {
            layer.setOpacity(alpha);
        }
    }

    /**
     * Apply a color to all cloud layers.
     *
     * @param color desired color (not null, unaffected)
     */
    public void setColor(ColorRGBA color) {
        assert color != null;

        for (CloudLayer layer : layers) {
            layer.setColor(color);
        }
    }

    /**
     * Alter the speed and direction of cloud motion.
     *
     * @param newRate multiple of the default rate
     */
    public void setRate(float newRate) {
        this.rate = newRate;
    }

    /**
     * Transition to a cloud weather preset.
     *
     * @param preset target preset (not null)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void transitionTo(SkyCloudPreset preset, float seconds) {
        transitionRuntime.transitionTo(preset, seconds);
    }

    /**
     * Update the cloud layers.
     *
     * @param updateInterval time interval between updates (in seconds, &ge;0)
     */
    public void update(float updateInterval) {
        assert updateInterval >= 0f : updateInterval;

        transitionRuntime.update(updateInterval);
        this.animationTime += updateInterval * rate;
        for (CloudLayer layer : layers) {
            layer.updateOffset(animationTime);
        }
    }

    /**
     * Serialize runtime state to an output capsule.
     *
     * @param capsule output capsule (not null)
     * @throws IOException from capsule
     */
    public void write(OutputCapsule capsule) throws IOException {
        assert capsule != null;

        capsule.write(layers, "cloudLayers", null);
        capsule.write(animationTime, "cloudsAnimationTime", 0f);
        capsule.write(rate, "cloudsRelativeSpeed", 1f);
    }
}
