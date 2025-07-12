---
sidebar_position: 2
---

# Setup 

:::info Version Note
This tutorial was originally created for an earlier version of Brobot but has been updated for version 1.1.0. The original code examples are available in documentation versions 1.0.6 and 1.0.7.
:::

## Start a new Spring Boot Project

A Spring Boot project can be created with the [Spring Initializr](https://start.spring.io/)
or directly in [Intellij](https://www.jetbrains.com/help/idea/spring-boot.html)
or [Spring Tools 4](https://marketplace.eclipse.org/content/spring-tools-4-aka-spring-tool-suite-4),
which works with Eclipse, Visual Studio Code, and Atom IDE.

## Add the Brobot Library
Version 1.1.0 is recommended for the latest features.  

Maven:

```xml
<dependency>
  <groupId>io.github.jspinak</groupId>
  <artifactId>brobot</artifactId>
  <version>1.1.0</version>
</dependency>
```

Gradle:

```gradle
implementation 'io.github.jspinak:brobot:1.1.0'
``` 

Some tips: 
- Note that Brobot 1.1.0 requires Java 21 or later.
- Don't forget to add JavaPoet as a dependency. JavaPoet writes the files in your 
State Structure.
- OpenCV is now included as a transitive dependency through JavaCV, so you don't need to install it separately.
All platforms (Windows, Mac, Linux) are supported out of the box.  

If you are using gradle, the full build.gradle file should look similar to this:

```gradle
plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'java'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '21'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Brobot 1.1.0+ includes Spring Boot, Lombok, SLF4J, and SikuliX as transitive dependencies
    implementation 'io.github.jspinak:brobot:1.1.0'
    
    // Lombok annotation processor still needed for compilation
    annotationProcessor 'org.projectlombok:lombok:1.18.32'
    
    // JavaPoet for code generation in State Structure Builder
    implementation 'com.squareup:javapoet:1.13.0'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

## Create Folders for Images

- Create a folder called `images` to hold the images
used by your Brobot application to manipulate GUI elements.   
- Create a folder called `screenshots` to store screenshots captured
by your Brobot application while you manipulate the target environment manually.  

The folder structure should look like this (the build folder will be created
automatically if using Gradle):  

<img src="https://jspinak.github.io/brobot/img/visual API folders.png" alt="project structure" width="200"/>
