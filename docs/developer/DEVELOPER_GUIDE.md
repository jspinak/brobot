# Brobot Developer Guide

Complete guide for developers working on the Brobot framework itself.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Module Structure](#module-structure)
- [Core Concepts](#core-concepts)
- [Development Setup](#development-setup)
- [Building and Testing](#building-and-testing)
- [Debugging Techniques](#debugging-techniques)
- [Performance Optimization](#performance-optimization)
- [Common Development Tasks](#common-development-tasks)
- [Release Process](#release-process)

## Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────┐
│                  User Application                    │
│            (Uses Brobot Public APIs)                 │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│              Brobot Library (library)                │
│  ┌──────────────┬──────────────┬──────────────┐    │
│  │   Actions    │    State     │  Datatypes   │    │
│  │   (Find,     │  Management  │  (Pattern,   │    │
│  │   Click,     │  (States &   │   Region,    │    │
│  │   Drag...)   │  Transitions)│   Match...)  │    │
│  └──────┬───────┴──────┬───────┴──────┬───────┘    │
│         │              │              │             │
│  ┌──────▼──────────────▼──────────────▼───────┐    │
│  │        SikuliX Integration Layer           │    │
│  │     (Screen capture, image matching)        │    │
│  └─────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│                SikuliX Library                       │
│         (OpenCV, Tesseract, Robot)                   │
└─────────────────────────────────────────────────────┘
```

### Design Principles

1. **Builder Pattern for Configuration**
   - All ActionConfig classes use builders
   - Immutable configuration objects
   - Fluent API for chaining

2. **State-Based Automation**
   - Application states model UI screens
   - Transitions represent navigation
   - Automatic state management

3. **Action-Oriented API**
   - High-level action abstractions
   - Composition over inheritance
   - Chainable action sequences

4. **Spring Boot Integration**
   - Dependency injection throughout
   - Auto-configuration for easy setup
   - Properties-based configuration

## Module Structure

### library (Core Framework)

**Purpose:** Core Brobot functionality without Spring Boot dependencies.

```
library/src/main/java/io/github/jspinak/brobot/
├── action/                     # Action implementations
│   ├── basic/                 # Basic actions (find, click, type)
│   ├── composite/             # Composite actions (drag, scroll)
│   ├── config/                # Action configuration classes
│   └── internal/              # Internal action utilities
├── datatypes/                  # Core data structures
│   ├── primitives/            # Basic types (Location, Region, Match)
│   └── state/                 # State management types
├── monitor/                    # Multi-monitor support
├── capture/                    # Screen capture utilities
├── matcher/                    # Image matching algorithms
├── tools/                      # Utility classes
│   ├── history/               # Illustration system
│   └── colorManagement/       # Color-based finding
└── config/                     # Configuration classes
    └── core/                  # BrobotProperties
```

**Key Classes:**
- `Action` - Main action executor
- `StateService` - State management
- `PatternFindOptions`, `ClickOptions`, etc. - Configuration
- `StateImage`, `StateRegion`, `StateLocation` - State objects

### library-test (Integration Tests)

**Purpose:** Integration tests requiring Spring Boot context.

```
library-test/src/test/java/
├── action/                    # Action integration tests
├── state/                     # State management tests
└── config/                    # Configuration tests
```

**Why Separate?**
- Faster library builds (no Spring Boot startup)
- Clear separation of unit vs integration tests
- Prevents circular dependencies

### docs (Documentation)

```
docs/
├── docs/                      # User documentation (published)
│   ├── 01-getting-started/
│   ├── 03-core-library/
│   ├── 04-testing/
│   └── ...
└── developer/                 # Developer documentation (this folder)
    ├── CONTRIBUTING.md
    ├── DEVELOPER_GUIDE.md
    └── API_DOCUMENTATION_TEMPLATE.md
```

### examples (Example Projects)

Working examples demonstrating Brobot features.

## Core Concepts

### 1. Actions and ActionConfig

**Pattern:** Builder-based configuration with execution separation.

```java
// Configuration is immutable
PatternFindOptions config = new PatternFindOptions.Builder()
    .setStrategy(PatternFindOptions.Strategy.BEST)
    .setSimilarity(0.85)
    .setIllustrate(ActionConfig.Illustrate.YES)
    .build();

// Execution uses configuration
ActionResult result = action.perform(config, stateImage);
```

**Key Classes:**
- `ActionConfig` - Abstract base for all configs
- `PatternFindOptions` - Find action configuration
- `ClickOptions` - Click action configuration
- `ActionResult` - Execution results

### 2. State Management

**Pattern:** Declarative state definition with automatic management.

```java
@State(initial = true)
public class LoginState {

    @StateImage
    public StateImage loginButton = new StateImage.Builder()
        .addPattern("login-button")
        .build();
}

@Transition(from = "LoginState", to = "DashboardState")
public class LoginTransition {
    public boolean execute() {
        return action.click(loginState.loginButton).isSuccess();
    }
}
```

**Key Components:**
- `@State` annotation - Marks state classes
- `@Transition` annotation - Defines state transitions
- `StateService` - Manages active states
- `TransitionService` - Executes transitions

### 3. Image Matching

**Pattern:** OpenCV-based similarity matching with Brobot abstractions.

```java
// Pattern is loaded from images/ directory
Pattern pattern = new Pattern("button.png");

// Matching happens through SikuliX
Match match = screen.find(pattern.sikuli());

// Wrapped in Brobot types
io.github.jspinak.brobot.datatypes.primitives.match.Match brobotMatch =
    new Match(match);
```

**Similarity Calculation:**
- Range: 0.0 (no match) to 1.0 (perfect match)
- Default threshold: 0.7
- OpenCV template matching algorithm
- Sub-pixel accuracy

### 4. Mock Mode

**Pattern:** Testing without GUI dependencies.

```java
// Enabled via properties
brobot.core.mock=true

// Mock timings (fast for tests)
brobot.mock.time-find-first=0.01
brobot.mock.time-click=0.01

// BrobotTestBase enables mock mode automatically
public class MyTest extends BrobotTestBase {
    @Test
    public void testAction() {
        // Runs in mock mode, no real screen interaction
    }
}
```

## Development Setup

### IDE Configuration (IntelliJ IDEA)

1. **Import Project**
   ```
   File → Open → Select brobot/build.gradle
   Import as Gradle project
   ```

2. **Enable Annotation Processing**
   ```
   Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   ☑ Enable annotation processing
   ```

3. **Configure Lombok**
   ```
   Settings → Plugins → Install "Lombok"
   Restart IDE
   ```

4. **Set Java SDK**
   ```
   File → Project Structure → Project SDK: 17 or higher
   Language Level: 17
   ```

5. **Code Style**
   ```
   Settings → Editor → Code Style → Java
   - Indent: 4 spaces
   - Continuation indent: 4 spaces
   - Keep maximum blank lines: 2
   ```

### Build Configuration

**gradle.properties (local):**
```properties
# Increase heap for large builds
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m

# Enable parallel builds
org.gradle.parallel=true
org.gradle.caching=true

# Daemon configuration
org.gradle.daemon=true
```

### Environment Variables

```bash
# Java home
export JAVA_HOME=/path/to/java17

# Gradle home (optional)
export GRADLE_HOME=/path/to/gradle

# Display for GUI tests (Linux)
export DISPLAY=:0
```

## Building and Testing

### Build Commands

```bash
# Clean build
./gradlew clean build

# Build without tests
./gradlew build -x test

# Build specific module
./gradlew :library:build

# Rebuild everything
./gradlew clean build --rerun-tasks

# Build with info logging
./gradlew build --info
```

### Testing Strategies

#### 1. Unit Tests (Fast, Isolated)

```bash
# Run library unit tests only
./gradlew :library:test --no-daemon

# Run specific test class
./gradlew :library:test --tests "PatternMatcherTest" --no-daemon

# Run with coverage
./gradlew :library:test jacocoTestReport
```

#### 2. Integration Tests (Spring Context)

```bash
# Run integration tests
./gradlew :library-test:test --no-daemon

# Run with Spring profile
./gradlew :library-test:test -Dspring.profiles.active=test
```

#### 3. Parallel Test Execution

For the 6000+ test suite:

```bash
# Python test runner (recommended for large suites)
python3 library/scripts/run-all-tests.py library --mode parallel --workers 8

# Individual execution (slower but more stable)
python3 library/scripts/run-all-tests.py library --mode sequential
```

### Test Debugging

**Enable Debug Logging:**
```properties
# application-test.properties
logging.level.io.github.jspinak.brobot=DEBUG
brobot.logging.verbosity=VERBOSE
```

**Run Single Test with Debugging:**
```bash
# IntelliJ: Right-click test method → Debug
# Command line with debugging
./gradlew :library:test --tests "MyTest.testMethod" --debug-jvm
```

**Common Test Issues:**

1. **Tests Hang**
   - Use `--no-daemon` flag
   - Kill existing daemons: `./gradlew --stop`
   - Use Python test runner

2. **Headless Exceptions**
   - Ensure tests extend `BrobotTestBase`
   - Check mock mode is enabled
   - Verify `brobot.core.mock=true`

3. **Out of Memory**
   - Increase heap: `-Xmx4g`
   - Use parallel execution with fewer workers
   - Run tests in smaller batches

## Debugging Techniques

### 1. Illustration System for Visual Debugging

```java
// Enable illustrations for debugging
PatternFindOptions debug = new PatternFindOptions.Builder()
    .setIllustrate(ActionConfig.Illustrate.YES)
    .build();

// Creates screenshot: history/20240115_103045_FIND_button.png
action.perform(debug, stateImage);
```

**Use Cases:**
- Verify search regions
- Check match quality
- Debug false positives/negatives
- Understand action flow

### 2. Logging Configuration

```properties
# Detailed action logging
brobot.console.actions.enabled=true
brobot.console.actions.level=VERBOSE

# State transition logging
brobot.console.transitions.enabled=true

# Performance metrics
brobot.console.performance.enabled=true
```

### 3. Breakpoint Strategies

**Strategic Breakpoints:**
```java
// Action execution
io.github.jspinak.brobot.action.Action.perform()

// Pattern matching
io.github.jspinak.brobot.matcher.ScreenObserver.findPattern()

// State transitions
io.github.jspinak.brobot.state.TransitionService.execute()
```

### 4. Mock Mode Debugging

```java
// Override mock timings for debugging
@TestPropertySource(properties = {
    "brobot.mock.time-find-first=1.0",  // Slower for observation
    "brobot.core.mock=true"
})
public class MyDebugTest extends BrobotTestBase {
    // Tests run slower for debugging
}
```

## Performance Optimization

### Profiling Guidelines

#### 1. Action Performance

**Measure action timing:**
```java
long start = System.currentTimeMillis();
ActionResult result = action.perform(config, stateImage);
long duration = System.currentTimeMillis() - start;
System.out.println("Action took: " + duration + "ms");
```

**Expected Timings:**
- Find (first match): 100-500ms
- Find (subsequent): 50-200ms
- Click: 10-50ms
- Type: 50-200ms

#### 2. Image Matching Optimization

**Reduce search region:**
```java
// Bad: Search entire screen
PatternFindOptions slow = new PatternFindOptions.Builder().build();

// Good: Search specific region
Region searchArea = new Region(100, 100, 300, 200);
PatternFindOptions fast = new PatternFindOptions.Builder()
    .setSearchRegions(List.of(searchArea))
    .build();
```

**Adjust similarity:**
```java
// Lower similarity = faster matching (but less accurate)
.setSimilarity(0.70)  // Fast

// Higher similarity = slower matching (but more accurate)
.setSimilarity(0.95)  // Slow
```

#### 3. Parallel Execution

```java
// Execute multiple finds in parallel
CompletableFuture<ActionResult> future1 =
    CompletableFuture.supplyAsync(() -> action.find(image1));
CompletableFuture<ActionResult> future2 =
    CompletableFuture.supplyAsync(() -> action.find(image2));

// Wait for all
CompletableFuture.allOf(future1, future2).join();
```

### Memory Management

**Monitor illustration storage:**
```bash
# Check history directory size
du -sh history/

# Clean old illustrations
find history/ -name "*.png" -mtime +7 -delete
```

**Heap sizing for tests:**
```bash
# Small test suite
-Xmx2g

# Large test suite (6000+ tests)
-Xmx4g -XX:MaxMetaspaceSize=512m
```

## Common Development Tasks

### Adding a New Action

1. **Create Options Class**
   ```java
   @Getter
   @Builder(toBuilder = true, builderClassName = "Builder")
   public class MyActionOptions extends ActionConfig {
       private final String specificOption;

       protected MyActionOptions(Builder builder) {
           super(builder);
           this.specificOption = builder.specificOption;
       }

       public static class Builder extends ActionConfig.Builder<Builder> {
           private String specificOption;

           public Builder setSpecificOption(String value) {
               this.specificOption = value;
               return self();
           }

           @Override
           protected Builder self() {
               return this;
           }

           @Override
           public MyActionOptions build() {
               return new MyActionOptions(this);
           }
       }
   }
   ```

2. **Implement Action Logic**
   ```java
   @Component
   public class MyAction {

       public ActionResult execute(MyActionOptions options, ObjectCollection objects) {
           // Implementation
           return new ActionResult(/* results */);
       }
   }
   ```

3. **Add to Action Facade**
   ```java
   // In Action class
   public ActionResult myAction(StateImage image) {
       MyActionOptions options = new MyActionOptions.Builder().build();
       return myActionExecutor.execute(options, toCollection(image));
   }
   ```

4. **Write Tests**
   ```java
   public class MyActionTest extends BrobotTestBase {
       @Test
       public void testMyAction() {
           // Test implementation
       }
   }
   ```

5. **Document**
   - Add Javadoc to Options class
   - Create user guide in `/docs/docs/`
   - Add example to examples project

### Adding a Configuration Property

1. **Add to BrobotProperties**
   ```java
   @ConfigurationProperties(prefix = "brobot")
   public class BrobotProperties {

       private final MyFeature myFeature = new MyFeature();

       @Getter
       @Setter
       public static class MyFeature {
           private boolean enabled = false;
           private int maxAttempts = 3;
       }
   }
   ```

2. **Document in properties-reference.md**
   ```markdown
   | `brobot.my-feature.enabled` | boolean | false | Enable my feature |
   | `brobot.my-feature.max-attempts` | int | 3 | Maximum attempts |
   ```

3. **Add Configuration Test**
   ```java
   @Test
   public void testMyFeatureConfiguration() {
       assertFalse(brobotProperties.getMyFeature().isEnabled());
       assertEquals(3, brobotProperties.getMyFeature().getMaxAttempts());
   }
   ```

### Updating Documentation

**User Documentation Checklist:**
- [ ] Update relevant guide in `/docs/docs/`
- [ ] Add complete code examples with imports
- [ ] Include expected output
- [ ] Update cross-references
- [ ] Test all code examples compile
- [ ] Add to navigation if new guide

**API Documentation Checklist:**
- [ ] Javadoc on all public methods
- [ ] Parameter descriptions with ranges/constraints
- [ ] Return value documentation
- [ ] Exception documentation
- [ ] Thread safety notes
- [ ] Example usage in complex APIs

## Release Process

### Version Numbering

Brobot follows [Semantic Versioning](https://semver.org/):

```
MAJOR.MINOR.PATCH

1.1.0 → 1.2.0  (new features, backward compatible)
1.1.0 → 2.0.0  (breaking changes)
1.1.0 → 1.1.1  (bug fixes only)
```

### Pre-Release Checklist

- [ ] All tests pass
- [ ] Documentation updated
- [ ] CHANGELOG.md updated
- [ ] Version numbers updated in build.gradle
- [ ] Breaking changes documented
- [ ] Migration guide created (if breaking changes)

### Release Steps

1. **Update Version**
   ```bash
   # Update build.gradle
   version = '1.2.0'
   ```

2. **Update CHANGELOG**
   ```markdown
   ## [1.2.0] - 2024-01-15

   ### Added
   - New feature description

   ### Changed
   - Changed feature description

   ### Fixed
   - Bug fix description

   ### Breaking Changes
   - Breaking change description with migration path
   ```

3. **Create Release Branch**
   ```bash
   git checkout -b release/1.2.0
   git push origin release/1.2.0
   ```

4. **Build and Test**
   ```bash
   ./gradlew clean build --rerun-tasks
   ./gradlew test --no-daemon
   ```

5. **Tag Release**
   ```bash
   git tag -a v1.2.0 -m "Release version 1.2.0"
   git push origin v1.2.0
   ```

6. **Publish**
   ```bash
   # Publish to Maven Central (if configured)
   ./gradlew publish
   ```

### Post-Release

- [ ] Merge release branch to main
- [ ] Update documentation site
- [ ] Announce release (GitHub Releases, etc.)
- [ ] Monitor for issues

## Advanced Topics

### Custom Matcher Implementation

To implement a custom image matching algorithm:

```java
public class CustomMatcher implements MatcherInterface {

    @Override
    public List<Match> find(Pattern pattern, BufferedImage screenshot) {
        // Custom matching logic
        return matches;
    }

    @Override
    public double calculateSimilarity(BufferedImage img1, BufferedImage img2) {
        // Custom similarity calculation
        return similarity;
    }
}
```

### Custom State Transition Logic

```java
@Component
public class ComplexTransition {

    @Autowired
    private Action action;

    @Autowired
    private StateService stateService;

    public boolean execute() {
        // Complex multi-step transition logic
        if (action.find(image1).isSuccess()) {
            stateService.activateState("State1");
            return true;
        }
        return false;
    }
}
```

### Multi-Monitor Custom Configuration

```java
@Configuration
public class CustomMonitorConfig {

    @Bean
    public MonitorManager customMonitorManager() {
        MonitorManager manager = new MonitorManager();
        manager.setOperationMonitor("find", 1);
        manager.setOperationMonitor("click", 2);
        return manager;
    }
}
```

## Resources

- **User Documentation:** `/docs/docs/`
- **API Documentation Template:** [API_DOCUMENTATION_TEMPLATE.md](./API_DOCUMENTATION_TEMPLATE.md)
- **Contributing Guide:** [CONTRIBUTING.md](./CONTRIBUTING.md)
- **Example Projects:** `/examples/`
- **Test Utilities:** `/library/src/test/java/io/github/jspinak/brobot/test/`

## Getting Help

- **GitHub Issues:** Bug reports and feature requests
- **GitHub Discussions:** Questions and community support
- **Code Reviews:** Submit PR for feedback

---

Happy coding! If you have questions or need clarification on any topic, please open a discussion or issue.
