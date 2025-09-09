#!/bin/bash

# Script to add proper test conditions to screen-dependent tests

echo "Adding test conditions to screen-dependent tests..."

# List of tests that require actual screen access
SCREEN_TESTS=(
    "CompareIDEScreenshotTest"
    "IDEScreenCaptureTest"
    "CoordinateMappingTest"
    "OffsetPatternAnalysisTest"
    "ScreenCaptureInvestigationTest"
    "DefinitivePatternTest"
    "FullScreenSearchTest"
    "MonitorDetectionTest"
    "DiagnoseDPIScalingTest"
    "IDEScreenshotReplicationTest"
    "ScreenCaptureComparisonTest"
    "LiveHighlightTest"
    "PhysicalResolutionCaptureTest"
)

# Function to add assumeFalse for mock mode
add_mock_check() {
    local file="$1"
    
    # Check if already has the assume statement
    if grep -q "assumeFalse.*FrameworkSettings.mock" "$file"; then
        echo "  Already has mock check: $(basename "$file")"
        return
    fi
    
    # Add import for Assumptions if not present
    if ! grep -q "import org.junit.jupiter.api.Assumptions" "$file"; then
        sed -i '/import org.junit.jupiter.api.Test;/a\import org.junit.jupiter.api.Assumptions;' "$file"
    fi
    
    # Add import for FrameworkSettings if not present
    if ! grep -q "import io.github.jspinak.brobot.config.core.FrameworkSettings" "$file"; then
        sed -i '/import io.github.jspinak.brobot/a\import io.github.jspinak.brobot.config.core.FrameworkSettings;' "$file"
    fi
    
    # Add assumeFalse at the beginning of each @Test method
    # This will skip the test if we're in mock mode
    sed -i '/@Test/{n;/^[[:space:]]*public void/{n;/^[[:space:]]*{/a\
        Assumptions.assumeFalse(FrameworkSettings.mock, "Skipping screen-dependent test in mock mode");
    }}' "$file"
    
    echo "  Added mock check to: $(basename "$file")"
}

# Process each screen-dependent test
for test_name in "${SCREEN_TESTS[@]}"; do
    file="/home/jspinak/brobot_parent/brobot/library/src/test/java/io/github/jspinak/brobot/debug/${test_name}.java"
    if [ -f "$file" ]; then
        add_mock_check "$file"
    fi
done

echo ""
echo "Screen-dependent tests have been updated to skip in mock mode."
echo "These tests will only run when FrameworkSettings.mock = false"