# Brobot Pattern Capture Tool - Development Plan

## Overview
Create a simple, standalone GUI tool for capturing screen regions and saving them as pattern images, using Brobot's modular capture system instead of SikuliX's capture mechanism. This ensures patterns are captured with the same settings that Brobot will use for matching.

## Core Features (Based on SikuliX IDE Analysis)

### 1. Screen Capture Functionality
- **Full-screen overlay** with darkened background (60% opacity)
- **Mouse selection** for defining capture region
- **Crosshair cursor** for precise selection
- **Real-time preview** of selected area (brightened)
- **ESC key** to cancel capture
- **Multi-monitor support**

### 2. Image Saving
- Save captured images to designated folder
- Auto-naming with timestamp or custom names
- PNG format (lossless for pattern matching)
- Optional: Save full screenshot alongside region

### 3. Capture Methods
- **Click and drag** to select region
- **Delayed capture** (configurable delay before capture)
- **Hotkey support** for quick capture

## Technical Architecture

### Components

#### 1. Main Application Class (`BrobotPatternCaptureTool.java`)
```java
- Spring Boot application
- Main window with toolbar
- Configuration management
- Image folder selection
```

#### 2. Capture Overlay (`CaptureOverlay.java`)
```java
- Transparent JFrame covering entire screen
- Uses Brobot's UnifiedCaptureService for screen capture
- Mouse event handling for selection
- Darkened background with bright selection area
```

#### 3. Capture Controller (`CaptureController.java`)
```java
- Coordinates capture process
- Manages capture providers (SikuliX, Robot, FFmpeg)
- Handles DPI scaling automatically
- Saves images to disk
```

#### 4. UI Components
```java
- CaptureButton - Initiates capture
- ImagePreview - Shows captured images
- SettingsPanel - Configure capture options
- FolderSelector - Choose save location
```

## Implementation Plan

### Phase 1: Core Capture Functionality
1. Create overlay window with selection capability
2. Integrate Brobot's UnifiedCaptureService
3. Implement mouse selection and region highlighting
4. Add ESC key cancellation

### Phase 2: Image Management
1. Implement image saving to disk
2. Add auto-naming with timestamps
3. Create image preview panel
4. Add folder selection dialog

### Phase 3: User Interface
1. Create main application window
2. Add toolbar with capture button
3. Implement settings panel
4. Add image gallery view

### Phase 4: Advanced Features
1. Multi-monitor support
2. Hotkey configuration
3. Capture delay settings
4. Batch capture mode

## Key Differences from SikuliX

### Advantages of Brobot Implementation
1. **Uses Brobot's capture system** - Ensures consistency between capture and matching
2. **Automatic DPI handling** - Uses `resize-factor=auto` for proper scaling
3. **Provider flexibility** - Can switch between SikuliX, Robot, FFmpeg
4. **Modern UI** - JavaFX or Swing with modern look
5. **Spring Boot integration** - Leverages Brobot's configuration system

### Simplified Features
1. No IDE integration (standalone tool)
2. No OCR naming (timestamp/manual only)
3. No thumbnail generation (simple list view)
4. No pattern editing (capture only)

## File Structure
```
/brobot/pattern-capture-tool/
├── src/main/java/
│   └── io/github/jspinak/brobot/patterncapture/
│       ├── BrobotPatternCaptureTool.java       # Main application
│       ├── ui/
│       │   ├── CaptureOverlay.java            # Screen overlay for selection
│       │   ├── MainWindow.java                # Main application window
│       │   ├── ImageGallery.java              # Display captured images
│       │   └── SettingsDialog.java            # Configuration dialog
│       ├── capture/
│       │   ├── CaptureController.java         # Capture coordination
│       │   ├── RegionSelector.java            # Mouse selection logic
│       │   └── ImageSaver.java                # Save images to disk
│       └── config/
│           └── CaptureConfig.java             # Configuration management
├── src/main/resources/
│   ├── application.properties                  # Brobot configuration
│   └── icons/                                 # UI icons
└── pom.xml                                     # Maven configuration
```

## Configuration Properties
```properties
# Capture settings
brobot.capture.provider=SIKULIX
brobot.dpi.resize-factor=auto
brobot.capture.tool.default-folder=./patterns
brobot.capture.tool.auto-naming=timestamp
brobot.capture.tool.capture-delay=0.5
brobot.capture.tool.save-screenshots=false
```

## Usage Workflow

1. **Launch Tool**
   - Opens main window
   - Shows current settings
   - Displays previously captured images

2. **Initiate Capture**
   - Click capture button or press hotkey
   - Optional delay before capture
   - Screen dims with overlay

3. **Select Region**
   - Click and drag to select area
   - Selected area shows in original brightness
   - Red frame around selection

4. **Save Image**
   - Release mouse to capture
   - Prompt for filename (optional)
   - Save to configured folder
   - Show in gallery

5. **Review Images**
   - View captured patterns
   - Delete unwanted captures
   - Copy image paths

## Benefits

1. **Consistency**: Patterns captured with same system used for matching
2. **DPI Awareness**: Automatic handling of Windows scaling
3. **Simplicity**: Focused tool for one purpose
4. **Integration**: Uses Brobot's configuration and capture system
5. **Flexibility**: Switch capture providers without code changes

## Next Steps

1. Create basic overlay capture prototype
2. Test with different DPI settings
3. Implement image saving
4. Build simple UI
5. Package as standalone JAR

This tool will provide Brobot users with a native way to capture patterns that are guaranteed to work with Brobot's pattern matching system.