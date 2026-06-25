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

/**
 * Integration defaults for atmosphere and weather bootstrapping.
 *
 * @author Take Some
 */
final public class SkyIntegrationConfig {
    /** Atmosphere property profile path. */
    final private String atmospherePath;
    /** True if clouds modulate the main light. */
    final private boolean cloudModFlag;
    /** Initial weather id. */
    final private String initialWeatherId;
    /** Weather transition duration. */
    final private float transitionSec;
    /** Lua weather registry path. */
    final private String weatherPath;

    /**
     * Instantiate integration configuration.
     *
     * @param atmospherePath atmosphere property path (not null, not empty)
     * @param weatherPath Lua weather registry path (not null, not empty)
     * @param initialWeatherId initial weather id (not null, not empty)
     * @param transitionSec transition duration in seconds (&ge;0)
     * @param cloudModFlag true if clouds modulate main light
     */
    public SkyIntegrationConfig(String atmospherePath, String weatherPath,
            String initialWeatherId, float transitionSec,
            boolean cloudModFlag) {
        Validate.nonEmpty(atmospherePath, "atmosphere path");
        Validate.nonEmpty(weatherPath, "weather path");
        Validate.nonEmpty(initialWeatherId, "weather id");
        Validate.nonNegative(transitionSec, "seconds");

        this.atmospherePath = atmospherePath;
        this.weatherPath = weatherPath;
        this.initialWeatherId = initialWeatherId;
        this.transitionSec = transitionSec;
        this.cloudModFlag = cloudModFlag;
    }

    /**
     * Return atmosphere property path.
     *
     * @return asset path
     */
    public String atmospherePath() {
        return atmospherePath;
    }

    /**
     * Test whether clouds modulate the main light.
     *
     * @return true if enabled
     */
    public boolean cloudModulation() {
        return cloudModFlag;
    }

    /**
     * Return initial weather id.
     *
     * @return weather id
     */
    public String initialWeatherId() {
        return initialWeatherId;
    }

    /**
     * Return transition duration.
     *
     * @return duration in seconds
     */
    public float transitionSec() {
        return transitionSec;
    }

    /**
     * Return weather registry path.
     *
     * @return asset path
     */
    public String weatherPath() {
        return weatherPath;
    }
}
