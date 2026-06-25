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
package jme3utilities.sky;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.ColorRGBA;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;
import java.io.IOException;
import java.util.Properties;
import jme3utilities.Validate;
import jme3utilities.sky.atmosphere.SkyAtmosphereProperties;

/**
 * Mutable atmospheric tuning profile used by SkyControl.
 * <p>
 * The profile intentionally remains renderer-independent: it does not replace
 * the existing dome shaders, but provides physically-inspired controls for
 * daylight extinction, horizon warming, twilight length, cloud brightness,
 * moonlight, starlight, ambient balance, bloom, and shadows.
 * <p>
 * All color components are linear-space multipliers as expected by jME lights.
 * Runtime code may either mutate the instance returned by
 * {@link SkyControl#getAtmosphere()} or install a copied profile by invoking
 * {@link SkyControl#setAtmosphere(SkyAtmosphere)}.
 *
 * @author Take Some
 */
public class SkyAtmosphere implements JmeCloneable, Savable {
    // *************************************************************************
    // fields

    /**
     * Strength of solar extinction through low atmosphere.
     */
    private float airMassStrength = 0.85f;
    /**
     * Multiplier applied to ambient light computed from sky/cloud color.
     */
    private float ambientScale = 1f;
    /**
     * Multiplier applied to generated bloom intensity.
     */
    private float bloomScale = 1f;
    /**
     * Height of the sun where horizon color shift fades out.
     */
    private float colorShiftAltitude = 0.35f;
    /**
     * Daytime cloud brightness multiplier.
     */
    private float cloudDayBrightness = 0.95f;
    /**
     * Moonlight boost added to nighttime cloud brightness.
     */
    private float cloudMoonBoost = 0.6f;
    /**
     * Minimum nighttime cloud brightness.
     */
    private float cloudNight = 0.22f;
    /**
     * Height of the sun where daylight is considered fully established.
     */
    private float fullDayAltitude = 0.25f;
    /**
     * Strength of low-altitude haze coloration.
     */
    private float hazeStrength = 1f;
    /**
     * Upper clamp for generated bloom intensity.
     */
    private float maxBloomIntensity = 1.7f;
    /**
     * Lower clamp for solar transmission at the horizon.
     */
    private float minSunTransmission = 0.08f;
    /**
     * Multiplier applied to computed shadow intensity.
     */
    private float shadowContrast = 1f;
    /**
     * Strength of warm color shift near sunrise and sunset.
     */
    private float sunsetWarmth = 1f;
    /**
     * Twilight reach expressed as sine of solar depression below horizon.
     */
    private float twilightLimit = 0.12f;
    /**
     * Full-moon light color.
     */
    private ColorRGBA moonLight
            = new ColorRGBA(0.25f, 0.28f, 0.42f, Constants.alphaMax);
    /**
     * Moonless-night light color.
     */
    private ColorRGBA starLight
            = new ColorRGBA(0.015f, 0.018f, 0.025f, Constants.alphaMax);
    /**
     * Full sunlight color before atmospheric extinction.
     */
    private ColorRGBA sunLight
            = new ColorRGBA(0.95f, 0.92f, 0.82f, Constants.alphaMax);
    /**
     * Horizon color around sunrise and sunset.
     */
    private ColorRGBA twilightColor
            = new ColorRGBA(0.95f, 0.36f, 0.12f, Constants.alphaMax);
    // *************************************************************************
    // constructors

