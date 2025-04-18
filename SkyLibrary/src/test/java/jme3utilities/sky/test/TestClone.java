/*
 Copyright (c) 2025 Stephen Gold

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

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.material.plugins.J3MLoader;
import com.jme3.renderer.Camera;
import com.jme3.texture.plugins.AWTLoader;
import jme3utilities.Heart;
import jme3utilities.sky.LunarPhase;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.StarsOption;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cloning/saving/loading of various objects.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestClone {
    // *************************************************************************
    // fields

    /**
     * AssetManager for {@code BinaryExporter.saveAndLoad()}
     */
    final private static AssetManager assetManager = new DesktopAssetManager();
    // *************************************************************************
    // new methods exposed

    /**
     * Test cloning/saving/loading a {@code SkyControl} object.
     */
    @Test
    public void testCloneSky() {
        assetManager.registerLoader(J3MLoader.class, "j3m", "j3md");
        assetManager.registerLoader(AWTLoader.class, "jpg", "png");
        assetManager.registerLocator(null, ClasspathLocator.class);

        Camera camera = new Camera(640, 480);
        float cloudFlattening = 0.1f;
        boolean bottomDome = false;
        SkyControl s = new SkyControl(assetManager, camera, cloudFlattening,
                StarsOption.Cube, bottomDome);
        s.setPhase(LunarPhase.WAXING_CRESCENT);

        SkyControl sClone = Heart.deepCopy(s);
        Assert.assertEquals(LunarPhase.WAXING_CRESCENT, sClone.getPhase());

        SkyControl sCopy = BinaryExporter.saveAndLoad(assetManager, s);
        Assert.assertEquals(LunarPhase.WAXING_CRESCENT, sCopy.getPhase());
    }
}
