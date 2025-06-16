# Brobot Runner Icons

This directory contains application icons for all platforms.

## Icon Files

### Application Icons
- `brobot.png` - Main application icon (256x256)
- `brobot.svg` - Scalable vector icon
- `brobot.ico` - Windows icon (convert from PNG)
- `brobot.icns` - macOS icon (generate with iconutil)

### File Association Icons
- `brobot-config.png` - JSON configuration file icon
- `brobot-config.ico` - Windows file icon
- `brobot-config.icns` - macOS file icon

## Generating Platform-Specific Icons

### Windows ICO
Use a tool like ImageMagick:
```
magick convert brobot-16.png brobot-32.png brobot-48.png brobot-256.png brobot.ico
```

### macOS ICNS
Run the provided script:
```
./create-icns.sh
```

### Linux
PNG files are ready to use. Install to appropriate directories:
- `/usr/share/icons/hicolor/SIZE/apps/brobot-runner.png`
