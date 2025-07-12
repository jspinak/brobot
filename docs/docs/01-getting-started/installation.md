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
implementation 'io.github.jspinak:brobot:1.1.0'
```

### Maven

In your `pom.xml` file, add the following within your `<dependencies>` block:

```xml
<dependency>
    <groupId>io.github.jspinak</groupId>
    <artifactId>brobot</artifactId>
    <version>1.1.0</version>
</dependency>
```

Your build tool will automatically download the Brobot library and its required transitive dependencies from Maven Central.

## Transitive Dependencies

Brobot is built on top of several powerful open-source libraries. When you add Brobot as a dependency, your build tool will automatically include these as well. You do not need to add them to your build file manually.

Starting from version 1.1.0, Brobot includes the following transitive dependencies:

### Core Dependencies (Automatically Included)

* **SikuliX API (2.0.5)**: The core engine used for all visual automation, screen analysis, and control of the mouse and keyboard.
* **JavaCV Platform (1.5.10)**: Provides the underlying computer vision functionality.
* **OpenCV Platform (4.9.0-1.5.10)**: Computer vision algorithms for image recognition.
* **FFmpeg Platform (6.1.1-1.5.10)**: Media handling capabilities.
* **Apache Commons Lang3 (3.0)**: Common utilities and helper methods.
* **Spring Context**: Core Spring Framework for dependency injection.
* **Spring Boot Autoconfigure**: Automatic configuration support for Spring Boot applications.
* **SLF4J API (2.0.9)**: Logging facade for consistent logging across the application.
* **Project Lombok (1.18.32)**: Reduces boilerplate code with annotations like `@Getter`, `@Setter`, etc.

### What This Means for Your Project

With Brobot 1.1.0+, you only need to add the Brobot dependency. The following are included automatically:
- All computer vision libraries (SikuliX, OpenCV, JavaCV)
- Spring Framework components
- Logging framework (SLF4J)
- Lombok for cleaner code

You'll still need to add:
- Spring Boot Starter Test (for testing)
- Any specific implementation libraries your project needs
- A concrete SLF4J implementation (like Logback) if not using Spring Boot

### Note on Lombok
Since Lombok is now a transitive dependency, you still need to configure your IDE to recognize Lombok annotations:
- **IntelliJ IDEA**: Install the Lombok plugin
- **Eclipse**: Install the Lombok plugin
- **VS Code**: Install the Lombok Annotations Support extension

You also need to add the annotation processor to your build file:
```groovy
annotationProcessor 'org.projectlombok:lombok:1.18.32'
```

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