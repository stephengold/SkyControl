# Take Some SkySimulation package

Internal Maven/Gradle package coordinates:

```kotlin
implementation("dev.takesome:sky-simulation:1.4.4")
```

Install the package into Maven Local:

```bat
gradlew.bat packageLocal
```

Use it from another Gradle project:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("dev.takesome:sky-simulation:1.4.4-SNAPSHOT")
}
```

The Java package namespace remains compatible with SkyControl:

```java
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.SkyAtmosphere;
```

This avoids breaking existing code while publishing the artifact under Take Some
coordinates.
