#!/bin/bash

# Packaging script for Brobot Runner without strict dependencies
# For users who have Java installed from other sources

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}Building Brobot Runner package (no dependencies)...${NC}"

# Get version from build.gradle
VERSION=$(grep "version = " runner/build.gradle | grep -oE "[0-9]+\.[0-9]+\.[0-9]+")
echo -e "Building version: ${YELLOW}$VERSION${NC}"

# Check if JAR exists
if [ ! -f "runner/build/libs/runner-${VERSION}.jar" ]; then
    echo -e "${RED}JAR file not found. Please build first with: ./gradlew :runner:bootJar${NC}"
    exit 1
fi

# Prepare staging directory
STAGING_DIR="runner/build/package-staging-nodeps"
rm -rf "$STAGING_DIR"
mkdir -p "$STAGING_DIR/opt/brobot-runner/lib"
mkdir -p "$STAGING_DIR/opt/brobot-runner/bin"
mkdir -p "$STAGING_DIR/usr/share/applications"
mkdir -p "$STAGING_DIR/usr/share/icons/hicolor/256x256/apps"

# Copy application JAR
echo "Copying application files..."
cp runner/build/libs/runner-${VERSION}.jar "$STAGING_DIR/opt/brobot-runner/lib/brobot-runner.jar"

# Create launcher script that detects Java
cat > "$STAGING_DIR/opt/brobot-runner/bin/brobot-runner" << 'EOF'
#!/bin/bash
# Brobot Runner launcher script with Java detection

# Try to find Java
find_java() {
    # Check JAVA_HOME first
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        echo "$JAVA_HOME/bin/java"
        return 0
    fi
    
    # Check common locations
    for java_path in \
        /usr/bin/java \
        /usr/local/bin/java \
        /opt/jdk-21/bin/java \
        /opt/java/bin/java \
        /usr/lib/jvm/java-21-openjdk-amd64/bin/java \
        /usr/lib/jvm/temurin-21-jdk-amd64/bin/java
    do
        if [ -x "$java_path" ]; then
            echo "$java_path"
            return 0
        fi
    done
    
    # Try which
    if command -v java > /dev/null 2>&1; then
        echo "java"
        return 0
    fi
    
    return 1
}

# Find JavaFX
find_javafx() {
    # Common JavaFX locations
    for fx_path in \
        "/usr/share/openjfx/lib" \
        "/usr/lib/openjfx/lib" \
        "$HOME/.openjfx/lib" \
        "/opt/javafx-21/lib" \
        "$JAVA_HOME/../javafx/lib"
    do
        if [ -d "$fx_path" ] && [ -f "$fx_path/javafx.base.jar" ]; then
            echo "$fx_path"
            return 0
        fi
    done
    
    # Check if JavaFX is bundled with Java
    if [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/lib/javafx.base.jar" ]; then
        echo "$JAVA_HOME/lib"
        return 0
    fi
    
    return 1
}

JAVA_CMD=$(find_java)
if [ $? -ne 0 ]; then
    echo "Error: Java not found. Please install Java 21 or later."
    echo "You can set JAVA_HOME environment variable to point to your Java installation."
    exit 1
fi

# Check Java version
JAVA_VERSION=$("$JAVA_CMD" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "Error: Java 21 or later is required. Found Java $JAVA_VERSION"
    exit 1
fi

echo "Using Java: $JAVA_CMD"

# Find JavaFX
JAVAFX_PATH=$(find_javafx)
if [ $? -eq 0 ]; then
    echo "Using JavaFX from: $JAVAFX_PATH"
    MODULE_PATH="--module-path $JAVAFX_PATH"
    ADD_MODULES="--add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing,javafx.web"
else
    echo "Warning: JavaFX not found in standard locations."
    echo "The application may not start properly without JavaFX."
    echo "Install JavaFX with: sudo apt install openjfx"
    MODULE_PATH=""
    ADD_MODULES=""
fi

# Launch application
exec "$JAVA_CMD" \
    -Xmx2048m \
    -Dspring.profiles.active=production \
    $MODULE_PATH \
    $ADD_MODULES \
    -jar "/opt/brobot-runner/lib/brobot-runner.jar" "$@"
EOF

chmod +x "$STAGING_DIR/opt/brobot-runner/bin/brobot-runner"

# Copy desktop entry
cp runner/packaging/brobot-runner.desktop "$STAGING_DIR/usr/share/applications/"

# Copy icon
cp runner/packaging/icons/brobot-256x256.png "$STAGING_DIR/usr/share/icons/hicolor/256x256/apps/brobot-runner.png"

# Create symlink for /usr/bin
mkdir -p "$STAGING_DIR/usr/bin"
ln -sf /opt/brobot-runner/bin/brobot-runner "$STAGING_DIR/usr/bin/brobot-runner"

echo -e "${GREEN}Creating DEB package without dependencies...${NC}"

# Create DEBIAN control directory
mkdir -p "$STAGING_DIR/DEBIAN"

# Create control file without strict dependencies
cat > "$STAGING_DIR/DEBIAN/control" << EOF
Package: brobot-runner
Version: $VERSION
Section: devel
Priority: optional
Architecture: all
Recommends: default-jre (>= 21) | openjdk-21-jre | temurin-21-jre, openjfx
Maintainer: Brobot Team <brobot@example.com>
Description: Desktop runner for Brobot automation framework
 Brobot Runner is a JavaFX-based desktop application for managing
 and executing Brobot automation projects. It provides a visual
 interface for configuration, execution, and monitoring.
 .
 This package requires Java 21 or later and JavaFX to be installed.
 The launcher script will attempt to find Java in common locations.
EOF

# Create postinst script
cat > "$STAGING_DIR/DEBIAN/postinst" << 'EOF'
#!/bin/bash
set -e

# Update desktop database
if command -v update-desktop-database > /dev/null; then
    update-desktop-database /usr/share/applications || true
fi

# Update icon cache
if command -v gtk-update-icon-cache > /dev/null; then
    gtk-update-icon-cache -f /usr/share/icons/hicolor || true
fi

# Check for Java
if ! /opt/brobot-runner/bin/brobot-runner --version > /dev/null 2>&1; then
    echo ""
    echo "Warning: Java 21 or JavaFX may not be properly installed."
    echo "Please ensure you have:"
    echo "  - Java 21 or later (from any source)"
    echo "  - JavaFX libraries"
    echo ""
fi

exit 0
EOF
chmod 755 "$STAGING_DIR/DEBIAN/postinst"

# Build DEB package
dpkg-deb --build "$STAGING_DIR" "runner/build/brobot-runner_${VERSION}_all_nodeps.deb"

echo -e "${GREEN}DEB package created: runner/build/brobot-runner_${VERSION}_all_nodeps.deb${NC}"
echo -e "${YELLOW}Note: This package uses 'Recommends' instead of 'Depends' for Java.${NC}"
echo -e "${YELLOW}Make sure Java 21+ and JavaFX are installed before running.${NC}"