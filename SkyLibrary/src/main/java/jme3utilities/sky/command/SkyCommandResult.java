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
package jme3utilities.sky.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jme3utilities.Validate;
import jme3utilities.sky.runtime.SkyEnvironmentSnapshot;

/**
 * Result of executing a SkySimulation command ABI entry.
 *
 * @author Take Some
 */
final public class SkyCommandResult {
    /** Executed command id. */
    final private String commandId;
    /** Human-readable result message. */
    final private String message;
    /** Optional environment snapshot. */
    final private SkyEnvironmentSnapshot snapshot;
    /** True if command execution succeeded. */
    final private boolean success;
    /** Optional string values returned by the command. */
    final private List<String> values;

    /**
     * Instantiate a result.
     *
     * @param commandId command id (not null, not empty)
     * @param success true for successful execution
     * @param message result message (not null)
     * @param values returned values (not null, copied)
     * @param snapshot returned snapshot, or null
     */
    private SkyCommandResult(String commandId, boolean success,
            String message, List<String> values,
            SkyEnvironmentSnapshot snapshot) {
        Validate.nonEmpty(commandId, "command id");
        Validate.nonNull(message, "message");
        Validate.nonNull(values, "values");

        this.commandId = commandId;
        this.success = success;
        this.message = message;
        this.values = Collections.unmodifiableList(new ArrayList<String>(
                values));
        this.snapshot = snapshot;
    }

    /**
     * Create a success result.
     *
     * @param commandId command id
     * @param message result message
     * @return new result
     */
    public static SkyCommandResult success(String commandId, String message) {
        SkyCommandResult result = new SkyCommandResult(commandId, true,
                message, Collections.<String>emptyList(), null);
        return result;
    }

    /**
     * Create a success result with environment snapshot.
     *
     * @param commandId command id
     * @param message result message
     * @param snapshot environment snapshot (not null)
     * @return new result
     */
    public static SkyCommandResult successSnapshot(String commandId,
            String message, SkyEnvironmentSnapshot snapshot) {
        Validate.nonNull(snapshot, "snapshot");

        SkyCommandResult result = new SkyCommandResult(commandId, true,
                message, Collections.<String>emptyList(), snapshot);
        return result;
    }

    /**
     * Create a success result with returned values.
     *
     * @param commandId command id
     * @param message result message
     * @param values returned values (not null, copied)
     * @return new result
     */
    public static SkyCommandResult successValues(String commandId,
            String message, List<String> values) {
        SkyCommandResult result = new SkyCommandResult(commandId, true,
                message, values, null);
        return result;
    }

    /**
     * Return command id.
     *
     * @return command id
     */
    public String commandId() {
        return commandId;
    }

    /**
     * Return result message.
     *
     * @return message
     */
    public String message() {
        return message;
    }

    /**
     * Return optional environment snapshot.
     *
     * @return snapshot, or null
     */
    public SkyEnvironmentSnapshot snapshot() {
        return snapshot;
    }

    /**
     * Test whether execution succeeded.
     *
     * @return true if successful
     */
    public boolean succeeded() {
        return success;
    }

    /**
     * Return optional string values.
     *
     * @return immutable values
     */
    public List<String> values() {
        return values;
    }
}
