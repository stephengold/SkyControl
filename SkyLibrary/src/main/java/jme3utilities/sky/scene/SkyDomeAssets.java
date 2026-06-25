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
package jme3utilities.sky.scene;

/**
 * Asset paths for built-in sky-dome models.
 * <p>
 * The procedural {@code DomeMesh} path remains the default runtime path until
 * OBJ-backed dome projection is implemented. These constants define the
 * packaged asset locations for future optional OBJ loading.
 *
 * @author Take Some
 */
public final class SkyDomeAssets {
    /**
     * Classpath directory for bundled sky-dome models.
     */
    final public static String modelDirectory = "Models/skies";
    /**
     * Built-in high-resolution sky-dome OBJ asset path.
     */
    final public static String builtInHighObj
            = modelDirectory + "/skydome_high.obj";
    /**
     * Material library next to {@link #builtInHighObj}.
     */
    final public static String builtInHighMtl
            = modelDirectory + "/skydome_high.mtl";

    /**
     * Hidden constructor.
     */
    private SkyDomeAssets() {
        // do nothing
    }
}
