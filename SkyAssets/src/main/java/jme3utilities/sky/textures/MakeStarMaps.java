/*
 Copyright (c) 2013-2025 Stephen Gold

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
package jme3utilities.sky.textures;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.jme3.math.FastMath;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.math.MyMath;
import jme3utilities.sky.Constants;

/**
 * Console application to generate starry sky texture maps for use with
 * SkyMaterial and DomeMesh, based on data from a star catalog. In the resulting
 * textures, east is at the top and north is to the right.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class MakeStarMaps {
    // *************************************************************************
    // constants and loggers

    /**
     * Earth's rate of rotation (radians per sidereal hour)
     */
    final private static float radiansPerHour
            = FastMath.TWO_PI / Constants.hoursPerDay;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(MakeStarMaps.class.getName());
    /**
     * application name for the usage message
     */
    final private static String applicationName = "MakeStarMaps";
    /**
     * filesystem path to the input file, an ASCII version of version 5 of the
     * Yale Bright Star Catalog, which may be downloaded from
     * http://tdc-www.harvard.edu/catalogs/bsc5.html
     */
    final private static String catalogFilePath = "src/main/resources/bsc5.dat";
    /**
     * English names for the faces of a cube, in the order expected by
     * jme3utilities.MyAsset#createStarMap()
     */
    final private static String[] faceName = {
        "right", "left", "top", "bottom", "front", "back"
    };
    /**
     * filesystem path to the output directory/folder
     */
    final private static String outputDirPath
            = "../SkyLibrary/src/main/resources/Textures/skies/star-maps";
    // *************************************************************************
    // fields

    /**
     * true means just display the usage message; false means run the
     * application
     */
    @Parameter(names = {"-h", "-u", "--help", "--usage"}, help = true,
            description = "display this usage message")
    private static boolean usageOnly = false;
    /**
     * true &rarr; generate textures for a cube; false &rarr; for a dome
     */
    @Parameter(names = {"-c", "--cube"}, description = "generate for a cube")
    private static boolean forCube = false;
    /**
     * stars read from the catalog
     */
    final private static Collection<Star> stars = new TreeSet<>();
    /**
     * name of preset
     */
    @Parameter(names = {"-p", "--preset"}, description = "specify preset")
    private static String presetName = "all";
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private MakeStarMaps() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the MakeStarMaps application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        // Mute the chatty loggers found in some imported packages.
        Heart.setLoggingLevels(Level.WARNING);

        // Instantiate the application.
        MakeStarMaps application = new MakeStarMaps();

        // Parse the command-line arguments.
        JCommander jCommander = new JCommander(application);
        jCommander.parse(arguments);
        jCommander.setProgramName(applicationName);
        if (usageOnly) {
            jCommander.usage();
            return;
        }
        if (!"all".equals(presetName)) {
            StarMapPreset preset = StarMapPreset.fromDescription(presetName);
            if (preset == null) { // invalid preset name
                jCommander.usage();
                return;
            }
        }

        // Log the working directory.
        String userDir = System.getProperty("user.dir");
        logger.log(Level.INFO, "working directory is {0}",
                MyString.quote(userDir));

        // Read the star catalog.
        readCatalog();
        if (stars.isEmpty()) {
            return;
        }

        // Generate texture maps.
        if ("all".equals(presetName)) {
            for (StarMapPreset preset : StarMapPreset.values()) {
                generateMap(preset);
            }

        } else {
            StarMapPreset preset = StarMapPreset.fromDescription(presetName);
            generateMap(preset);
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Generate starry sky texture map(s) for the specified preset.
     *
     * @param preset map preset to generate (not null)
     */
    private static void generateMap(StarMapPreset preset) {
        assert preset != null;

        float latitude = preset.latitude();
        logger.log(Level.FINE, "latitude is {0} degrees",
                MyMath.toDegrees(latitude));

        float siderealHour = preset.hour();
        logger.log(Level.FINE, "sidereal time is {0} hours", siderealHour);

        int textureSize = preset.textureSize();
        logger.log(Level.FINE, "resolution is {0} pixels", textureSize);

        // Convert the sidereal time from hours to radians.
        float siderealTime = siderealHour * radiansPerHour;

        if (forCube) { // Generate 6 texture maps for a cube.
            RenderedImage[] images = StarMapRenderer.generateCubeMap(
                    stars, latitude, siderealTime, textureSize);
            assert images.length == 6 : images.length;
            for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
                String filePath = String.format("%s/%s/%s_%s%d.png",
                        outputDirPath, preset.textureFileName(),
                        preset.textureFileName(), faceName[faceIndex],
                        faceIndex + 1);
                try {
                    Heart.writeImage(filePath, images[faceIndex]);
                } catch (IOException exception) {
                    // ignored
                }
            }

        } else { // Generate a texture map for a dome.
            RenderedImage image = StarMapRenderer.generateDomeMap(
                    stars, latitude, siderealTime, textureSize);
            String filePath = String.format("%s/%s.png", outputDirPath,
                    preset.textureFileName());
            try {
                Heart.writeImage(filePath, image);
            } catch (IOException exception) {
                // ignored
            }
        }
    }

    /**
     * Read the star catalog and add each valid star to the collection.
     */
    private static void readCatalog() {
        stars.clear();
        stars.addAll(BrightStarCatalogReader.read(catalogFilePath));
    }

}
