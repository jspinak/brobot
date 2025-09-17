# Configuration Migration Guide

## Overview

Starting with Brobot 1.1.0, the framework uses Spring's property-based configuration instead of static fields. This guide helps you migrate from the old `FrameworkSettings` approach to the new configuration system.

## What's Changed

### Old Way (Pre-1.1.0)
```java
// Programmatically setting values
// Mock mode is now configured via application.properties:
// brobot.core.mock=true;
FrameworkSettings.moveMouseDelay = 1.0f;
FrameworkSettings.saveSnapshots = true;
```

### New Way (1.1.0+)
```yaml
# application.yml
brobot:
  core:
    mock: true
  mouse:
    move-delay: 1.0
  screenshot:
    save-snapshots: true
```

## Default Configuration

Brobot now includes `brobot-defaults.properties` with sensible defaults for all settings. You only need to override values you want to change.

### Viewing Defaults
The complete list of defaults can be found in the library at:
- `brobot-defaults.properties` in the Brobot jar
- [GitHub source](https://github.com/jspinak/brobot/blob/main/library/src/main/resources/brobot-defaults.properties)

## Migration Steps

### 1. Remove Static Configuration Code

Replace any code that sets `FrameworkSettings` fields:

```java
// Remove this
@PostConstruct
public void configureBrobot() {
    // Mock mode is now configured via application.properties:
// brobot.core.mock=false;
    FrameworkSettings.saveHistory = true;
    FrameworkSettings.historyPath = "my-history/";
}
```

### 2. Add Configuration Properties

Add to your `application.yml`:

```yaml
brobot:
  core:
    mock: false
  screenshot:
    save-history: true
    history-path: my-history/
```

Or `application.properties`:

```properties
brobot.core.mock=false
brobot.screenshot.save-history=true
brobot.screenshot.history-path=my-history/
```

### 3. Use Environment Variables

You can also use environment variables:

```bash
export BROBOT_CORE_MOCK=true
export BROBOT_SCREENSHOT_SAVE_HISTORY=true
```

## Configuration Reference

### Core Settings
```yaml
brobot:
  core:
    image-path: images          # Where to find images
    mock: false                 # Enable mock mode
    headless: false             # Run headless
    package-name: com.example   # Default package for generated code
```

### Mouse Settings
```yaml
brobot:
  mouse:
    move-delay: 0.5             # Delay for mouse movements
    pause-before-down: 0.0      # Pause before mouse down
    pause-after-down: 0.0       # Pause after mouse down
    pause-before-up: 0.0        # Pause before mouse up
    pause-after-up: 0.0         # Pause after mouse up
```

### Screenshot Settings
```yaml
brobot:
  screenshot:
    save-snapshots: false       # Save screenshots
    save-history: false         # Save illustrated history
    path: screenshots/          # Screenshot directory
    history-path: history/      # History directory
```

### Complete Example
```yaml
brobot:
  core:
    image-path: classpath:images/
    mock: false
    headless: false
  mouse:
    move-delay: 0.3
    pause-after-down: 0.1
  screenshot:
    save-snapshots: true
    save-history: true
    path: build/screenshots/
    history-path: build/history/
  illustration:
    draw-find: true
    draw-click: true
  analysis:
    k-means-in-profile: 5
  startup:
    verify-initial-states: true
    initial-states: HOME,LOGIN
```

## Backward Compatibility

During the transition period:
- `FrameworkSettings` fields are still updated from properties
- Old code continues to work
- You can gradually migrate to using `BrobotProperties` directly

### Accessing Properties in Code

If you need to access configuration in your code:

```java
@Component
public class MyComponent {
    @Autowired
    private BrobotProperties brobotProperties;
    
    public void doSomething() {
        if (brobotProperties.getCore().isMock()) {
            // Mock mode logic
        }
    }
}
```

## Benefits of the New System

1. **External Configuration**: Change behavior without recompiling
2. **Environment-Specific**: Different settings for dev/test/prod
3. **Type Safety**: Properties are validated at startup
4. **Documentation**: IDE support for property completion
5. **Centralized**: All settings in one place
6. **Cloud-Native**: Works with config servers and K8s ConfigMaps

## Troubleshooting

### Properties Not Loading
- Ensure your `application.yml` is in `src/main/resources`
- Check for typos in property names
- Verify Spring Boot is finding your configuration

### Viewing Active Configuration
Add this to see what properties are loaded:
```properties
logging.level.org.springframework.boot.context.properties=DEBUG
```

### Override Order
Properties are loaded in this order (later overrides earlier):
1. `brobot-defaults.properties` (library defaults)
2. `application.properties`
3. `application.yml`
4. Environment variables
5. Command-line arguments

## Future Plans

In Brobot 2.0:
- `FrameworkSettings` will be deprecated
- All code will use `BrobotProperties` directly
- Static configuration will be removed

Start migrating now to prepare for the future!