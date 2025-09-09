#!/bin/bash

# Script to fix Gradle cache issues in CI/CD environments

echo "Fixing Gradle cache issues..."

# 1. Stop any running Gradle daemons
./gradlew --stop 2>/dev/null || true

# 2. Clear Gradle caches that might be corrupted
rm -rf ~/.gradle/caches/modules-2/modules-2.lock
rm -rf ~/.gradle/caches/*/plugin-resolution/
rm -rf ~/.gradle/caches/*/fileHashes/
rm -rf ~/.gradle/caches/journal-*
rm -rf ~/.gradle/caches/build-cache-*

# 3. Clear project-specific caches
rm -rf .gradle/
rm -rf build/
rm -rf */build/
rm -rf */bin/
rm -rf */out/

# 4. Clear Gradle wrapper cache if needed
if [ "$1" == "--full" ]; then
    echo "Performing full cache clear..."
    rm -rf ~/.gradle/wrapper/
    rm -rf ~/.gradle/caches/
fi

# 5. Set proper permissions
chmod +x gradlew
chmod +x gradlew.bat 2>/dev/null || true

# 6. Create necessary directories
mkdir -p ~/.gradle
mkdir -p .gradle

# 7. Set headless mode for CI environments
if [ "$CI" == "true" ] || [ "$GITHUB_ACTIONS" == "true" ]; then
    echo "Setting headless mode for CI environment..."
    export JAVA_TOOL_OPTIONS="-Djava.awt.headless=true"
    echo "java.awt.headless=true" >> ~/.gradle/gradle.properties
fi

echo "Gradle cache fix complete!"

# 8. Verify Gradle is working
echo "Verifying Gradle setup..."
./gradlew --version

echo "Ready to run builds!"