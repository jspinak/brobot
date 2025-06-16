package io.github.jspinak.brobot.runner.testutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility to help fix JavaFX tests by applying the initialization pattern.
 */
public class FixJavaFXTest {
    
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java FixJavaFXTest <test-file-path>");
            return;
        }
        
        Path testFile = Paths.get(args[0]);
        fixTestFile(testFile);
    }
    
    public static void fixTestFile(Path testFile) throws IOException {
        List<String> lines = Files.readAllLines(testFile);
        StringBuilder fixed = new StringBuilder();
        
        boolean hasImports = false;
        boolean hasBeforeAll = false;
        boolean inClass = false;
        boolean addedBeforeAll = false;
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            
            // Skip @ExtendWith(ApplicationExtension.class)
            if (line.contains("@ExtendWith") && line.contains("ApplicationExtension")) {
                // Remove the line, but keep other extensions
                if (line.contains("MockitoExtension")) {
                    fixed.append("@ExtendWith(MockitoExtension.class)\n");
                }
                continue;
            }
            
            // Skip ApplicationExtension import
            if (line.contains("import") && line.contains("ApplicationExtension")) {
                continue;
            }
            
            // Add JavaFXTestUtils import after other imports
            if (!hasImports && line.startsWith("import") && line.contains("org.junit")) {
                fixed.append("import io.github.jspinak.brobot.runner.testutil.JavaFXTestUtils;\n");
                hasImports = true;
            }
            
            // Check if we have @BeforeAll
            if (line.contains("@BeforeAll")) {
                hasBeforeAll = true;
            }
            
            // Skip @Start methods
            if (line.trim().equals("@Start")) {
                // Skip until we find the closing brace
                i++; // Skip the method signature
                int braceCount = 0;
                boolean started = false;
                while (i < lines.size()) {
                    String skipLine = lines.get(i);
                    if (skipLine.contains("{")) {
                        started = true;
                        braceCount++;
                    }
                    if (skipLine.contains("}")) {
                        braceCount--;
                        if (started && braceCount == 0) {
                            i++;
                            break;
                        }
                    }
                    i++;
                }
                i--; // Back up one since the loop will increment
                continue;
            }
            
            // Add @BeforeAll after class declaration if not present
            if (!addedBeforeAll && !hasBeforeAll && line.contains("public class") && line.contains("Test")) {
                fixed.append(line).append("\n");
                fixed.append("\n");
                fixed.append("    @BeforeAll\n");
                fixed.append("    public static void initJavaFX() throws InterruptedException {\n");
                fixed.append("        JavaFXTestUtils.initJavaFX();\n");
                fixed.append("    }\n");
                addedBeforeAll = true;
                inClass = true;
                continue;
            }
            
            // Fix @BeforeEach methods that create JavaFX components
            if (inClass && line.contains("@BeforeEach")) {
                fixed.append(line).append("\n");
                i++;
                if (i < lines.size()) {
                    String methodLine = lines.get(i);
                    // Add throws InterruptedException if not present
                    if (!methodLine.contains("throws")) {
                        methodLine = methodLine.replace(")", ") throws InterruptedException");
                    }
                    fixed.append(methodLine).append("\n");
                    
                    // Look for component creation
                    i++;
                    if (i < lines.size() && lines.get(i).contains("{")) {
                        fixed.append(lines.get(i)).append("\n");
                        i++;
                        
                        // Collect the method body
                        StringBuilder methodBody = new StringBuilder();
                        int braceCount = 1;
                        while (i < lines.size() && braceCount > 0) {
                            String bodyLine = lines.get(i);
                            if (bodyLine.contains("{")) braceCount++;
                            if (bodyLine.contains("}")) braceCount--;
                            
                            if (braceCount > 0) {
                                methodBody.append(bodyLine).append("\n");
                            } else {
                                // Wrap in runOnFXThread if it creates components
                                if (methodBody.toString().contains("= new")) {
                                    fixed.append("        JavaFXTestUtils.runOnFXThread(() -> {\n");
                                    fixed.append(methodBody.toString());
                                    fixed.append("        });\n");
                                } else {
                                    fixed.append(methodBody.toString());
                                }
                                fixed.append(bodyLine).append("\n");
                            }
                            i++;
                        }
                        i--;
                    }
                }
                continue;
            }
            
            fixed.append(line).append("\n");
        }
        
        Files.writeString(testFile, fixed.toString());
        System.out.println("Fixed: " + testFile);
    }
}