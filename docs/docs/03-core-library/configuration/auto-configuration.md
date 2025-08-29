# Auto-Configuration in Brobot

Brobot provides comprehensive auto-configuration capabilities that eliminate the need for applications to manually configure most framework settings. This document describes the auto-configuration features that are built into the Brobot library.

## Overview

Brobot applications should require minimal to no configuration files. The framework automatically handles:
- DPI scaling and cross-version compatibility
- Image path resolution and verification
- Console reporter output levels
- Mock mode optimization for testing
- Cross-platform compatibility

## Built-in Configuration Classes

### 1. BrobotDPIConfiguration

**Purpose**: Automatically detects and compensates for DPI scaling differences and Java version compatibility issues.

**Features**:
- Detects Windows DPI scaling (125%, 150%, etc.)
- Applies appropriate pattern scaling compensation
- Handles Java 8 vs Java 9+ rendering differences
- Configures consistent color rendering across versions

**Auto-configured settings**:
```java
Settings.AlwaysResize    // Automatically set based on DPI scaling
Settings.MinSimilarity    // Adjusted for Java version differences
Settings.CheckLastSeen    // Enabled for performance
```

**Java Version Compatibility**:
- Java 9+: Applies rendering consistency settings
- Disables hardware acceleration for consistent pattern matching
- Adjusts similarity thresholds for version-specific rendering differences

### 2. ConsoleReporterInitializer

**Purpose**: Automatically configures console output levels based on logging verbosity settings.

**Verbosity Mapping**:
- `QUIET` → `ConsoleReporter.OutputLevel.NONE`
- `NORMAL` → `ConsoleReporter.OutputLevel.LOW`
- `VERBOSE` → `ConsoleReporter.OutputLevel.HIGH`

**Configuration**: Set via `brobot.logging.verbosity` property

### 3. ImagePathManager

**Purpose**: Intelligent image path resolution with multiple fallback strategies.

**Features**:
- Automatic path resolution strategies (absolute, relative, classpath, JAR)
- JAR extraction for packaged applications
- Image verification and diagnostics
- Alternative location checking

**New Methods**:
```java
// Verify expected images and directories
boolean verifyImageSetup(List<String> expectedDirs, List<String> expectedImages)

// Get diagnostic information
Map<String, Object> getDiagnostics()
```

### 4. MockConfiguration with Test Profile Optimization

**Purpose**: Automatically optimizes settings when running tests with the `test` profile.

**Test Profile Features** (activated with `@Profile("test")`):
- Forces mock mode for consistent testing
- Ultra-fast operation timings (0.005-0.02 seconds)
- Disables all visual elements (drawing, snapshots)
- Removes all mouse pauses and delays
- Enables deterministic state management

**Optimized Settings**:
```properties
# Automatically applied with test profile
brobot.framework.mock=true
brobot.mock.time.find-first=0.01
brobot.mock.time.click=0.005
brobot.draw.*=false
brobot.pause.*=0
```

### 5. BrobotAutoConfiguration

**Purpose**: Central auto-configuration that ties everything together.

**Features**:
- Conditional bean creation based on environment
- ExecutionEnvironment detection (mock vs live)
- Primary bean designations for conflict resolution
- Automatic wrapper configuration

## Configuration Properties

While Brobot auto-configures most settings, you can override them via `application.properties`:

```properties
# Core settings (usually auto-detected)
brobot.core.image-path=images          # Auto-resolved by ImagePathManager
brobot.framework.mock=false            # Auto-set to true in test profile

# Logging (affects ConsoleReporter output)
brobot.logging.verbosity=NORMAL        # QUIET, NORMAL, or VERBOSE

# DPI/Scaling (usually auto-detected)
brobot.dpi.manual-scaling=125          # Override auto-detection if needed

# Test optimization (auto-applied with test profile)
spring.profiles.active=test            # Activates all test optimizations
```

## Migration from Application-Specific Configuration

If you're migrating from application-specific configuration classes, here's what's now handled by the framework:

| Old Application Config | Brobot Framework Replacement |
|------------------------|------------------------------|
| `SikuliXIDEMatchingConfig` | `BrobotDPIConfiguration` |
| `ImagePathConfiguration` | `ImagePathManager` |
| `ConsoleReporterConfiguration` | `ConsoleReporterInitializer` |
| `ImageSetupVerifier` | `ImagePathManager.verifyImageSetup()` |
| `CrossVersionCompatibilityConfig` | `BrobotDPIConfiguration` |
| `TestProfileMockConfiguration` | `MockConfiguration.TestProfileOptimization` |

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
   @Autowired
   private ImagePathManager imagePathManager;
   
   // Get diagnostic information
   Map<String, Object> diagnostics = imagePathManager.getDiagnostics();
   
   // Verify expected images
   boolean valid = imagePathManager.verifyImageSetup(
       Arrays.asList("images/states", "images/patterns"),
       Arrays.asList("images/states/home.png", "images/patterns/button.png")
   );
   ```

## Example: Minimal Brobot Application

With auto-configuration, a Brobot application needs minimal setup:

```java
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
brobot.core.image-path=images
brobot.logging.verbosity=NORMAL
```

The framework handles all DPI scaling, image path resolution, console output configuration, and test optimizations automatically.