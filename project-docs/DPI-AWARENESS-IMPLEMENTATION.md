# DPI Awareness Implementation in Brobot

## Summary

Brobot now **disables DPI awareness by default** to ensure screen captures occur at physical resolution (e.g., 1920x1080) instead of logical resolution (e.g., 1536x864 on 125% scaled displays). This provides maximum compatibility with patterns captured in SikuliX IDE.

## The Problem

- **Java 8** (used by SikuliX IDE): Captures at physical resolution
- **Java 21** (used by Brobot): By default captures at logical resolution due to DPI awareness
- This mismatch caused pattern matching failures

## The Solution

### Implementation Files

1. **`DPIAwarenessDisabler.java`** - Core class that disables DPI awareness
   - Sets system properties before any AWT classes are loaded
   - Must run very early in JVM lifecycle
   
2. **`DPIApplicationContextInitializer.java`** - Spring initializer
   - Ensures DPI settings are applied before Spring context creation
   
3. **`DPIConfiguration.java`** - Updated to use the disabler
   - Reports DPI status during configuration
   
4. **`META-INF/spring.factories`** - Registers the initializer
   - Ensures automatic loading in Spring Boot applications
   
5. **`brobot-defaults.properties`** - Default configuration
   - `brobot.dpi.disable=true` by default

## Configuration Options

### Method 1: Application Properties (Recommended)
```properties
# Disable DPI awareness (default: true)
brobot.dpi.disable=true

# To enable DPI awareness (Java 21 default)
brobot.dpi.disable=false
```

### Method 2: Environment Variable
```bash
export BROBOT_DISABLE_DPI=true
java -jar your-app.jar
```

### Method 3: JVM Argument
```bash
java -Dbrobot.dpi.disable=true -jar your-app.jar
```

## Verification

Run the verification tool to confirm DPI settings:

```bash
java -cp brobot.jar io.github.jspinak.brobot.tools.diagnostics.DPIVerificationTool
```

Expected output with DPI awareness disabled:
```
DPI AWARENESS STATUS:
   DPI awareness DISABLED - physical resolution capture
   
SIKULIX CAPTURE:
   Captured Size: 1920x1080
   
ANALYSIS:
   ✓ SUCCESS: Capturing at PHYSICAL resolution!
   Patterns captured in SikuliX IDE will match directly.
```

## Testing

Run the test suite to verify functionality:

```bash
./gradlew test --tests "DPIAwarenessDisablerTest"
```

## Benefits

1. **Compatibility**: Patterns from SikuliX IDE work without modification
2. **Consistency**: Same capture behavior across Java versions
3. **Simplicity**: No need for scaling compensation
4. **Flexibility**: Can be disabled if logical resolution is preferred

## Migration Guide

### For Existing Applications

No changes required! DPI awareness is disabled by default. Your existing patterns should work better.

### If You Want Logical Resolution

Set `brobot.dpi.disable=false` in your application.properties and adjust patterns accordingly.

## Technical Details

The implementation sets these system properties early in JVM startup:
- `sun.java2d.dpiaware=false` - Primary DPI awareness control
- `sun.java2d.uiScale=1.0` - Forces 1:1 scaling
- `sun.java2d.win.uiScale=1.0` - Windows-specific scaling
- `sun.java2d.uiScale.enabled=false` - Linux/GTK scaling
- `sun.java2d.metal.uiScale=1.0` - macOS Metal rendering

These must be set before any AWT/Swing classes are loaded, which is why we use:
1. Static initializer blocks
2. Spring ApplicationContextInitializer
3. Early bean initialization

## Troubleshooting

If captures are still at logical resolution:

1. **Check timing**: Ensure no AWT classes are loaded before our initializer
2. **Check security**: Some environments may prevent setting system properties
3. **Use JVM args**: As a fallback, set properties as JVM arguments
4. **Verify with tool**: Run DPIVerificationTool to diagnose

## Compatibility

- **Java 8**: No effect (already captures at physical resolution)
- **Java 11-20**: May improve consistency
- **Java 21+**: Essential for SikuliX IDE pattern compatibility
- **All OS**: Windows, Linux, macOS supported

## Version History

- **v1.1.0**: Initial implementation of DPI awareness disabling
- Default behavior changed to disable DPI awareness
- Added configuration options and verification tools