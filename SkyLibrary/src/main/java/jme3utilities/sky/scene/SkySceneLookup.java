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

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import jme3utilities.MySpatial;
import jme3utilities.mesh.DomeMesh;
import jme3utilities.sky.SkyMaterial;

/**
 * Internal scene-graph lookup helpers for sky controls.
 *
 * @author Take Some
 */
public final class SkySceneLookup {
    /**
     * Hidden constructor.
     */
    private SkySceneLookup() {
        // do nothing
    }

    /**
     * Access the bottom dome geometry.
     *
     * @param subtreeNode sky subtree node (not null, unaffected)
     * @return the pre-existing geometry, or null if none
     */
    public static Geometry bottomDome(Node subtreeNode) {
        Geometry result = geometry(subtreeNode, SkyNodeNames.bottom);
        return result;
    }

    /**
     * Access the clouds-only dome geometry.
     *
     * @param subtreeNode sky subtree node (not null, unaffected)
     * @return the pre-existing geometry, or null if none
     */
    public static Geometry cloudsOnlyDome(Node subtreeNode) {
        Geometry result = geometry(subtreeNode, SkyNodeNames.clouds);
        return result;
    }

    /**
     * Access a dome mesh from an optional geometry.
     *
     * @param geometry geometry to inspect, or null
     * @return the pre-existing dome mesh, or null if the geometry is null
     */
    public static DomeMesh domeMesh(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        DomeMesh result = (DomeMesh) geometry.getMesh();
        return result;
    }

    /**
     * Access a material from an optional geometry.
     *
     * @param geometry geometry to inspect, or null
     * @return the pre-existing material, or null if the geometry is null
     */
    public static Material material(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        Material result = geometry.getMaterial();
        return result;
    }

    /**
     * Access the material from a sky-material geometry.
     *
     * @param geometry geometry to inspect (not null, unaffected)
     * @return the pre-existing sky material (not null)
     */
    public static SkyMaterial skyMaterial(Geometry geometry) {
        assert geometry != null;
        SkyMaterial result = (SkyMaterial) geometry.getMaterial();

        assert result != null;
        return result;
    }

    /**
     * Access the stars parent node.
     *
     * @param subtreeNode sky subtree node (not null, unaffected)
     * @return the pre-existing node, or null if none
     */
    public static Node starsNode(Node subtreeNode) {
        Node result = (Node) MySpatial.findChild(
                subtreeNode, SkyNodeNames.starsNode);
        return result;
    }

    /**
     * Access the top dome geometry.
     *
     * @param subtreeNode sky subtree node (not null, unaffected)
     * @return the pre-existing geometry, or null if none
     */
    public static Geometry topDome(Node subtreeNode) {
        Geometry result = geometry(subtreeNode, SkyNodeNames.top);
        return result;
    }

    /**
     * Locate a named geometry under the specified subtree.
     *
     * @param subtreeNode sky subtree node (not null, unaffected)
     * @param childName desired child name (not null)
     * @return the pre-existing geometry, or null if none
     */
    private static Geometry geometry(Node subtreeNode, String childName) {
        assert subtreeNode != null;
        assert childName != null;
        Geometry result = (Geometry) MySpatial.findChild(
                subtreeNode, childName);

        return result;
    }
}
