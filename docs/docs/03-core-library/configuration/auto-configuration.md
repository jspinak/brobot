# Auto-Configuration in Brobot

Brobot provides comprehensive auto-configuration capabilities that eliminate the need for applications to manually configure most framework settings. This document describes the auto-configuration features that are built into the Brobot library.

## Overview

Brobot applications should require minimal to no configuration files. The framework automatically handles:
- DPI scaling and cross-version compatibility
- Image path resolution and verification
- Console reporter output levels
- Mock mode optimization for testing
- Cross-platform compatibility

### Auto-Configuration Architecture

Brobot's auto-configuration system is built on Spring Boot's auto-configuration mechanism:

1. **Discovery**: `META-INF/spring.factories` registers `BrobotAutoConfiguration` for automatic discovery
2. **Property Binding**: Spring Boot binds `brobot.*` properties to `@ConfigurationProperties` classes
3. **Conditional Beans**: `@ConditionalOnMissingBean` allows applications to override defaults
4. **Profile-Based**: `brobot-defaults.properties` and `brobot-test-defaults.properties` provide environment-specific defaults
5. **Early Initialization**: `ApplicationContextInitializer` classes run before bean creation for system-level setup

**Configuration Layers** (in order of precedence):
1. Application's `application.properties` (highest priority)
2. Profile-specific properties (`application-{profile}.properties`)
3. Brobot profile defaults (`brobot-test-defaults.properties` for test profile)
4. Brobot base defaults (`brobot-defaults.properties`)
5. Java system properties
6. Auto-detected values (DPI, paths, environment)

This layered approach ensures applications can override any setting while maintaining sensible defaults for all environments.

## Built-in Configuration Classes

### 1. DPIConfiguration

**Purpose**: Automatically detects and compensates for DPI scaling differences across different display configurations.

**Features**:
- Detects Windows DPI scaling (125%, 150%, etc.)
- Applies appropriate pattern scaling compensation
- Configures consistent pattern matching across different DPIs

**Auto-configured SikuliX settings**:
```java
// These SikuliX Settings are automatically configured by DPIConfiguration
org.sikuli.basics.Settings.AlwaysResize    // Set based on DPI scaling factor
org.sikuli.basics.Settings.MinSimilarity    // Adjusted for scaling differences (typically 0.7-0.8)
org.sikuli.basics.Settings.CheckLastSeen    // Enabled (true) for performance optimization
```

**How it works**:
- Detects DPI via `DPIAutoDetector.detectDPIScaleFactor()`
- Applies resize factor to `Settings.AlwaysResize` when DPI > 1.0
- Configures SikuliX before any pattern matching occurs

**Configuration**: Usually auto-detected, but can be manually overridden via `brobot.dpi.resize-factor` property

### 2. LoggingVerbosityConfig

**Purpose**: Automatically configures logging output levels based on verbosity settings.

**Verbosity Levels**:
- `QUIET` - Minimal single-line output (✓/✗ Action State.Object • timing)
- `NORMAL` - Essential information (action type, state/object, results, match coordinates)
- `VERBOSE` - All information (everything from NORMAL plus timing, search regions, match scores, performance metrics)

**Auto-configured via**:
```java
@ConfigurationProperties(prefix = "brobot.logging")
public class LoggingVerbosityConfig {
    private VerbosityLevel verbosity = VerbosityLevel.NORMAL;  // Default
    private NormalModeConfig normal;   // Controls showTiming, showMatchCoordinates, etc.
    private VerboseModeConfig verbose; // Controls showSearchRegions, showMatchScores, etc.
}
```

**Configuration**: Set via `brobot.logging.verbosity` property (values: QUIET, NORMAL, VERBOSE)

### 3. Image Find Debugging (New)

**Purpose**: Provides comprehensive debugging for image finding operations with colorful console output and visual reports.

**Features**:
- Colorful console output with success/failure indicators
- Visual annotations on screenshots
- HTML/JSON reports with statistics
- Pattern vs match comparison grids

**Auto-configured when enabled**:
- AOP interceptors for Find operations
- Visual debug renderer
- Report generator
- Session management

