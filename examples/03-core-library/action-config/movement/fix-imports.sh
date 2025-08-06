#!/bin/bash

# Fix imports in all Java files
find src/main/java -name "*.java" -exec sed -i '' '
s/io\.github\.jspinak\.brobot\.action\.interfaces\.Action/io.github.jspinak.brobot.action.Action/g
s/io\.github\.jspinak\.brobot\.model\.action\.ActionResult/io.github.jspinak.brobot.action.ActionResult/g
s/io\.github\.jspinak\.brobot\.model\.mouse\.MouseMoveOptions/io.github.jspinak.brobot.action.basic.move.MouseMoveOptions/g
s/io\.github\.jspinak\.brobot\.model\.mouse\.MovementPattern/io.github.jspinak.brobot.action.basic.move.MovementOptions/g
s/io\.github\.jspinak\.brobot\.model\.mouse\.MouseDownOptions/io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions/g
s/io\.github\.jspinak\.brobot\.model\.mouse\.MouseUpOptions/io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions/g
s/io\.github\.jspinak\.brobot\.model\.drag\.DragOptions/io.github.jspinak.brobot.action.basic.drag.DragOptions/g
s/io\.github\.jspinak\.brobot\.model\.drag\.DragSpeed/io.github.jspinak.brobot.action.basic.drag.DragOptions/g
s/io\.github\.jspinak\.brobot\.model\.region\.Location/io.github.jspinak.brobot.model.element.Location/g
s/io\.github\.jspinak\.brobot\.model\.region\.Region/io.github.jspinak.brobot.model.element.Region/g
s/io\.github\.jspinak\.brobot\.model\.pattern\.PatternFindOptions/io.github.jspinak.brobot.action.basic.find.PatternFindOptions/g
s/io\.github\.jspinak\.brobot\.model\.scroll\.ScrollOptions/io.github.jspinak.brobot.action.basic.scroll.ScrollOptions/g
s/io\.github\.jspinak\.brobot\.model\.scroll\.ScrollDirection/io.github.jspinak.brobot.action.basic.scroll.ScrollOptions/g
s/io\.github\.jspinak\.brobot\.model\.state\.ObjectCollection/io.github.jspinak.brobot.action.ObjectCollection/g
' {} \;

# Remove lombok annotations
find src/main/java -name "*.java" -exec sed -i '' '
s/import lombok\.extern\.slf4j\.Slf4j;/import org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;/g
' {} \;

# Fix @Slf4j annotations
find src/main/java -name "*.java" -exec sed -i '' '
/@Slf4j/d
' {} \;

# Add logger declaration after class declaration
find src/main/java -name "*.java" -exec perl -i -pe '
if (/^public class (\w+)/) {
    $classname = $1;
    $_ .= "    private static final Logger log = LoggerFactory.getLogger(" . $classname . ".class);\n";
}
' {} \;

echo "Imports fixed!"