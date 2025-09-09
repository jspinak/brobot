# Brobot Pattern Capture Tool Guide

## Overview

The Brobot Pattern Capture Tool is a standalone application that provides SikuliX IDE-like functionality for capturing screen patterns. Based on extensive testing, **Windows Snipping Tool produces the cleanest patterns** that achieve the best match rates during runtime automation.

## Key Features

- 📸 **Multiple Capture Providers**: SikuliX, Robot, and FFmpeg (JavaCV)
- 🎯 **100% Compatibility**: Identical results to Windows Snipping Tool with FFmpeg
- 🖼️ **Image Gallery**: View and manage captured patterns
- ⚡ **Hotkeys**: F1 for instant capture, F2 for delayed capture
- 📁 **Auto-organization**: Saves patterns with timestamps
- 🔧 **Provider Switching**: Change capture methods on the fly

## Installation

### Prerequisites
- Java 21 or higher
- Brobot library built and installed

### Building the Tool

```bash
# From the brobot directory
cd pattern-capture-tool
./build.sh

# Or manually
./gradlew :pattern-capture-tool:bootJar
```

### Running the Tool

#### Windows
```batch
cd pattern-capture-tool
java -jar build\libs\pattern-capture-tool-1.0.0.jar
```

#### Linux/Mac
```bash
cd pattern-capture-tool
./run.sh
```

## Usage Guide

### Basic Capture Workflow

1. **Launch the tool**
   ```bash
   java -jar pattern-capture-tool-1.0.0.jar
   ```

2. **For best results, use Windows Snipping Tool** (Win+Shift+S)
   - Produces cleanest patterns with 95-100% match rates
   - Better than using the tool's FFmpeg capture for patterns
   - The tool is useful for testing and organizing patterns

3. **Capture a pattern**
   - Click "Capture" or press F1
   - Screen darkens with overlay
   - Click and drag to select region
   - Press ESC to cancel

4. **Pattern is auto-saved**
   - Saved to `./patterns/` folder
   - Named with timestamp
   - Visible in image gallery

### Capture Providers Comparison

Based on extensive testing:

| Capture Method | Best Use | Runtime Match Rate |
|----------------|----------|-------------------|
| **Windows Snipping Tool** | **Pattern creation (recommended)** | **95-100%** |
| **Tool FFmpeg** | Testing/validation | 70-80% |
| **Tool SikuliX** | Legacy compatibility | 70-77% |
| **Tool Robot** | Quick testing | 70-77% |

### Hotkeys

- **F1** - Instant capture
- **F2** - Delayed capture (500ms default)
- **ESC** - Cancel capture selection

### Settings

Access via the "Settings" button:

```properties
# Default configuration
pattern.capture.default-folder=./patterns
pattern.capture.capture-delay=500
pattern.capture.save-screenshots=false
pattern.capture.show-dimensions=true
```

## Advanced Features

### Gallery Management

The image gallery provides:
- **Thumbnail view** of all captured patterns
- **Click to copy path** to clipboard
- **Double-click** to open in default viewer
- **Right-click menu**:
  - Copy path
  - Copy filename
  - Open in viewer
  - Show in folder
  - Delete

### Batch Capture Mode

For capturing multiple patterns quickly:

1. Set up your application in the desired state
2. Use F2 for delayed capture
3. Position windows/dialogs
4. Capture multiple regions in sequence

### Resolution Handling

The tool automatically handles DPI scaling:

- **Physical capture** (1920x1080): Scaled to fit logical display
- **Logical capture** (1536x864): Displayed 1:1
- **Selection coordinates**: Automatically converted to correct resolution

## Configuration for Brobot

### Optimal Settings

After capturing patterns with the tool, configure Brobot:

```properties
# application.properties
brobot.capture.provider=JAVACV_FFMPEG  # Match the tool's FFmpeg
brobot.dpi.resize-factor=auto
brobot.action.similarity=0.70
```

### Pattern Organization

Recommended folder structure:
```
project/
├── images/
│   ├── login/
│   │   ├── username-field.png
│   │   ├── password-field.png
│   │   └── submit-button.png
│   ├── dashboard/
│   │   ├── menu-icon.png
│   │   └── search-bar.png
│   └── common/
│       └── logo.png
└── patterns/  # Tool's default save location
```

