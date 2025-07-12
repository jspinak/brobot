# Project Setup

## Directory Structure

Create the following directory structure for your Claude Automator project:

```
claude-automator/
├── src/main/java/com/claude/automator/
│   ├── states/
│   ├── transitions/
│   ├── automation/
│   ├── config/
│   └── ClaudeAutomatorApplication.java
├── src/main/resources/
│   ├── application.properties
│   └── images/
│       ├── working/
│       │   ├── claude-icon-1.png
│       │   ├── claude-icon-2.png
│       │   ├── claude-icon-3.png
│       │   └── claude-icon-4.png
│       └── prompt/
│           └── claude-prompt.png
├── build.gradle
└── settings.gradle
```

## Gradle Configuration

### build.gradle

```gradle
plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'java'
}

group = 'com.claude'
version = '1.0.0'
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
    
    // Additional dependencies for this project
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
    
    // Lombok annotation processor still needed for compilation
    annotationProcessor 'org.projectlombok:lombok:1.18.32'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

test {
    useJUnitPlatform()
}

// Copy images to build directory
task copyImages(type: Copy) {
    from 'images'
    into "$buildDir/resources/main/images"
}

processResources.dependsOn copyImages
```

### settings.gradle (for Local Development)

```gradle
rootProject.name = 'claude-automator'

// Use local Brobot library for development
includeBuild('../brobot') {
    dependencySubstitution {
        substitute module('io.github.jspinak:brobot') using project(':library')
    }
}
```

:::tip Local Development
The `includeBuild` configuration allows you to use your local Brobot library instead of the Maven Central version. This is perfect for:
- Testing new Brobot features
- Contributing to Brobot development
- Debugging library issues
:::

## Application Properties

Create `src/main/resources/application.yml`:

```yaml
# Spring Boot Configuration
spring:
  application:
    name: claude-automator

# Logging Configuration
logging:
  level:
    com.claude.automator: DEBUG
    io.github.jspinak.brobot: INFO

# Brobot Configuration (v1.1.0+)
brobot:
  core:
    image-path: classpath:images/   # Images loaded from classpath
    mock: false
    headless: false
  startup:
    verify-initial-states: true
    initial-states: PROMPT
    fallback-search: false
    startup-delay: 2
  sikuli:
    highlight: true
```

:::tip Configuration Formats
You can use either `application.properties` or `application.yml`. The YAML format shown above provides better structure for nested properties.
:::

## Image Preparation

Place your screenshots in the appropriate folders:

1. **Working State Images** (`images/working/`):
   - Multiple variations of Claude's response icon
   - Name them: `claude-icon-1.png`, `claude-icon-2.png`, etc.

2. **Prompt State Images** (`images/prompt/`):
   - Screenshot of Claude's prompt interface
   - Name it: `claude-prompt.png`

:::warning Image Guidelines
- Remove `.png` extensions when referencing images in code
- Crop images to show only the relevant UI element
- Include multiple variations if the element changes appearance
:::

## Main Application Class

Create `src/main/java/com/claude/automator/ClaudeAutomatorApplication.java`:

```java
package com.claude.automator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.claude.automator",
    "io.github.jspinak.brobot"  // Include Brobot components
})
public class ClaudeAutomatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClaudeAutomatorApplication.class, args);
    }
}
```

## Next Steps

With the project structure in place, we'll start implementing the states using modern Brobot patterns.