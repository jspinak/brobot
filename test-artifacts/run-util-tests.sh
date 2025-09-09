#!/usr/bin/env bash
echo "Running util package tests..."
./gradlew :library:test --tests "io.github.jspinak.brobot.util.*" --continue 2>&1
