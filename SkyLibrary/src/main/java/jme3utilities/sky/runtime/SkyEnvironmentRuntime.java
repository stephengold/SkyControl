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

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import jme3utilities.Validate;
import jme3utilities.sky.cloud.SkyCloudPreset;
import jme3utilities.sky.cloud.SkyCloudPresetDefinition;

/**
 * Game-facing sky environment runtime.
 * <p>
 * This object bridges existing visual sky controls with world simulation code:
 * weather changes still drive cloud presets, while games can read visibility,
 * precipitation, wind, light levels, and snapshots. Weather changes are also
 * published as typed events so gameplay, AI, fog-of-war, UI, and audio systems
 * can subscribe to exactly the states they care about.
 *
 * @author Take Some
 */
final public class SkyEnvironmentRuntime {
    /** World clock facade. */
    final private SkyWorldClock clock;
    /** Optional weather sink implemented by the visual sky control. */
    final private WeatherApplier weatherApplier;
    /** Weather subscription registry and dispatcher. */
    final private SkyWeatherSubscriptionRegistry weatherSubscriptions
            = new SkyWeatherSubscriptionRegistry();
    /** Latest lighting output. */
    private SkyLightingSnapshot lightingSnapshot = SkyLightingSnapshot.empty();
    /** Current weather state. */
    private SkyWeatherState weatherState = SkyWeatherState.fair();

    /**
     * Instantiate a standalone environment runtime.
     */
    public SkyEnvironmentRuntime() {
        this(null, null);
    }

    /**
     * Instantiate an environment runtime.
     *
     * @param timeApplier optional sink for clock changes
     * @param weatherApplier optional sink for weather changes
     */
    public SkyEnvironmentRuntime(SkyWorldClock.TimeApplier timeApplier,
            WeatherApplier weatherApplier) {
        this.clock = new SkyWorldClock(timeApplier);
        this.weatherApplier = weatherApplier;
    }

    /**
     * Return average ambient light level.
     *
     * @return average ambient channel intensity
     */
    public float ambientLightLevel() {
        ColorRGBA ambient = lightingSnapshot.ambientColor(null);
        float result = (ambient.r + ambient.g + ambient.b) / 3f;
        return result;
    }

    /**
     * Remove all weather subscriptions.
     */
    public void clearWeatherSubscriptions() {
        weatherSubscriptions.clear();
    }

    /**
     * Access the world clock facade.
     *
     * @return pre-existing clock
     */
    public SkyWorldClock clock() {
        return clock;
    }

    /**
     * Test whether the current lighting state is night-like.
     *
     * @return true if the sun is not above the horizon
     */
    public boolean isNight() {
        return !lightingSnapshot.sunUp();
    }

    /**
     * Test whether the current weather is storm-like.
     *
     * @return true if storm-like
     */
    public boolean isStorm() {
        return weatherState.isStorm();
    }

    /**
     * Copy the latest lighting snapshot.
     *
     * @return copied lighting snapshot
     */
    public SkyLightingSnapshot lighting() {
        return lightingSnapshot.copy();
    }

    /**
     * Copy the main light direction.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return copied direction
     */
    public Vector3f mainLightDirection(Vector3f storeResult) {
        return lightingSnapshot.mainDirection(storeResult);
    }

    /**
     * Return precipitation intensity.
     *
     * @return precipitation fraction
     */
    public float precipitation() {
        return weatherState.precipitation();
    }

    /**
     * Remove all subscriptions owned by the specified listener.
     *
     * @param listener listener to remove (not null)
     * @return number of removed subscriptions
     */
    public int removeWeatherListener(SkyWeatherListener listener) {
        Validate.nonNull(listener, "listener");

        int result = weatherSubscriptions.removeListener(listener);
        return result;
    }

