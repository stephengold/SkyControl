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
package jme3utilities.sky.config;

import jme3utilities.Validate;
import jme3utilities.math.MyMath;
import jme3utilities.sky.Constants;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.SunAndStars;

/**
 * Initial astronomical clock configuration.
 *
 * @author Take Some
 */
final public class SkyClockConfig {
    /** Time of day in hours. */
    final private float hour;
    /** Observer latitude in degrees. */
    final private float latitudeDeg;
    /** One-based Gregorian month. */
    final private int solarMonth;
    /** Gregorian day of month. */
    final private int solarDay;

    /**
     * Instantiate clock configuration.
     *
     * @param hour time of day in hours (&le;24, &ge;0)
     * @param latitudeDeg observer latitude in degrees (&le;90, &ge;-90)
     * @param solarMonth one-based Gregorian month (&le;12, &ge;1)
     * @param solarDay Gregorian day of month (&le;31, &ge;1)
     */
    public SkyClockConfig(float hour, float latitudeDeg,
            int solarMonth, int solarDay) {
        Validate.inRange(hour, "hour", 0f, Constants.hoursPerDay);
        Validate.inRange(latitudeDeg, "latitude", -90f, 90f);
        Validate.inRange(solarMonth, "month", 1, 12);
        Validate.inRange(solarDay, "day", 1, 31);

        this.hour = hour;
        this.latitudeDeg = latitudeDeg;
        this.solarMonth = solarMonth;
        this.solarDay = solarDay;
    }

    /**
     * Apply this clock configuration to a sky control.
     *
     * @param skyControl target control (not null)
     */
    public void applyTo(SkyControl skyControl) {
        Validate.nonNull(skyControl, "control");

        SunAndStars sunAndStars = skyControl.getSunAndStars();
        sunAndStars.setHour(hour);
        sunAndStars.setObserverLatitude(MyMath.toRadians(latitudeDeg));
        sunAndStars.setSolarLongitude(solarMonth - 1, solarDay);
        skyControl.environment().clock().setTimeOfDay(hour);
    }

    /**
     * Return time of day.
     *
     * @return hour of day
     */
    public float hour() {
        return hour;
    }

    /**
     * Return observer latitude in degrees.
     *
     * @return latitude in degrees
     */
    public float latitudeDeg() {
        return latitudeDeg;
    }

    /**
     * Return one-based Gregorian month.
     *
     * @return month
     */
    public int solarMonth() {
        return solarMonth;
    }

    /**
     * Return Gregorian day of month.
     *
     * @return day
     */
    public int solarDay() {
        return solarDay;
    }
}