    /**
     * Instantiate an Earth-like atmospheric profile.
     */
    public SkyAtmosphere() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Apply overrides from Java properties.
     * <p>
     * Recognized color keys use comma-separated components: r,g,b or r,g,b,a.
     * Recognized keys are: airMassStrength, ambientScale, bloomScale,
     * colorShiftAltitude, cloudDayBrightness, cloudMoonBoost,
     * cloudNight, fullDayAltitude, hazeStrength, maxBloomIntensity,
     * minSunTransmission, shadowContrast, sunsetWarmth, twilightLimit,
     * moonLight, starLight, sunLight, and twilightColor.
     *
     * @param properties source properties (not null)
     */
    public void apply(Properties properties) {
        Validate.nonNull(properties, "properties");

        setAirMassStrength(SkyAtmosphereProperties.readFloat(properties,
                "airMassStrength", airMassStrength));
        setAmbientScale(SkyAtmosphereProperties.readFloat(
                properties, "ambientScale", ambientScale));
        setBloomScale(SkyAtmosphereProperties.readFloat(
                properties, "bloomScale", bloomScale));
        setColorShiftAltitude(SkyAtmosphereProperties.readFloat(properties,
                "colorShiftAltitude", colorShiftAltitude));
        setCloudDayBrightness(SkyAtmosphereProperties.readFloat(properties,
                "cloudDayBrightness", cloudDayBrightness));
        setCloudMoonBoost(SkyAtmosphereProperties.readFloat(properties,
                "cloudMoonBoost", cloudMoonBoost));
        setCloudNight(SkyAtmosphereProperties.readFloat(properties,
                "cloudNight", cloudNight));
        setFullDayAltitude(SkyAtmosphereProperties.readFloat(properties,
                "fullDayAltitude", fullDayAltitude));
        setHazeStrength(SkyAtmosphereProperties.readFloat(
                properties, "hazeStrength", hazeStrength));
        setMaxBloomIntensity(SkyAtmosphereProperties.readFloat(properties,
                "maxBloomIntensity", maxBloomIntensity));
        setMinSunTransmit(SkyAtmosphereProperties.readFloat(properties,
                "minSunTransmission", minSunTransmission));
        setShadowContrast(SkyAtmosphereProperties.readFloat(properties,
                "shadowContrast", shadowContrast));
        setSunsetWarmth(SkyAtmosphereProperties.readFloat(
                properties, "sunsetWarmth", sunsetWarmth));
        setTwilightLimit(SkyAtmosphereProperties.readFloat(
                properties, "twilightLimit", twilightLimit));

        setMoonLight(SkyAtmosphereProperties.readColor(
                properties, "moonLight", moonLight));
        setStarLight(SkyAtmosphereProperties.readColor(
                properties, "starLight", starLight));
        setSunLight(SkyAtmosphereProperties.readColor(
                properties, "sunLight", sunLight));
        setTwilightColor(SkyAtmosphereProperties.readColor(
                properties, "twilightColor", twilightColor));
    }

    /**
     * Copy this profile.
     *
     * @return a new profile equivalent to this one
     */
    public SkyAtmosphere copy() {
        SkyAtmosphere result = new SkyAtmosphere();
        result.copyFrom(this);
        return result;
    }

    /**
     * Copy the moonlight color.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return copied color
     */
    public ColorRGBA copyMoonLight(ColorRGBA storeResult) {
        ColorRGBA result = copyColor(moonLight, storeResult);
        return result;
    }

    /**
     * Copy the starlight color.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return copied color
     */
    public ColorRGBA copyStarLight(ColorRGBA storeResult) {
        ColorRGBA result = copyColor(starLight, storeResult);
        return result;
    }

    /**
     * Copy the sunlight color.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return copied color
     */
    public ColorRGBA copySunLight(ColorRGBA storeResult) {
        ColorRGBA result = copyColor(sunLight, storeResult);
        return result;
    }

    /**
     * Copy the twilight color.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return copied color
     */
    public ColorRGBA copyTwilightColor(ColorRGBA storeResult) {
        ColorRGBA result = copyColor(twilightColor, storeResult);
        return result;
    }

    /**
     * Copy all tunables from another profile.
     *
     * @param source source profile (not null, unaffected)
     */
    final public void copyFrom(SkyAtmosphere source) {
        Validate.nonNull(source, "source");

        this.airMassStrength = source.airMassStrength;
        this.ambientScale = source.ambientScale;
        this.bloomScale = source.bloomScale;
        this.colorShiftAltitude = source.colorShiftAltitude;
        this.cloudDayBrightness = source.cloudDayBrightness;
        this.cloudMoonBoost = source.cloudMoonBoost;
        this.cloudNight = source.cloudNight;
        this.fullDayAltitude = source.fullDayAltitude;
        this.hazeStrength = source.hazeStrength;
        this.maxBloomIntensity = source.maxBloomIntensity;
        this.minSunTransmission = source.minSunTransmission;
        this.shadowContrast = source.shadowContrast;
        this.sunsetWarmth = source.sunsetWarmth;
        this.twilightLimit = source.twilightLimit;
        this.moonLight = source.moonLight.clone();
        this.starLight = source.starLight.clone();
        this.sunLight = source.sunLight.clone();
        this.twilightColor = source.twilightColor.clone();
    }

