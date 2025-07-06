#!/bin/bash

# Create a universal distribution package (ZIP with JAR + launch scripts)

set -e

VERSION=$(grep "version = " runner/build.gradle | grep -oE "[0-9]+\.[0-9]+\.[0-9]+")
DIST_NAME="brobot-runner-$VERSION"
DIST_DIR="runner/build/$DIST_NAME"

echo "Creating universal distribution package v$VERSION..."

# Clean and create distribution directory
rm -rf "$DIST_DIR" "$DIST_DIR.zip"
mkdir -p "$DIST_DIR/lib"
mkdir -p "$DIST_DIR/bin"

# Copy JAR
cp runner/build/libs/runner-$VERSION.jar "$DIST_DIR/lib/brobot-runner.jar"

# Create README
cat > "$DIST_DIR/README.txt" << EOF
Brobot Runner v$VERSION
======================

Requirements:
- Java 21 or later
- JavaFX 21 or later

Installation:
1. Ensure Java 21+ is installed
2. Install JavaFX (if not bundled with your Java)
3. Run the appropriate launch script for your platform

Running:
- Windows: Double-click 'brobot-runner.bat'
- Linux/Mac: Run './brobot-runner.sh'
- Or: java -jar lib/brobot-runner.jar

For more information, see https://github.com/your-repo
EOF

# Create Windows batch file
cat > "$DIST_DIR/bin/brobot-runner.bat" << 'EOF'
@echo off
setlocal

rem Find Java
if defined JAVA_HOME (
    set JAVA_CMD="%JAVA_HOME%\bin\java"
) else (
    set JAVA_CMD=java
)

rem Check Java version
%JAVA_CMD% -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java not found. Please install Java 21 or later.
    pause
    exit /b 1
)

rem Run application
%JAVA_CMD% -Xmx2048m -jar "%~dp0\..\lib\brobot-runner.jar" %*
EOF

# Create Unix shell script
cat > "$DIST_DIR/bin/brobot-runner.sh" << 'EOF'
#!/bin/bash

# Find Java
if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD="java"
fi

# Check if Java exists
if ! command -v "$JAVA_CMD" &> /dev/null; then
    echo "Error: Java not found. Please install Java 21 or later."
    exit 1
fi

# Get script directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Run application
"$JAVA_CMD" -Xmx2048m -jar "$DIR/../lib/brobot-runner.jar" "$@"
EOF

chmod +x "$DIST_DIR/bin/brobot-runner.sh"

# Create macOS app bundle (basic, unsigned)
APP_DIR="$DIST_DIR/Brobot Runner.app"
mkdir -p "$APP_DIR/Contents/MacOS"
mkdir -p "$APP_DIR/Contents/Resources"
mkdir -p "$APP_DIR/Contents/Java"

# Copy JAR to app bundle
cp "$DIST_DIR/lib/brobot-runner.jar" "$APP_DIR/Contents/Java/"

# Create Info.plist
cat > "$APP_DIR/Contents/Info.plist" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key>
    <string>Brobot Runner</string>
    <key>CFBundleDisplayName</key>
    <string>Brobot Runner</string>
    <key>CFBundleIdentifier</key>
    <string>io.github.jspinak.brobot.runner</string>
    <key>CFBundleVersion</key>
    <string>$VERSION</string>
    <key>CFBundleShortVersionString</key>
    <string>$VERSION</string>
    <key>CFBundleExecutable</key>
    <string>launcher</string>
    <key>CFBundleIconFile</key>
    <string>brobot.icns</string>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
EOF

# Create launcher for Mac app
cat > "$APP_DIR/Contents/MacOS/launcher" << 'EOF'
#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
java -Xmx2048m -XstartOnFirstThread -jar "$DIR/../Java/brobot-runner.jar"
EOF
chmod +x "$APP_DIR/Contents/MacOS/launcher"

# Copy icon if available
if [ -f "runner/packaging/icons/brobot.icns" ]; then
    cp "runner/packaging/icons/brobot.icns" "$APP_DIR/Contents/Resources/"
fi

# Create ZIP
cd runner/build
zip -r "$DIST_NAME.zip" "$DIST_NAME"
cd ../..

echo "Universal package created: runner/build/$DIST_NAME.zip"
echo ""
echo "This package includes:"
echo "  - JAR file"
echo "  - Windows batch launcher"
echo "  - Linux/Unix shell launcher"  
echo "  - Basic macOS app bundle (unsigned)"
echo ""
echo "Users need Java 21+ and JavaFX installed."