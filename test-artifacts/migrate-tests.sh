#!/bin/bash

# Script to migrate tests from claude-automator to brobot library structure

SOURCE_DIR="/home/jspinak/brobot_parent/claude-automator/src/test/java"
LIBRARY_TEST_DIR="/home/jspinak/brobot_parent/brobot/library/src/test/java/io/github/jspinak/brobot"
INTEGRATION_TEST_DIR="/home/jspinak/brobot_parent/brobot/library-test/src/test/java/io/github/jspinak/brobot/integration"

# Arrays for integration tests (with Spring annotations)
INTEGRATION_TESTS=(
    "MockConfigurationTest.java"
    "BrobotIntegrationTest.java"
    "MockTransitionTest.java"
    "MixedModeExecutionTest.java"
    "ExecutionEnvironmentTest.java"
    "ConfigurationLoggingTest.java"
    "MockModeTest.java"
    "MockAutomationFlowTest.java"
    "ScreenshotSaveDebugTest.java"
    "ProfileBasedMockVerificationTest.java"
    "InitializationIntegrationTest.java"
    "ScalingDiagnosticTest.java"
    "MinimalScreenshotTest.java"
    "IterationLoggingTest.java"
    "EventListenerVerificationTest.java"
    "HighlightDebugTest.java"
    "HighlightingTest.java"
    "ImagePathVerificationTest.java"
    "MouseMovementTest.java"
    "CaptureDebugTest.java"
)

# Function to check if file is integration test
is_integration_test() {
    local filename=$(basename "$1")
    for test in "${INTEGRATION_TESTS[@]}"; do
        if [[ "$filename" == "$test" ]]; then
            return 0
        fi
    done
    # Also check for Spring annotations in file
    if grep -q "@SpringBootTest\|ApplicationContext\|@ExtendWith.*SpringExtension" "$1" 2>/dev/null; then
        return 0
    fi
    return 1
}

# Function to update package declaration
update_package() {
    local file="$1"
    local new_package="$2"
    
    # Update package declaration
    sed -i "s/^package com\.claude\.automator.*;/package ${new_package};/" "$file"
    
    # Add BrobotTestBase import if not present
    if ! grep -q "import io.github.jspinak.brobot.test.BrobotTestBase;" "$file"; then
        # Add after package declaration
        sed -i "/^package /a\\\\nimport io.github.jspinak.brobot.test.BrobotTestBase;" "$file"
    fi
    
    # Update class to extend BrobotTestBase if it's a test class and doesn't already extend it
    if grep -q "public class.*Test[[:space:]]*{" "$file"; then
        sed -i "s/public class \([^[:space:]]*Test\)[[:space:]]*{/public class \1 extends BrobotTestBase {/" "$file"
    elif grep -q "public class.*Test[[:space:]]*extends" "$file" && ! grep -q "extends BrobotTestBase" "$file"; then
        # If it extends something else, we need to be more careful
        echo "Warning: $file extends another class, manual review needed"
    fi
}

# Process all test files
echo "Starting migration of test files..."

# Move debug tests
for file in "$SOURCE_DIR"/com/claude/automator/debug/*.java; do
    if [[ -f "$file" ]]; then
        filename=$(basename "$file")
        
        if is_integration_test "$file"; then
            target_dir="$INTEGRATION_TEST_DIR/debug"
            new_package="io.github.jspinak.brobot.integration.debug"
        else
            target_dir="$LIBRARY_TEST_DIR/debug"
            new_package="io.github.jspinak.brobot.debug"
        fi
        
        echo "Moving $filename to $target_dir"
        cp "$file" "$target_dir/$filename"
        update_package "$target_dir/$filename" "$new_package"
    fi
done

# Move logging tests
for file in "$SOURCE_DIR"/com/claude/automator/logging/*.java; do
    if [[ -f "$file" ]]; then
        filename=$(basename "$file")
        
        if is_integration_test "$file"; then
            target_dir="$INTEGRATION_TEST_DIR/logging"
            new_package="io.github.jspinak.brobot.integration.logging"
        else
            target_dir="$LIBRARY_TEST_DIR/logging"
            new_package="io.github.jspinak.brobot.logging"
        fi
        
        echo "Moving $filename to $target_dir"
        cp "$file" "$target_dir/$filename"
        update_package "$target_dir/$filename" "$new_package"
    fi
done

# Move config tests
for file in "$SOURCE_DIR"/com/claude/automator/config/*.java; do
    if [[ -f "$file" ]]; then
        filename=$(basename "$file")
        
        if is_integration_test "$file"; then
            target_dir="$INTEGRATION_TEST_DIR/config"
            new_package="io.github.jspinak.brobot.integration.config"
        else
            target_dir="$LIBRARY_TEST_DIR/config"
            new_package="io.github.jspinak.brobot.config"
        fi
        
        echo "Moving $filename to $target_dir"
        cp "$file" "$target_dir/$filename"
        update_package "$target_dir/$filename" "$new_package"
    fi
done

# Move states tests
mkdir -p "$LIBRARY_TEST_DIR/states"
for file in "$SOURCE_DIR"/com/claude/automator/states/*.java; do
    if [[ -f "$file" ]]; then
        filename=$(basename "$file")
        target_dir="$LIBRARY_TEST_DIR/states"
        new_package="io.github.jspinak.brobot.states"
        
        echo "Moving $filename to $target_dir"
        cp "$file" "$target_dir/$filename"
        update_package "$target_dir/$filename" "$new_package"
    fi
done

# Move root level tests
for file in "$SOURCE_DIR"/com/claude/automator/*.java; do
    if [[ -f "$file" ]]; then
        filename=$(basename "$file")
        
        if is_integration_test "$file"; then
            target_dir="$INTEGRATION_TEST_DIR"
            new_package="io.github.jspinak.brobot.integration"
        else
            target_dir="$LIBRARY_TEST_DIR"
            new_package="io.github.jspinak.brobot"
        fi
        
        echo "Moving $filename to $target_dir"
        cp "$file" "$target_dir/$filename"
        update_package "$target_dir/$filename" "$new_package"
    fi
done

# Handle special case TestRegionDebug.java if it exists
if [[ -f "$SOURCE_DIR/TestRegionDebug.java" ]]; then
    echo "Moving TestRegionDebug.java to $LIBRARY_TEST_DIR/debug"
    cp "$SOURCE_DIR/TestRegionDebug.java" "$LIBRARY_TEST_DIR/debug/TestRegionDebug.java"
    update_package "$LIBRARY_TEST_DIR/debug/TestRegionDebug.java" "io.github.jspinak.brobot.debug"
fi

echo "Migration complete!"
echo ""
echo "Summary:"
echo "- Unit tests moved to: $LIBRARY_TEST_DIR"
echo "- Integration tests moved to: $INTEGRATION_TEST_DIR"
echo ""
echo "Next steps:"
echo "1. Review the migrated tests for compilation errors"
echo "2. Run: ./gradlew :library:test to verify unit tests"
echo "3. Run: ./gradlew :library-test:test to verify integration tests"
echo "4. Remove the claude-automator test directory after verification"