    /**
     * Return the strength of solar extinction through low atmosphere.
     *
     * @return extinction strength (&ge;0)
     */
    public float getAirMassStrength() {
        return airMassStrength;
    }

    /**
     * Return the ambient light scale.
     *
     * @return multiplier (&ge;0)
     */
    public float getAmbientScale() {
        return ambientScale;
    }

    /**
     * Return the bloom scale.
     *
     * @return multiplier (&ge;0)
     */
    public float getBloomScale() {
        return bloomScale;
    }

    /**
     * Return the altitude where warm horizon shift fades out.
     *
     * @return sine of solar altitude (&gt;0, &le;1)
     */
    public float getColorShiftAltitude() {
        return colorShiftAltitude;
    }

    /**
     * Return the daytime cloud brightness multiplier.
     *
     * @return multiplier (&ge;0)
     */
    public float getCloudDayBrightness() {
        return cloudDayBrightness;
    }

    /**
     * Return the moonlight boost applied to clouds at night.
     *
     * @return multiplier (&ge;0)
     */
    public float getCloudMoonBoost() {
        return cloudMoonBoost;
    }

    /**
     * Return the minimum cloud brightness at night.
     *
     * @return multiplier (&ge;0)
     */
    public float getCloudNight() {
        return cloudNight;
    }

    /**
     * Return the altitude where daylight is fully established.
     *
     * @return sine of solar altitude (&gt;0, &le;1)
     */
    public float getFullDayAltitude() {
        return fullDayAltitude;
    }

    /**
     * Return the strength of low-altitude haze coloration.
     *
     * @return fraction (&le;1, &ge;0)
     */
    public float getHazeStrength() {
        return hazeStrength;
    }

    /**
     * Return the bloom intensity clamp.
     *
     * @return intensity (&ge;0)
     */
    public float getMaxBloomIntensity() {
        return maxBloomIntensity;
    }

    /**
     * Return the lower clamp for solar transmission.
     *
     * @return fraction (&le;1, &ge;0)
     */
    public float getMinSunTransmit() {
        return minSunTransmission;
    }

    /**
     * Return the shadow contrast multiplier.
     *
     * @return multiplier (&ge;0)
     */
    public float getShadowContrast() {
        return shadowContrast;
    }

    /**
     * Return the strength of warm sunrise/sunset coloration.
     *
     * @return fraction (&le;1, &ge;0)
     */
    public float getSunsetWarmth() {
        return sunsetWarmth;
    }

    /**
     * Return twilight reach below the horizon.
     *
     * @return sine of solar depression (&gt;0, &le;1)
     */
    public float getTwilightLimit() {
        return twilightLimit;
    }

    /**
     * Alter the strength of solar extinction through low atmosphere.
     *
     * @param strength desired extinction strength (&ge;0)
     */
    public void setAirMassStrength(float strength) {
        Validate.nonNegative(strength, "strength");
        this.airMassStrength = strength;
    }

    /**
     * Alter the ambient light scale.
     *
     * @param scale desired multiplier (&ge;0)
     */
    public void setAmbientScale(float scale) {
        Validate.nonNegative(scale, "scale");
        this.ambientScale = scale;
    }

    /**
     * Alter the bloom scale.
     *
     * @param scale desired multiplier (&ge;0)
     */
    public void setBloomScale(float scale) {
        Validate.nonNegative(scale, "scale");
        this.bloomScale = scale;
    }

    /**
     * Alter the altitude where warm horizon shift fades out.
     *
     * @param altitude sine of solar altitude (&gt;0, &le;1)
     */
    public void setColorShiftAltitude(float altitude) {
        SkyAtmosphereProperties.validatePosFraction(altitude, "altitude");
        this.colorShiftAltitude = altitude;
    }

    /**
     * Alter the daytime cloud brightness multiplier.
     *
     * @param brightness desired multiplier (&ge;0)
     */
    public void setCloudDayBrightness(float brightness) {
        Validate.nonNegative(brightness, "brightness");
        this.cloudDayBrightness = brightness;
    }

    /**
     * Alter the moonlight boost applied to clouds at night.
     *
     * @param boost desired multiplier (&ge;0)
     */
    public void setCloudMoonBoost(float boost) {
        Validate.nonNegative(boost, "boost");
        this.cloudMoonBoost = boost;
    }

