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
* **JavaCV Platform (1.5.3)**: Provides the underlying computer vision functionality.
* **OpenCV Platform (4.3.0-1.5.3)**: Computer vision algorithms for image recognition (version matched to SikuliX 2.0.5).
* **FFmpeg Platform (4.2.2-1.5.3)**: Media handling capabilities.
* **Apache Commons Lang3 (3.0)**: Common utilities and helper methods.
* **Spring Context**: Core Spring Framework for dependency injection.
* **Spring Boot Autoconfigure**: Automatic configuration support for Spring Boot applications.
* **Spring Boot Starter AOP**: Aspect-oriented programming support for Brobot's internal features.
* **Jakarta Annotation API (2.1.1)**: Support for annotations like `@PostConstruct`.
* **SLF4J API (2.0.9)**: Logging facade for consistent logging across the application.

### What This Means for Your Project

With Brobot 1.1.0+, you only need to add the Brobot dependency. The following are included automatically:
- All computer vision libraries (SikuliX, OpenCV, JavaCV, FFmpeg)
- Spring Framework components (Context, Boot Autoconfigure, AOP)
- Logging framework (SLF4J API)
- Jakarta annotations support

You'll still need to add:
- **Spring Boot Starter** (if running as a Spring Boot application)
- **Spring Boot Starter Test** (for testing)
- **Lombok** (NOT a transitive dependency - you must add it yourself)
- A concrete SLF4J implementation (like Logback) if not using Spring Boot
- Any specific implementation libraries your project needs

### Note on Lombok

**Important**: Lombok is NOT included as a transitive dependency. You must add it to your project:

**Gradle:**
```groovy
dependencies {
    implementation 'io.github.jspinak:brobot:1.1.0'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

**Maven:**
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

You also need to configure your IDE to recognize Lombok annotations:
- **IntelliJ IDEA**: Install the Lombok plugin
- **Eclipse**: Install the Lombok plugin
- **VS Code**: Install the Lombok Annotations Support extension

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
    implementation 'io.github.jspinak:brobot:1.1.1-SNAPSHOT' // Example version
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