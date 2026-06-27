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
 * Immutable weather-state event delivered to subscribers.
 *
 * @author Take Some
 */
final public class SkyWeatherEvent {
    /** New/current weather state. */
    final private SkyWeatherState current;
    /** Previous weather state. */
    final private SkyWeatherState previous;
    /** Monotonic runtime-local event sequence. */
    final private long sequence;
    /** Source category for the change. */
    final private SkyWeatherChangeSource source;
    /** Requested visual transition duration. */
    final private float transitionSeconds;

    /**
     * Instantiate an event.
     *
     * @param sequence monotonic runtime-local sequence number (&ge;0)
     * @param previous previous weather state (not null, unaffected)
     * @param current current weather state (not null, unaffected)
     * @param transitionSeconds requested transition duration (&ge;0)
     * @param source source category (not null)
     */
    public SkyWeatherEvent(long sequence, SkyWeatherState previous,
            SkyWeatherState current, float transitionSeconds,
            SkyWeatherChangeSource source) {
        if (sequence < 0L) {
            throw new IllegalArgumentException(
                    "sequence must be non-negative");
        }
        Validate.nonNull(previous, "previous weather");
        Validate.nonNull(current, "current weather");
        Validate.nonNegative(transitionSeconds, "seconds");
        Validate.nonNull(source, "source");

        this.sequence = sequence;
        this.previous = previous.copy();
        this.current = current.copy();
        this.transitionSeconds = transitionSeconds;
        this.source = source;
    }

    /**
     * Copy the new/current weather state.
     *
     * @return copied state
     */
    public SkyWeatherState currentWeather() {
        return current.copy();
    }

    /**
     * Return the new/current weather id.
     *
     * @return weather id
     */
    public String currentWeatherId() {
        return current.id();
    }

    /**
     * Test whether the event represents current-state replay.
     *
     * @return true for replay, false for a real state transition
     */
    public boolean isCurrentReplay() {
        return source == SkyWeatherChangeSource.CURRENT;
    }

    /**
     * Copy the previous weather state.
     *
     * @return copied state
     */
    public SkyWeatherState previousWeather() {
        return previous.copy();
    }

    /**
     * Return the previous weather id.
     *
     * @return weather id
     */
    public String previousWeatherId() {
        return previous.id();
    }

    /**
     * Return the runtime-local sequence number.
     *
     * @return sequence number
     */
    public long sequence() {
        return sequence;
    }

    /**
     * Return the source category.
     *
     * @return source category
     */
    public SkyWeatherChangeSource source() {
        return source;
    }

    /**
     * Return the requested visual transition duration.
     *
     * @return duration in seconds
     */
    public float transitionSeconds() {
        return transitionSeconds;
    }

    /**
     * Describe this weather event.
     *
     * @return description string
     */
    @Override
    public String toString() {
        return "SkyWeatherEvent[sequence=" + sequence
                + ", previous=" + previous.id()
                + ", current=" + current.id()
                + ", seconds=" + transitionSeconds
                + ", source=" + source + "]";
    }
}
