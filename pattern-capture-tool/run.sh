#!/bin/bash

# Run script for Brobot Pattern Capture Tool

# Check if JAR exists
JAR_FILE="build/libs/pattern-capture-tool-1.0.0.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file not found. Building application..."
    ./build.sh
fi

# Run the application
echo "Starting Brobot Pattern Capture Tool..."
java -jar "$JAR_FILE"