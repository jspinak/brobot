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
   Download from: https://adoptium.net/
   
2. Install JavaFX (if not bundled with your Java)
   Download from: https://openjfx.io/
   Extract to C:\\javafx or another location

Running:
- Windows: Double-click 'bin/brobot-runner.bat'
  If it closes immediately, run 'bin/brobot-runner-debug.bat' to see errors
- Linux/Mac: Run './bin/brobot-runner.sh'
- Or: java -jar lib/brobot-runner.jar

Troubleshooting:
- If the window closes immediately on Windows, run brobot-runner-debug.bat
- Make sure JavaFX is in one of these locations:
  * C:\\Program Files\\JavaFX\\javafx-21\\lib
  * C:\\javafx\\lib
  * %USERPROFILE%\\javafx\\lib
  * Or set JAVAFX_PATH environment variable

For more information, see https://github.com/jspinak/brobot
EOF

# Create Windows batch file
cat > "$DIST_DIR/bin/brobot-runner.bat" << 'EOF'
@echo off
setlocal enabledelayedexpansion

rem Find Java
if defined JAVA_HOME (
    set "JAVA_CMD=%JAVA_HOME%\bin\java"
) else (
    set "JAVA_CMD=java"
)

rem Check Java version
"%JAVA_CMD%" -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java not found. Please install Java 21 or later.
    pause
    exit /b 1
)

rem Get script directory
set "SCRIPT_DIR=%~dp0"

rem Check for JavaFX in common Windows locations
set "JAVAFX_PATH="
for %%p in (
    "%JAVA_HOME%\..\javafx\lib"
    "%JAVA_HOME%\lib"
    "C:\Program Files\JavaFX\javafx-21\lib"
    "C:\javafx\lib"
    "%USERPROFILE%\javafx\lib"
) do (
    if exist "%%~p\javafx.base.jar" (
        set "JAVAFX_PATH=%%~p"
        goto :found_javafx
    )
)
:found_javafx

rem Set JavaFX parameters if found
if defined JAVAFX_PATH (
    set "MODULE_PATH=--module-path "%JAVAFX_PATH%""
    set "ADD_MODULES=--add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing,javafx.web"
) else (
    set "MODULE_PATH="
    set "ADD_MODULES="
)

rem Run application
"%JAVA_CMD%" -Xmx2048m %MODULE_PATH% %ADD_MODULES% -jar "%SCRIPT_DIR%\..\lib\brobot-runner.jar" %*

rem If application exits with error, pause to show error message
if errorlevel 1 (
    echo.
    echo Application exited with error. Check if JavaFX is installed.
    pause
)
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

# Copy debug batch file
cp runner/brobot-runner-debug.bat "$DIST_DIR/bin/" 2>/dev/null || {
    # If debug file doesn't exist, create it inline
    cat > "$DIST_DIR/bin/brobot-runner-debug.bat" << 'EOF'
@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Brobot Runner Debug Launcher
echo ========================================

rem Find Java
if defined JAVA_HOME (
    set "JAVA_CMD=%JAVA_HOME%\bin\java"
    echo Using JAVA_HOME: %JAVA_HOME%
) else (
    set "JAVA_CMD=java"
    echo Using system Java
)

rem Check Java version
echo.
echo Checking Java version...
"%JAVA_CMD%" -version 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: Java not found. Please install Java 21 or later.
    echo You can download it from: https://adoptium.net/
    pause
    exit /b 1
)

rem Get the directory of this batch file
set "SCRIPT_DIR=%~dp0"
set "JAR_PATH=%SCRIPT_DIR%..\lib\brobot-runner.jar"

rem Check if JAR exists
if not exist "%JAR_PATH%" (
    echo.
    echo ERROR: JAR file not found at: %JAR_PATH%
    echo Please ensure the package was extracted correctly.
    pause
    exit /b 1
)

echo.
echo JAR found at: %JAR_PATH%

rem Check for JavaFX
echo.
echo Checking for JavaFX...
set "JAVAFX_PATH="

rem Common JavaFX locations on Windows
for %%p in (
    "%JAVA_HOME%\..\javafx\lib"
    "%JAVA_HOME%\lib"
    "C:\Program Files\JavaFX\javafx-21\lib"
    "C:\Program Files\JavaFX\lib"
    "C:\javafx\lib"
    "%USERPROFILE%\javafx\lib"
    "%USERPROFILE%\.javafx\lib"
) do (
    if exist "%%~p\javafx.base.jar" (
        set "JAVAFX_PATH=%%~p"
        echo Found JavaFX at: %%~p
        goto :javafx_found
    )
)

:javafx_found
if not defined JAVAFX_PATH (
    echo.
    echo WARNING: JavaFX not found in common locations.
    echo The application requires JavaFX to run.
    echo.
    echo Please download JavaFX from: https://openjfx.io/
    echo Extract it and set JAVAFX_PATH environment variable.
    echo.
    echo Attempting to run anyway (may fail)...
    set "MODULE_PATH="
    set "ADD_MODULES="
) else (
    set "MODULE_PATH=--module-path "%JAVAFX_PATH%""
    set "ADD_MODULES=--add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing,javafx.web"
)

rem Run application with error output
echo.
echo Starting Brobot Runner...
echo Command: "%JAVA_CMD%" -Xmx2048m %MODULE_PATH% %ADD_MODULES% -jar "%JAR_PATH%"
echo ========================================
echo.

"%JAVA_CMD%" -Xmx2048m %MODULE_PATH% %ADD_MODULES% -jar "%JAR_PATH%" %*

set ERROR_CODE=%ERRORLEVEL%

if %ERROR_CODE% neq 0 (
    echo.
    echo ========================================
    echo Application exited with error code: %ERROR_CODE%
    echo.
    echo Common issues:
    echo - Missing JavaFX: Download from https://openjfx.io/
    echo - Wrong Java version: Need Java 21+
    echo - Missing dependencies in JAR
    echo ========================================
)

echo.
echo Press any key to close...
pause >nul
EOF
}

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