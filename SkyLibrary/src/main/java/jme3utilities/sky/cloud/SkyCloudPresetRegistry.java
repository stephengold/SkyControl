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
package jme3utilities.sky.cloud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jme3utilities.Validate;

/**
 * Registry of data-driven cloud/weather presets.
 *
 * @author Take Some
 */
final public class SkyCloudPresetRegistry {
    /** Definitions by stable id. */
    final private Map<String, SkyCloudPresetDefinition> definitions;

    /**
     * Instantiate a registry.
     *
     * @param definitions definitions to register (not null, copied)
     */
    public SkyCloudPresetRegistry(
            Collection<SkyCloudPresetDefinition> definitions) {
        Validate.nonNull(definitions, "definitions");

        Map<String, SkyCloudPresetDefinition> map
                = new LinkedHashMap<String, SkyCloudPresetDefinition>();
        for (SkyCloudPresetDefinition definition : definitions) {
            Validate.nonNull(definition, "definition");
            map.put(definition.id(), definition);
        }
        this.definitions = Collections.unmodifiableMap(map);
    }

    /**
     * Create a registry containing built-in enum presets.
     *
     * @return new registry
     */
    public static SkyCloudPresetRegistry builtIns() {
        List<SkyCloudPresetDefinition> list
                = new ArrayList<SkyCloudPresetDefinition>();
        for (SkyCloudPreset preset : SkyCloudPreset.values()) {
            list.add(SkyCloudPresetDefinition.fromPreset(preset));
        }
        SkyCloudPresetRegistry result = new SkyCloudPresetRegistry(list);
        return result;
    }

    /**
     * Access a definition by id.
     *
     * @param id stable id (not null, not empty)
     * @return definition, or null if not registered
     */
    public SkyCloudPresetDefinition get(String id) {
        Validate.nonEmpty(id, "id");

        SkyCloudPresetDefinition result = definitions.get(id);
        return result;
    }

    /**
     * Return registered ids.
     *
     * @return immutable id set
     */
    public Set<String> ids() {
        return definitions.keySet();
    }

    /**
     * Require a definition by id.
     *
     * @param id stable id (not null, not empty)
     * @return definition
     */
    public SkyCloudPresetDefinition require(String id) {
        SkyCloudPresetDefinition result = get(id);
        if (result == null) {
            throw new IllegalArgumentException("Unknown sky preset: " + id);
        }
        return result;
    }

    /**
     * Return registered definitions.
     *
     * @return immutable definitions collection
     */
    public Collection<SkyCloudPresetDefinition> values() {
        return definitions.values();
    }
}
