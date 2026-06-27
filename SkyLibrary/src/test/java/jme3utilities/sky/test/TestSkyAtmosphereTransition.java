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
package jme3utilities.sky.test;

import jme3utilities.sky.SkyAtmosphere;
import jme3utilities.sky.atmosphere.SkyAtmosphereTransitionRuntime;
import jme3utilities.sky.atmosphere.SkyGradientStyle;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test runtime atmosphere gradient transitions.
 *
 * @author Take Some
 */
public class TestSkyAtmosphereTransition {
    /**
     * Verify gradient preset transitions interpolate runtime scale values.
     */
    @Test
    public void testGradientTransition() {
        SkyAtmosphere atmosphere = new SkyAtmosphere();
        atmosphere.setGradientStyle(SkyGradientStyle.REALISTIC);
        atmosphere.setSunsetIntensity(0.75f);
        atmosphere.setSunHaloIntensity(0.75f);
        atmosphere.setMoonHaloIntensity(0.65f);
        SkyAtmosphereTransitionRuntime runtime
                = new SkyAtmosphereTransitionRuntime();

        runtime.transitionStyle(atmosphere, SkyGradientStyle.FANTASY, 2f);
        Assert.assertTrue(runtime.isActive());
        Assert.assertEquals(SkyGradientStyle.FANTASY,
                atmosphere.getGradientStyle());
        Assert.assertEquals(SkyGradientStyle.REALISTIC.horizonScale(),
                atmosphere.getHorizonScale(), 0.0001f);

        runtime.update(atmosphere, 1f);
        float expectedHorizon = midpoint(
                SkyGradientStyle.REALISTIC.horizonScale(),
                SkyGradientStyle.FANTASY.horizonScale());
        Assert.assertEquals(expectedHorizon,
                atmosphere.getHorizonScale(), 0.0001f);
        Assert.assertEquals(1.275f,
                atmosphere.getSunsetIntensity(), 0.0001f);

        runtime.update(atmosphere, 1f);
        Assert.assertFalse(runtime.isActive());
        Assert.assertEquals(SkyGradientStyle.FANTASY.horizonScale(),
                atmosphere.getHorizonScale(), 0.0001f);
        Assert.assertEquals(SkyGradientStyle.FANTASY.presetMoonHalo(),
                atmosphere.getMoonHaloIntensity(), 0.0001f);
    }

    /**
     * Verify individual intensity transitions keep style scales untouched.
     */
    @Test
    public void testIntensityTransition() {
        SkyAtmosphere atmosphere = new SkyAtmosphere();
        SkyAtmosphereTransitionRuntime runtime
                = new SkyAtmosphereTransitionRuntime();
        float initialScale = atmosphere.getHaloScale();

        runtime.transitionMoon(atmosphere, 1.8f, 4f);
        runtime.update(atmosphere, 2f);
        Assert.assertEquals(1.4f,
                atmosphere.getMoonHaloIntensity(), 0.0001f);
        Assert.assertEquals(initialScale, atmosphere.getHaloScale(), 0.0001f);

        runtime.update(atmosphere, 2f);
        Assert.assertFalse(runtime.isActive());
        Assert.assertEquals(1.8f,
                atmosphere.getMoonHaloIntensity(), 0.0001f);
    }

    /**
     * Return midpoint of two values.
     *
     * @param a first value
     * @param b second value
     * @return midpoint
     */
    private static float midpoint(float a, float b) {
        float result = (a + b) / 2f;
        return result;
    }
}
