---
sidebar_position: 1
---

# A Simple Automation App with Brobot 1.1.0.

:::info Version Note
This tutorial was originally created for an earlier version of Brobot but has been updated for version 1.1.0. The original code examples are available in documentation versions 1.0.6 and 1.0.7.
:::

This tutorial demonstrates how to create a Brobot automation application using version 1.1.0. 
The repository for this project is at https://github.com/jspinak/mrdoob.

This tutorial shows how to set up a Brobot application. In this tutorial, you will create a 
state structure (or model of the GUI environment) manually. First make sure you have the right 
dependencies. If you are using a JAR file, your build.gradle file will look like this:

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.0'
    id 'io.spring.dependency-management' version '1.1.5'
}

group = 'io.github.jspinak.brobot'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Brobot 1.1.0+ includes Spring Boot, Lombok, SLF4J, SikuliX, and JavaCV as transitive dependencies
    implementation 'io.github.jspinak:brobot:1.1.0'
    
    // Lombok annotation processor still needed for compilation
    annotationProcessor 'org.projectlombok:lombok:1.18.32'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    
    // Additional dependencies specific to this tutorial
    implementation 'com.github.kwhat:jnativehook:2.2.2'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

