#!/bin/bash

# Quick script to test a few known test classes to identify hanging issues
# This tests a small subset first before running the full suite

echo "Quick Test Check - Testing a subset of test classes"
echo "====================================================="

# Stop gradle daemons
./gradlew --stop

# Test a few specific test classes with short timeout
TESTS=(
    "io.github.jspinak.brobot.logging.DiagnosticLoggerTest"
    "io.github.jspinak.brobot.logging.modular.ActionLoggingServiceTest"
    "io.github.jspinak.brobot.logging.unified.BrobotLoggerTest"
    "io.github.jspinak.brobot.test.BrobotTestBaseTest"
    "io.github.jspinak.brobot.util.file.FilenameUtilsTest"
)

for test in "${TESTS[@]}"; do
    echo ""
    echo "Testing: $test"
    echo "----------------------------------------"
    
    # Run with 30 second timeout
    timeout 30 ./gradlew :library:test --tests "$test" --no-daemon --no-build-cache 2>&1 | tail -5
    
    EXIT_CODE=$?
    if [ $EXIT_CODE -eq 124 ]; then
        echo "❌ TIMEOUT - Test hung after 30 seconds"
    elif [ $EXIT_CODE -eq 0 ]; then
        echo "✅ PASSED"
    else
        echo "⚠️ FAILED or ERROR (exit code: $EXIT_CODE)"
    fi
done

echo ""
echo "Quick check complete. If any tests timed out, they are likely causing the hanging issue."