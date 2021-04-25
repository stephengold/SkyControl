<img height="150" src="https://i.imgur.com/HfTbw30.png">

[The SkyControl Project][skycontrol] provides a sky-simulation library for
[the jMonkeyEngine (JME) game engine][jme].

It contains 3 sub-projects:

 1. SkyLibrary: the SkyControl runtime library
 2. SkyExamples: example applications
 3. SkyAssets: generate textures included in the library

Complete source code (in Java) is provided under
[a 3-clause BSD license][license].

Summary of features:

 + sun, moon, stars, horizon haze, and up to 6 cloud layers
 + compatible with static backgrounds such as cube maps
 + high resolution textures are provided -- or customize with your own textures
 + compatible with effects such as `SimpleWater`, shadows, bloom, and cartoon edges
 + continuous and reversible motion and blending of cloud layers
 + option to foreshorten clouds near the horizon
 + continuous and reversible motion of sun, moon, and stars based on time of day
 + updater to synchronize lighting and shadows with sun, moon, and clouds
 + continuous scaling of sun, moon, and clouds
 + option for continuously variable phase of the moon
 + demonstration apps and online tutorial provided
 + complete source code provided under FreeBSD license

<a name="toc"/>

## Contents of this document

 + [How to add SkyControl to an existing project](#add)
 + [Downloads](#downloads)
 + [Conventions](#conventions)
 + [How to build SkyControl from source](#build)
 + [External links](#links)
 + [History](#history)
 + [Acknowledgments](#acks)

<a name="add"/>

## How to add SkyControl to an existing project

Adding SkyControl to an existing [jMonkeyEngine][jme] project should be
a simple 6-step process:

 1. Add SkyControl and its dependencies to the classpath.
 2. Disable any existing sky which might interfere with SkyControl.
 3. Add a `SkyControl` instance to some node in the scene graph.
 4. Configure the `SkyControl` instance.
 5. Enable the `SkyControl` instance.
 6. Test and tune as necessary.

The SkyControl Library depends on [the Heart Library][heart],
which in turn depends on
the standard jme3-core library from jMonkeyEngine.

#### For Gradle projects

For projects built using Maven or Gradle, it is sufficient to specify the
dependency on the SkyControl Library.  The build tools should automatically
resolve the remaining dependencies automatically.

    repositories {
        mavenCentral()
    }
    dependencies {
        compile 'com.github.stephengold:SkyControl:0.9.31+for33'
    }

#### For Ant projects

For project built using [Ant], download the SkyControl and Heart
libraries from GitHub:

 + https://github.com/stephengold/SkyControl/releases/tag/0.9.31+for33
 + https://github.com/stephengold/Heart/releases/tag/6.4.3+for33

You'll want both class jars
and probably the `-sources` and `-javadoc` jars as well.

Open the project's properties in the IDE (JME 3.2 SDK or NetBeans 8.2):

 1. Right-click on the project (not its assets) in the "Projects" window.
 2. Select "Properties to open the "Project Properties" dialog.
 3. Under "Categories:" select "Libraries".
 4. Click on the "Compile" tab.
 5. Add the `Heart` class jar:
    + Click on the "Add JAR/Folder" button.
    + Navigate to the download folder.
    + Select the "Heart-6.4.3+for33.jar" file.
    + Click on the "Open" button.
 6. (optional) Add jars for javadoc and sources:
    + Click on the "Edit" button.
    + Click on the "Browse..." button to the right of "Javadoc:"
    + Select the "Heart-6.4.3+for33-javadoc.jar" file.
    + Click on the "Open" button.
    + Click on the "Browse..." button to the right of "Sources:"
    + Select the "Heart-6.4.3+for33-sources.jar" file.
    + Click on the "Open" button again.
    + Click on the "OK" button to close the "Edit Jar Reference" dialog.
 7. Similarly, add the `SkyControl` jar(s).
 8. Click on the "OK" button to exit the "Project Properties" dialog.

[Jump to table of contents](#toc)

<a name="downloads"/>

## Downloads

Newer releases (since v0.9.27) can be downloaded from
[GitHub](https://github.com/stephengold/SkyControl/releases).

Older releases (v0.9.0 through v0.9.26) can be downloaded from
[the Jme3-utilities Project](https://github.com/stephengold/jme3-utilities/releases).

Newer Maven artifacts (since v0.9.30) are available from
[MavenCentral](https://search.maven.org/artifact/com.github.stephengold/SkyControl).

Old Maven artifacts (v0.9.25 through v0.9.29) are available from
[JCenter](https://bintray.com/stephengold/com.github.stephengold/SkyControl).

The oldest Maven artifacts (v0.9.0 through v0.9.24) are available from
[JFrog Bintray](https://bintray.com/stephengold/jme3utilities/SkyControl).

[Jump to table of contents](#toc)

<a name="conventions"/>

## Conventions

Package names begin with `jme3utilities.sky`

Both the source code and the pre-built libraries are compatible with JDK 7.

In the default world coordinate system:

 + the `+X` axis points toward the northern horizon
 + the `+Y` axis points up (toward the zenith)
 + the `+Z` axis points toward the eastern horizon

However these axis assignments can be overridden using `SunAndStars.setAxes()`.

[Jump to table of contents](#toc)

<a name="build"/>

## How to build SkyControl from source

### IDE setup

 + The setup instructions in this section are for jMonkeyEngine 3.2 SDKs
   (which are based on the NetBeans 8 IDE) and aren't expected to work with
   jMonkeyEngine 3.3 SDKs (which are based on the NetBeans 11 IDE).
 + It's easy to develop jMonkeyEngine 3.3 applications on a
   jMonkeyEngine 3.2 SDK, provided you use Gradle instead of Ant.
 + If you already have a jMonkeyEngine 3.2 SDK installed, skip to step 6.

The hardware and software requirements of the IDE are documented at
[the JME wiki](https://wiki.jmonkeyengine.org/docs/3.3/getting-started/requirements.html).

 1. Download a jMonkeyEngine 3.2 Software Development Kit (SDK) from
    [GitHub](https://github.com/jMonkeyEngine/sdk/releases).
 2. Install the SDK, which includes:
    + the engine itself,
    + an IDE based on NetBeans,
    + various IDE plugins, and
    + the Blender 3D application.
 3. Open the IDE.
 4. The first time you open the IDE, it prompts you to
    specify a folder for storing projects:
    + Fill in the "Folder name" text box.
    + Click on the "Set Project Folder" button.
 5. The first time you open the IDE, you should update
    all the pre-installed plugins:
    + Menu bar -> "Tools" -> "Plugins" to open the "Plugins" dialog.
    + Click on the "Update" button to open the "Plugin Installer" wizard.
    + Click on the "Next >" button.
    + After the plugins have downloaded, click "Finish".
    + The IDE will restart.
 6. In order to open the SkyControl Project in the IDE (or NetBeans),
    you will need to install the `Gradle Support` plugin:
    + Menu bar -> "Tools" -> "Plugins" to open the "Plugins" dialog.
    + Click on the "Available Plugins" tab.
    + Check the box next to "Gradle Support" in the "Gradle" category.
     If this plugin isn't shown in the IDE's "Plugins" tool,
     you can download it from
     [GitHub](https://github.com/kelemen/netbeans-gradle-project/releases).
    + Click on the "Install" button to open the "Plugin Installer" wizard.
    + Click on the "Next >" button.
    + Check the box next to
     "I accept the terms in all the license agreements."
    + Click on the "Install" button.
    + When the "Verify Certificate" dialog appears,
     click on the "Continue" button.
    + Click on the "Finish" button.
    + The IDE will restart.

### Source files

Clone the SkyControl repository using Git:

 1. Open the "Clone Repository" wizard in the IDE:
     + Menu bar -> "Team" -> "Git" -> "Clone..." or
     + Menu bar -> "Team" -> "Remote" -> "Clone..."
 2. For "Repository URL:" specify
    `https://github.com/stephengold/SkyControl.git`
 3. Clear the "User:" and "Password:" text boxes.
 4. For "Clone into:" specify a writable folder (on a local filesystem)
    that doesn't already contain "SkyControl".
 5. Click on the "Next >" button.
 6. Make sure the "for_jME3.3" remote branch is checked.
 7. Click on the "Next >" button again.
 8. Make sure the Checkout Branch is set to "for_jME3.3".
 9. Make sure the "Scan for NetBeans Projects after Clone" box is checked.
10. Click on the "Finish" button.
11. When the "Clone Completed" dialog appears, click on the "Open Project..."
    button.
12. Expand the root project node to reveal the 3 sub-projects.
13. Select all 3 using control-click, then click on the
    "Open" button.

### Build the project

 1. In the "Projects" window of the IDE,
    right-click on the "SkyExamples" sub-project to select it.
 2. Select "Build".

### How to build SkyControl without an IDE

 1. Install a [Java Development Kit (JDK)][openJDK],
    if you don't already have one.
 2. Download and extract the SkyControl source code from GitHub:
   + using Git:
     + `git clone https://github.com/stephengold/SkyControl.git`
     + `cd SkyControl`
     + `git checkout -b latest 0.9.31+for33`
   + using a web browser:
     + browse to https://github.com/stephengold/SkyControl/releases/0.9.31+for33
     + follow the "Source code (zip)" link
     + save the ZIP file
     + extract the contents of the saved ZIP file
     + `cd` to the extracted directory/folder
 3. Set the `JAVA_HOME` environment variable:
   + using Bash:  `export JAVA_HOME="` *path to your JDK* `"`
   + using Windows Command Prompt:  `set JAVA_HOME="` *path to your JDK* `"`
 4. Run the [Gradle] wrapper:
   + using Bash:  `./gradlew build`
   + using Windows Command Prompt:  `.\gradlew build`

After a successful build,
Maven artifacts will be found in `SkyLibrary/build/libs`.

You can install the Maven artifacts to your local cache:
 + using Bash:  `./gradlew publishToMavenLocal`
 + using Windows Command Prompt:  `.\gradlew publishToMavenLocal`

[Jump to table of contents](#toc)

<a name="links"/>

## External links

  + November 2013 [SkyControl demo video](https://www.youtube.com/watch?v=FsJRM6tr3oQ)
  + January 2014 [SkyControl update video](https://www.youtube.com/watch?v=gE4wxgBIkaw)
  + A [driving simulator](https://github.com/stephengold/jme-vehicles)
    that uses SkyControl.
  + A [flight simulation game](https://github.com/ZoltanTheHun/SkyHussars)
    that uses SkyControl.

[ant]: https://ant.apache.org "Apache Ant Project"
[blender]: https://docs.blender.org "Blender Project"
[bsd3]: https://opensource.org/licenses/BSD-3-Clause "3-Clause BSD License"
[chrome]: https://www.google.com/chrome "Chrome"
[elements]: http://www.adobe.com/products/photoshop-elements.html "Photoshop Elements"
[findbugs]: http://findbugs.sourceforge.net "FindBugs Project"
[firefox]: https://www.mozilla.org/en-US/firefox "Firefox"
[git]: https://git-scm.com "Git"
[github]: https://github.com "GitHub"
[gradle]: https://gradle.org "Gradle Project"
[heart]: https://github.com/stephengold/Heart "Heart Project"
[imgur]: https://imgur.com/ "Imgur"
[jfrog]: https://www.jfrog.com "JFrog"
[jme]: https://jmonkeyengine.org  "jMonkeyEngine Project"
[license]: https://github.com/stephengold/SkyControl/blob/for_jME3.3/LICENSE "SkyControl license"
[log]: https://github.com/stephengold/SkyControl/blob/for_jME3.3/SkyLibrary/release-notes.md "release log"
[markdown]: https://daringfireball.net/projects/markdown "Markdown Project"
[meld]: http://meldmerge.org/ "Meld Tool"
[mint]: https://linuxmint.com "Linux Mint Project"
[netbeans]: https://netbeans.org "NetBeans Project"
[nifty]: http://nifty-gui.github.io/nifty-gui "Nifty GUI Project"
[obs]: https://obsproject.com "Open Broadcaster Software Project"
[openJDK]: https://openjdk.java.net "OpenJDK Project"
[skycontrol]: https://github.com/stephengold/SkyControl "SkyControl Project"
[sonatype]: https://www.sonatype.com "Sonatype"
[utilities]: https://github.com/stephengold/jme3-utilities "Jme3-utilities Project"
[winmerge]: http://winmerge.org "WinMerge Project"
[youtube]: https://www.youtube.com/ "YouTube"

[Jump to table of contents](#toc)

<a name="history"/>

## History

The evolution of the project is chronicled in [its release log][log].

SkyControl has its roots in SkyDome by Cris (aka "t0neg0d").

From November 2013 to September 2015,
SkyControl was part of the Jme3-utilities Project at
[Google Code](https://code.google.com/archive/).

From September 2015 to August 2020,
SkyControl was part of the Jme3-utilities Project at
[GitHub](https://github.com/stephengold/jme3-utilities).

Since August 2020, SkyControl has been a separate project at
[GitHub][skycontrol].

Old (2014) versions of SkyControl can still be found in
[the jMonkeyEngine-Contributions Project](https://github.com/jMonkeyEngine-Contributions/SkyControl).

[Jump to table of contents](#toc)

<a name="acks"/>

## Acknowledgments

Like most projects, the SkyControl Project builds on the work of many who
have gone before.  I therefore acknowledge the following
artists and software developers:

+ Cris (aka "t0neg0d") for creating SkyDome (which provided both an inspiration
  and a starting point for SkyControl) and also for encouraging me to run with
  it ... thank you yet again!
+ Paul Speed, for helpful insights which got me unstuck during debugging
+ Rémy Bouquet (aka "nehon") for many helpful insights
+ Alexandr Brui (aka "javasabr") for a solving a problem with the
  de-serialization of `SkyControl`
+ the brave souls who volunteered to be alpha testers for SkyControl, including:
    + Davis Rollman
    + "Lockhead"
    + Jonatan Dahl
    + Mindaugas (aka "eraslt")
    + Thomas Kluge
    + "pixelapp"
    + Roger (aka "stenb")
+ the beta testers for SkyControl, including:
    + "madjack"
    + Benjamin D.
    + "Fissll"
    + Davis Rollman
+ users who found and reported bugs in later versions:
    + Rami Manaf
    + Anton Starastsin (aka "Antonystar")
+ the creators of (and contributors to) the following software:
    + Adobe Photoshop Elements
    + the Ant build tool
    + the [Blender] 3-D animation suite
    + the [FindBugs] source-code analyzer
    + the [Firefox] and [Google Chrome][chrome] web browsers
    + Gimp, the GNU Image Manipulation Program
    + the [Git] revision-control system and GitK commit viewer
    + the [Gradle] build tool
    + Guava core libraries for Java
    + the Java compiler, standard doclet, and virtual machine
    + the JCommander Java framework
    + [jMonkeyEngine][jme] and the jME3 Software Development Kit
    + the [Linux Mint][mint] operating system
    + LWJGL, the Lightweight Java Game Library
    + the [Markdown] document-conversion tool
    + the [Meld] visual merge tool
    + Microsoft Windows
    + the [NetBeans] integrated development environment
    + the [Nifty] graphical user-interface library
    + [Open Broadcaster Software Studio][obs]
    + the PMD source-code analyzer
    + Alex Peterson's Spacescape tool
    + the Subversion revision-control systems
    + the [WinMerge] differencing and merging tool

Many of SkyControl's assets were based on the works of others who licensed their
works under liberal terms or contributed them to the public domain.
For this I thank:

+ Cris (aka "t0neg0d")
+ Jacques Descloitres, MODIS Rapid Response Team, NASA/GSFC
+ Tom Ruen

I am grateful to [Github], [Sonatype], [JFrog], [YouTube], and [Imgur]
for providing free hosting for this project
and many other open-source projects.

I'm also grateful to Quinn (for lending me one of her microphones) and finally
my dear Holly, for keeping me sane.

If I've misattributed anything or left anyone out, please let me know so I can
correct the situation: sgold@sonic.net

[Jump to table of contents](#toc)