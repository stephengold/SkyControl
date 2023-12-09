# release log for the SkyControl library and related software

## Version 1.0.5 released on 9 December 2023

Notable changes:
+ bugfix:  `IllegalAccessException` in examples
+ on macOS specify -XstartOnFirstThread
+ use GLSL300 when available (for compatibility with WebGL)
+ base on version 8.8.0 of the Heart Library and target JME 3.6.1-stable

## Version 1.0.4 released on 24 March 2023

Notable changes:
+ publicize `SkyControlCore.numCloudLayers`
+ base on version 8.3.2 of the Heart Library and target JME 3.6.0-stable
+ add a "Created-By" attribute to the class-jar manifest
+ replace `java.util.Calendar` with `java.time.LocalDate`

## Version 1.0.3 released on 26 June 2022

Notable changes:
+ base on version 8.0.0 of the Heart Library and target JME 3.5.2-stable
+ add the `AppChooser` and `WaterExample` apps
+ add the "checkstyle" plugin to the build

## Version 1.0.2 released on 8 March 2022

Notable changes:
+ add Core profile support to J3MDs and shaders
+ improve the examples:
  + add command-line arguments to adjust the logging level
    and bypass the Settings Dialog (for portability)
  + add LWJGL natives for MacOSX_ARM64 (for portability)
+ base on version 7.4.1 of the Heart Library

## Version 1.0.1 released on 9 February 2022

Notable changes:
+ add messages to exceptions
+ base on version 7.3.0 of the Heart Library and target JME 3.5.0-stable
  and Java v8+

## Version 1.0.0 released on 22 August 2021

Notable changes:
+ base on version 7.0.0 of the Heart Library
+ bind the Z key in `TestSkyControl`

## Version 0.9.32 released on 31 May 2021

Notable changes:
+ base on version 6.4.4 of the Heart Library and target JME 3.4.0-stable

## Version 0.9.31+for34 released on 24 April 2021

Notable changes:
+ target Java 8
+ base on version 6.4.3+for34 of the Heart Library and target JME 3.4.0-beta1

## Version 0.9.30 released on 9 February 2021

Notable changes:
+ publish to MavenCentral instead of JCenter
+ add the `CubeMapExampleAfter` application
+ base on version 6.4.2 of the Heart Library

## Version 0.9.29 released on 7 October 2020

Notable changes:
+ resolve issue #1 (`IllegalArgumentException` for certain locales)

## Version 0.9.28 released on 12 September 2020

Notable changes:
+ add caller-provided storage to 4 methods. (API change)
+ specify GLSL100 in material definitions, to support Android
+ add the capability to customize the world coordinate system
+ convert `TestSunAndStars` from a sample app to an automated test

## Version 0.9.27 released on 17 August 2020

Notable changes:
+ use JCommander version 1.80 in tests
+ base on version 6.0.0 of the Heart Library and target JME 3.3.2-stable
+ SkyControl became a project separate from jme3-utilities

## Version 0.9.26 released on 1 April 2020

Notable changes:
+ use JCommander version 1.78 in tests
+ base on version 5.2.1 of the Heart Library and target JME 3.3.0-stable

## Version 0.9.25for33 released on 4 February 2020

Notable changes:
+ Changed the Maven groupId from "jme3utilities" to "com.github.stephengold"
+ base on version 5.0.0for33 of the Heart Library

## Version 0.9.24for33 released on 4 January 2020

Notable changes:
+ base on version 4.3.0for33 of the Heart Library
  and target the NEW JME 3.3.0-beta1

## Version 0.9.23for33 released on 8 December 2019

Notable changes:
+ base on version 4.2.0for33 of the Heart Library and target JME v3.3.0-beta1,
  which was later deleted!

## Version 0.9.22for33 released on 23 September 2019

Notable changes:
+ protect no-arg constructors used only for serialization
+ base on version 4.0.0for33 of the Heart Library and target JME 3.3.0-alpha5

## Version 0.9.21for33 released on 25 August 2019

Notable changes:
+ remove 2 deprecated constructors
+ update the FloorControl quad's transform in case the frustum changes
+ base on version 3.0.0for33 of the Heart Library