    /**
     * Restore weather state without applying a cloud transition.
     * <p>
     * This is used when cloning or loading runtime state that has already been
     * applied to cloud layers.
     *
     * @param state weather state to restore (not null, unaffected)
     */
    public void restoreWeather(SkyWeatherState state) {
        Validate.nonNull(state, "state");

        SkyWeatherState previous = weatherState.copy();
        this.weatherState = state.copy();
        publishWeatherChange(previous, weatherState, 0f,
                SkyWeatherChangeSource.RESTORE);
    }

    /**
     * Change weather using a built-in cloud preset.
     *
     * @param preset target cloud preset (not null)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void setWeather(SkyCloudPreset preset, float seconds) {
        Validate.nonNull(preset, "preset");
        Validate.nonNegative(seconds, "seconds");

        SkyWeatherState previous = weatherState.copy();
        this.weatherState = SkyWeatherState.fromPreset(preset);
        if (weatherApplier != null) {
            weatherApplier.applyWeather(preset, seconds);
        }
        publishWeatherChange(previous, weatherState, seconds,
                SkyWeatherChangeSource.PRESET);
    }

    /**
     * Change weather using a data-driven cloud preset definition.
     *
     * @param definition target preset definition (not null, unaffected)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void setWeather(SkyCloudPresetDefinition definition,
            float seconds) {
        Validate.nonNull(definition, "definition");
        Validate.nonNegative(seconds, "seconds");

        SkyWeatherState previous = weatherState.copy();
        this.weatherState = SkyWeatherState.fromDefinition(definition);
        if (weatherApplier != null) {
            weatherApplier.applyWeather(definition, seconds);
        }
        publishWeatherChange(previous, weatherState, seconds,
                SkyWeatherChangeSource.DEFINITION);
    }

    /**
     * Change weather state.
     *
     * @param state target state (not null, unaffected)
     * @param seconds transition duration in seconds (&ge;0)
     */
    public void setWeather(SkyWeatherState state, float seconds) {
        Validate.nonNull(state, "state");
        Validate.nonNegative(seconds, "seconds");

        SkyWeatherState previous = weatherState.copy();
        this.weatherState = state.copy();
        if (weatherApplier != null && state.cloudPreset() != null) {
            weatherApplier.applyWeather(state.cloudPreset(), seconds);
        }
        publishWeatherChange(previous, weatherState, seconds,
                SkyWeatherChangeSource.STATE);
    }

    /**
     * Create a full environment snapshot.
     *
     * @return new snapshot
     */
    public SkyEnvironmentSnapshot snapshot() {
        SkyEnvironmentSnapshot result = new SkyEnvironmentSnapshot(
                clock.timeOfDayHours(), weatherState, lightingSnapshot);
        return result;
    }

    /**
     * Subscribe to every weather-state change.
     *
     * @param listener callback (not null)
     * @return subscription handle
     */
    public SkyWeatherSubscription subscribeWeather(
            SkyWeatherListener listener) {
        SkyWeatherSubscription result = subscribeWeather(
                SkyWeatherFilters.any(), listener, false);
        return result;
    }

    /**
     * Subscribe to every weather-state change.
     *
     * @param listener callback (not null)
     * @param notifyCurrent true to immediately replay current weather if it
     * matches
     * @return subscription handle
     */
    public SkyWeatherSubscription subscribeWeather(
            SkyWeatherListener listener, boolean notifyCurrent) {
        SkyWeatherSubscription result = subscribeWeather(
                SkyWeatherFilters.any(), listener, notifyCurrent);
        return result;
    }

    /**
     * Subscribe to a stable weather id, case-insensitively.
     *
     * @param weatherId weather id (not null, not empty)
     * @param listener callback (not null)
     * @return subscription handle
     */
    public SkyWeatherSubscription subscribeWeather(String weatherId,
            SkyWeatherListener listener) {
        SkyWeatherSubscription result = subscribeWeather(
                SkyWeatherFilters.id(weatherId), listener, false);
        return result;
    }

