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

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry and dispatcher for game-facing weather subscriptions.
 *
 * @author Take Some
 */
final class SkyWeatherSubscriptionRegistry {
    /** Message logger for this class. */
    final private static Logger logger
            = Logger.getLogger(SkyWeatherSubscriptionRegistry.class.getName());

    /** Active weather subscriptions. */
    final private CopyOnWriteArrayList<SkyWeatherSubscription> subscriptions
            = new CopyOnWriteArrayList<SkyWeatherSubscription>();

    /** Runtime-local event sequence counter. */
    private long eventSequence = 0L;

    /**
     * Remove all subscriptions.
     *
     * @return number of removed subscriptions
     */
    int clear() {
        for (SkyWeatherSubscription subscription : subscriptions) {
            subscription.markCancelled();
        }
        int removed = subscriptions.size();
        subscriptions.clear();
        logger.log(Level.FINE,
                "sky weather subscriptions cleared: removed={0}", removed);
        return removed;
    }

    /**
     * Publish a weather-state change.
     *
     * @param previous previous state
     * @param current current state
     * @param seconds requested transition duration
     * @param source source category
     */
    void publish(SkyWeatherState previous, SkyWeatherState current,
            float seconds, SkyWeatherChangeSource source) {
        assert previous != null;
        assert current != null;
        assert seconds >= 0f : seconds;
        assert source != null;

        SkyWeatherEvent event = new SkyWeatherEvent(++eventSequence,
                previous, current, seconds, source);
        logger.log(Level.INFO, "sky weather changed: {0}", event);

        SkyWeatherState eventState = event.currentWeather();
        for (SkyWeatherSubscription subscription : subscriptions) {
            if (subscription.matches(eventState)) {
                dispatchSafely(subscription, event);
            }
        }
    }

    /**
     * Remove all subscriptions owned by the specified listener.
     *
     * @param listener listener to remove
     * @return number of removed subscriptions
     */
    int removeListener(SkyWeatherListener listener) {
        assert listener != null;

        int removed = 0;
        for (SkyWeatherSubscription subscription : subscriptions) {
            if (subscription.listener() == listener
                    && subscriptions.remove(subscription)) {
                subscription.markCancelled();
                ++removed;
            }
        }
        logger.log(Level.FINE,
                "sky weather listener removed: listener={0}, removed={1}",
                new Object[]{listener, removed});
        return removed;
    }

    /**
     * Return the number of active subscriptions.
     *
     * @return subscription count
     */
    int size() {
        return subscriptions.size();
    }

    /**
     * Add a subscription.
     *
     * @param owner owning runtime
     * @param filter subscription filter
     * @param listener callback
     * @param current current weather state
     * @param notifyCurrent true to replay the current state if it matches
     * @return new subscription
     */
    SkyWeatherSubscription subscribe(SkyEnvironmentRuntime owner,
            SkyWeatherFilter filter, SkyWeatherListener listener,
            SkyWeatherState current, boolean notifyCurrent) {
        assert owner != null;
        assert filter != null;
        assert listener != null;
        assert current != null;

        SkyWeatherSubscription result = new SkyWeatherSubscription(
                owner, filter, listener);
        subscriptions.add(result);
        logger.log(Level.FINE, "sky weather subscription added: {0}", result);
        if (notifyCurrent) {
            dispatchCurrent(result, current);
        }
        return result;
    }

    /**
     * Remove a subscription.
     *
     * @param subscription subscription to remove
     * @return true if removed
     */
    boolean unsubscribe(SkyWeatherSubscription subscription) {
        assert subscription != null;

        boolean removed = subscriptions.remove(subscription);
        if (removed) {
            subscription.markCancelled();
            logger.log(Level.FINE,
                    "sky weather subscription removed: {0}", subscription);
        }
        return removed;
    }

    /**
     * Deliver a current-state replay to a new subscription.
     *
     * @param subscription subscription to notify
     * @param current current weather state
     */
    private void dispatchCurrent(SkyWeatherSubscription subscription,
            SkyWeatherState current) {
        assert subscription != null;
        assert current != null;
        if (!subscription.matches(current)) {
            return;
        }

        SkyWeatherEvent event = new SkyWeatherEvent(++eventSequence,
                current, current, 0f, SkyWeatherChangeSource.CURRENT);
        dispatchSafely(subscription, event);
    }

    /**
     * Dispatch an event and isolate common listener failures.
     *
     * @param subscription destination subscription
     * @param event event payload
     */
    private void dispatchSafely(SkyWeatherSubscription subscription,
            SkyWeatherEvent event) {
        assert subscription != null;
        assert event != null;
        try {
            subscription.dispatch(event);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            logger.log(Level.WARNING,
                    "sky weather listener failed: subscription="
                    + subscription + ", event=" + event, exception);
        }
    }
}
