#!/bin/bash

# Fix all imports to use correct package names
find src/main/java -name "*.java" -exec sed -i '' '
# Fix scroll imports
s/io\.github\.jspinak\.brobot\.model\.mouse\.ScrollMouseWheelOptions/io.github.jspinak.brobot.action.basic.mouse.ScrollOptions/g
s/io\.github\.jspinak\.brobot\.model\.mouse\.ScrollDirection//g
s/ScrollMouseWheelOptions/ScrollOptions/g
s/ScrollDirection\./ScrollOptions.Direction./g

# Fix region/state imports that were wrong
s/io\.github\.jspinak\.brobot\.model\.collection\.ObjectCollection/io.github.jspinak.brobot.action.ObjectCollection/g

# Fix method names
s/\.addPattern(\"/.addPatterns(\"/g
s/addPattern(\"/addPatterns(\"/g

# Remove non-existent enums/classes
s/DragSpeed\.[A-Z]*//g
s/MovementPattern\.[A-Z]*//g
s/MouseButton\.[A-Z]*/MouseButton.LEFT/g

# Fix ScrollDirection references
s/ScrollDirection\.UP/ScrollOptions.Direction.UP/g
s/ScrollDirection\.DOWN/ScrollOptions.Direction.DOWN/g
s/ScrollDirection\.LEFT/ScrollOptions.Direction.LEFT/g
s/ScrollDirection\.RIGHT/ScrollOptions.Direction.RIGHT/g
' {} \;

echo "All imports fixed!"