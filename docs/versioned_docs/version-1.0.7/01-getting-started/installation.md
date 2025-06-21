---
sidebar_position: 6
title: 'Installation'
---

# Installation

Brobot is published to Maven Central, making it easy to include in any standard Java project using Maven or Gradle.

## Prerequisites

Before you begin, please ensure your development environment meets the following requirements:

* **Java 21 or higher**: The Brobot library is built targeting Java 21.
* **Build Tool**: Your project must be managed by a build tool that can resolve dependencies from Maven Central, such as Maven or Gradle.
* **Spring Framework (Recommended)**: Brobot is built using the Spring Framework. While it can be used in other contexts, it is designed to integrate seamlessly into a Spring or Spring Boot application.

## Adding the Brobot Dependency

To add the Brobot library to your project, add the following dependency to your build configuration file.

### Gradle

In your `build.gradle` or `build.gradle.kts` file, add the following to your `dependencies` block:

```groovy
implementation 'io.github.jspinak:brobot:1.0.7'
```

### Maven

In your `pom.xml` file, add the following within your `<dependencies>` block:

```xml
<dependency>
    <groupId>io.github.jspinak</groupId>
    <artifactId>brobot</artifactId>
    <version>1.0.7</version>
</dependency>
```

Your build tool will automatically download the Brobot library and its required transitive dependencies from Maven Central.

## Transitive Dependencies

Brobot is built on top of several powerful open-source libraries. When you add Brobot as a dependency, your build tool will automatically include these as well. You do not need to add them to your build file manually.

The key transitive dependencies include:

* **SikuliX**: The core engine used for all visual automation, screen analysis, and control of the mouse and keyboard.
* **JavaCV (OpenCV)**: Provides the underlying computer vision functionality.
* **Spring Framework**: Used for dependency injection and application configuration.

## Using Unstable (Snapshot) Versions

If you want to use the latest, unreleased features, you can use a `SNAPSHOT` version. These builds are unstable and not recommended for production use.

To use a snapshot version, you must first add the Sonatype OSS Snapshots repository to your build configuration, and then specify a snapshot version for the dependency.

### Gradle (build.gradle)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://s01.oss.sonatype.org/content/repositories/snapshots/' }
}

dependencies {
    implementation 'io.github.jspinak:brobot:1.0.8-SNAPSHOT' // Example version
}
```

### Maven (pom.xml)

```xml
<repositories>
    <repository>
        <id>sonatype-snapshots</id>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.github.jspinak</groupId>
        <artifactId>brobot</artifactId>
        <version>1.0.8-SNAPSHOT</version> </dependency>
</dependencies>
```