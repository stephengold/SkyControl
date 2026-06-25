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
package jme3utilities.sky.control;

import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import java.io.IOException;

/**
 * Runtime state for lunar latitude and phase angle.
 *
 * @author Take Some
 */
public final class SkyCelestialState {
    // *************************************************************************
    // fields

    /**
     * Difference in celestial longitude between the moon and the sun.
     */
    private float longitudeDifference = FastMath.PI;
    /**
     * Moon celestial latitude, measured north from the ecliptic.
     */
    private float lunarLatitude = 0f;
    // *************************************************************************
    // constructors

    /**
     * Instantiate default celestial runtime state.
     */
    public SkyCelestialState() {
        // do nothing
    }

    /**
     * Instantiate a copy of the specified state.
     *
     * @param source state to copy (not null, unaffected)
     */
    private SkyCelestialState(SkyCelestialState source) {
        assert source != null;

        this.longitudeDifference = source.longitudeDifference;
        this.lunarLatitude = source.lunarLatitude;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Create a copy of this state.
     *
     * @return a new instance
     */
    public SkyCelestialState copy() {
        SkyCelestialState result = new SkyCelestialState(this);
        return result;
    }

    /**
     * Compute the contribution of the moon to nighttime illumination.
     *
     * @return fraction (&le;1, &ge;0)
     */
    public float moonIllumination() {
        float fullAngle = FastMath.abs(longitudeDifference - FastMath.PI);
        if (lunarLatitude != 0f) {
            float cos = FastMath.cos(fullAngle) * FastMath.cos(lunarLatitude);
            fullAngle = FastMath.acos(cos);
        }
        assert fullAngle >= 0f : fullAngle;
        assert fullAngle <= FastMath.PI : fullAngle;

        float result = 1f - FastMath.saturate(fullAngle * 0.6f);

        assert result >= 0f : result;
        assert result <= 1f : result;
        return result;
    }

    /**
     * Read celestial state from an input capsule.
     *
     * @param capsule input capsule (not null)
     * @return a new instance
     * @throws IOException from capsule
     */
    public static SkyCelestialState read(InputCapsule capsule)
            throws IOException {
        assert capsule != null;

        SkyCelestialState result = new SkyCelestialState();
        result.lunarLatitude = capsule.readFloat("lunarLatitude", 0f);
        result.longitudeDifference
                = capsule.readFloat("phaseAngle", FastMath.PI);

        return result;
    }

    /**
     * Return the lunar phase angle relative to the sun.
     *
     * @return radians east of the sun
     */
    public float longitudeDifference() {
        return longitudeDifference;
    }

    /**
     * Return the lunar latitude.
     *
     * @return radians north of the ecliptic
     */
    public float lunarLatitude() {
        return lunarLatitude;
    }

    /**
     * Alter the difference in celestial longitude.
     *
     * @param longitudeDifference radians east of the sun
     */
    public void setLongitudeDifference(float longitudeDifference) {
        this.longitudeDifference = longitudeDifference;
    }

    /**
     * Alter the lunar latitude and phase angle.
     *
     * @param longitudeDifference radians east of the sun
     * @param lunarLatitude radians north of the ecliptic
     */
    public void setPhase(float longitudeDifference, float lunarLatitude) {
        this.longitudeDifference = longitudeDifference;
        this.lunarLatitude = lunarLatitude;
    }

    /**
     * Serialize celestial state to an output capsule.
     *
     * @param capsule output capsule (not null)
     * @throws IOException from capsule
     */
    public void write(OutputCapsule capsule) throws IOException {
        assert capsule != null;

        capsule.write(lunarLatitude, "lunarLatitude", 0f);
        capsule.write(longitudeDifference, "phaseAngle", FastMath.PI);
    }
}
