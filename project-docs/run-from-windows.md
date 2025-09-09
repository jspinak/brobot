# Running the Capture Test from Windows

## Quick Start (Windows PowerShell or CMD)

### If project is in Windows filesystem:
```cmd
cd C:\Users\jspin\Documents\brobot_parent\brobot
gradlew.bat runCaptureTest --no-daemon
```

### If project is only in WSL:
```cmd
cd \\wsl.localhost\Debian\home\jspinak\brobot_parent\brobot
gradlew.bat runCaptureTest --no-daemon
```

## Why Run from Windows?

When running from Windows instead of WSL:
- SikuliX can properly capture the Windows desktop
- Pattern matching works correctly with Windows UI elements
- DPI scaling is handled properly by Windows
- No issues with X11 forwarding or display variables

## Troubleshooting

### If gradlew.bat is not found:
The project may only have the Unix gradlew script. Create gradlew.bat:
1. Download from: https://github.com/gradle/gradle/blob/master/gradlew.bat
2. Place in project root
3. Make sure gradle/wrapper/gradle-wrapper.jar exists

### If Java is not found:
Make sure Java is installed on Windows (not just in WSL):
```cmd
java -version
```

If not installed, download from: https://adoptium.net/

### Pattern File Paths
The test looks for patterns in:
```
/home/jspinak/brobot_parent/claude-automator/images/prompt/
```

On Windows, this translates to:
```
C:\Users\jspin\Documents\brobot_parent\claude-automator\images\prompt\
```
or
```
\\wsl.localhost\Debian\home\jspinak\brobot_parent\claude-automator\images\prompt\
```

Make sure the pattern files are accessible from Windows.

## Expected Output

When running successfully from Windows, you should see:
- Captures at actual Windows resolution (1920x1080 or your display resolution)
- Pattern matches with similarity scores
- Generated visualization PNGs in `debug_captures` folder
- Match overlays showing where patterns were found

## Debug Captures Location

The test saves debug images to:
```
[project_root]\library\debug_captures\
```

These include:
- screen_*.png - Full screen captures
- test_capture_*.png - Test captures at different resolutions
- match_viz_*.png - Match visualizations with overlays
- visual_comparison_*.png - Side-by-side pattern comparisons