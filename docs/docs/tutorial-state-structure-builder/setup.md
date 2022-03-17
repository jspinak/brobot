---
sidebar_position: 2
---

# Setup 

## Start a new Spring Boot Project

A Spring Boot project can be created with the [Spring Initializr](https://start.spring.io/)
or directly in [Intellij](https://www.jetbrains.com/help/idea/spring-boot.html)
or [Spring Tools 4](https://marketplace.eclipse.org/content/spring-tools-4-aka-spring-tool-suite-4),
which works with Eclipse, Visual Studio Code, and Atom IDE.

## Add the Brobot Library
Version 1.0.3 is needed.  

Maven:

        <dependency>
          <groupId>io.github.jspinak</groupId>
          <artifactId>brobot</artifactId>
          <version>1.0.3</version>
        </dependency>

Gradle:

        implementation 'io.github.jspinak:brobot:1.0.3' 

Note that Brobot works with Java version 14 or later.  
Don't forget to add JavaPoet as a dependency. JavaPoet writes the files in your 
State Structure.  
The full build.gradle file should look similar to this:

        plugins {
            id 'org.springframework.boot' version '2.6.2'
            id 'io.spring.dependency-management' version '1.0.11.RELEASE'
            id 'java'
        }
        
        group = 'com.example'
        version = '0.0.1-SNAPSHOT'
        sourceCompatibility = '14'
        
        configurations {
            compileOnly {
                extendsFrom annotationProcessor
            }
        }
        
        repositories {
            mavenCentral()
        }
        
        dependencies {
            implementation 'org.springframework.boot:spring-boot-starter'
            compileOnly 'org.projectlombok:lombok'
            annotationProcessor 'org.projectlombok:lombok'
            testImplementation 'org.springframework.boot:spring-boot-starter-test'
            implementation 'io.github.jspinak:brobot:1.0.3'
            implementation('com.sikulix:sikulixapi:2.0.5') {
                exclude group: 'org.slf4j', module: 'slf4j-nop'
            }
            implementation group: 'com.squareup', name: 'javapoet', version: '1.9.0'
        }

## Create Folders for Images

- Create a folder called `images` to hold the images
used by your Brobot application to manipulate GUI elements.   
- Create a folder called `screenshots` to store screenshots captured
by your Brobot application while you manipulate the target environment manually.  

The folder structure should look like this (the build folder will be created
automatically if using Gradle):  

<img src="https://jspinak.github.io/brobot/img/visual API folders.png" alt="project structure" width="200"/>
