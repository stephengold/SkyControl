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
package jme3utilities.sky.material;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.TextureKey;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Minimal DDS texture loader for unsupported cloud maps.
 *
 * @author Take Some
 */
public final class SkyDdsTextureLoader {
    /** DDS fixed header size in bytes. */
    final private static int headerSize = 128;

    /** Hidden constructor. */
    private SkyDdsTextureLoader() {
        // do nothing
    }

    /**
     * Load a DDS texture using a small built-in parser.
     *
     * @param assetManager asset manager (not null)
     * @param assetPath texture asset path (not null)
     * @return new texture
     */
    public static Texture loadTexture(
            AssetManager assetManager, String assetPath) {
        assert assetManager != null;
        assert assetPath != null;

        TextureKey key = new TextureKey(assetPath, false);
        AssetInfo info = assetManager.locateAsset(key);
        if (info == null) {
            throw new AssetNotFoundException(assetPath);
        }

        try (InputStream stream = info.openStream()) {
            Image image = readImage(stream);
            Texture2D result = new Texture2D(image);
            result.setKey(key);
            return result;
        } catch (IOException exception) {
            throw new AssetLoadException(
                    "Failed to load DDS asset " + assetPath, exception);
        }
    }

    /**
     * Read a DDS image from a stream.
     *
     * @param stream input stream (not null)
     * @return new image
     * @throws IOException if reading fails or format is unsupported
     */
    private static Image readImage(InputStream stream) throws IOException {
        byte[] data = readAll(stream);
        if (data.length < headerSize || data[0] != 'D' || data[1] != 'D'
                || data[2] != 'S' || data[3] != ' ') {
            throw new IOException("not a DDS file");
        }

        int height = littleInt(data, 12);
        int width = littleInt(data, 16);
        int depth = littleInt(data, 24);
        int mipLevels = littleInt(data, 28);
        String fourCc = fourCc(data, 84);
        if (depth <= 0) {
            depth = 1;
        }
        if (mipLevels <= 0) {
            mipLevels = 1;
        }

        FormatInfo format = format(fourCc);
        MipData mipData = copyMipData(
                data, width, height, mipLevels, format.blockBytes);
        Image result = new Image(format.imageFormat, width, height,
                mipData.buffer, mipData.sizes, ColorSpace.Linear);
        return result;
    }

    /**
     * Copy available mip levels into one direct buffer.
     *
     * @param data DDS file bytes (not null)
     * @param width base width (&gt;0)
     * @param height base height (&gt;0)
     * @param mipLevels declared mip level count (&gt;0)
     * @param blockBytes bytes in one 4x4 compressed block
     * @return new mip data
     */
    private static MipData copyMipData(byte[] data, int width, int height,
            int mipLevels, int blockBytes) {
        int[] sizes = new int[mipLevels];
        int offset = headerSize;
        int usedLevels = 0;
        for (int level = 0; level < mipLevels; ++level) {
            int levelWidth = Math.max(1, width >> level);
            int levelHeight = Math.max(1, height >> level);
            int blockWidth = Math.max(1, (levelWidth + 3) / 4);
            int blockHeight = Math.max(1, (levelHeight + 3) / 4);
            int size = blockWidth * blockHeight * blockBytes;
            if (offset + size > data.length) {
                break;
            }
            sizes[level] = size;
            offset += size;
            usedLevels = level + 1;
        }
        if (usedLevels == 0) {
            throw new AssetLoadException("DDS contains no mip data");
        }

        int totalBytes = offset - headerSize;
        ByteBuffer buffer = BufferUtils.createByteBuffer(totalBytes);
        buffer.put(data, headerSize, totalBytes);
        buffer.flip();
        int[] actualSizes = Arrays.copyOf(sizes, usedLevels);
        MipData result = new MipData(buffer, actualSizes);
        return result;
    }

    /**
     * Look up image format information for a FourCC.
     *
     * @param fourCc FourCC text (not null)
     * @return format information
     * @throws IOException if the FourCC is unsupported
     */
    private static FormatInfo format(String fourCc) throws IOException {
        FormatInfo result;
        switch (fourCc) {
            case "DXT1":
                result = new FormatInfo(Image.Format.DXT1, 8);
                break;
            case "DXT3":
                result = new FormatInfo(Image.Format.DXT3, 16);
                break;
            case "DXT5":
                result = new FormatInfo(Image.Format.DXT5, 16);
                break;
            case "ATI1":
            case "BC4U":
                result = new FormatInfo(Image.Format.RGTC1, 8);
                break;
            case "BC4S":
                result = new FormatInfo(Image.Format.SIGNED_RGTC1, 8);
                break;
            case "ATI2":
            case "BC5U":
                result = new FormatInfo(Image.Format.RGTC2, 16);
                break;
            case "BC5S":
                result = new FormatInfo(Image.Format.SIGNED_RGTC2, 16);
                break;
            default:
                throw new IOException("unsupported FourCC: " + fourCc);
        }

        return result;
    }


    /**
     * Read all bytes from a stream.
     *
     * @param stream input stream (not null)
     * @return byte array
     * @throws IOException if reading fails
     */
    private static byte[] readAll(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[16 * 1024];
        int count;
        while ((count = stream.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }
        byte[] result = output.toByteArray();
        return result;
    }

    /**
     * Read a little-endian int.
     *
     * @param data source bytes (not null)
     * @param offset byte offset
     * @return integer value
     */
    private static int littleInt(byte[] data, int offset) {
        int result = data[offset] & 0xFF;
        result |= (data[offset + 1] & 0xFF) << 8;
        result |= (data[offset + 2] & 0xFF) << 16;
        result |= (data[offset + 3] & 0xFF) << 24;
        return result;
    }

    /**
     * Read a FourCC.
     *
     * @param data source bytes (not null)
     * @param offset byte offset
     * @return FourCC string
     */
    private static String fourCc(byte[] data, int offset) {
        char[] chars = new char[4];
        for (int i = 0; i < 4; ++i) {
            chars[i] = (char) (data[offset + i] & 0xFF);
        }
        String result = new String(chars);
        return result;
    }

    /** Format metadata. */
    private static class FormatInfo {
        /** Block size in bytes. */
        final private int blockBytes;
        /** jME image format. */
        final private Image.Format imageFormat;

        /**
         * Instantiate metadata.
         *
         * @param imageFormat jME image format
         * @param blockBytes block size in bytes
         */
        FormatInfo(Image.Format imageFormat, int blockBytes) {
            this.imageFormat = imageFormat;
            this.blockBytes = blockBytes;
        }
    }

    /** Mip data. */
    private static class MipData {
        /** Packed mip buffer. */
        final private ByteBuffer buffer;
        /** Mip sizes. */
        final private int[] sizes;

        /**
         * Instantiate mip data.
         *
         * @param buffer packed mip buffer
         * @param sizes mip sizes
         */
        MipData(ByteBuffer buffer, int[] sizes) {
            this.buffer = buffer;
            this.sizes = sizes;
        }
    }
}
