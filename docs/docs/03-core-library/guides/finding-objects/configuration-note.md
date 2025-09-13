---
sidebar_position: 0
---

# Configuration Note

## Framework Configuration

Brobot uses Spring Boot's configuration system. All framework settings should be configured through:

- `application.yml` (recommended)
- `application.properties`
- Environment variables

**Do not** directly set static fields on `FrameworkSettings` - this is deprecated and only exists for backward compatibility.

## Example Configuration

### Using application.yml:

```yaml
brobot:
  core:
    mock: true              # Enable mock mode for testing
    image-path: images/     # Path to image resources
  screenshot:
    save-history: true      # Save action history
    save-snapshots: true    # Save screenshots
    path: screenshots/      # Screenshot directory
    history-path: history/  # History directory
  mouse:
    move-delay: 0.5         # Mouse movement delay
    pause-before-down: 0.0  # Pause before mouse down
```

### Using application.properties:

```properties
# Core settings
brobot.mock=true
brobot.core.image-path=images/

# Screenshot settings
brobot.screenshot.save-history=true
brobot.screenshot.save-snapshots=true
brobot.screenshot.path=screenshots/
brobot.screenshot.history-path=history/

# Mouse settings
brobot.mouse.move-delay=0.5
brobot.mouse.pause-before-down=0.0
```

## Default Values

Default configuration values are provided in `brobot-defaults.properties` within the Brobot library. Your application configuration will override these defaults.

## Mock Mode Screenshots

When using mock mode with predefined screenshots:

1. Configure mock mode in your properties file
2. Place screenshot files in the configured screenshot directory
3. The framework will use these files instead of capturing from the screen

For a complete list of available configuration properties, see the BrobotProperties class in the source code or refer to the `brobot-defaults.properties` file in the Brobot library.