#!/bin/bash

# Build script for all brobot examples
echo "Building all brobot example projects..."
echo ""

# Counter for success/failure
SUCCESS=0
FAILED=0
FAILED_PROJECTS=""

# Function to build a project
build_project() {
    local dir=$1
    local name=$2
    
    echo "========================================="
    echo "Building: $name"
    echo "Path: $dir"
    echo "========================================="
    
    cd "$dir"
    if ./gradlew clean build --no-daemon > /tmp/build_output.log 2>&1; then
        echo "✓ SUCCESS: $name"
        ((SUCCESS++))
    else
        echo "✗ FAILED: $name"
        echo "Error output:"
        tail -20 /tmp/build_output.log
        ((FAILED++))
        FAILED_PROJECTS="$FAILED_PROJECTS\n  - $name"
    fi
    echo ""
}

# Build all projects
build_project "/home/jspinak/brobot_parent/brobot/examples/01-getting-started/action-hierarchy" "action-hierarchy"
build_project "/home/jspinak/brobot_parent/brobot/examples/01-getting-started/pure-actions-quickstart" "pure-actions-quickstart"
build_project "/home/jspinak/brobot_parent/brobot/examples/01-getting-started/quick-start" "quick-start"
build_project "/home/jspinak/brobot_parent/brobot/examples/03-core-library/action-config/conditional-chains-examples" "conditional-chains-examples"
build_project "/home/jspinak/brobot_parent/brobot/examples/03-core-library/action-config/movement" "movement"
build_project "/home/jspinak/brobot_parent/brobot/examples/03-core-library/guides/advanced-illustration-system" "advanced-illustration-system"
build_project "/home/jspinak/brobot_parent/brobot/examples/03-core-library/guides/automatic-action-logging" "automatic-action-logging"
build_project "/home/jspinak/brobot_parent/brobot/examples/03-core-library/guides/automatic-action-logging-fixed" "automatic-action-logging-fixed"
build_project "/home/jspinak/brobot_parent/brobot/examples/03-core-library/guides/finding-objects/combining-finds" "combining-finds"
build_project "/home/jspinak/brobot_parent/brobot/examples/03-core-library/guides/finding-objects/using-color" "using-color"
build_project "/home/jspinak/brobot_parent/brobot/examples/03-core-library/testing/enhanced-mocking" "enhanced-mocking"
build_project "/home/jspinak/brobot_parent/brobot/examples/03-core-library/testing/unit-testing" "unit-testing"
build_project "/home/jspinak/brobot_parent/brobot/examples/03-core-library/tutorials/tutorial-basics" "tutorial-basics"
build_project "/home/jspinak/brobot_parent/brobot/examples/03-core-library/tutorials/tutorial-claude-automator" "tutorial-claude-automator"

# Summary
echo ""
echo "========================================="
echo "BUILD SUMMARY"
echo "========================================="
echo "Successful builds: $SUCCESS"
echo "Failed builds: $FAILED"

if [ $FAILED -gt 0 ]; then
    echo ""
    echo "Failed projects:$FAILED_PROJECTS"
fi

echo ""
if [ $FAILED -eq 0 ]; then
    echo "✓ All projects built successfully!"
    exit 0
else
    echo "✗ Some projects failed to build"
    exit 1
fi