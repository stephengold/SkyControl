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
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Renders complete dome and cube star maps from catalog stars.
 *
 * @author Take Some
 */
final class StarMapRenderer {
    /** Message logger for this class. */
    final private static Logger logger
            = Logger.getLogger(StarMapRenderer.class.getName());

    /**
     * Hidden constructor.
     */
    private StarMapRenderer() {
        // do nothing
    }

    /**
     * Generate 6 starry sky texture maps for a cube.
     *
     * @param stars stars to plot (not null)
     * @param latitude radians north of the equator (&le;Pi/2, &ge;-Pi/2)
     * @param siderealTime radians since sidereal midnight (&lt;2*Pi, &ge;0)
     * @param textureSize size of each texture map (pixels per side, &gt;2)
     * @return new array of 6 images
     */
    static RenderedImage[] generateCubeMap(Collection<Star> stars,
            float latitude, float siderealTime, int textureSize) {
        assert stars != null;
        assert latitude >= -FastMath.HALF_PI : latitude;
        assert latitude <= FastMath.HALF_PI : latitude;
        assert siderealTime >= 0f : siderealTime;
        assert siderealTime < FastMath.TWO_PI : siderealTime;
        assert textureSize > 2 : textureSize;

        BufferedImage[] maps = new BufferedImage[6];
        for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
            maps[faceIndex] = new BufferedImage(
                    textureSize, textureSize, BufferedImage.TYPE_BYTE_GRAY);
        }

        int plotCount = 0;
        for (Star star : stars) {
            boolean success = StarMapPlotter.plotOnCube(
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
     * @param stars stars to plot (not null)
     * @param latitude radians north of the equator (&le;Pi/2, &ge;-Pi/2)
     * @param siderealTime radians since sidereal midnight (&lt;2*Pi, &ge;0)
     * @param textureSize size of the texture map (pixels per side, &gt;2)
     * @return new image
     */
    static RenderedImage generateDomeMap(Collection<Star> stars,
            float latitude, float siderealTime, int textureSize) {
        assert stars != null;
        assert latitude >= -FastMath.HALF_PI : latitude;
        assert latitude <= FastMath.HALF_PI : latitude;
        assert siderealTime >= 0f : siderealTime;
        assert siderealTime < FastMath.TWO_PI : siderealTime;
        assert textureSize > 2 : textureSize;

        BufferedImage map = new BufferedImage(
                textureSize, textureSize, BufferedImage.TYPE_BYTE_GRAY);

        int plotCount = 0;
        for (Star star : stars) {
            boolean success = StarMapPlotter.plotOnDome(
                    map, star, latitude, siderealTime, textureSize);
            if (success) {
                ++plotCount;
            }
        }
        logger.log(Level.FINE, "plotted {0} stars", plotCount);

        return map;
    }
}
