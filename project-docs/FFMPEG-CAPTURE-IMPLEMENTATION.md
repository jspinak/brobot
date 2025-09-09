# FFmpeg Screen Capture Implementation for Brobot

## Overview

Brobot now supports FFmpeg-based screen capture as an alternative to SikuliX. This ensures **physical resolution capture** regardless of Java version or DPI settings, solving the pattern matching issues caused by Java 21's DPI awareness.

## Benefits

1. **Physical Resolution Capture**: Always captures at physical pixels (e.g., 1920x1080)
2. **Cross-Platform**: Works on Windows, macOS, and Linux
3. **Java Version Independent**: Same behavior in Java 8, 11, 17, 21+
4. **No DPI Scaling Issues**: Bypasses Java's graphics layer entirely
5. **Better Performance**: Direct OS-level capture, often faster than Robot API

## Installation

### Windows
Download FFmpeg from https://ffmpeg.org/download.html
```bash
# Or use Chocolatey
choco install ffmpeg
```

### macOS
```bash
brew install ffmpeg
```

### Linux
```bash
# Ubuntu/Debian
sudo apt-get install ffmpeg

# Fedora/RHEL
sudo yum install ffmpeg

# Arch
sudo pacman -S ffmpeg
```

## Configuration

### application.properties
```properties
# Choose capture provider
brobot.capture.provider=AUTO  # AUTO, FFMPEG, or SIKULIX

# Prefer physical resolution (recommended)
brobot.capture.prefer-physical=true

# Fallback to SikuliX if FFmpeg fails
brobot.capture.fallback-enabled=true

# FFmpeg settings
brobot.capture.ffmpeg.path=ffmpeg  # Path to ffmpeg executable
brobot.capture.ffmpeg.timeout=5    # Capture timeout in seconds
brobot.capture.ffmpeg.format=png   # Output format
brobot.capture.ffmpeg.log-level=error  # FFmpeg log level
```

## Architecture

### Provider Interface
```java
public interface CaptureProvider {
    BufferedImage captureScreen() throws IOException;
    BufferedImage captureRegion(Rectangle region) throws IOException;
    boolean isAvailable();
    ResolutionType getResolutionType();  // PHYSICAL or LOGICAL
}
```

### Implementation Classes
- **FFmpegCaptureProvider**: Uses FFmpeg for physical resolution capture
- **SikuliXCaptureProvider**: Falls back to SikuliX (logical resolution in Java 21)
- **BrobotCaptureService**: Manages providers and automatic selection

### Automatic Provider Selection

1. **User-configured provider** (if set and available)
2. **FFmpeg** (if available and physical resolution preferred)
3. **SikuliX** (fallback)

## Usage

### Basic Usage (Automatic)
```java
@Autowired
private BrobotCaptureService captureService;

// Captures using best available provider
BufferedImage screen = captureService.captureScreen();
```

### Force Specific Provider
```java
// Switch to FFmpeg
captureService.setProvider("FFMPEG");

// Switch to SikuliX
captureService.setProvider("SIKULIX");

// Check available providers
System.out.println(captureService.getProvidersInfo());
```

### Existing Code Compatibility
The existing `ScreenshotCapture` class has been updated to use the new capture service automatically:

```java
@Autowired
private ScreenshotCapture screenshot;

// Uses FFmpeg if available, falls back to SikuliX
screenshot.captureScreenshot("test");
```

## How It Works

### Windows
- Uses `gdigrab` to capture directly from GDI
- Captures physical display buffer
- Example: `ffmpeg -f gdigrab -i desktop output.png`

### macOS
- Uses `AVFoundation` framework
- Captures at Retina resolution (physical pixels)
- Example: `ffmpeg -f avfoundation -i "1:" output.png`

### Linux
- Uses `x11grab` for X11 systems
- Captures from X server at physical resolution
- Example: `ffmpeg -f x11grab -i :0 output.png`

## Testing

Run the test suite to verify FFmpeg integration:

```bash
./gradlew test --tests "FFmpegCaptureTest"
```

Expected output:
```
=== CAPTURE COMPARISON TEST ===
FFmpeg capture: 1920x1080 (PHYSICAL)
SikuliX capture: 1536x864 (LOGICAL)
Resolution ratio: 1.25
→ Detected 125% DPI scaling
→ FFmpeg captures at physical resolution (as expected)
→ SikuliX captures at logical resolution in Java 21
```

## Troubleshooting

### FFmpeg Not Found
```
[FFmpeg] Provider not available
```
**Solution**: Ensure FFmpeg is installed and in PATH, or set full path:
```properties
brobot.capture.ffmpeg.path=/usr/local/bin/ffmpeg
```

### Permission Denied (macOS)
```
AVFoundation: Permission to access screen denied
```
**Solution**: Grant screen recording permission in System Preferences → Security & Privacy → Screen Recording

### Wrong Display Captured (Linux)
```
[FFmpeg] Capturing wrong screen
```
**Solution**: Set DISPLAY environment variable:
```bash
export DISPLAY=:0.0  # Primary display
export DISPLAY=:0.1  # Secondary display
```

### Timeout Errors
```
FFmpeg capture timed out after 5 seconds
```
**Solution**: Increase timeout:
```properties
brobot.capture.ffmpeg.timeout=10
```

## Performance Comparison

| Provider | Resolution | Speed | Java Version | DPI Issues |
|----------|------------|-------|--------------|------------|
| **FFmpeg** | Physical (1920x1080) | Fast | All | None |
| **SikuliX (Java 8)** | Physical | Medium | Java 8 only | None |
| **SikuliX (Java 21)** | Logical (1536x864) | Medium | Java 9+ | Yes |
| **Robot API** | Logical | Slow | Java 9+ | Yes |

## Migration Guide

### For Existing Applications

No code changes required! The capture service automatically uses FFmpeg when available.

### For Custom Implementations

Replace direct SikuliX calls:

```java
// Old
Screen screen = new Screen();
BufferedImage img = screen.capture().getImage();

// New
@Autowired
BrobotCaptureService captureService;
BufferedImage img = captureService.captureScreen();
```

## Best Practices

1. **Install FFmpeg** on all machines running Brobot
2. **Verify installation** with `ffmpeg -version`
3. **Use AUTO provider** for automatic fallback
4. **Keep patterns at physical resolution** for best matching
5. **Test both providers** in your environment

## Future Improvements

- [ ] Hardware acceleration support
- [ ] Video capture for recordings
- [ ] Multi-monitor capture optimization
- [ ] WebRTC capture for web applications
- [ ] Native JNI implementation for better performance

## Conclusion

FFmpeg integration provides a robust solution to DPI scaling issues in modern Java versions. By capturing at physical resolution directly from the OS, it ensures consistent pattern matching regardless of Java version or display scaling settings.