    /**
     * Alter the minimum cloud brightness at night.
     *
     * @param brightness desired multiplier (&ge;0)
     */
    public void setCloudNight(float brightness) {
        Validate.nonNegative(brightness, "brightness");
        this.cloudNight = brightness;
    }

    /**
     * Alter the altitude where daylight is fully established.
     *
     * @param altitude sine of solar altitude (&gt;0, &le;1)
     */
    public void setFullDayAltitude(float altitude) {
        SkyAtmosphereProperties.validatePosFraction(altitude, "altitude");
        this.fullDayAltitude = altitude;
    }

    /**
     * Alter the strength of low-altitude haze coloration.
     *
     * @param strength desired fraction (&le;1, &ge;0)
     */
    public void setHazeStrength(float strength) {
        Validate.fraction(strength, "strength");
        this.hazeStrength = strength;
    }

    /**
     * Alter the bloom intensity clamp.
     *
     * @param intensity desired intensity (&ge;0)
     */
    public void setMaxBloomIntensity(float intensity) {
        Validate.nonNegative(intensity, "intensity");
        this.maxBloomIntensity = intensity;
    }

    /**
     * Alter the lower clamp for solar transmission.
     *
     * @param fraction desired fraction (&le;1, &ge;0)
     */
    public void setMinSunTransmit(float fraction) {
        Validate.fraction(fraction, "fraction");
        this.minSunTransmission = fraction;
    }

    /**
     * Alter the full-moon light color.
     *
     * @param color desired color (not null, unaffected)
     */
    public void setMoonLight(ColorRGBA color) {
        Validate.nonNull(color, "color");
        this.moonLight = color.clone();
    }

    /**
     * Alter the shadow contrast multiplier.
     *
     * @param contrast desired multiplier (&ge;0)
     */
    public void setShadowContrast(float contrast) {
        Validate.nonNegative(contrast, "contrast");
        this.shadowContrast = contrast;
    }

    /**
     * Alter the moonless-night light color.
     *
     * @param color desired color (not null, unaffected)
     */
    public void setStarLight(ColorRGBA color) {
        Validate.nonNull(color, "color");
        this.starLight = color.clone();
    }

    /**
     * Alter full sunlight color before atmospheric extinction.
     *
     * @param color desired color (not null, unaffected)
     */
    public void setSunLight(ColorRGBA color) {
        Validate.nonNull(color, "color");
        this.sunLight = color.clone();
    }

    /**
     * Alter the strength of warm sunrise/sunset coloration.
     *
     * @param strength desired fraction (&le;1, &ge;0)
     */
    public void setSunsetWarmth(float strength) {
        Validate.fraction(strength, "strength");
        this.sunsetWarmth = strength;
    }

    /**
     * Alter twilight reach below the horizon.
     *
     * @param limit sine of solar depression (&gt;0, &le;1)
     */
    public void setTwilightLimit(float limit) {
        SkyAtmosphereProperties.validatePosFraction(limit, "limit");
        this.twilightLimit = limit;
    }

    /**
     * Alter horizon color around sunrise and sunset.
     *
     * @param color desired color (not null, unaffected)
     */
    public void setTwilightColor(ColorRGBA color) {
        Validate.nonNull(color, "color");
        this.twilightColor = color.clone();
    }
    // *************************************************************************
    // JmeCloneable methods

    /**
     * Convert this shallow-cloned profile into a deep-cloned one.
     *
     * @param cloner active cloner
     * @param original the profile from which this profile was shallow-cloned
     */
    @Override
    public void cloneFields(Cloner cloner, Object original) {
        this.moonLight = cloner.clone(moonLight);
        this.starLight = cloner.clone(starLight);
        this.sunLight = cloner.clone(sunLight);
        this.twilightColor = cloner.clone(twilightColor);
    }

    /**
     * Create a shallow clone for the JME cloner.
     *
     * @return a new instance (not null)
     */
    @Override
    public SkyAtmosphere jmeClone() {
        try {
            SkyAtmosphere clone = (SkyAtmosphere) clone();
            return clone;
        } catch (CloneNotSupportedException exception) {
            throw new RuntimeException(exception);
        }
    }
    // *************************************************************************
    // Object methods

    /**
     * Clone this profile.
     *
     * @return new profile equivalent to this one
     * @throws CloneNotSupportedException from Object.clone()
     */
    @Override
    public SkyAtmosphere clone() throws CloneNotSupportedException {
        SkyAtmosphere clone = (SkyAtmosphere) super.clone();
        clone.moonLight = moonLight.clone();
        clone.starLight = starLight.clone();
        clone.sunLight = sunLight.clone();
        clone.twilightColor = twilightColor.clone();
        return clone;
    }
    // *************************************************************************
    // Savable methods

