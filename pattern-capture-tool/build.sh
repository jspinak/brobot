#!/bin/bash

# Build script for Brobot Pattern Capture Tool

echo "Building Brobot Pattern Capture Tool..."

# Navigate to parent directory to build library first
cd ../

# Build the library module first (dependency)
echo "Building library module..."
./gradlew :library:build -x test

# Build the pattern capture tool
echo "Building pattern capture tool..."
./gradlew :pattern-capture-tool:build -x test

# Create executable JAR
echo "Creating executable JAR..."
./gradlew :pattern-capture-tool:bootJar

echo "Build complete!"
echo ""
echo "To run the application:"
echo "  java -jar pattern-capture-tool/build/libs/pattern-capture-tool-1.0.0.jar"
echo ""
echo "Or use the run script:"
echo "  ./pattern-capture-tool/run.sh"