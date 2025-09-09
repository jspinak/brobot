#!/bin/bash

# Script to update imports in migrated tests to use correct Brobot classes

echo "Updating imports in migrated tests..."

# Function to update imports in a file
update_file_imports() {
    local file="$1"
    
    # Update DPI-related imports
    sed -i 's|import io\.github\.jspinak\.brobot\.util\.DPIScalingDetector;|import io.github.jspinak.brobot.config.dpi.DPIAutoDetector;|g' "$file"
    sed -i 's|import io\.github\.jspinak\.brobot\.config\.BrobotDPIConfig;|import io.github.jspinak.brobot.config.dpi.DPIConfiguration;|g' "$file"
    
    # Update class references
    sed -i 's|DPIScalingDetector\.detectScalingFactor()|new DPIAutoDetector().detectScalingFactor()|g' "$file"
    sed -i 's|DPIScalingDetector\.getScalingDescription()|new DPIAutoDetector().getScalingDescription()|g' "$file"
    sed -i 's|DPIScalingDetector\.hasScaling()|new DPIAutoDetector().hasScaling()|g' "$file"
    sed -i 's|BrobotDPIConfig|DPIConfiguration|g' "$file"
    
    # Remove or comment out references to non-existent ImageNormalizer
    sed -i 's|import com\.claude\.automator\.util\.ImageNormalizer;|// ImageNormalizer not available - need to implement or remove|g' "$file"
    sed -i 's|ImageNormalizer imageNormalizer|Object imageNormalizer \/\/ TODO: ImageNormalizer not available|g' "$file"
    
    # Fix the disabled annotations
    sed -i 's|// Missing: DPIScalingDetector|import io.github.jspinak.brobot.config.dpi.DPIAutoDetector;|g' "$file"
    sed -i 's|// Missing: BrobotDPIConfig|import io.github.jspinak.brobot.config.dpi.DPIConfiguration;|g' "$file"
}

# Update all Java files in the debug directories
for file in /home/jspinak/brobot_parent/brobot/library/src/test/java/io/github/jspinak/brobot/debug/*.java; do
    if [ -f "$file" ]; then
        echo "Updating: $(basename "$file")"
        update_file_imports "$file"
    fi
done

for file in /home/jspinak/brobot_parent/brobot/library-test/src/test/java/io/github/jspinak/brobot/integration/debug/*.java; do
    if [ -f "$file" ]; then
        echo "Updating: $(basename "$file")"
        update_file_imports "$file"
    fi
done

# Re-enable DPIScalingTest since we found the correct classes
if [ -f "/home/jspinak/brobot_parent/brobot/library/src/test/java/io/github/jspinak/brobot/debug/DPIScalingTest.java" ]; then
    echo "Re-enabling DPIScalingTest..."
    sed -i '/@Disabled("Missing DPIScalingDetector and BrobotDPIConfig classes")/d' \
        "/home/jspinak/brobot_parent/brobot/library/src/test/java/io/github/jspinak/brobot/debug/DPIScalingTest.java"
fi

echo "Import updates complete!"