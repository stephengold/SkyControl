/*
 Copyright (c) 2013-2023, Stephen Gold
 All rights reserved.

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

import com.beust.jcommander.Parameter;
import java.util.logging.Logger;

/**
 * Command-line parameters of the TestSkyControl application.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class TestSkyControlParameters {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TestSkyControlParameters.class.getName());
    // *************************************************************************
    // fields

    /**
     * true means use the cyclone cloud map; false means use default cloud maps
     */
    @Parameter(names = {"-c", "--cyclone"},
            description = "use cyclone cloud map")
    private boolean cyclone = false;
    /**
     * true means use a star cube; false means use star domes instead
     * <p>
     * This setting is ignored when singleDome is true.
     */
    @Parameter(names = {"-n", "--nocubes"},
            description = "don't use star cubes")
    private boolean noCubes = false;
    /**
     * true means use a shadow filter; false means use a shadow renderer
     */
    @Parameter(names = {"-f", "--filter"}, description = "use a shadow filter")
    private boolean shadowFilter = false;
    /**
     * true means show the setting dialog; false means don't show it
     */
    @Parameter(names = "--showSettingsDialog",
            description = "show the setting dialog")
    private boolean showSettingsDialog = false;
    /**
     * true means use just a single dome; false means use multiple domes
     */
    @Parameter(names = {"-s", "--single"},
            description = "use just a single dome")
    private boolean singleDome = false;
    /**
     * true means just display the usage message; false means run the
     * application
     */
    @Parameter(names = {"-h", "-u", "--help", "--usage"}, help = true,
            description = "display this usage message")
    private boolean usageOnly = false;
    /**
     * true means more log output; false means less output
     */
    @Parameter(names = {"-v", "--verbose"},
            description = "additional log output")
    private boolean verboseLogging = false;
    /**
     * true means scene with water; false means no water
     */
    @Parameter(names = {"-w", "--water"}, description = "scene with water")
    private boolean water = false;
    // *************************************************************************
    // new methods exposed

    /**
     * Test whether star cubes are allowed.
     */
    boolean cubes() {
        return !noCubes;
    }

    /**
     * Test whether the cyclone option was specified.
     */
    boolean cyclone() {
        return cyclone;
    }

    /**
     * Test whether the shadow filter option was specified.
     */
    boolean shadowFilter() {
        return shadowFilter;
    }

    /**
     * Test whether the settings-dialog option was specified.
     */
    boolean showSettingsDialog() {
        return showSettingsDialog;
    }

    /**
     * Test whether the single-dome option was specified.
     */
    boolean singleDome() {
        return singleDome;
    }

    /**
     * Test whether the "usage only" option was specified.
     */
    boolean usageOnly() {
        return usageOnly;
    }

    /**
     * Test whether the verbose-logging option was specified.
     */
    boolean verboseLogging() {
        return verboseLogging;
    }

    /**
     * Test whether the water option was specified.
     */
    boolean water() {
        return water;
    }
}