    /**
     * De-serialize this profile, for example when loading from a J3O file.
     *
     * @param importer (not null)
     * @throws IOException from importer
     */
    @Override
    public void read(JmeImporter importer) throws IOException {
        InputCapsule capsule = importer.getCapsule(this);

        airMassStrength = capsule.readFloat("airMassStrength", 0.85f);
        ambientScale = capsule.readFloat("ambientScale", 1f);
        bloomScale = capsule.readFloat("bloomScale", 1f);
        colorShiftAltitude = capsule.readFloat("colorShiftAltitude", 0.35f);
        cloudDayBrightness = capsule.readFloat("cloudDayBrightness", 0.95f);
        cloudMoonBoost = capsule.readFloat("cloudMoonBoost", 0.6f);
        cloudNight = capsule.readFloat("cloudNight", 0.22f);
        fullDayAltitude = capsule.readFloat("fullDayAltitude", 0.25f);
        hazeStrength = capsule.readFloat("hazeStrength", 1f);
        maxBloomIntensity = capsule.readFloat("maxBloomIntensity", 1.7f);
        minSunTransmission
                = capsule.readFloat("minSunTransmission", 0.08f);
        shadowContrast = capsule.readFloat("shadowContrast", 1f);
        sunsetWarmth = capsule.readFloat("sunsetWarmth", 1f);
        twilightLimit = capsule.readFloat("twilightLimit", 0.12f);
        moonLight = (ColorRGBA) capsule.readSavable(
                "moonLight",
                new ColorRGBA(0.25f, 0.28f, 0.42f, Constants.alphaMax));
        starLight = (ColorRGBA) capsule.readSavable(
                "starLight",
                new ColorRGBA(0.015f, 0.018f, 0.025f, Constants.alphaMax));
        sunLight = (ColorRGBA) capsule.readSavable(
                "sunLight",
                new ColorRGBA(0.95f, 0.92f, 0.82f, Constants.alphaMax));
        twilightColor = (ColorRGBA) capsule.readSavable(
                "twilightColor",
                new ColorRGBA(0.95f, 0.36f, 0.12f, Constants.alphaMax));
    }

    /**
     * Serialize this profile, for example when saving to a J3O file.
     *
     * @param exporter (not null)
     * @throws IOException from exporter
     */
    @Override
    public void write(JmeExporter exporter) throws IOException {
        OutputCapsule capsule = exporter.getCapsule(this);

        capsule.write(airMassStrength, "airMassStrength", 0.85f);
        capsule.write(ambientScale, "ambientScale", 1f);
        capsule.write(bloomScale, "bloomScale", 1f);
        capsule.write(colorShiftAltitude, "colorShiftAltitude", 0.35f);
        capsule.write(cloudDayBrightness, "cloudDayBrightness", 0.95f);
        capsule.write(cloudMoonBoost, "cloudMoonBoost", 0.6f);
        capsule.write(cloudNight, "cloudNight", 0.22f);
        capsule.write(fullDayAltitude, "fullDayAltitude", 0.25f);
        capsule.write(hazeStrength, "hazeStrength", 1f);
        capsule.write(maxBloomIntensity, "maxBloomIntensity", 1.7f);
        capsule.write(minSunTransmission, "minSunTransmission", 0.08f);
        capsule.write(shadowContrast, "shadowContrast", 1f);
        capsule.write(sunsetWarmth, "sunsetWarmth", 1f);
        capsule.write(twilightLimit, "twilightLimit", 0.12f);
        capsule.write(moonLight, "moonLight", null);
        capsule.write(starLight, "starLight", null);
        capsule.write(sunLight, "sunLight", null);
        capsule.write(twilightColor, "twilightColor", null);
    }
    // *************************************************************************
    // private methods

    /**
     * Copy a color.
     *
     * @param source source color (not null)
     * @param storeResult storage for the result (modified if not null)
     * @return copied color
     */
    private static ColorRGBA copyColor(
            ColorRGBA source, ColorRGBA storeResult) {
        ColorRGBA result = (storeResult == null)
                ? new ColorRGBA() : storeResult;
        result.set(source);
        return result;
    }


}