    /**
     * Subscribe to a stable weather id, case-insensitively.
     *
     * @param weatherId weather id (not null, not empty)
     * @param listener callback (not null)
     * @param notifyCurrent true to immediately replay current weather if it
     * matches
     * @return subscription handle
     */
    public SkyWeatherSubscription subscribeWeather(String weatherId,
            SkyWeatherListener listener, boolean notifyCurrent) {
        SkyWeatherSubscription result = subscribeWeather(
                SkyWeatherFilters.id(weatherId), listener, notifyCurrent);
        return result;
    }

    /**
     * Subscribe to weather states matching the specified filter.
     *
     * @param filter filter to apply to new/current state (not null)
     * @param listener callback (not null)
     * @return subscription handle
     */
    public SkyWeatherSubscription subscribeWeather(SkyWeatherFilter filter,
            SkyWeatherListener listener) {
        SkyWeatherSubscription result = subscribeWeather(
                filter, listener, false);
        return result;
    }

    /**
     * Subscribe to weather states matching the specified filter.
     *
     * @param filter filter to apply to new/current state (not null)
     * @param listener callback (not null)
     * @param notifyCurrent true to immediately replay current weather if it
     * matches
     * @return subscription handle
     */
    public SkyWeatherSubscription subscribeWeather(SkyWeatherFilter filter,
            SkyWeatherListener listener, boolean notifyCurrent) {
        Validate.nonNull(filter, "filter");
        Validate.nonNull(listener, "listener");

        SkyWeatherSubscription result = weatherSubscriptions.subscribe(
                this, filter, listener, weatherState, notifyCurrent);
        return result;
    }

    /**
     * Copy the main light direction as a sun-direction proxy.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return copied direction
     */
    public Vector3f sunDirection(Vector3f storeResult) {
        return mainLightDirection(storeResult);
    }

    /**
     * Unsubscribe a weather subscription.
     *
     * @param subscription subscription handle (not null)
     * @return true if an active subscription was removed
     */
    public boolean unsubscribeWeather(
            SkyWeatherSubscription subscription) {
        Validate.nonNull(subscription, "subscription");
        if (subscription.owner() != this) {
            return false;
        }

        boolean result = weatherSubscriptions.unsubscribe(subscription);
        return result;
    }

    /**
     * Update the latest lighting snapshot.
     *
     * @param snapshot new lighting state (not null, unaffected)
     */
    public void updateLighting(SkyLightingSnapshot snapshot) {
        Validate.nonNull(snapshot, "snapshot");

        this.lightingSnapshot = snapshot.copy();
    }

    /**
     * Return visibility multiplier.
     *
     * @return visibility fraction
     */
    public float visibility() {
        return weatherState.visibility();
    }

    /**
     * Copy current weather state.
     *
     * @return copied weather state
     */
    public SkyWeatherState weather() {
        return weatherState.copy();
    }

    /**
     * Return the number of active weather subscriptions.
     *
     * @return subscription count
     */
    public int weatherSubscriptionCount() {
        return weatherSubscriptions.size();
    }

    /**
     * Return wind intensity.
     *
     * @return wind strength fraction
     */
    public float windStrength() {
        return weatherState.windStrength();
    }

    /**
     * Publish a weather-state change.
     *
     * @param previous previous state
     * @param current current state
     * @param seconds requested transition duration
     * @param source source category
     */
    private void publishWeatherChange(SkyWeatherState previous,
            SkyWeatherState current, float seconds,
            SkyWeatherChangeSource source) {
        weatherSubscriptions.publish(previous, current, seconds, source);
    }

    /**
     * Sink for applying visual cloud preset changes.
     */
    public interface WeatherApplier {
        /**
         * Apply cloud preset weather to the visual sky.
         *
         * @param preset target cloud preset
         * @param seconds transition duration in seconds
         */
        void applyWeather(SkyCloudPreset preset, float seconds);

        /**
         * Apply data-driven cloud preset weather to the visual sky.
         *
         * @param definition target preset definition
         * @param seconds transition duration in seconds
         */
        void applyWeather(SkyCloudPresetDefinition definition, float seconds);
    }
}
