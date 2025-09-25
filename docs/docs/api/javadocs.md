---
sidebar_position: 1
---

# JavaDocs API Reference

## Online JavaDocs

The complete JavaDocs for Brobot are available online:

- **[Current Version (1.0.7)](https://jspinak.github.io/brobot/api/)** - Currently deployed version
- **Version 1.1.0** - Will be available after release at `https://jspinak.github.io/brobot/api/1.1.0/`

After version 1.1.0 is released and deployed:
- **[Latest Version](https://jspinak.github.io/brobot/api/latest/)** - Will point to 1.1.0
- **[Version 1.1.0](https://jspinak.github.io/brobot/api/1.1.0/)** - Specific version documentation

## Maven Central Integration

When you include Brobot in your project via Maven or Gradle, your IDE will automatically download and display the JavaDocs:

### Maven
```xml
<dependency>
    <groupId>io.github.jspinak</groupId>
    <artifactId>brobot</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'io.github.jspinak:brobot:1.1.0'
```

## Generating JavaDocs Locally

To generate JavaDocs locally from the source code:

```bash
# Clone the repository
git clone https://github.com/jspinak/brobot.git
cd brobot

# Generate JavaDocs
./gradlew :library:javadoc

# The JavaDocs will be generated in:
# library/build/docs/javadoc/
```

To generate JavaDocs with versioning (for GitHub Pages deployment):

```bash
# Generate versioned JavaDocs
./gradlew :library:javadocForGitHubPages

# Create latest symlink
./gradlew :library:createLatestSymlink

# The JavaDocs will be in:
# library/build/docs/javadoc/1.1.0/
# library/build/docs/javadoc/latest/
```

## Key API Packages

### Core Packages

- **[io.github.jspinak.brobot.actions](https://jspinak.github.io/brobot/api/latest/io/github/jspinak/brobot/actions/package-summary.html)** - Action execution and management
- **[io.github.jspinak.brobot.datatypes](https://jspinak.github.io/brobot/api/latest/io/github/jspinak/brobot/datatypes/package-summary.html)** - Core data types (State, StateImage, etc.)
- **[io.github.jspinak.brobot.config](https://jspinak.github.io/brobot/api/latest/io/github/jspinak/brobot/config/package-summary.html)** - Configuration and settings

### Action Options

- **[io.github.jspinak.brobot.actions.options](https://jspinak.github.io/brobot/api/latest/io/github/jspinak/brobot/actions/options/package-summary.html)** - Action configuration classes
  - `PatternFindOptions` - Configure pattern matching
  - `ClickOptions` - Configure click actions
  - `TypeOptions` - Configure typing actions
  - `DragOptions` - Configure drag operations

### State Management

- **[io.github.jspinak.brobot.state](https://jspinak.github.io/brobot/api/latest/io/github/jspinak/brobot/state/package-summary.html)** - State machine implementation
- **[io.github.jspinak.brobot.transitions](https://jspinak.github.io/brobot/api/latest/io/github/jspinak/brobot/transitions/package-summary.html)** - State transitions

### Image Processing

- **[io.github.jspinak.brobot.imageUtils](https://jspinak.github.io/brobot/api/latest/io/github/jspinak/brobot/imageUtils/package-summary.html)** - Image utilities and processing
- **[io.github.jspinak.brobot.mockObservations](https://jspinak.github.io/brobot/api/latest/io/github/jspinak/brobot/mockObservations/package-summary.html)** - Mock mode for testing

## Important Classes

### Action Classes
- `Action` - Main class for performing actions
- `ActionBuilder` - Fluent API for building complex actions
- `ConditionalActionChain` - Chain conditional actions

### Data Types
- `State` - Represents an application state
- `StateImage` - Pattern or image to find
- `Region` - Screen region definition
- `Location` - Screen location/point
- `Matches` - Pattern match results

### Configuration
- `FrameworkSettings` - Global framework settings
- `BrobotSettings` - Application-specific settings

## IDE Integration

Most modern IDEs will automatically:
1. Download JavaDocs when you add Brobot as a dependency
2. Show documentation on hover
3. Provide auto-completion with documentation
4. Allow navigation to source code (if sources are attached)

### IntelliJ IDEA
JavaDocs are automatically downloaded and displayed. Press `Ctrl+Q` (Windows/Linux) or `F1` (Mac) to view quick documentation.

### Eclipse
JavaDocs are downloaded with the dependency. Hover over any Brobot class or method to see documentation.

### VS Code
With Java extensions installed, JavaDocs will be displayed on hover and in auto-completion.

## Contributing to Documentation

To improve the JavaDocs:

1. Add JavaDoc comments to your code:
```java
/**
 * Performs a click action on the specified StateImage.
 *
 * @param stateImage The image pattern to click on
 * @return ActionResult containing the result of the action
 * @throws ActionException if the action fails
 */
public ActionResult click(StateImage stateImage) {
    // Implementation
}
```

2. Follow JavaDoc conventions:
   - Use `@param` for parameters
   - Use `@return` for return values
   - Use `@throws` for exceptions
   - Use `@see` for related classes/methods
   - Use `@since` for version information

3. Generate and review locally before submitting PR:
```bash
./gradlew :library:javadoc
# Open library/build/docs/javadoc/index.html in browser
```

## Version History

- **1.1.0** - Upcoming release with enhanced API documentation
- **1.0.7** - Current stable release (JavaDocs available online)
- Previous versions available on [Maven Central](https://central.sonatype.com/artifact/io.github.jspinak/brobot/versions)

For detailed release notes, see the [GitHub Releases](https://github.com/jspinak/brobot/releases) page.