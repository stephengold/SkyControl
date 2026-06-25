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
package jme3utilities.sky.cloud;

/**
 * Asset paths for built-in and optional cloud textures.
 * <p>
 * jME can load DDS textures through the regular AssetManager pipeline, so the
 * preset paths intentionally point at DDS files where richer assets are used.
 *
 * @author Take Some
 */
final public class SkyCloudAssets {
    /** Classpath directory for current built-in cloud textures. */
    final public static String directory = "Textures/skies/clouds";
    /** Classpath directory for optional weather-preset cloud textures. */
    final public static String presetDirectory = directory + "/presets";

    /** Resource-side cloud weather preset registry. */
    final public static String registry
            = presetDirectory + "/cloud-weather-presets.json";

    /** Runtime Lua ABI cloud weather preset registry. */
    final public static String luaRegistry = "helix/lua/sky/weather.lua";

    /** Existing clear fallback alpha map. */
    final public static String clear = directory + "/clear.png";
    /** Existing procedural FBM alpha map. */
    final public static String fbm = directory + "/fbm.png";
    /** Existing overcast fallback alpha map. */
    final public static String overcast = directory + "/overcast.png";

    /** Optional wispy cirrocumulus alpha map. */
    final public static String wispyCirrocumulus
            = presetDirectory + "/wispy/skyhat_cirrocumulus01_ap.dds";
    /** Optional wispy cloud alpha map. */
    final public static String wisps
            = presetDirectory + "/wispy/wisps_ap.dds";
    /** Optional cloudy base alpha map. */
    final public static String cloudyBase
            = presetDirectory + "/cloudy/trialap.dds";
    /** Optional shared marble/detail alpha map. */
    final public static String marbleDetail
            = presetDirectory + "/cloudy/cloudhat_marble02_ap.dds";
    /** Optional rain alpha map. */
    final public static String rain
            = presetDirectory + "/rain/skyhat_rain02_ap.dds";
    /** Optional storm alpha map. */
    final public static String storm
            = presetDirectory + "/storm/stormclouds_ap.dds";
    /** Optional nimbus alpha map. */
    final public static String nimbus
            = presetDirectory + "/nimbus/final_nimbusclouds_ap.dds";


    /** Optional wispy cirrocumulus normal map. */
    final public static String wispyCirrocumulusNormal
            = presetDirectory + "/wispy/skyhat_cirrocumulus01_nrm.dds";
    /** Optional wispy cloud normal map. */
    final public static String wispsNormal
            = presetDirectory + "/wispy/wisps_nrm.dds";
    /** Optional cloudy base normal map. */
    final public static String cloudyBaseNormal
            = presetDirectory + "/cloudy/trialn.dds";
    /** Optional shared detail normal map. */
    final public static String cloudyDetailNormal
            = presetDirectory + "/cloudy/detail1_nrm.dds";
    /** Optional rain normal map. */
    final public static String rainNormal
            = presetDirectory + "/rain/skyhat_rain02_n2.dds";
    /** Optional storm cloud normal map. */
    final public static String stormNormal
            = presetDirectory + "/storm/stormclouds_nrm.dds";
    /** Optional storm rain normal map. */
    final public static String stormRainNormal
            = presetDirectory + "/storm/skyhat_rain02_n2.dds";
    /** Optional nimbus normal map. */
    final public static String nimbusNormal
            = presetDirectory + "/nimbus/final_nimbusclouds_n.dds";

    /** Hidden constructor. */
    private SkyCloudAssets() {
        // do nothing
    }
}