## Troubleshooting

### Issue: "JavaCV FFmpeg not available"

**Solution**: Ensure Brobot library is properly built:
```bash
./gradlew :library:build
```

### Issue: Captured image appears zoomed

**Solution**: This is fixed in the latest version. The tool now:
1. Detects physical vs logical resolution
2. Scales display appropriately
3. Captures at original resolution

### Issue: Can't capture full screen

**Solution**: The tool now correctly includes all pixels (fixed off-by-one error)

### Issue: Different colors than expected

**Normal behavior** - slight color variations don't affect pattern matching:
- FFmpeg: 24-bit color
- Windows: 32-bit color
- Difference is negligible for matching

## Best Practices

### For Optimal Pattern Matching

1. **Use Windows Snipping Tool** (Win+Shift+S) for pattern creation
   - macOS: Use Cmd+Shift+4
   - Linux: Use GNOME Screenshot or Spectacle
2. **Capture small, unique elements** (< 200x200 pixels)
3. **Avoid capturing text** (use OCR instead)
4. **Include some background** for context
5. **Test patterns** with this tool after capture

### Pattern Quality Guidelines

✅ **Good Patterns:**
- Buttons with unique icons
- Distinctive UI elements  
- Fixed-size components
- High contrast areas

❌ **Poor Patterns:**
- Large areas of solid color
- Text that might change
- Animated elements
- Semi-transparent overlays

## Performance Metrics

Based on testing with various providers:

| Metric | FFmpeg | SikuliX | Robot |
|--------|--------|---------|--------|
| Capture Speed | ~0.5s | ~0.3s | ~0.3s |
| File Size (1920x1080) | 255 KB | 324 KB* | 324 KB* |
| Compression Ratio | 23.7x | 12.0x | 12.0x |
| Match Accuracy | 100% | 97% | 97% |

*At logical resolution (1536x864)

## Integration with CI/CD

### Automated Pattern Capture

```java
// Headless pattern validation
@Test
public void validatePatterns() {
    File patternDir = new File("patterns");
    for (File pattern : patternDir.listFiles()) {
        StateImage img = new StateImage.Builder()
            .withPath(pattern.getPath())
            .build();
        
        // Validate pattern loads correctly
        assertNotNull(img);
        assertTrue(pattern.length() > 0);
    }
}
```

### Docker Support

```dockerfile
FROM openjdk:21
COPY pattern-capture-tool-1.0.0.jar /app/
COPY patterns/ /app/patterns/
WORKDIR /app
# Note: Requires display for capture
```

## Extending the Tool

The tool is built with Spring Boot and can be extended:

### Adding Custom Providers

```java
@Component
public class CustomProvider implements CaptureProvider {
    @Override
    public String getName() {
        return "CUSTOM";
    }
    
    @Override
    public BufferedImage captureScreen() {
        // Your capture implementation
    }
}
```

### Adding Export Formats

```java
// In ImageGalleryPanel
public void exportToSikuliXBundle() {
    // Export patterns in SikuliX format
}
```

## Research Background

This tool was developed after extensive testing comparing:
- 3 Brobot capture methods (SikuliX, Robot, FFmpeg)
- 5 external tools (Windows, SikuliX IDE, Pattern Capture Tool variants)
- 28 pairwise comparisons
- 1000+ test patterns

**Key finding**: While FFmpeg captures match Windows Snipping Tool when comparing full screenshots, **Windows Snipping Tool patterns achieve significantly better match rates (95-100% vs 70-80%)** when used as patterns during runtime automation. This is because clean, artifact-free patterns from native OS tools match better against runtime screenshots.

## Support

For issues or questions:
- Check the [capture comparison study](capture-methods-comparison.md)
- Review [quick start guide](quick-start-capture-setup.md)
- File issues on GitHub

## Version History

### v1.0.0 (Current)
- Initial release
- Three capture providers
- Image gallery
- Hotkey support
- DPI scaling fixes
- Off-by-one capture fix

### Planned Features
- State detection from screen
- Batch capture mode
- Pattern optimization
- OCR integration
- Cloud storage support

## License

Part of the Brobot project - see main project license.