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
import jme3utilities.sky.Constants;

/**
 * Small world-clock facade for driving sky time from game simulation.
 *
 * @author Take Some
 */
final public class SkyWorldClock {
    /** External sink for time-of-day changes. */
    final private TimeApplier applier;
    /** Current time of day in hours. */
    private float timeOfDayHours;

    /**
     * Instantiate a standalone clock.
     */
    public SkyWorldClock() {
        this(null);
    }

    /**
     * Instantiate a clock with a time sink.
     *
     * @param applier sink for time-of-day changes, or null
     */
    public SkyWorldClock(TimeApplier applier) {
        this.applier = applier;
        this.timeOfDayHours = 0f;
    }

    /**
     * Advance the clock by simulation seconds.
     *
     * @param seconds elapsed simulation seconds (&ge;0)
     * @param secondsPerDay simulated seconds per sky day (&gt;0)
     */
    public void advance(float seconds, float secondsPerDay) {
        Validate.nonNegative(seconds, "seconds");
        Validate.positive(secondsPerDay, "seconds per day");

        float hours = timeOfDayHours
                + seconds * Constants.hoursPerDay / secondsPerDay;
        setTimeOfDay(wrapHours(hours));
    }

    /**
     * Set the time of day.
     *
     * @param hours time of day in hours (&le;24, &ge;0)
     */
    public void setTimeOfDay(float hours) {
        Validate.inRange(hours, "time of day", 0f, Constants.hoursPerDay);

        this.timeOfDayHours = hours;
        if (applier != null) {
            applier.applyTimeOfDay(hours);
        }
    }

    /**
     * Return the time of day.
     *
     * @return hours since midnight
     */
    public float timeOfDayHours() {
        return timeOfDayHours;
    }

    /**
     * Wrap hours into the supported time-of-day interval.
     *
     * @param hours raw hours
     * @return wrapped hours
     */
    private static float wrapHours(float hours) {
        float result = hours;
        while (result < 0f) {
            result += Constants.hoursPerDay;
        }
        while (result > Constants.hoursPerDay) {
            result -= Constants.hoursPerDay;
        }
        return result;
    }

    /**
     * External sink for clock changes.
     */
    public interface TimeApplier {
        /**
         * Apply a time-of-day value.
         *
         * @param timeOfDayHours time of day in hours
         */
        void applyTimeOfDay(float timeOfDayHours);
    }
}
