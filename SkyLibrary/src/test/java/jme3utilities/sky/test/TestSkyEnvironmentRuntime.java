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
import com.jme3.math.Vector3f;
import jme3utilities.sky.cloud.SkyCloudPreset;
import jme3utilities.sky.cloud.SkyCloudPresetDefinition;
import jme3utilities.sky.runtime.SkyEnvironmentRuntime;
import jme3utilities.sky.runtime.SkyEnvironmentSnapshot;
import jme3utilities.sky.runtime.SkyLightingSnapshot;
import jme3utilities.sky.runtime.SkyLightingState;
import jme3utilities.sky.runtime.SkyWeatherEvent;
import jme3utilities.sky.runtime.SkyWeatherFilters;
import jme3utilities.sky.runtime.SkyWeatherListener;
import jme3utilities.sky.runtime.SkyWeatherSubscription;
import jme3utilities.sky.runtime.SkyWorldClock;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the game-facing sky environment runtime.
 *
 * @author Take Some
 */
public class TestSkyEnvironmentRuntime {
    /**
     * Test clock, weather, lighting, and snapshot behavior.
     */
    @Test
    public void testEnvironmentRuntime() {
        final float[] appliedTime = {-1f};
        final SkyCloudPreset[] appliedPreset = new SkyCloudPreset[1];
        final float[] appliedSeconds = {-1f};

        SkyEnvironmentRuntime runtime = new SkyEnvironmentRuntime(
                new SkyWorldClock.TimeApplier() {
                    @Override
                    public void applyTimeOfDay(float timeOfDayHours) {
                        appliedTime[0] = timeOfDayHours;
                    }
                },
                new SkyEnvironmentRuntime.WeatherApplier() {
                    @Override
                    public void applyWeather(
                            SkyCloudPreset preset, float seconds) {
                        appliedPreset[0] = preset;
                        appliedSeconds[0] = seconds;
                    }

                    @Override
                    public void applyWeather(
                            SkyCloudPresetDefinition definition,
                            float seconds) {
                        appliedPreset[0] = definition.builtIn();
                        appliedSeconds[0] = seconds;
                    }
                });

        runtime.clock().setTimeOfDay(18.5f);
        Assert.assertEquals(18.5f, appliedTime[0], 0f);
        Assert.assertEquals(18.5f, runtime.clock().timeOfDayHours(), 0f);

        runtime.setWeather(SkyCloudPreset.STORM, 45f);
        Assert.assertEquals(SkyCloudPreset.STORM, appliedPreset[0]);
        Assert.assertEquals(45f, appliedSeconds[0], 0f);
        Assert.assertTrue(runtime.isStorm());
        Assert.assertEquals(0.35f, runtime.visibility(), 0.0001f);
        Assert.assertEquals(0.95f, runtime.precipitation(), 0.0001f);

        ColorRGBA ambient = new ColorRGBA(0.2f, 0.3f, 0.4f, 1f);
        ColorRGBA background = new ColorRGBA(0.1f, 0.15f, 0.2f, 1f);
        ColorRGBA main = new ColorRGBA(0.8f, 0.7f, 0.6f, 1f);
        Vector3f direction = new Vector3f(0f, 1f, 0f);
        SkyLightingState state = new SkyLightingState(
                0.7f, 0.4f, true, false);
        SkyLightingSnapshot lighting = new SkyLightingSnapshot(
                ambient, background, main, direction, state);
        runtime.updateLighting(lighting);

        ambient.set(0f, 0f, 0f, 0f);
        Assert.assertEquals(0.2f,
                runtime.lighting().ambientColor(null).r, 0.0001f);
        Assert.assertFalse(runtime.isNight());
        Assert.assertEquals((0.2f + 0.3f + 0.4f) / 3f,
                runtime.ambientLightLevel(), 0.0001f);

        SkyEnvironmentSnapshot snapshot = runtime.snapshot();
        Assert.assertEquals(18.5f, snapshot.timeOfDayHours(), 0f);
        Assert.assertEquals(SkyCloudPreset.STORM, snapshot.cloudPreset());
        Assert.assertEquals(0.9f, snapshot.windStrength(), 0.0001f);
        Assert.assertEquals(0.7f, snapshot.bloom(), 0.0001f);
        Assert.assertEquals(0.4f, snapshot.shadowIntensity(), 0.0001f);
    }

    /**
     * Test weather subscriptions and filtered delivery.
     */
    @Test
    public void testWeatherSubscriptions() {
        SkyEnvironmentRuntime runtime = new SkyEnvironmentRuntime();
        final int[] allCount = {0};
        final int[] stormCount = {0};
        final int[] rainLikeCount = {0};
        final String[] previousId = {null};
        final String[] currentId = {null};
        final float[] transitionSeconds = {-1f};

        SkyWeatherSubscription all = runtime.subscribeWeather(
                new SkyWeatherListener() {
                    @Override
                    public void onWeatherChanged(SkyWeatherEvent event) {
                        ++allCount[0];
                        previousId[0] = event.previousWeatherId();
                        currentId[0] = event.currentWeatherId();
                        transitionSeconds[0] = event.transitionSeconds();
                    }
                });
        runtime.subscribeWeather("storm", new SkyWeatherListener() {
            @Override
            public void onWeatherChanged(SkyWeatherEvent event) {
                ++stormCount[0];
            }
        });
        runtime.subscribeWeather(SkyWeatherFilters.precipitating(0.5f),
                new SkyWeatherListener() {
                    @Override
                    public void onWeatherChanged(SkyWeatherEvent event) {
                        ++rainLikeCount[0];
                    }
                });

        Assert.assertEquals(3, runtime.weatherSubscriptionCount());
        runtime.setWeather(SkyCloudPreset.STORM, 12f);
        Assert.assertEquals(1, allCount[0]);
        Assert.assertEquals(1, stormCount[0]);
        Assert.assertEquals(1, rainLikeCount[0]);
        Assert.assertEquals("fair", previousId[0]);
        Assert.assertEquals("storm", currentId[0]);
        Assert.assertEquals(12f, transitionSeconds[0], 0f);

        Assert.assertTrue(all.cancel());
        Assert.assertFalse(all.isActive());
        Assert.assertEquals(2, runtime.weatherSubscriptionCount());
        runtime.setWeather(SkyCloudPreset.FAIR, 0f);
        Assert.assertEquals(1, allCount[0]);
        Assert.assertEquals(1, stormCount[0]);
        Assert.assertEquals(1, rainLikeCount[0]);

        final int[] currentReplayCount = {0};
        runtime.subscribeWeather(SkyWeatherFilters.any(),
                new SkyWeatherListener() {
                    @Override
                    public void onWeatherChanged(SkyWeatherEvent event) {
                        if (event.isCurrentReplay()) {
                            ++currentReplayCount[0];
                        }
                    }
                }, true);
        Assert.assertEquals(1, currentReplayCount[0]);
    }
}
