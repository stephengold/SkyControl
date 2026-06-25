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
package jme3utilities.sky.config;

import jme3utilities.Validate;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.StarsOption;

/**
 * Rendering configuration for constructing a sky control.
 *
 * @author Take Some
 */
final public class SkyRenderConfig {
    /** True to create the lower sky dome. */
    final private boolean lowerDomeFlag;
    /** Flattening value for the clouds-only dome. */
    final private float cloudFlattening;
    /** Vertical offset for the clouds-only dome. */
    final private float cloudsYOffset;
    /** Star rendering mode. */
    final private StarsOption starsOption;

    /**
     * Instantiate render configuration.
     *
     * @param starsOption star rendering mode (not null)
     * @param cloudFlattening cloud flattening (&lt;1, &ge;0)
     * @param cloudsYOffset cloud dome Y offset (&lt;1, &ge;0)
     * @param lowerDomeFlag true to create the lower sky dome
     */
    public SkyRenderConfig(StarsOption starsOption, float cloudFlattening,
            float cloudsYOffset, boolean lowerDomeFlag) {
        Validate.nonNull(starsOption, "stars option");
        if (!(cloudFlattening >= 0f && cloudFlattening < 1f)) {
            throw new IllegalArgumentException("invalid cloud flattening");
        }
        if (!(cloudsYOffset >= 0f && cloudsYOffset < 1f)) {
            throw new IllegalArgumentException("invalid cloud offset");
        }

        this.starsOption = starsOption;
        this.cloudFlattening = cloudFlattening;
        this.cloudsYOffset = cloudsYOffset;
        this.lowerDomeFlag = lowerDomeFlag;
    }

    /**
     * Apply post-construction render settings.
     *
     * @param skyControl target control (not null)
     */
    public void applyTo(SkyControl skyControl) {
        Validate.nonNull(skyControl, "control");

        skyControl.setCloudsYOffset(cloudsYOffset);
    }

    /**
     * Test whether the lower sky dome should be created.
     *
     * @return true if enabled
     */
    public boolean lowerDome() {
        return lowerDomeFlag;
    }

    /**
     * Return cloud flattening.
     *
     * @return flattening value
     */
    public float cloudFlattening() {
        return cloudFlattening;
    }

    /**
     * Return clouds Y offset.
     *
     * @return offset value
     */
    public float cloudsYOffset() {
        return cloudsYOffset;
    }

    /**
     * Return star rendering mode.
     *
     * @return star rendering mode
     */
    public StarsOption starsOption() {
        return starsOption;
    }
}