## Version 0.9.20for33 released on 7 August 2019

Notable changes:
+ base on version 2.31.0for33 of the Heart Library and target JME 3.3.0-alpha2

## Version 0.9.18 released on 7 June 2019

Notable changes:
+ base on version 2.28.1 of the Heart Library and target JME 3.2.3-stable

## Version 0.9.17 released on 10 March 2019

Notable changes:
+ prevent shadows from being cast by/on a `FloorControl`
+ add `LandscapeControl` (from the debug library) to the tests
+ base on version 2.21 of the heart library

## Version 0.9.16 released on 14 January 2018

Notable changes:
+ base on version 2.18 of the heart library and target JME 3.2.2-stable

## Version 0.9.15 released on 28 December 2018

Notable changes:
+ automate downloading and decompression of the star catalog
+ base on version 2.17 of the heart library and target JME 3.2.2-beta1

## Version 0.9.14 released on 23 September 2018

Notable changes:
+ rename `Constants.getVersionShort()` to `versionShort()`. (API change)
+ base on version 2.10 of the heart library

## Version 0.9.13 released on 24 July 2018

Notable changes:
+ deprecate the old constructors for `SkyControl` and `SkyControlCore`
+ rm bogus assert from `TestSkyControlRun`
+ base on version 2.5 of the heart library

## Version 0.9.12 released on 18 February 2018

Notable changes:
+ add new constructors and a `TwoDomes` stars option, for use with edge filters
+ add a `--nocubes` command-line parameter to `TestSkyControl`
+ add a cartoon-edge filter to `TestSkyControl`

## Version 0.9.11 released on 25 January 2018

Notable changes:
+ base on heart library v2.0 to make this library physics-independent

## Version 0.9.10 released on 23 January 2018

Notable changes:
+ update `SkyControl` and `FloorControl` cameras from the `RenderManager`

## Version 0.9.9 released on 22 January 2018

Notable changes:
+ target JME v3.2.1
+ give each `Updater` its own list of view ports

## Version 0.9.8 released on 14 January 2018

Notable changes:
+ fix a bug in `Updater` that caused an `IllegalArgumentException` while cloning
+ standardize the BSD license texts for tests

## Version 0.9.7for32 released on 5 December 2017

Notable changes:
+ 1st release to target JME 3.2
+ use a contrast-adjustment filter in `GlobeRenderer`
+ rename private fields

## Version 0.9.6 released on 19 September 2017

Notable changes:
+ use the `DomeMesh` class from the heart library
+ move wireframe material to heart library
+ make `SkyControl` class `JmeCloneable`
+ fix some potential aliasing bugs related to `Material.setColor()`
+ standardize the BSD license texts

## Version 0.9.5 released on 20 May 2017

Moved 2 general-purpose packages to new "jme3-utilities-heart" library, which
  this library now depends upon.

## Version 0.9.4 released on 14 May 2017

Notable changes:
+ fix a logic bug which set the main light to the wrong color

## Version 0.9.3 released on 5 May 2017

Notable changes:
+ add utility methods
+ use latest jCommander in tests
+ exclude assets from sources JAR

## Version 0.9.2 released on 9 April 2017

Notable changes:
+ fix a de-serialization bug that affected SkyControl and FloorControl
+ move all "virally licensed" assets out of the SkyControl library
+ build using Gradle instead of Ant
+ rename the {get/set}Cloud{Rate/YOffset} methods in the API

## Version 0.9.1 released on 8 March 2017

Major features added:
+ SkyControl is now serializable.
+ SkyControl no longer restricts the moon to the ecliptic.
+ TestSkyControl now includes a hotkey editor.
+ TestSkyControl no longer disables flyCam.

Other notable changes:
+ SkyControl puts moving stars on quads instead of domes - fewer triangles!
+ improved compatibility with jMonkeyEngine 3.1
+ App states are now based on NamedAppState instead of AbstractAppState.
+ moved noise/polygon/spline classes (not used by SkyControl) out of library

## Version 0.9.0 released on 21 January 2017

This was the initial baseline release.