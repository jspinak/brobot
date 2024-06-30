---
sidebar_position: 1
---

# A Simple Automation App with Brobot 1.0.7.

When this tutorial was created, version 1.0.7 was still in development and not in Maven Central. 
If you are reading this before 1.0.7 is on Maven Central and want to follow the tutorial,
look first at the tutorial about 
[using the latest code in GitHub](/docs/tutorial-using-the-latest-code-in-github/intro). 
The repository for this project is at https://github.com/jspinak/mrdoob.

This tutorial shows how to set up a Brobot application. In this tutorial, you will create a 
state structure (or model of the GUI environment) manually. First make sure you have the right 
dependencies. If you are using a JAR file, your build.gradle file will look like this:

         plugins {
            id 'java'
            id 'org.springframework.boot' version '3.3.0'
            id 'io.spring.dependency-management' version '1.1.5'
         }
         
         group = 'io.github.jspinak.brobot'
         version = '0.0.1-SNAPSHOT'
         
         java {
            toolchain {
               languageVersion = JavaLanguageVersion.of(20)
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
            implementation 'org.springframework.boot:spring-boot-starter'
            compileOnly 'org.projectlombok:lombok'
            annotationProcessor 'org.projectlombok:lombok'
            testImplementation 'org.springframework.boot:spring-boot-starter-test'
            testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
            implementation('com.sikulix:sikulixapi:2.0.5') {
               exclude group: 'org.slf4j', module: 'slf4j-nop'
            }
            implementation group: 'org.bytedeco', name: 'javacv-platform', version: '1.5.10'
            implementation files('../brobot/library/build/libs/library-1.0.7.jar') // the relative path to the JAR
            implementation 'com.github.kwhat:jnativehook:2.2.2'
         }
         
         tasks.named('test') {
            useJUnitPlatform()
         }

