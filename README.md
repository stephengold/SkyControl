<img height="150" src="https://i.imgur.com/HfTbw30.png" alt="SkyControl logo">

[The SkyControl Project][skycontrol] provides a sky-simulation library for
[the jMonkeyEngine (JME) game engine][jme].

It contains 3 sub-projects:

1. SkyLibrary: the SkyControl runtime library and its automated tests
2. SkyExamples: example applications
3. SkyAssets: generate textures included in the library

Complete source code (in Java) is provided under
[a 3-clause BSD license][license].


<a name="toc"></a>

## Contents of this document

+ [Important features](#features)
+ [How to add SkyControl to an existing project](#add)
+ [How to build SkyControl from source](#build)
+ [Downloads](#downloads)
+ [Conventions](#conventions)
+ [External links](#links)
+ [History](#history)
+ [Acknowledgments](#acks)


<a name="features"></a>

## Important features

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

[Jump to table of contents](#toc)


<a name="add"></a>

## How to add SkyControl to an existing project

Adding SkyControl to an existing [jMonkeyEngine][jme] project should be
a simple 6-step process:

 1. Add SkyControl and its dependencies to the classpath.
 2. Disable any existing sky which might interfere with SkyControl.
 3. Add a `SkyControl` instance to some node in the scene graph.
 4. Configure the `SkyControl` instance.
 5. Enable the `SkyControl` instance.
 6. Test and tune as necessary.

The SkyControl Library depends on
the standard "jme3-effects" library from jMonkeyEngine and
[the Heart Library][heart],
which in turn depends on
the standard "jme3-core" library from jMonkeyEngine.

For projects built using Maven or [Gradle], it is sufficient to add a
dependency on the SkyControl Library.  The build tools should automatically
resolve the remaining dependencies.

### Gradle-built projects

Add to the project’s "build.gradle" file:

    repositories {
        mavenCentral()
    }
    dependencies {
        implementation 'com.github.stephengold:SkyControl:1.0.5'
    }

For some older versions of Gradle,
it's necessary to replace `implementation` with `compile`.

### Maven-built projects

Add to the project’s "pom.xml" file:

    <repositories>
      <repository>
        <id>mvnrepository</id>
        <url>https://repo1.maven.org/maven2/</url>
      </repository>
    </repositories>

    <dependency>
      <groupId>com.github.stephengold</groupId>
      <artifactId>SkyControl</artifactId>
      <version>1.0.5</version>
    </dependency>

### Ant-built projects

For projects built using [Ant], download the SkyControl and [Heart]
libraries from GitHub:

+ https://github.com/stephengold/SkyControl/releases/tag/latest
+ https://github.com/stephengold/Heart/releases/tag/8.3.2

You'll definitely want both class jars
and probably the "-sources" and "-javadoc" jars as well.

Open the project's properties in the IDE (JME SDK or NetBeans):

1. Right-click on the project (not its assets) in the "Projects" window.
2. Select "Properties" to open the "Project Properties" dialog.
3. Under "Categories:" select "Libraries".
4. Click on the "Compile" tab.
5. Add the [Heart] class jar:
  + Click on the "Add JAR/Folder" button.
  + Navigate to the download folder.
  + Select the "Heart-8.3.2.jar" file.
  + Click on the "Open" button.
6. (optional) Add jars for javadoc and sources:
  + Click on the "Edit" button.
  + Click on the "Browse..." button to the right of "Javadoc:"
  + Select the "Heart-8.3.2-javadoc.jar" file.
  + Click on the "Open" button.
  + Click on the "Browse..." button to the right of "Sources:"
  + Select the "Heart-8.3.2-sources.jar" file.
  + Click on the "Open" button again.
  + Click on the "OK" button to close the "Edit Jar Reference" dialog.
7. Similarly, add the SkyControl jar(s).
8. Click on the "OK" button to exit the "Project Properties" dialog.

[Jump to table of contents](#toc)


<a name="build"></a>

## How to build SkyControl from source

1. Install a [Java Development Kit (JDK)][adoptium],
   if you don't already have one.
2. Point the `JAVA_HOME` environment variable to your JDK installation:
   (The path might be something like "C:\Program Files\Java\jre1.8.0_301"
   or "/usr/lib/jvm/java-8-openjdk-amd64/" or
   "/Library/Java/JavaVirtualMachines/liberica-jdk-17-full.jdk/Contents/Home" .)
  + using Bash or Zsh: `export JAVA_HOME="` *path to installation* `"`
  + using [Fish]: `set -g JAVA_HOME "` *path to installation* `"`
  + using Windows Command Prompt: `set JAVA_HOME="` *path to installation* `"`
  + using PowerShell: `$env:JAVA_HOME = '` *path to installation* `'`
3. Download and extract the SkyControl source code from GitHub:
  + using Git:
    + `git clone https://github.com/stephengold/SkyControl.git`
    + `cd SkyControl`
    + `git checkout -b latest 1.0.5`
  + using a web browser:
    + browse to [the latest release][latest]
    + follow the "Source code (zip)" link
    + save the ZIP file
    + extract the contents of the saved ZIP file
    + `cd` to the extracted directory/folder
4. Run the [Gradle] wrapper:
  + using Bash or Fish or PowerShell or Zsh: `./gradlew build`
  + using Windows Command Prompt: `.\gradlew build`

After a successful build,
Maven artifacts will be found in "SkyLibrary/build/libs".

You can install the artifacts to your local Maven repository:
+ using Bash or Fish or PowerShell or Zsh: `./gradlew install`
+ using Windows Command Prompt: `.\gradlew install`

You can restore the project to a pristine state:
+ using Bash or Fish or PowerShell or Zsh: `./gradlew clean`
+ using Windows Command Prompt: `.\gradlew clean`

[Jump to table of contents](#toc)


<a name="downloads"></a>

## Downloads

Newer releases (since v0.9.27) can be downloaded from
[GitHub](https://github.com/stephengold/SkyControl/releases).

Older releases (v0.9.0 through v0.9.26) can be downloaded from
[the Jme3-utilities Project](https://github.com/stephengold/jme3-utilities/releases).

Newer Maven artifacts (since v0.9.30) are available from
[MavenCentral](https://central.sonatype.com/artifact/com.github.stephengold/SkyControl/1.0.4/versions).

Old Maven artifacts (v0.9.25 through v0.9.29) are available from JCenter.

[Jump to table of contents](#toc)


<a name="conventions"></a>

## Conventions

Package names begin with `jme3utilities.sky`

The source code and pre-built libraries are compatible with JDK 8.

In the default world coordinate system:

+ the `+X` axis points toward the northern horizon
+ the `+Y` axis points up (toward the zenith)
+ the `+Z` axis points toward the eastern horizon

However, these axis assignments can be overridden using `SunAndStars.setAxes()`.

[Jump to table of contents](#toc)


<a name="links"></a>

## External links

+ November 2013 [SkyControl demo video](https://www.youtube.com/watch?v=FsJRM6tr3oQ)
+ January 2014 [SkyControl update video](https://www.youtube.com/watch?v=gE4wxgBIkaw)
+ A [driving simulator](https://github.com/stephengold/jme-vehicles)
  that uses SkyControl.
+ A [flight simulation game](https://github.com/ZoltanTheHun/SkyHussars)
  that uses SkyControl.

[Jump to table of contents](#toc)


<a name="history"></a>

## History

The evolution of this project is chronicled in
[its release log][log].

SkyControl has its roots in SkyDome by Cris (aka "t0neg0d").

From November 2013 to September 2015,
SkyControl was part of the Jme3-utilities Project at
[Google Code](https://code.google.com/archive/).

From September 2015 to August 2020,
SkyControl was part of the Jme3-utilities Project at
[GitHub](https://github.com/stephengold/jme3-utilities).

Since August 2020, SkyControl has been a separate project, hosted at
[GitHub][skycontrol].

Old (2014) versions of SkyControl can still be found in
[the jMonkeyEngine-Contributions Project](https://github.com/jMonkeyEngine-Contributions/SkyControl).

[Jump to table of contents](#toc)


<a name="acks"></a>

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
    + the [Checkstyle] tool
    + the [FindBugs] source-code analyzer
    + the [Firefox] and [Chrome] web browsers
    + Gimp, the GNU Image Manipulation Program
    + the [Git] revision-control system and GitK commit viewer
    + the [GitKraken] client
    + the [Gradle] build tool
    + Guava core libraries for Java
    + the [IntelliJ IDEA][idea] and [NetBeans] integrated development environments
    + the [Java] compiler, standard doclet, and runtime environment
    + the JCommander Java framework
    + [jMonkeyEngine][jme] and the jME3 Software Development Kit
    + the [Linux Mint][mint] operating system
    + LWJGL, the Lightweight Java Game Library
    + the [Markdown] document-conversion tool
    + the [Meld] visual merge tool
    + Microsoft Windows
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

I am grateful to [GitHub], [Sonatype], [JFrog], [YouTube], and [Imgur]
for providing free hosting for this project
and many other open-source projects.

I'm also grateful to Quinn (for lending me one of her microphones) and finally
my dear Holly, for keeping me sane.

If I've misattributed anything or left anyone out, please let me know, so I can
correct the situation: sgold@sonic.net

[Jump to table of contents](#toc)


[adoptium]: https://adoptium.net/releases.html "Adoptium Project"
[ant]: https://ant.apache.org "Apache Ant Project"
[blender]: https://docs.blender.org "Blender Project"
[bsd3]: https://opensource.org/licenses/BSD-3-Clause "3-Clause BSD License"
[checkstyle]: https://checkstyle.org "Checkstyle"
[chrome]: https://www.google.com/chrome "Chrome"
[elements]: https://www.adobe.com/products/photoshop-elements.html "Photoshop Elements"
[findbugs]: http://findbugs.sourceforge.net "FindBugs Project"
[firefox]: https://www.mozilla.org/en-US/firefox "Firefox"
[fish]: https://fishshell.com/ "Fish command-line shell"
[git]: https://git-scm.com "Git"
[github]: https://github.com "GitHub"
[gitkraken]: https://www.gitkraken.com "GitKraken client"
[gradle]: https://gradle.org "Gradle Project"
[heart]: https://github.com/stephengold/Heart "Heart Project"
[idea]: https://www.jetbrains.com/idea/ "IntelliJ IDEA"
[imgur]: https://imgur.com/ "Imgur"
[java]: https://en.wikipedia.org/wiki/Java_(programming_language) "Java programming language"
[jfrog]: https://www.jfrog.com "JFrog"
[jme]: https://jmonkeyengine.org "jMonkeyEngine Project"
[latest]: https://github.com/stephengold/SkyControl/releases/latest "latest release"
[license]: https://github.com/stephengold/SkyControl/blob/master/LICENSE "SkyControl license"
[log]: https://github.com/stephengold/SkyControl/blob/master/SkyLibrary/release-notes.md "release log"
[markdown]: https://daringfireball.net/projects/markdown "Markdown Project"
[meld]: https://meldmerge.org "Meld merge tool"
[mint]: https://linuxmint.com "Linux Mint Project"
[netbeans]: https://netbeans.org "NetBeans Project"
[nifty]: http://nifty-gui.github.io/nifty-gui "Nifty GUI Project"
[obs]: https://obsproject.com "Open Broadcaster Software Project"
[skycontrol]: https://github.com/stephengold/SkyControl "SkyControl Project"
[sonatype]: https://www.sonatype.com "Sonatype"
[utilities]: https://github.com/stephengold/jme3-utilities "Jme3-utilities Project"
[winmerge]: https://winmerge.org "WinMerge Project"
[youtube]: https://www.youtube.com/ "YouTube"
