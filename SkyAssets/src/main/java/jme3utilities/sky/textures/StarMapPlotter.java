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
package jme3utilities.sky.textures;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVector3f;
import jme3utilities.mesh.DomeMesh;
import jme3utilities.sky.Constants;

/**
 * Draws stars onto dome and cube star-map textures.
 *
 * @author Take Some
 */
final class StarMapPlotter {
    /** Luminosity of the faintest stars to draw. */
    final private static float luminosityCutoff = 0.1f;
    /** Number of points per ellipse. */
    final private static int ellipseNumPoints = 32;
    /** Luminosity ratio between successive stellar magnitudes. */
    final private static float pogsonsRatio = FastMath.pow(100f, 0.2f);
    /** Sample dome mesh for calculating texture coordinates. */
    final private static DomeMesh domeMesh = new DomeMesh(3, 2);
    /** x-coordinates used to draw an ellipse. */
    final private static int[] ellipseXs = new int[ellipseNumPoints];
    /** y-coordinates used to draw an ellipse. */
    final private static int[] ellipseYs = new int[ellipseNumPoints];
    /** Message logger for this class. */
    final private static Logger logger
            = Logger.getLogger(StarMapPlotter.class.getName());

    /**
     * Hidden constructor.
     */
    private StarMapPlotter() {
        // do nothing
    }

    /**
     * Plot a star's position at the specified time onto a cube.
     *
     * @param maps texture maps for 6 cube faces (not null, modified)
     * @param star star to plot (not null)
     * @param latitude radians north of the equator (&le;Pi/2, &ge;-Pi/2)
     * @param siderealTime radians since sidereal midnight (&lt;2*Pi, &ge;0)
     * @param textureSize size of the texture map (pixels per side, &gt;2)
     * @return true if the star was plotted, otherwise false
     */
    static boolean plotOnCube(BufferedImage[] maps, Star star,
            float latitude, float siderealTime, int textureSize) {
        Vector3f world = StarMapProjection.worldDirection(
                star, latitude, siderealTime);
        float apparentMagnitude = star.getApparentMagnitude();
        boolean result = plotOnCube(
                maps, apparentMagnitude, textureSize, world);

        return result;
    }

    /**
     * Plot a star's position at the specified time onto a dome.
     *
     * @param map texture map (not null, modified)
     * @param star star to plot (not null)
     * @param latitude radians north of the equator (&le;Pi/2, &ge;-Pi/2)
     * @param siderealTime radians since sidereal midnight (&lt;2*Pi, &ge;0)
     * @param textureSize size of the texture map (pixels per side, &gt;2)
     * @return true if the star was plotted, otherwise false
     */
    static boolean plotOnDome(BufferedImage map, Star star,
            float latitude, float siderealTime, int textureSize) {
        Vector3f world = StarMapProjection.worldDirection(
                star, latitude, siderealTime);
        if (world.y < 0f) {
            return false;
        }

        float apparentMagnitude = star.getApparentMagnitude();
        boolean result = plotOnDome(
                map, apparentMagnitude, textureSize, world);

        return result;
    }

    /**
     * Plot a four-pointed star shape on a texture map.
     *
     * @param map texture map (not null)
     * @param luminosity star luminosity in white-pixel units (&le;37, &gt;0)
     * @param textureSize texture size in pixels per side (&gt;2)
     * @param uv star texture coordinates (not null)
     * @return true if the star was plotted, otherwise false
     */
    private static boolean plot4PointStar(BufferedImage map, float luminosity,
            int textureSize, Vector2f uv) {
        assert luminosity > 0f : luminosity;
        assert luminosity <= 37f : luminosity;
        assert textureSize > 2 : textureSize;
        assert uv != null;

        int minPixels = (int) FastMath.ceil(luminosity);
        assert minPixels >= 1 : minPixels;
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
        Color color = new Color(brightness, brightness, brightness);
        float cornerOffset = 0.5f * (squareSize - 1);
        int x = Math.round(uv.x * textureSize - cornerOffset);
        int y = Math.round(uv.y * textureSize - cornerOffset);

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
     * Draw a dome-corrected ellipse onto a texture map.
     *
     * @param map texture map (not null)
     * @param luminosity star luminosity (&gt;0)
     * @param textureSize texture size in pixels per side (&gt;2)
     * @param uv star texture coordinates (not null)
     */
    private static void plotEllipseForDome(BufferedImage map, float luminosity,
            int textureSize, Vector2f uv) {
        assert map != null;
        assert luminosity > 0f : luminosity;
        assert textureSize > 2 : textureSize;
        assert uv != null;

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
            ellipseXs[i] = Math.round(uv.x * textureSize + dx);
            ellipseYs[i] = Math.round(uv.y * textureSize + dy);
        }
        Graphics2D graphics = map.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillPolygon(ellipseXs, ellipseYs, ellipseNumPoints);
    }