**Configuration**: Enable via `brobot.debug.image.enabled=true`

**See**: [Image Find Debugging Guide](../tools/image-find-debugging.md) and [Properties Reference](./properties-reference.md#image-find-debugging)

### 4. ImagePathManager

**Purpose**: Intelligent image path resolution with multiple fallback strategies.

**Features**:
- Automatic path resolution strategies (absolute, relative, classpath, JAR)
- JAR extraction for packaged applications
- Image verification and diagnostics
- Alternative location checking

**How it works**:
```java
@Component
public class ImagePathManager {
    private final List<PathResolver> resolvers = Arrays.asList(
        new AbsolutePathResolver(),      // Try as absolute path
        new WorkingDirectoryResolver(),   // Try relative to working directory
        new ClasspathResolver(),          // Try classpath resources
        new JarRelativeResolver(),        // Extract from JAR if needed
        new CommonLocationResolver()      // Try common locations (src/main/resources/images, etc.)
    );

    public synchronized void initialize(String basePath) {
        // Resolve path using strategy pattern
        Path path = Paths.get(basePath);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(basePath);
        }

        // Create directory if needed
        Files.createDirectories(path);

        // Configure SikuliX ImagePath
        ImagePath.setBundlePath(path.toString());
    }
}
```

**Key Methods**:
```java
// Verify expected images and directories
boolean verifyImageSetup(List<String> expectedDirs, List<String> expectedImages)

// Get diagnostic information (lines 216-227)
Map<String, Object> getDiagnostics()
```

**Resolution Strategies**:
1. **AbsolutePathResolver** - Direct absolute paths
2. **WorkingDirectoryResolver** - Paths relative to `user.dir`
3. **ClasspathResolver** - Resources on classpath
4. **JarRelativeResolver** - Extract from JAR to temp directory
5. **CommonLocationResolver** - Standard locations (`src/main/resources/images`, `images/`, etc.)

### 5. MockConfiguration with Test Profile Optimization

**Purpose**: Automatically optimizes settings when running tests with the `test` profile.

**Test Profile Features** (activated via Spring's `test` profile or `brobot-test-defaults.properties`):
- Forces mock mode for consistent testing
- Ultra-fast operation timings (0.005-0.04 seconds range)
- Disables all visual elements (drawing, snapshots)
- Removes all mouse pauses and delays
- Enables deterministic state management

**How it works**:
- Spring Boot automatically loads `brobot-test-defaults.properties` when `test` profile is active
- These properties override values from `brobot-defaults.properties`
- No code changes needed - just activate the test profile
- Test profile is auto-activated by `@SpringBootTest` annotation

**Optimized Settings** (from `brobot-test-defaults.properties`):
```properties
# Mock mode activation
brobot.mock=true

# Fast operation timings (in seconds)
brobot.mock.time-find-first=0.01
brobot.mock.time-find-all=0.02
brobot.mock.time-drag=0.03
brobot.mock.time-click=0.005
brobot.mock.time-move=0.01
brobot.mock.time-find-histogram=0.03
brobot.mock.time-find-color=0.03
brobot.mock.time-classify=0.04

# Disabled visual elements
brobot.highlight.enabled=false
brobot.sikuli.highlight=false
brobot.screenshot.save-snapshots=false
brobot.screenshot.save-history=false
brobot.illustration.draw-find=false
brobot.illustration.draw-click=false
brobot.illustration.draw-drag=false

# Zero delays for fast execution
brobot.mouse.move-delay=0
brobot.mouse.pause-before-down=0
brobot.mouse.pause-after-down=0
brobot.mouse.pause-before-up=0
brobot.mouse.pause-after-up=0
brobot.action.pause-after=0
```

### 6. BrobotAutoConfiguration

**Purpose**: Central auto-configuration that ties everything together.

**Features**:
- Conditional bean creation based on environment
- ExecutionEnvironment detection (mock vs live)
- Primary bean designations for conflict resolution
- Automatic wrapper configuration

**Auto-configured beans**:
```java
@AutoConfiguration
@Import({BrobotConfig.class, BrobotDefaultsConfiguration.class, ...})
@EnableConfigurationProperties({BrobotProperties.class, ImageDebugConfig.class})
public class BrobotAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExecutionEnvironment executionEnvironment(...) {
        // Detects environment: mock, display, or headless
        boolean mockMode = mockModeResolver.isMockMode();
        return ExecutionEnvironment.builder()
            .mockMode(mockMode)
            .forceHeadless(properties.getCore().isHeadless())
            .allowScreenCapture(!mockMode)
            .build();
    }
}
```

**How it works**:
- Registered in `META-INF/spring.factories` for automatic discovery
- Imports core configuration classes (BrobotConfig, BrobotDefaultsConfiguration)
- Enables @ConfigurationProperties binding for BrobotProperties
- Creates ExecutionEnvironment bean based on MockModeResolver
- Prints startup mode: "[Brobot] Mode: mock|display|headless"

### 7. NativeLibraryDebugSuppressor

**Purpose**: Automatically suppresses debug output from JavaCV/FFmpeg native libraries.

**Features**:
- Suppresses messages like "Debug: Loading library nppig64_12"
- Suppresses "Debug: Failed to load for nppig64_12: java.lang.UnsatisfiedLinkError"
- Suppresses "Debug: Collecting org.bytedeco.javacpp.Pointer$NativeDeallocator"
- Runs very early in startup process via Spring ApplicationContextInitializer

**How it works**:
```java
public class NativeLibraryDebugSuppressor
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    // Static initializer runs when class is loaded
    static {
        System.setProperty("org.bytedeco.javacpp.logger.debug", "false");
        System.setProperty("org.bytedeco.javacv.debug", "false");
        System.setProperty("org.bytedeco.javacpp.logger", "slf4j");
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        // Check if suppression is enabled (default: true)
        boolean suppress = env.getProperty("brobot.native.logging.suppress", Boolean.class, true);
        if (suppress) {
            // Set system properties and use reflection to configure JavaCPP Loader
            suppressNativeDebugOutput();
        }
    }
}
```

**Configuration**:
```properties
# Enable/disable suppression (default: true)
brobot.native.logging.suppress=true

# Fine-grained control (defaults: false)
brobot.javacpp.debug=false
brobot.javacv.debug=false
```

**Why it's needed**: JavaCV/FFmpeg libraries print debug messages directly to stdout/stderr, bypassing Java's logging system. These messages cannot be controlled through standard logging configuration and require system properties to be set before the libraries are loaded.

**Registration**: Configured in `META-INF/spring.factories` as an ApplicationContextInitializer for early execution before bean creation.

## Configuration Properties

While Brobot auto-configures most settings, you can override them via `application.properties`:

```properties
# Core settings (usually auto-detected)
brobot.image-path=images              # Auto-resolved by ImagePathManager
brobot.mock=false                     # Auto-set to true in test profile

# Logging (affects logging output)
brobot.logging.verbosity=NORMAL        # QUIET, NORMAL, or VERBOSE

# DPI/Scaling (usually auto-detected)
brobot.dpi.resize-factor=1.25          # Override auto-detection if needed (e.g., 1.25 for 125% scaling)

# Native Library Debug Suppression (since 1.0)
brobot.native.logging.suppress=true    # Suppress JavaCV/FFmpeg debug messages (default: true)
brobot.javacpp.debug=false            # Enable JavaCPP debug output (default: false)
brobot.javacv.debug=false             # Enable JavaCV debug output (default: false)

# Test optimization (auto-applied with test profile)
spring.profiles.active=test            # Activates all test optimizations
```

## Migration from Application-Specific Configuration

If you're migrating from application-specific configuration classes, here's what's now handled by the framework:

| Common Configuration Need | Brobot Framework Replacement |
|--------------------------|------------------------------|
| DPI scaling detection | `DPIConfiguration` |
| Image path resolution | `ImagePathManager` |
| Logging configuration | `LoggingVerbosityConfig` |
| Image verification | `ImagePathManager.verifyImageSetup()` |
| Test profile optimization | `brobot-test-defaults.properties` |
| JavaCV debug suppression | `NativeLibraryDebugSuppressor` |

:::info Application Migration
The table above shows common configuration needs and their Brobot framework replacements. If you had custom configuration classes in your application, they can typically be removed as the framework now handles these concerns automatically.
:::

## Configuration Lifecycle

Understanding the order of configuration initialization helps troubleshoot issues and customize behavior:

### Startup Sequence

1. **Early System Setup** (Before Spring Context)
   - `NativeLibraryDebugSuppressor` static initializer sets system properties
   - JVM arguments take effect (`-Djava.awt.headless=true`)
   - Native libraries (JavaCV/FFmpeg) are loaded

2. **Spring Context Initialization** (ApplicationContextInitializer Phase)
   - `NativeLibraryDebugSuppressor.initialize()` configures suppression
   - Properties are loaded: `brobot-defaults.properties` → profile-specific → `application.properties`
   - Environment variables and system properties override file properties

3. **Auto-Configuration Phase** (`@AutoConfiguration`)
   - `BrobotAutoConfiguration` imports core configuration classes
   - `@EnableConfigurationProperties` enables property binding
   - `BrobotProperties`, `LoggingVerbosityConfig`, `ImageDebugConfig` are bound

4. **Bean Creation Phase**
   - `ExecutionEnvironment` bean created (detects mock/display/headless)
   - `ImagePathManager` initializes and configures SikuliX `ImagePath`
   - `DPIConfiguration` detects DPI and configures `Settings.AlwaysResize`
   - Action beans, wrapper beans, and state management components created

5. **Post-Initialization** (`@PostConstruct`)
   - State machines initialize
   - Initial state verification (if enabled)
   - Application-specific initialization

### Configuration Points

You can customize behavior at different phases:

| Phase | Customization Method | Example Use Case |
|-------|---------------------|------------------|
| Early System | System properties (`-D` flags) | Force headless mode: `-Djava.awt.headless=true` |
| Properties | `application.properties` | Override DPI: `brobot.dpi.resize-factor=1.5` |
| Bean Override | Provide `@Bean` in application | Custom `ExecutionEnvironment` configuration |
| Post-Init | `@PostConstruct` in `@Component` | Application-specific setup after Brobot ready |

## Best Practices

1. **Let the framework auto-configure**: Don't create custom configuration classes unless absolutely necessary.

2. **Use profiles for environment-specific settings**:
   ```bash
   # For testing
   java -jar app.jar --spring.profiles.active=test
   
   # For production
   java -jar app.jar --spring.profiles.active=prod
   ```

3. **Override only when necessary**: Use properties files to override specific settings rather than creating configuration classes.

4. **Leverage built-in diagnostics**:
   ```java
   import io.github.jspinak.brobot.config.core.ImagePathManager;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.stereotype.Component;
   import java.util.Arrays;
   import java.util.Map;

   @Component
   public class ImageVerification {
       @Autowired
       private ImagePathManager imagePathManager;

       public void checkImageSetup() {
           // Get diagnostic information
           Map<String, Object> diagnostics = imagePathManager.getDiagnostics();

           // Verify expected images
           boolean valid = imagePathManager.verifyImageSetup(
               Arrays.asList("images/states", "images/patterns"),
               Arrays.asList("images/states/home.png", "images/patterns/button.png")
           );
       }
   }
   ```

## Example: Minimal Brobot Application

With auto-configuration, a Brobot application needs minimal setup:

```java
package com.myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.myapp",
    "io.github.jspinak.brobot"  // Enables all auto-configuration
})
public class MyBrobotApp {
    public static void main(String[] args) {
        SpringApplication.run(MyBrobotApp.class, args);
    }
}
```

Optional `application.properties`:
```properties
# That's it! Everything else is auto-configured
brobot.image-path=images
brobot.logging.verbosity=NORMAL
```

The framework handles all DPI scaling, image path resolution, logging configuration, and test optimizations automatically.

## Troubleshooting Auto-Configuration

### Common Issues and Solutions

#### Issue: "Images not found" despite correct path configuration

**Symptoms**: Pattern matching fails even though image files exist

**Diagnosis**:
```java
@Autowired
private ImagePathManager imagePathManager;

public void diagnoseImagePath() {
    Map<String, Object> diagnostics = imagePathManager.getDiagnostics();
    System.out.println("Diagnostics: " + diagnostics);

    // Verify expected images
    boolean valid = imagePathManager.verifyImageSetup(
        Arrays.asList("images/states", "images/patterns"),
        Arrays.asList("images/states/home.png")
    );
}
```

**Solutions**:
1. Check `brobot.image-path` property in `application.properties`
2. Ensure images are in `src/main/resources/images/` for development
3. For packaged JARs, place images in working directory relative to JAR
4. Enable diagnostics: `brobot.logging.verbosity=VERBOSE`

#### Issue: DPI scaling makes patterns not match

**Symptoms**: Patterns work on one machine but not another with different DPI

**Diagnosis**: Check if DPI detection is working
```properties
# In application.properties, enable verbose logging
brobot.logging.verbosity=VERBOSE
```
Look for startup messages about DPI detection.

**Solutions**:
1. Let auto-detection work: Don't set `brobot.dpi.resize-factor` unless necessary
2. If auto-detection fails, manually set: `brobot.dpi.resize-factor=1.25` (for 125% Windows scaling)
3. Verify SikuliX settings are being configured: Look for `Settings.AlwaysResize` in logs
4. Capture patterns on same DPI as execution environment

#### Issue: Native library debug spam in logs

**Symptoms**: Console flooded with "Debug: Loading library..." messages

**Solution**: NativeLibraryDebugSuppressor should handle this automatically. If not:
```properties
# Ensure suppression is enabled (should be default)
brobot.native.logging.suppress=true
```

If messages persist, they may be from before suppressor runs. Check startup order.

#### Issue: Tests run slowly

**Symptoms**: Tests take minutes instead of seconds

**Diagnosis**: Check if test profile is active
```java
@Autowired
private BrobotProperties properties;

@Test
public void checkMockMode() {
    assertTrue(properties.isMock(), "Should be in mock mode during tests");
}
```

**Solutions**:
1. Ensure `@SpringBootTest` is used (auto-activates test profile)
2. Manually activate test profile: `@ActiveProfiles("test")`
3. Verify `brobot-test-defaults.properties` is on classpath
4. Check that `brobot.mock=true` in test configuration

#### Issue: Custom configuration not taking effect

**Symptoms**: Properties set in `application.properties` are ignored

**Diagnosis**: Check property precedence
```java
@Autowired
private Environment environment;

@Test
public void checkPropertySources() {
    for (PropertySource<?> ps : ((ConfigurableEnvironment) environment).getPropertySources()) {
        System.out.println(ps.getName());
    }
    System.out.println("brobot.mock = " + environment.getProperty("brobot.mock"));
}
```

**Solutions**:
1. Ensure `application.properties` is in `src/main/resources/`
2. Check for typos in property names (should be `brobot.*`, not `brobot-*`)
3. Verify profile-specific properties override correctly
4. Use Spring Boot's property binding validation

### Verifying Auto-Configuration

To verify Brobot auto-configuration is working correctly:

```java
@SpringBootTest
class AutoConfigurationVerificationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void verifyBrobotBeansAreConfigured() {
        // Check core beans exist
        assertTrue(context.containsBean("executionEnvironment"));
        assertTrue(context.containsBean("imagePathManager"));
        assertTrue(context.containsBean("brobotProperties"));

        // Verify properties are bound
        BrobotProperties props = context.getBean(BrobotProperties.class);
        assertNotNull(props.getImagePath());

        // Verify ExecutionEnvironment is configured
        ExecutionEnvironment env = context.getBean(ExecutionEnvironment.class);
        assertNotNull(env);
        System.out.println("Execution mode: " +
            (env.isMockMode() ? "mock" : (env.hasDisplay() ? "display" : "headless")));
    }
}
```

## Related Documentation

- **[Properties Reference](./properties-reference.md)** - Complete list of all `brobot.*` properties
- **[Testing Guide](../../04-testing/testing-intro.md)** - Using test profile and mock mode
- **[Image Find Debugging](../tools/image-find-debugging.md)** - Troubleshooting pattern matching
- **[Getting Started](../../01-getting-started/quick-start.md)** - Setting up your first Brobot application