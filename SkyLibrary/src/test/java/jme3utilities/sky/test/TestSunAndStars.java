/*
 Copyright (c) 2013-2020, Stephen Gold
 All rights reserved.

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

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.Calendar;
import jme3utilities.math.MyMath;
import jme3utilities.sky.SunAndStars;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the SunAndStars class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestSunAndStars {
    // *************************************************************************
    // fields

    private Node node = new Node();
    // *************************************************************************
    // new methods exposed

    /**
     * Test the SunAndStars class.
     */
    @Test
    public void testSunAndStars() {
        /*
         * Verify the defaults.
         */
        SunAndStars sas = new SunAndStars();
        assertEquals(0f, 0f, 1f, sas.eastDirection(null), 0f);
        Assert.assertEquals(0f, sas.getHour(), 0f);
        Assert.assertEquals(MyMath.toRadians(51.1788f),
                sas.getObserverLatitude(), 0.0001f);
        Assert.assertEquals(0f, sas.getSolarLongitude(), 0f);
        assertEquals(1f, 0f, 0f, sas.northDirection(null), 0f);
        Assert.assertEquals(FastMath.PI, sas.siderealAngle(), 0f);
        Assert.assertEquals(12f, sas.siderealHour(), 0f);
        assertEquals(0.7791f, -0.6269f, 0f, sas.sunDirection(null), 0.0001f);
        assertEquals(0f, 1f, 0f, sas.upDirection(null), 0f);
        /*
         * Convert some interesting coordinates.
         */
        assertEquals(1f, 2f, 3f, sas.convertToWorld(1f, 2f, 3f, null), 0f);
        assertEquals(0.7791f, -0.6269f, 0f,
                sas.convertToWorld(0f, 0f, null), 0.0001f); // Pisces
        Vector3f ncpDir = sas.convertToWorld(new Vector3f(0f, 0f, 1f), null);
        assertEquals(0.6269f, 0.7791f, 0f, ncpDir, 0.0001f);
        /*
         * Apply various orientations to the test node.
         */
        final boolean invert = true;
        sas.orientEquatorialSky(node, invert);
        assertEquals(0.9019f, 0.4319f, 0f, 0f, node.getWorldRotation(),
                0.0001f);

        final boolean doNotInvert = false;
        sas.orientEquatorialSky(node, doNotInvert);
        assertEquals(-0.9019f, -0.4319f, 0f, 0f, node.getWorldRotation(),
                0.0001f);

        sas.orientStarDomes(node, null);
        assertEquals(-0.3323f, -0.9432f, 0f, 0f, node.getWorldRotation(),
                0.0001f);

        sas.orientStarDomes(null, node);
        assertEquals(-0.9432f, 0.3323f, 0f, 0f, node.getWorldRotation(),
                0.0001f);
        /*
         * Test solar midnight on some interesting dates.
         */
        sas.setSolarLongitude(Calendar.DECEMBER, 31);
        assertEquals(0f, 0f, 1f, sas.eastDirection(null), 0f);
        Assert.assertEquals(0f, sas.getHour(), 0f);
        Assert.assertEquals(MyMath.toRadians(51.1788f),
                sas.getObserverLatitude(), 0.0001f);
        Assert.assertEquals(4.91f, sas.getSolarLongitude(), 0.0005f);
        assertEquals(1f, 0f, 0f, sas.northDirection(null), 0f);
        Assert.assertEquals(1.7855f, sas.siderealAngle(), 0.0005f);
        Assert.assertEquals(6.82f, sas.siderealHour(), 0.001f);
        assertEquals(0.4729f, -0.8811f, 0f, sas.sunDirection(null), 0.0001f);
        assertEquals(0f, 1f, 0f, sas.upDirection(null), 0f);

        sas.setSolarLongitude(Calendar.JANUARY, 1);
        Assert.assertEquals(4.927f, sas.getSolarLongitude(), 0.0005f);
        Assert.assertEquals(1.804f, sas.siderealAngle(), 0.0005f);
        Assert.assertEquals(6.89f, sas.siderealHour(), 0.001f);
        assertEquals(0.4742f, -0.8804f, 0f, sas.sunDirection(null), 0.0001f);

        sas.setSolarLongitude(Calendar.FEBRUARY, 29); // leap day
        Assert.assertEquals(5.94f, sas.getSolarLongitude(), 0.0005f);
        Assert.assertEquals(2.825f, sas.siderealAngle(), 0.0005f);
        Assert.assertEquals(10.79f, sas.siderealHour(), 0.001f);
        assertEquals(0.6881f, -0.7256f, 0f, sas.sunDirection(null), 0.0001f);

        sas.setSolarLongitude(Calendar.MARCH, 1);
        Assert.assertEquals(5.957f, sas.getSolarLongitude(), 0.0005f);
        Assert.assertEquals(2.8407f, sas.siderealAngle(), 0.0005f);
        Assert.assertEquals(10.851f, sas.siderealHour(), 0.001f);
        assertEquals(0.6928f, -0.7211f, 0f, sas.sunDirection(null), 0.0001f);

        sas.setSolarLongitude(Calendar.MARCH, 20); // equinox
        Assert.assertEquals(0f, sas.getSolarLongitude(), 0.0005f);
        Assert.assertEquals(FastMath.PI, sas.siderealAngle(), 0.0005f);
        Assert.assertEquals(12f, sas.siderealHour(), 0.001f);
        assertEquals(0.7791f, -0.6269f, 0f, sas.sunDirection(null), 0.0001f);
        /*
         * Test solar midnight at some interesting latitudes.
         */
        sas.setObserverLatitude(FastMath.HALF_PI); // north pole
        assertEquals(0f, 0f, 1f, sas.eastDirection(null), 0f);
        Assert.assertEquals(0f, sas.getHour(), 0f);
        Assert.assertEquals(FastMath.HALF_PI, sas.getObserverLatitude(),
                0.0001f);
        Assert.assertEquals(0f, sas.getSolarLongitude(), 0f);
        assertEquals(1f, 0f, 0f, sas.northDirection(null), 0f);
        Assert.assertEquals(FastMath.PI, sas.siderealAngle(), 0f);
        Assert.assertEquals(12f, sas.siderealHour(), 0f);
        assertEquals(1f, 0f, 0f, sas.sunDirection(null), 0.0001f);
        assertEquals(0f, 1f, 0f, sas.upDirection(null), 0f);

        sas.setObserverLatitude(0f); // equator
        Assert.assertEquals(0f, sas.getObserverLatitude(), 0.0001f);
        assertEquals(0f, -1f, 0f, sas.sunDirection(null), 0.0001f);

        sas.setObserverLatitude(-FastMath.HALF_PI); // south pole
        Assert.assertEquals(-FastMath.HALF_PI, sas.getObserverLatitude(),
                0.0001f);
        assertEquals(-1f, 0f, 0f, sas.sunDirection(null), 0.0001f);
        /*
         * Test some interesting times of day.
         */
        sas.setHour(23f + (59f + 59f / 60f) / 60f); // one second to midnight
        assertEquals(0f, 0f, 1f, sas.eastDirection(null), 0f);
        Assert.assertEquals(23.9997f, sas.getHour(), 0.0001f);
        Assert.assertEquals(-FastMath.HALF_PI, sas.getObserverLatitude(),
                0.0001f);
        Assert.assertEquals(0f, sas.getSolarLongitude(), 0f);
        assertEquals(1f, 0f, 0f, sas.northDirection(null), 0f);
        Assert.assertEquals(3.14152f, sas.siderealAngle(), 0.00001f);
        Assert.assertEquals(11.9997f, sas.siderealHour(), 0.0001f);
        assertEquals(-1f, 0f, 0f, sas.sunDirection(null), 0.0001f);
        assertEquals(0f, 1f, 0f, sas.upDirection(null), 0f);

        sas.setHour(6f); // 6 a.m.
        Assert.assertEquals(6f, sas.getHour(), 0.0001f);
        Assert.assertEquals(1.5f * FastMath.PI, sas.siderealAngle(), 0.00001f);
        Assert.assertEquals(18f, sas.siderealHour(), 0.0001f);
        assertEquals(0f, 0f, 1f, sas.sunDirection(null), 0.0001f);
        /*
         * Test some interesting world coordinate systems.
         */
        sas.setAxes(new Vector3f(0f, 1f, 0f), new Vector3f(0f, 0f, 1f)); // Zup
        assertEquals(1f, 0f, 0f, sas.eastDirection(null), 0f);
        Assert.assertEquals(6f, sas.getHour(), 0.0001f);
        Assert.assertEquals(-FastMath.HALF_PI, sas.getObserverLatitude(),
                0.0001f);
        Assert.assertEquals(0f, sas.getSolarLongitude(), 0f);
        assertEquals(0f, 1f, 0f, sas.northDirection(null), 0f);
        Assert.assertEquals(1.5f * FastMath.PI, sas.siderealAngle(), 0.00001f);
        Assert.assertEquals(18f, sas.siderealHour(), 0.0001f);
        assertEquals(1f, 0f, 0f, sas.sunDirection(null), 0.0001f);
        assertEquals(0f, 0f, 1f, sas.upDirection(null), 0f);

        sas.setAxes(new Vector3f(-1f, 0f, 0f),
                new Vector3f(0f, -1f, 0f)); // (south, down, east)
        assertEquals(0f, 0f, 1f, sas.eastDirection(null), 0f);
        assertEquals(-1f, 0f, 0f, sas.northDirection(null), 0f);
        assertEquals(0f, 0f, 1f, sas.sunDirection(null), 0.0001f);
        assertEquals(0f, -1f, 0f, sas.upDirection(null), 0f);
        /*
         * Test cloning.
         */
        SunAndStars copy;
        try {
            copy = sas.clone();
        } catch (CloneNotSupportedException exception) {
            Assert.fail("A CloneNotSupportedException was thrown.");
        }
        // TODO verify the copy
    }
    // *************************************************************************
    // private methods

    private void assertEquals(float x, float y, float z, float w,
            Quaternion quaternion, float tolerance) {
        Assert.assertEquals(x, quaternion.getX(), tolerance);
        Assert.assertEquals(y, quaternion.getY(), tolerance);
        Assert.assertEquals(z, quaternion.getZ(), tolerance);
        Assert.assertEquals(w, quaternion.getW(), tolerance);
    }

    private void assertEquals(float x, float y, float z, Vector3f vector,
            float tolerance) {
        Assert.assertEquals(x, vector.x, tolerance);
        Assert.assertEquals(y, vector.y, tolerance);
        Assert.assertEquals(z, vector.z, tolerance);
    }
}
