# Brobot Runner Packaging

This directory contains resources for packaging the Brobot Runner application as native installers.

## Directory Structure

- `icons/` - Application and file type icons for different platforms
  - `brobot.ico` - Windows application icon
  - `brobot.icns` - macOS application icon  
  - `brobot.png` - Linux application icon (256x256 recommended)
  - `brobot-config.ico` - Windows file association icon
  - `brobot-config.icns` - macOS file association icon
  - `brobot-config.png` - Linux file association icon

- `licenses/` - License files to include in installers
  - Add your LICENSE.txt file here

- `config/` - Default configuration files to bundle

## Building Native Installers

### Prerequisites
- JDK 14+ (for jpackage tool)
- Platform-specific tools:
  - Windows: WiX Toolset (for MSI creation)
  - macOS: Xcode command line tools
  - Linux: rpm-build (for RPM) or dpkg-dev (for DEB)

### Build Commands

From the runner module directory:

```bash
# Validate jpackage is available
./gradlew validateJpackage

# Build installer for current platform
./gradlew jpackage

# Build Windows installer (Windows only)
./gradlew jpackageWindows

# Build macOS installer (macOS only)
./gradlew jpackageMacOS

# Build Linux DEB installer (Linux only)
./gradlew jpackageLinux

# Build Linux RPM installer (Linux only)
./gradlew jpackageLinux -PlinuxType=rpm
```

### Output
Installers will be created in `build/installers/`

## Icon Requirements

### Windows (.ico)
- Multiple resolutions: 16x16, 32x32, 48x48, 256x256
- 32-bit color depth

### macOS (.icns)
- Use iconutil to create from iconset
- Required sizes: 16x16, 32x32, 128x128, 256x256, 512x512 (and @2x variants)

### Linux (.png)
- 256x256 PNG recommended
- SVG also supported for scalable icons

## Customization

Edit `jpackage.gradle` to customize:
- Application metadata (name, version, vendor)
- JVM options and memory settings
- File associations
- Installation options
- Menu shortcuts

## Troubleshooting

1. **jpackage not found**: Ensure you're using JDK 14+
2. **Windows signing**: Unsigned installers will show security warnings
3. **macOS notarization**: Required for distribution outside App Store
4. **Linux permissions**: May need root access for system-wide install