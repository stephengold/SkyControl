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

import com.jme3.math.ColorRGBA;
import jme3utilities.sky.SkyAtmosphere;
import jme3utilities.sky.atmosphere.SkyGradientStyle;
import jme3utilities.sky.atmosphere.SkyLightingModel;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test atmospheric, sun, and moon gradient math.
 *
 * @author Take Some
 */
public class TestSkyGradientModel {
    /**
     * Verify horizon weighting peaks at the horizon.
     */
    @Test
    public void testHorizonWeight() {
        SkyAtmosphere atmosphere = new SkyAtmosphere();
        float limit = atmosphere.getTwilightLimit();

        Assert.assertEquals(1f,
                SkyLightingModel.horizonWeight(0f, limit), 0f);
        Assert.assertEquals(0f,
                SkyLightingModel.horizonWeight(limit, limit), 0f);
        Assert.assertEquals(0f,
                SkyLightingModel.horizonWeight(-limit, limit), 0f);
    }

    /**
     * Verify low sun is warmer than high sun.
     */
    @Test
    public void testSunsetWarmth() {
        SkyAtmosphere atmosphere = new SkyAtmosphere();
        ColorRGBA high = SkyLightingModel.daylightColor(atmosphere, 1f);
        ColorRGBA horizon = SkyLightingModel.daylightColor(atmosphere, 0.01f);

        Assert.assertTrue(warmth(horizon) > warmth(high));
    }

    /**
     * Verify sun and moon glow gradients diverge from source colors.
     */
    @Test
    public void testObjectGlow() {
        SkyAtmosphere atmosphere = new SkyAtmosphere();
        ColorRGBA sunColor = SkyLightingModel.daylightColor(atmosphere, 0f);
        ColorRGBA sunGlow = SkyLightingModel.sunGlowColor(atmosphere, 0f);
        ColorRGBA highMoon = SkyLightingModel.lunarColor(atmosphere, 1f);
        ColorRGBA lowMoon = SkyLightingModel.lunarColor(atmosphere, 0f);

        Assert.assertTrue(sunGlow.r > sunColor.r);
        Assert.assertTrue(warmth(lowMoon) > warmth(highMoon));
    }

    /**
     * Verify ABI-facing intensity controls affect output.
     */
    @Test
    public void testStyleIntensity() {
        SkyAtmosphere realistic = new SkyAtmosphere();
        realistic.setGradientStyle(SkyGradientStyle.REALISTIC);
        realistic.setSunsetIntensity(0.75f);
        SkyAtmosphere fantasy = new SkyAtmosphere();
        fantasy.setGradientStyle(SkyGradientStyle.FANTASY);
        fantasy.setSunsetIntensity(1.8f);
        fantasy.setSunHaloIntensity(1.6f);
        fantasy.setMoonHaloIntensity(1.5f);

        ColorRGBA tame = SkyLightingModel.horizonColor(realistic, 0f);
        ColorRGBA wild = SkyLightingModel.horizonColor(fantasy, 0f);
        ColorRGBA tameMoon = SkyLightingModel.moonGlowColor(realistic, 0f);
        ColorRGBA wildMoon = SkyLightingModel.moonGlowColor(fantasy, 0f);

        Assert.assertTrue(warmth(wild) > warmth(tame));
        Assert.assertTrue(wildMoon.r > tameMoon.r);
    }

    /**
     * Return a red/blue warmth ratio.
     *
     * @param color color to inspect
     * @return warmth ratio
     */
    private static float warmth(ColorRGBA color) {
        float result = color.r / color.b;
        return result;
    }
}
