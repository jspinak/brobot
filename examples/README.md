# Brobot Examples

This directory contains working examples that demonstrate various features of the Brobot automation framework.

## Available Examples

### 1. Quick Start (`quick-start/`)
A minimal example showing basic find and click operations.
- Basic StateImage usage
- PatternFindOptions configurations
- Simple action execution
- Spring Boot integration

### 2. Claude Automator (`claude-automator/`)
A complete example demonstrating advanced features:
- Annotation-based state management (`@State`, `@Transition`)
- Continuous monitoring with scheduled tasks
- Action chaining (find → click → type)
- State transitions and navigation
- Automatic state recovery

### 3. Login Automation (`LoginAutomationExample.java`)
A standalone example showing:
- Login workflow automation
- Retry logic implementation
- State verification
- Error handling

## Getting Started

Each example includes:
- Complete source code
- Build configuration (`build.gradle`)
- README with detailed instructions
- Required directory structure

## Building Examples

All examples use Gradle and can be built with:
```bash
cd example-name
./gradlew build
```

## Requirements

- Java 21 or higher
- Gradle 8.x
- Screenshot images of UI elements to automate

## Learning Path

1. Start with `quick-start` to understand basic concepts
2. Move to `LoginAutomationExample.java` for workflow patterns
3. Explore `claude-automator` for advanced features and best practices

## Documentation

For more information, see the main Brobot documentation at:
https://jspinak.github.io/brobot/