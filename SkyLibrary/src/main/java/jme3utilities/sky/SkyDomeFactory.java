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

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import jme3utilities.MyAsset;
import jme3utilities.mesh.DomeMesh;
import jme3utilities.sky.scene.SkyNodeNames;

/**
 * Factory for sky dome geometries and default star-map nodes.
 *
 * @author Take Some
 */
final class SkyDomeFactory {
    /** Number of samples in each longitudinal arc of a major dome. */
    final private static int numLongSamples = 16;
    /** Number of samples around the rim of a dome. */
    final private static int numRimSamples = 60;
    /** Reusable mesh for smooth, inward-facing domes. */
    final private static DomeMesh hemisphereMesh = new DomeMesh(
            numRimSamples, numLongSamples, Constants.topU,
            Constants.topV, Constants.uvScale, true);
    /** Negative Y axis. */
    final private static Vector3f negativeUnitY = new Vector3f(0f, -1f, 0f);
    /** Local copy of unitX. */
    final private static Vector3f unitX = new Vector3f(1f, 0f, 0f);
    /** Local copy of unitZ. */
    final private static Vector3f unitZ = new Vector3f(0f, 0f, 1f);

    /**
     * Hidden constructor.
     */
    private SkyDomeFactory() {
        // do nothing
    }

    /**
     * Attach a stars node as the outermost sky child.
     *
     * @param subtree sky subtree (not null)
     * @param starsNode stars node (not null)
     */
    static void attachStars(Node subtree, Node starsNode) {
        subtree.attachChildAt(starsNode, 0);
    }

    /**
     * Create a new sky subtree and attach its base geometries.
     *
     * @param cloudFlattening cloud-dome flattening (&ge;0, &lt;1)
     * @param bottomEnabled true to create the bottom dome
     * @param topMaterial top material (not null)
     * @param bottomMaterial bottom material, or null
     * @param cloudsMaterial cloud material (not null)
     * @return new sky subtree node
     */
    static Node createSubtree(float cloudFlattening, boolean bottomEnabled,
            SkyMaterial topMaterial, Material bottomMaterial,
            SkyMaterial cloudsMaterial) {
        Node result = new Node(SkyNodeNames.skyNode);
        result.setQueueBucket(Bucket.Sky);
        result.setShadowMode(ShadowMode.Off);

        Geometry top = createTop(topMaterial);
        result.attachChild(top);
        if (bottomEnabled) {
            Geometry bottom = createBottom(bottomMaterial);
            result.attachChild(bottom);
        }
        if (cloudsMaterial != topMaterial) {
            Geometry clouds = createClouds(cloudFlattening, cloudsMaterial);
            result.attachChild(clouds);
        }

        return result;
    }


    /**
     * Create default star-map node for a newly constructed control.
     *
     * @param assetManager asset manager (not null)
     * @param option star rendering option (not null)
     * @return new stars node, or null when no node is needed
     */
    static Node createDefaultStars(
            AssetManager assetManager, StarsOption option) {
        Node result;
        switch (option) {
            case Cube:
                result = createStars(assetManager, option, "equator");
                break;
            case TwoDomes:
                result = createStars(
                        assetManager, option, "Textures/skies/star-maps");
                break;
            case TopDome:
                result = null;
                break;
            default:
                throw new IllegalStateException("option = " + option);
        }

        return result;
    }

    /**
     * Create a star-map node for the specified option.
     *
     * @param assetManager asset manager (not null)
     * @param option star rendering option (not null)
     * @param assetName asset name or path (not null)
     * @return new stars node, or null for TopDome
     */
    static Node createStars(
            AssetManager assetManager, StarsOption option, String assetName) {
        Node result;
        switch (option) {
            case Cube:
                result = MyAsset.createStarMapQuads(assetManager, assetName);
                result.setName(SkyNodeNames.starsNode);
                break;
            case TwoDomes:
                result = createStarDomes(assetManager, assetName);
                break;
            case TopDome:
                result = null;
                break;
            default:
                throw new IllegalStateException("option = " + option);
        }

        return result;
    }

    /**
     * Create a bottom dome geometry.
     *
     * @param material bottom material (not null)
     * @return new bottom geometry
     */
    private static Geometry createBottom(Material material) {
        DomeMesh mesh = new DomeMesh(numRimSamples, 2, Constants.topU,
                Constants.topV, Constants.uvScale, true);
        Geometry result = new Geometry(SkyNodeNames.bottom, mesh);
        Quaternion upsideDown = new Quaternion();
        upsideDown.lookAt(unitX, negativeUnitY);
        result.setLocalRotation(upsideDown);
        result.setMaterial(material);

        return result;
    }

    /**
     * Create a clouds-only dome geometry.
     *
     * @param flattening cloud-dome flattening (&gt;0, &lt;1)
     * @param material cloud material (not null)
     * @return new cloud dome geometry
     */
    private static Geometry createClouds(
            float flattening, SkyMaterial material) {
        assert flattening > 0f : flattening;
        assert flattening < 1f : flattening;

        Geometry result = new Geometry(
                SkyNodeNames.clouds, hemisphereMesh);
        float yScale = 1f - flattening;
        result.setLocalScale(1f, yScale, 1f);
        result.setMaterial(material);

        return result;
    }

    /**
     * Load a star map onto a sphere formed by 2 domes.
     *
     * @param assetManager asset manager (not null)
     * @param assetPath asset folder path (not null)
     * @return new orphan node
     */
    private static Node createStarDomes(
            AssetManager assetManager, String assetPath) {
        Node result = new Node(SkyNodeNames.starsNode);
        Geometry north = createStarHemisphere(assetManager, assetPath,
                SkyNodeNames.northStars, "northern.png", -FastMath.HALF_PI);
        result.attachChild(north);
        Geometry south = createStarHemisphere(assetManager, assetPath,
                SkyNodeNames.southStars, "southern.png", FastMath.HALF_PI);
        result.attachChild(south);

        return result;
    }

    /**
     * Create one hemisphere of a two-dome star map.
     *
     * @param assetManager asset manager (not null)
     * @param folder asset folder path (not null)
     * @param geometryName geometry name (not null)
     * @param imageName image filename (not null)
     * @param angle rotation angle around Z
     * @return new star hemisphere geometry
     */
    private static Geometry createStarHemisphere(AssetManager assetManager,
            String folder, String geometryName, String imageName, float angle) {
        Geometry result = new Geometry(geometryName, hemisphereMesh);
        String assetPath = folder + "/" + imageName;
        Material material = MyAsset.createUnshadedMaterial(
                assetManager, assetPath);
        result.setMaterial(material);
        Quaternion orientation = new Quaternion();
        orientation.fromAngleAxis(angle, unitZ);
        result.setLocalRotation(orientation);

        return result;
    }

    /**
     * Create a top dome geometry.
     *
     * @param material top material (not null)
     * @return new top geometry
     */
    private static Geometry createTop(SkyMaterial material) {
        Geometry result = new Geometry(
                SkyNodeNames.top, (DomeMesh) hemisphereMesh.clone());
        result.setMaterial(material);

        return result;
    }
}
