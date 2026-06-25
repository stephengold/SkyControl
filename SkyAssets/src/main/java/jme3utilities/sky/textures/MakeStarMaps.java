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
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyString;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyQuaternion;
import jme3utilities.math.MyVector3f;
import jme3utilities.mesh.DomeMesh;
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
     * luminosity of the faintest stars to include
     */
    final private static float luminosityCutoff = 0.1f;
    /**
     * luminosity ratio between successive stellar magnitudes (5th root of 100)
     */
    final private static float pogsonsRatio = FastMath.pow(100f, 0.2f);
    /**
     * Earth's rate of rotation (radians per sidereal hour)
     */
    final private static float radiansPerHour
            = FastMath.TWO_PI / Constants.hoursPerDay;
    /**
     * number of points per ellipse
     */
    final private static int ellipseNumPoints = 32;
    /**
     * x-coordinates used to draw an ellipse
     */
    final private static int[] ellipseXs = new int[ellipseNumPoints];
    /**
     * y-coordinates used to draw an ellipse
     */
    final private static int[] ellipseYs = new int[ellipseNumPoints];
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
     * sample dome mesh for calculating texture coordinates
     */
    final private static DomeMesh domeMesh = new DomeMesh(3, 2);
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
     * Calculate the texture coordinates of a point that lies in the specified
     * direction from the center of the cube.
     *
     * @param direction (length&gt;0, unaffected)
     * @param faceIndex (&ge;0, &lt;6) which face of the cube
     * @return a new vector, or null if direction is too far outside the face
     */
    private static Vector2f cubeUV(Vector3f direction, int faceIndex) {
        assert direction != null;
        assert !MyVector3f.isZero(direction);
        assert faceIndex >= 0 : faceIndex;
        assert faceIndex < 6 : faceIndex;

        Vector3f faceDir = MyAsset.copyFaceDirection(faceIndex);
        Vector3f norm = direction.normalize();
        float dot = faceDir.dot(norm);
        if (dot < 0.5f) { // way outside the face
            return null;
        }

        // project outward to the plane of the face
        norm.divideLocal(dot);

        // convert to texture coordinates
        Vector3f uDir = MyAsset.copyUDirection(faceIndex);
        Vector3f vDir = MyAsset.copyVDirection(faceIndex);
        float u = 0.5f * (1f + uDir.dot(norm));
        float v = 0.5f * (1f + vDir.dot(norm));
        Vector2f uv = new Vector2f(u, v);

        return uv;
    }

    /**
     * Generate 6 starry sky texture maps for a cube.
     *
     * @param latitude radians north of the equator (&le;Pi/2, &ge;-Pi/2)
     * @param siderealTime radians since sidereal midnight (&lt;2*Pi, &ge;0)
     * @param textureSize size of each texture map (pixels per side, &gt;2)
     * @return new instance
     */
    private static RenderedImage[] generateCubeMap(
            float latitude, float siderealTime, int textureSize) {
        assert latitude >= -FastMath.HALF_PI : latitude;
        assert latitude <= FastMath.HALF_PI : latitude;
        assert siderealTime >= 0f : siderealTime;
        assert siderealTime < FastMath.TWO_PI : siderealTime;
        assert textureSize > 2 : textureSize;

        // Create a blank, grayscale buffered image for each texture map.
        BufferedImage[] maps = new BufferedImage[6];
        for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
            maps[faceIndex] = new BufferedImage(
                    textureSize, textureSize, BufferedImage.TYPE_BYTE_GRAY);
        }

        // Plot individual stars on the images, starting with the faintest.
        int plotCount = 0;
        for (Star star : stars) {
            boolean success = plotStarOnCube(
                    maps, star, latitude, siderealTime, textureSize);
            if (success) {
                ++plotCount;
            }
        }
        logger.log(Level.FINE, "plotted {0} stars", plotCount);

        return maps;
    }

    /**
     * Generate a starry sky texture map for a dome.
     *
     * @param latitude radians north of the equator (&le;Pi/2, &ge;-Pi/2)
     * @param siderealTime radians since sidereal midnight (&lt;2*Pi, &ge;0)
     * @param textureSize size of the texture map (pixels per side, &gt;2)
     * @return new instance
     */
    private static RenderedImage generateDomeMap(
            float latitude, float siderealTime, int textureSize) {
        assert latitude >= -FastMath.HALF_PI : latitude;
        assert latitude <= FastMath.HALF_PI : latitude;
        assert siderealTime >= 0f : siderealTime;
        assert siderealTime < FastMath.TWO_PI : siderealTime;
        assert textureSize > 2 : textureSize;

        // Create a blank, grayscale buffered image for the texture map.
        BufferedImage map = new BufferedImage(
                textureSize, textureSize, BufferedImage.TYPE_BYTE_GRAY);

        // Plot individual stars on the image, starting with the faintest.
        int plotCount = 0;
        for (Star star : stars) {
            boolean success = plotStarOnDome(
                    map, star, latitude, siderealTime, textureSize);
            if (success) {
                ++plotCount;
            }
        }
        logger.log(Level.FINE, "plotted {0} stars", plotCount);

        return map;
    }

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
            RenderedImage[] images = generateCubeMap(latitude, siderealTime,
                    textureSize);
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
            RenderedImage image = generateDomeMap(latitude, siderealTime,
                    textureSize);
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
     * Plot a four-pointed star shape on a texture map.
     *
     * @param map texture map (not null)
     * @param luminosity star's relative luminosity (in terms of pure white
     * pixels, &le;37, &gt;0)
     * @param textureSize size of the texture map (pixels per side, &gt;2)
     * @param uv star's texture coordinates (not null)
     * @return true if the star was successfully plotted, otherwise false
     */
    private static boolean plot4PointStar(BufferedImage map, float luminosity,
            int textureSize, Vector2f uv) {
        assert luminosity > 0f : luminosity;
        assert luminosity <= 37f : luminosity;
        assert textureSize > 2 : textureSize;
        assert uv != null;
        /*
         * Convert the star's luminosity into a shape and pixel color.
         *
         * The shape must be big enough to ensure that the pixels will not be
         * oversaturated. For instance, a star with luminosity=4.1
         * must fill at least 5 pixels.
         */
        int minPixels = (int) FastMath.ceil(luminosity);
        assert minPixels >= 1 : minPixels;
        /*
         * Star shapes consist of a square portion (up to 5x5 pixels)
         * plus optional rays.  Rays are used only with odd-sized squares;
         * they add either 1 or 3 pixels to each side of the square.
         * In other words, they add either 4 or 12 pixels.
         */
        int raySize;
        int squareSize;
        if (minPixels == 1) {
            raySize = 0;
            squareSize = 1;
        } else if (minPixels <= 4) {
            raySize = 0;
            squareSize = 2;
        } else if (minPixels <= 5) {
            raySize = 1;
            squareSize = 1;
        } else if (minPixels <= 9) {
            raySize = 0;
            squareSize = 3;
        } else if (minPixels <= 13) {
            raySize = 1;
            squareSize = 3;
        } else if (minPixels <= 16) {
            raySize = 0;
            squareSize = 4;
        } else if (minPixels <= 21) {
            raySize = 3;
            squareSize = 3;
        } else if (minPixels <= 29) {
            raySize = 1;
            squareSize = 5;
        } else if (minPixels <= 37) {
            raySize = 3;
            squareSize = 5;
        } else {
            logger.log(Level.SEVERE, "no shape contains {0} pixels", minPixels);
            return false;
        }
        int numPixels = squareSize * squareSize + 4 * raySize;
        assert numPixels >= minPixels : minPixels;
        int brightness = Math.round(255f * luminosity / numPixels);
        assert brightness >= 0 : brightness;
        assert brightness <= 255 : brightness;
        // TODO tint based on spectral type
        Color color = new Color(brightness, brightness, brightness);
        /*
         * Convert the texture coordinates into (x, y) image coordinates of
         * the square's upper-left pixel.
         */
        float u = uv.x;
        float v = uv.y;
        float cornerOffset = 0.5f * (squareSize - 1);
        int x = Math.round(u * textureSize - cornerOffset);
        int y = Math.round(v * textureSize - cornerOffset);

        // Plot the star onto the texture map.
        Graphics2D graphics = map.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(x, y, squareSize, squareSize);
        if (raySize == 0) {
            return true;
        }

        assert MyMath.isOdd(squareSize) : squareSize;
        int halfSize = (squareSize - 1) / 2;
        switch (raySize) {
            case 1:
                graphics.fillRect(x - 1, y + halfSize, 1, 1);
                graphics.fillRect(x + halfSize, y - 1, 1, 1);
                graphics.fillRect(x + halfSize, y + squareSize, 1, 1);
                graphics.fillRect(x + squareSize, y + halfSize, 1, 1);
                break;

            case 3:
                graphics.fillRect(x - 1, y + halfSize - 1, 1, 3);
                graphics.fillRect(x + halfSize - 1, y - 1, 3, 1);
                graphics.fillRect(x + halfSize - 1, y + squareSize, 3, 1);
                graphics.fillRect(x + squareSize, y + halfSize - 1, 1, 3);
                break;

            default:
                assert false : raySize;
        }
        return true;
    }

    /**
     * Draw an ellipse -- a circle stretched to compensate for UV distortion
     * near the rim of the dome.
     *
     * @param map texture map (not null)
     * @param luminosity star's relative luminosity (in terms of pure white
     * pixels, &gt;0)
     * @param textureSize size of the texture map (pixels per side, &gt;2)
     * @param uv star's texture coordinates (not null)
     */
    private static void plotEllipseForDome(BufferedImage map, float luminosity,
            int textureSize, Vector2f uv) {
        assert map != null;
        assert luminosity > 0f : luminosity;
        assert textureSize > 2 : textureSize;
        assert uv != null;
        float u = uv.x;
        float v = uv.y;
        assert u >= Constants.uvMin : u;
        assert u <= Constants.uvMax : u;
        assert v >= Constants.uvMin : v;
        assert v <= Constants.uvMax : v;

        Vector2f offset = uv.subtract(Constants.topUV);
        float topDist = offset.length();
        float xDir;
        float yDir;
        if (topDist > 0f) {
            xDir = offset.x / topDist;
            yDir = offset.y / topDist;
        } else {
            xDir = 1f;
            yDir = 0f;
        }
        float stretchFactor = 1f
                + Constants.stretchCoefficient * topDist * topDist;
        float a = FastMath.sqrt(luminosity * stretchFactor / FastMath.PI);
        float b = a / stretchFactor;

        for (int i = 0; i < ellipseNumPoints; ++i) {
            float theta = FastMath.TWO_PI * i / ellipseNumPoints;
            float da = a * FastMath.cos(theta);
            float db = b * FastMath.sin(theta);
            float dx = db * xDir + da * yDir;
            float dy = db * yDir - da * xDir;
            int x = Math.round(u * textureSize + dx);
            int y = Math.round(v * textureSize + dy);
            ellipseXs[i] = x;
            ellipseYs[i] = y;
        }
        Graphics2D graphics = map.createGraphics();
        graphics.setColor(Color.WHITE); // TODO tint based on spectral type
        graphics.fillPolygon(ellipseXs, ellipseYs, ellipseNumPoints);
    }

    /**
     * Draw an ellipse -- a circle stretched to compensate for UV distortion
     * near the edges of the quad.
     *
     * @param map texture map (not null)
     * @param luminosity star's relative luminosity (&gt;0)
     * @param textureSize size of the texture map (pixels per side, &gt;2)
     * @param worldDirection the star's world coordinates (length=1)
     * @param faceIndex which face of the cube (&ge;0, &lt;6)
     */
    private static void plotEllipseForQuad(BufferedImage map, float luminosity,
            int textureSize, Vector3f worldDirection, int faceIndex) {
        assert map != null;
        assert luminosity > 0f : luminosity;
        assert textureSize > 2 : textureSize;
        assert worldDirection != null;
        assert faceIndex >= 0 : faceIndex;
        assert faceIndex < 6 : faceIndex;

        Vector3f basis1 = worldDirection.clone();
        Vector3f basis2 = new Vector3f();
        Vector3f basis3 = new Vector3f();
        MyVector3f.generateBasis(basis1, basis2, basis3);
        float numPixels = textureSize * textureSize;
        float area = luminosity / numPixels;
        float r = 1.2f * FastMath.sqrt(area);

        Vector3f p = new Vector3f();
        for (int i = 0; i < ellipseNumPoints; ++i) {
            float theta = FastMath.TWO_PI * i / ellipseNumPoints;
            float rCos = r * FastMath.cos(theta);
            float rSin = r * FastMath.sin(theta);
            p.scaleAdd(rCos, basis2, basis1);
            p.scaleAdd(rSin, basis3, p);
            Vector2f uv = cubeUV(p, faceIndex);
            if (uv == null) {
                return;
            }
            int x = Math.round(uv.x * textureSize);
            int y = Math.round(uv.y * textureSize);
            ellipseXs[i] = x;
            ellipseYs[i] = y;
        }
        Graphics2D graphics = map.createGraphics();
        graphics.setColor(Color.WHITE); // TODO tint based on spectral type
        graphics.fillPolygon(ellipseXs, ellipseYs, ellipseNumPoints);
    }

    /**
     * Plot a star's position at the specified time onto a cube.
     *
     * @param maps texture maps for 6 cube faces (not null, modified)
     * @param star star to plot (not null)
     * @param latitude radians north of the equator (&le;Pi/2, &ge;-Pi/2)
     * @param siderealTime radians since sidereal midnight (&lt;2*Pi, &ge;0)
     * @param textureSize size of the texture map (pixels per side, &gt;2)
     * @return true if the star was successfully plotted, otherwise false
     */
    private static boolean plotStarOnCube(BufferedImage[] maps, Star star,
            float latitude, float siderealTime, int textureSize) {
        assert maps != null;
        assert maps.length == 6 : maps.length;
        assert star != null;
        assert latitude >= -FastMath.HALF_PI : latitude;
        assert latitude <= FastMath.HALF_PI : latitude;
        assert siderealTime >= 0f : siderealTime;
        assert siderealTime < FastMath.TWO_PI : siderealTime;
        assert textureSize > 2 : textureSize;

        Vector3f equatorial = star.getEquatorialLocation(siderealTime);
        /*
         * Convert equatorial coordinates to world coordinates, where:
         *   +X points to the north horizon
         *   +Y points to the zenith
         *   +Z points to the east horizon
         *
         * The conversion consists of a (latitude - Pi/2) rotation about the Y
         * (east) axis followed by permutation of the axes.
         */
        float coLatitude = FastMath.HALF_PI - latitude;
        Quaternion rotation = new Quaternion();
        rotation.fromAngleNormalAxis(-coLatitude, Vector3f.UNIT_Y);
        Vector3f rotated = MyQuaternion.rotate(rotation, equatorial, null);
        assert rotated.isUnitVector() : rotated;
        Vector3f world = new Vector3f(-rotated.x, rotated.z, rotated.y);

        float apparentMagnitude = star.getApparentMagnitude();
        boolean success = plotStarOnCube(maps, apparentMagnitude,
                textureSize, world);

        return success;
    }

    /**
     * Plot a star onto a texture map for a cube.
     *
     * @param maps texture maps for 6 cube faces (not null, modified)
     * @param apparentMagnitude the star's brightness (log scale)
     * @param textureSize size of the texture map (pixels per side, &lt;2)
     * @param worldDirection the star's world coordinates (length=1)
     * @return true if the star was successfully plotted, otherwise false
     */
    private static boolean plotStarOnCube(BufferedImage[] maps,
            float apparentMagnitude, int textureSize, Vector3f worldDirection) {
        assert maps != null;
        assert maps.length == 6 : maps.length;
        assert worldDirection != null;
        assert worldDirection.isUnitVector() : worldDirection;
        assert textureSize > 2 : textureSize;

        // Convert apparent magnitude to relative luminosity.
        float resolution = textureSize / 2_048f;
        float luminosity0 = 100f * resolution * resolution;
        float luminosity = luminosity0 * 1.5f
                * FastMath.pow(pogsonsRatio, -apparentMagnitude);
        if (luminosity < luminosityCutoff) {
            return false;
        }

        boolean success = false;
        for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
            /*
             * Convert world direction to texture coordinates on this
             * face of the cube.
             */
            Vector2f uv = cubeUV(worldDirection, faceIndex);
            if (uv != null) {
                BufferedImage map = maps[faceIndex];

                if (luminosity <= 37f) {
                    boolean success2 = plot4PointStar(map, luminosity,
                            textureSize, uv);
                    if (success2) {
                        success = true;
                    }
                } else {
                    plotEllipseForQuad(map, luminosity, textureSize,
                            worldDirection, faceIndex);
                    success = true;
                }
            }
        }
        return success;
    }

    /**
     * Plot a star's position at the specified time onto a texture map for a
     * dome.
     *
     * @param map texture map (not null, modified)
     * @param star star to plot (not null)
     * @param latitude radians north of the equator (&le;Pi/2, &ge;-Pi/2)
     * @param siderealTime radians since sidereal midnight (&lt;2*Pi, &ge;0)
     * @param textureSize size of the texture map (pixels per side, &gt;2)
     * @return true if the star was successfully plotted, otherwise false
     */
    private static boolean plotStarOnDome(BufferedImage map, Star star,
            float latitude, float siderealTime, int textureSize) {
        assert map != null;
        assert star != null;
        assert latitude >= -FastMath.HALF_PI : latitude;
        assert latitude <= FastMath.HALF_PI : latitude;
        assert siderealTime >= 0f : siderealTime;
        assert siderealTime < FastMath.TWO_PI : siderealTime;
        assert textureSize > 2 : textureSize;

        Vector3f equatorial = star.getEquatorialLocation(siderealTime);
        /*
         * Convert equatorial coordinates to world coordinates, where:
         *   +X points to the north horizon
         *   +Y points to the zenith
         *   +Z points to the east horizon
         *
         * The conversion consists of a (latitude - Pi/2) rotation about the Y
         * (east) axis followed by permutation of the axes.
         */
        float coLatitude = FastMath.HALF_PI - latitude;
        Quaternion rotation = new Quaternion();
        rotation.fromAngleNormalAxis(-coLatitude, Vector3f.UNIT_Y);
        Vector3f rotated = MyQuaternion.rotate(rotation, equatorial, null);
        assert rotated.isUnitVector() : rotated;
        if (rotated.z < 0f) { // The star lies below the horizon, so skip it.
            return false;
        }
        Vector3f world = new Vector3f(-rotated.x, rotated.z, rotated.y);

        float apparentMagnitude = star.getApparentMagnitude();
        boolean success = plotStarOnDome(
                map, apparentMagnitude, textureSize, world);

        return success;
    }

    /**
     * Plot a star on a texture map for a dome.
     *
     * @param map texture map (not null)
     * @param apparentMagnitude the star's brightness
     * @param textureSize size of the texture map (pixels per side, &lt;2)
     * @param worldDirection the star's world coordinates (length=1)
     * @return true if the star was successfully plotted, otherwise false
     */
    private static boolean plotStarOnDome(BufferedImage map,
            float apparentMagnitude, int textureSize, Vector3f worldDirection) {
        assert map != null;
        assert worldDirection != null;
        assert worldDirection.isUnitVector() : worldDirection;
        assert textureSize > 2 : textureSize;

        // Convert apparent magnitude to relative luminosity.
        float resolution = textureSize / 2_048f;
        float luminosity0 = 37f * resolution * resolution;
        float luminosity
                = luminosity0 * FastMath.pow(pogsonsRatio, -apparentMagnitude);
        if (luminosity < luminosityCutoff) {
            return false;
        }

        // Convert world direction to texture coordinates on a dome.
        Vector2f uv = domeMesh.directionUV(worldDirection);

        if (luminosity <= 37f) {
            boolean success = plot4PointStar(map, luminosity, textureSize, uv);
            return success;
        }
        plotEllipseForDome(map, luminosity, textureSize, uv);
        return true;
    }

    /**
     * Read the star catalog and add each valid star to the collection.
     */
    private static void readCatalog() {
        stars.clear();
        stars.addAll(BrightStarCatalogReader.read(catalogFilePath));
    }

}
