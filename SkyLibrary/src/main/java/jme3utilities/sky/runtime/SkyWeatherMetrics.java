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
 * Immutable game-facing weather metrics.
 *
 * @author Take Some
 */
final public class SkyWeatherMetrics {
    /** Approximate visible cloud coverage. */
    final private float cloudiness;
    /** Lightning probability/intensity hint. */
    final private float lightningChance;
    /** Precipitation intensity. */
    final private float precipitation;
    /** Visibility multiplier exposed to game simulation. */
    final private float visibility;
    /** Wind intensity. */
    final private float windStrength;

    /**
     * Instantiate weather metrics.
     *
     * @param cloudiness visible cloud coverage (&le;1, &ge;0)
     * @param visibility visibility multiplier (&le;1, &ge;0)
     * @param precipitation precipitation intensity (&le;1, &ge;0)
     * @param windStrength wind intensity (&le;1, &ge;0)
     * @param lightningChance lightning chance/intensity (&le;1, &ge;0)
     */
    public SkyWeatherMetrics(float cloudiness, float visibility,
            float precipitation, float windStrength, float lightningChance) {
        Validate.fraction(cloudiness, "cloudiness");
        Validate.fraction(visibility, "visibility");
        Validate.fraction(precipitation, "precipitation");
        Validate.fraction(windStrength, "wind strength");
        Validate.fraction(lightningChance, "lightning chance");

        this.cloudiness = cloudiness;
        this.visibility = visibility;
        this.precipitation = precipitation;
        this.windStrength = windStrength;
        this.lightningChance = lightningChance;
    }

    /**
     * Return approximate cloud coverage.
     *
     * @return cloudiness fraction
     */
    public float cloudiness() {
        return cloudiness;
    }

    /**
     * Copy this metrics object.
     *
     * @return new equivalent metrics
     */
    public SkyWeatherMetrics copy() {
        SkyWeatherMetrics result = new SkyWeatherMetrics(cloudiness,
                visibility, precipitation, windStrength, lightningChance);
        return result;
    }

    /**
     * Return lightning probability/intensity hint.
     *
     * @return lightning chance
     */
    public float lightningChance() {
        return lightningChance;
    }

    /**
     * Return precipitation intensity.
     *
     * @return precipitation fraction
     */
    public float precipitation() {
        return precipitation;
    }

    /**
     * Return visibility multiplier exposed to simulation code.
     *
     * @return visibility fraction
     */
    public float visibility() {
        return visibility;
    }

    /**
     * Return wind intensity.
     *
     * @return wind strength fraction
     */
    public float windStrength() {
        return windStrength;
    }

    @Override
    public String toString() {
        return "SkyWeatherMetrics[cloudiness=" + cloudiness
                + ", visibility=" + visibility
                + ", precipitation=" + precipitation
                + ", windStrength=" + windStrength
                + ", lightningChance=" + lightningChance + "]";
    }
}