    /**
     * Draw a cube-face corrected ellipse onto a texture map.
     *
     * @param map texture map (not null)
     * @param luminosity star luminosity (&gt;0)
     * @param textureSize texture size in pixels per side (&gt;2)
     * @param worldDirection star world direction (length=1)
     * @param faceIndex cube face index (&ge;0, &lt;6)
     */
    private static void plotEllipseForQuad(BufferedImage map, float luminosity,
            int textureSize, Vector3f worldDirection, int faceIndex) {
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
            p.scaleAdd(r * FastMath.cos(theta), basis2, basis1);
            p.scaleAdd(r * FastMath.sin(theta), basis3, p);
            Vector2f uv = StarMapProjection.cubeUV(p, faceIndex);
            if (uv == null) {
                return;
            }
            ellipseXs[i] = Math.round(uv.x * textureSize);
            ellipseYs[i] = Math.round(uv.y * textureSize);
        }
        Graphics2D graphics = map.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillPolygon(ellipseXs, ellipseYs, ellipseNumPoints);
    }

    /**
     * Plot a star onto cube texture maps.
     *
     * @param maps texture maps for 6 cube faces (not null, modified)
     * @param apparentMagnitude star brightness
     * @param textureSize texture size in pixels per side (&gt;2)
     * @param worldDirection star world direction (length=1)
     * @return true if the star was plotted, otherwise false
     */
    private static boolean plotOnCube(BufferedImage[] maps,
            float apparentMagnitude, int textureSize, Vector3f worldDirection) {
        float resolution = textureSize / 2_048f;
        float luminosity0 = 100f * resolution * resolution;
        float luminosity = luminosity0 * 1.5f
                * FastMath.pow(pogsonsRatio, -apparentMagnitude);
        if (luminosity < luminosityCutoff) {
            return false;
        }

        boolean result = false;
        for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
            Vector2f uv = StarMapProjection.cubeUV(worldDirection, faceIndex);
            if (uv != null) {
                BufferedImage map = maps[faceIndex];
                if (luminosity <= 37f) {
                    result |= plot4PointStar(map, luminosity, textureSize, uv);
                } else {
                    plotEllipseForQuad(map, luminosity, textureSize,
                            worldDirection, faceIndex);
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Plot a star onto a dome texture map.
     *
     * @param map texture map (not null, modified)
     * @param apparentMagnitude star brightness
     * @param textureSize texture size in pixels per side (&gt;2)
     * @param worldDirection star world direction (length=1)
     * @return true if the star was plotted, otherwise false
     */
    private static boolean plotOnDome(BufferedImage map,
            float apparentMagnitude, int textureSize, Vector3f worldDirection) {
        float resolution = textureSize / 2_048f;
        float luminosity0 = 37f * resolution * resolution;
        float luminosity
                = luminosity0 * FastMath.pow(pogsonsRatio, -apparentMagnitude);
        if (luminosity < luminosityCutoff) {
            return false;
        }

        Vector2f uv = domeMesh.directionUV(worldDirection);
        if (luminosity <= 37f) {
            return plot4PointStar(map, luminosity, textureSize, uv);
        }
        plotEllipseForDome(map, luminosity, textureSize, uv);
        return true;
    }
}
