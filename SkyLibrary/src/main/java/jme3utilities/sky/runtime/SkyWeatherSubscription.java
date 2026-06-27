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
 * Handle returned by the weather subscription API.
 *
 * @author Take Some
 */
final public class SkyWeatherSubscription {
    /** Subscriber filter. */
    final private SkyWeatherFilter filter;
    /** Subscriber callback. */
    final private SkyWeatherListener listener;
    /** Owning runtime. */
    final private SkyEnvironmentRuntime owner;
    /** True while the subscription can receive events. */
    private boolean active = true;

    /**
     * Instantiate a subscription.
     *
     * @param owner owning runtime (not null)
     * @param filter subscription filter (not null)
     * @param listener callback (not null)
     */
    SkyWeatherSubscription(SkyEnvironmentRuntime owner,
            SkyWeatherFilter filter, SkyWeatherListener listener) {
        Validate.nonNull(owner, "owner");
        Validate.nonNull(filter, "filter");
        Validate.nonNull(listener, "listener");

        this.owner = owner;
        this.filter = filter;
        this.listener = listener;
    }

    /**
     * Cancel this subscription.
     *
     * @return true if this call removed an active subscription
     */
    public boolean cancel() {
        boolean result = owner.unsubscribeWeather(this);
        return result;
    }

    /**
     * Test whether this subscription is still active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }

    /** Dispatch an event to the listener. */
    void dispatch(SkyWeatherEvent event) {
        assert event != null;
        listener.onWeatherChanged(event);
    }

    /** Return the listener. */
    SkyWeatherListener listener() {
        return listener;
    }

    /** Mark the subscription as cancelled. */
    void markCancelled() {
        active = false;
    }

    /** Test whether a state matches this subscription. */
    boolean matches(SkyWeatherState state) {
        assert state != null;
        return active && filter.matches(state);
    }

    /** Return the owning runtime. */
    SkyEnvironmentRuntime owner() {
        return owner;
    }

    @Override
    public String toString() {
        return "SkyWeatherSubscription[active=" + active
                + ", filter=" + filter + "]";
    }
}
