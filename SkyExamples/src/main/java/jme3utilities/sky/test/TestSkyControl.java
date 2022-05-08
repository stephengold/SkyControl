/*
 Copyright (c) 2013-2022, Stephen Gold
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

import com.beust.jcommander.JCommander;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeVersion;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.nifty.GuiApplication;
import jme3utilities.nifty.bind.BindScreen;
import jme3utilities.sky.Constants;
import jme3utilities.ui.ActionApplication;
import jme3utilities.ui.InputMode;

/**
 * Test/demonstrate the SkyControl class using a heads-up display (HUD). The
 * application's main entry point is here.
 * <p>
 * Use the 'H' key to toggle HUD visibility. Use the 'F1' key to edit hotkey
 * bindings.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestSkyControl extends GuiApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TestSkyControl.class.getName());
    /**
     * application name for its window's title bar and its usage message
     */
    final private static String applicationName
            = TestSkyControl.class.getSimpleName();
    /**
     * path to hotkey bindings configuration asset
     */
    final private static String hotkeyBindingsAssetPath
            = "Interface/bindings/TestSkyControl.properties";
    // *************************************************************************
    // fields

    /**
     * Nifty screen for editing hotkey bindings
     */
    final private static BindScreen bindScreen = new BindScreen();
    /**
     * anti-aliasing factor in effect (samples per pixel, &ge;0, &le;16)
     */
    static int numSamples = 0;
    /**
     * heads-up display (HUD)
     */
    static TestSkyControlHud hud = new TestSkyControlHud();
    /**
     * scene management app state
     */
    static TestSkyControlRun run = new TestSkyControlRun();
    /**
     * command-line parameters
     */
    final static TestSkyControlParameters parameters
            = new TestSkyControlParameters();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the TestSkyControl application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        /*
         * Parse the command-line arguments into {@code parameters}.
         */
        JCommander jCommander = new JCommander(parameters);
        jCommander.parse(arguments);
        jCommander.setProgramName(applicationName);
        if (parameters.usageOnly()) {
            jCommander.usage();
            return;
        }

        Level generalLoggingLevel = parameters.verboseLogging()
                ? Level.INFO : Level.WARNING;
        Heart.setLoggingLevels(generalLoggingLevel);
        logger.setLevel(Level.INFO);
        Logger.getLogger(TestSkyControlRun.class.getName())
                .setLevel(Level.INFO);
        /*
         * Don't pause on lost focus.  This simplifies debugging and
         * permits the application to keep running while minimized.
         */
        TestSkyControl application = new TestSkyControl();
        application.setPauseOnLostFocus(false);

        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        /*
         * Customize the window's title bar.
         */
        String title = applicationName + " " + MyString.join(arguments);
        settings.setTitle(title);
        application.setSettings(settings);

        boolean showSettingsDialog = false; // parameters.showSettingsDialog();
        application.setShowSettings(showSettingsDialog);
        /*
         * Designate a sandbox directory.
         * This has to be done *prior to* initialization.
         */
        try {
            ActionApplication.designateSandbox("Written Assets");
        } catch (IOException exception) {
        }

        application.start();
    }
    // *************************************************************************
    // GuiApplication methods

    /**
     * Initialize this application.
     */
    @Override
    public void guiInitializeApplication() {
        numSamples = settings.getSamples();
        /*
         * Log library versions.
         */
        logger.log(Level.INFO, "jme3-core version is {0}",
                MyString.quote(JmeVersion.FULL_NAME));
        logger.log(Level.INFO, "SkyControl version is {0}",
                MyString.quote(Constants.versionShort()));

        //Heart.detachAll(stateManager, DebugKeysAppState.class);
        /*
         * Disable display of jME3 statistics.
         * These displays can be re-enabled by pressing the F5 hotkey.
         */
        setDisplayFps(false);
        setDisplayStatView(false);
        /*
         * Create and attach the heads-up display (HUD), the scene manger,
         * and the hotkey bindings editor.
         */
        boolean success = stateManager.attach(hud);
        assert success;
        success = stateManager.attach(run);
        assert success;
        success = stateManager.attach(bindScreen);
        assert success;
        /*
         * Default input mode directly influences the scene manager.
         */
        InputMode dim = getDefaultInputMode();
        dim.influence(run);

        dim.setConfigPath(hotkeyBindingsAssetPath);
    }

    /**
     * Process an action (from the GUI or keyboard) that wasn't handled by the
     * default input mode or the HUD.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        if (ongoing) {
            switch (actionString) {
                case "edit bindings":
                    InputMode im = InputMode.getActiveMode();
                    bindScreen.activate(im);
                    return;
                case "load scene":
                    run.load();
                    return;
                case "print scene":
                    run.dump();
                    return;
                case "save scene":
                    run.save();
                    return;
                case "toggle hud":
                    run.toggleHud();
                    return;
            }
        }
        /*
         * The action is not handled: forward it to the superclass.
         */
        super.onAction(actionString, ongoing, tpf);
    }
}
