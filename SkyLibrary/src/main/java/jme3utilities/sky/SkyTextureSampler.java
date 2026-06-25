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

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.texture.image.ImageRaster;

/**
 * Texture sampling utilities for sky materials.
 *
 * @author Take Some
 */
final class SkyTextureSampler {
    /**
     * Hidden constructor.
     */
    private SkyTextureSampler() {
        // do nothing
    }

    /**
     * Sample the red component of a rasterized texture.
     *
     * @param colorImage the texture to sample (not null, unaffected)
     * @param uv texture coordinates to sample (not null, unaffected)
     * @return red intensity (&le;1, &ge;0)
     */
    static float sampleRed(ImageRaster colorImage, Vector2f uv) {
        assert colorImage != null;
        assert uv != null;
        float u = uv.x;
        float v = uv.y;
        assert u >= Constants.uvMin : uv;
        assert u < Constants.uvMax : uv;
        assert v >= Constants.uvMin : uv;
        assert v < Constants.uvMax : uv;

        int width = colorImage.getWidth();
        float x = u * width;
        int x0 = (int) FastMath.floor(x);
        float xFraction1 = x - x0;
        float xFraction0 = 1 - xFraction1;
        int x1 = (x0 + 1) % width;

        int height = colorImage.getHeight();
        float y = v * width;
        int y0 = (int) FastMath.floor(y);
        float yFraction1 = y - y0;
        float yFraction0 = 1 - yFraction1;
        int y1 = (y0 + 1) % height;

        float r00 = colorImage.getPixel(x0, y0).r;
        float r01 = colorImage.getPixel(x1, y0).r;
        float r10 = colorImage.getPixel(x0, y1).r;
        float r11 = colorImage.getPixel(x1, y1).r;

        float result = r00 * xFraction0 * yFraction0
                + r01 * xFraction0 * yFraction1
                + r10 * xFraction1 * yFraction0
                + r11 * xFraction1 * yFraction1;

        assert result >= Constants.alphaMin : result;
        assert result <= Constants.alphaMax : result;
        return result;
    }
}
