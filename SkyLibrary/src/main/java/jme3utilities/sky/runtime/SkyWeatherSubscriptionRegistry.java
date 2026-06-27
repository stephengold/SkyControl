package jme3utilities.sky.runtime;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry and dispatcher for game-facing weather subscriptions.
 */
final class SkyWeatherSubscriptionRegistry {
    final private static Logger logger
            = Logger.getLogger(SkyWeatherSubscriptionRegistry.class.getName());

    final private CopyOnWriteArrayList<SkyWeatherSubscription> subscriptions
            = new CopyOnWriteArrayList<SkyWeatherSubscription>();

    private long eventSequence = 0L;

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

    int size() {
        return subscriptions.size();
    }

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

    private void dispatchSafely(SkyWeatherSubscription subscription,
            SkyWeatherEvent event) {
        assert subscription != null;
        assert event != null;
        try {
            subscription.dispatch(event);
        } catch (RuntimeException exception) {
            logger.log(Level.WARNING,
                    "sky weather listener failed: subscription="
                    + subscription + ", event=" + event, exception);
        }
    }
}
