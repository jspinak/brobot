# Brobot Examples

This folder contains example projects demonstrating Brobot features, organized to match the documentation structure.

## Structure

Examples are organized by documentation section:

### 01-getting-started/
- **quick-start** - Basic Brobot setup and simple automation
- **pure-actions-quickstart** - New Pure Actions API for cleaner code
- **action-hierarchy** - Understanding Basic and Complex Actions

### 03-core-library/

#### guides/
- **automatic-action-logging** - Built-in logging functionality
- **advanced-illustration-system** - Visual debugging and analysis
- **finding-objects/**
  - **using-color** - Color-based object detection
  - **combining-finds** - Nested and confirmed find operations
  - **movement** - Motion detection across scenes

#### action-config/
- **examples** - Form automation and validation
- **conditional-chains-examples** - ConditionalActionChain patterns

#### tutorials/
- **tutorial-basics** - State management fundamentals
- **tutorial-claude-automator** - Annotation-driven automation

### 04-testing/
- **unit-testing** - Testing Brobot automations
- **enhanced-mocking** - Advanced mocking capabilities

## Running Examples

Each example is a standalone Spring Boot project:

1. Navigate to the example directory
2. Build: `./gradlew build`
3. Run: `./gradlew bootRun`
4. See the example's README for specific instructions

## Documentation Links

Each example corresponds to documentation pages. The README in each project shows the exact documentation page(s) it demonstrates.

## Prerequisites

- Java 11 or higher
- Gradle (wrapper included in each project)
- Brobot 1.1.0 or higher

## Finding Examples

To find examples for a specific topic:
1. Check the documentation page you're reading
2. Look for the corresponding example in this folder structure
3. The example code matches the documentation code exactly