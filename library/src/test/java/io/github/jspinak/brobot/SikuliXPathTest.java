package io.github.jspinak.brobot;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Pattern;

import io.github.jspinak.brobot.test.BrobotTestBase;

public class SikuliXPathTest extends BrobotTestBase {

    @Test
    public void testCorrectImagePathConfiguration() {
        System.out.println("=== Testing Correct SikuliX Path Configuration ===");
        System.out.println("Working directory: " + System.getProperty("user.dir"));

        // Clear any existing paths
        ImagePath.reset();

        // Test 1: Set bundle path to "images" only (no subdirectory additions)
        System.out.println("\n--- Test 1: Bundle path only (CORRECT) ---");
        ImagePath.setBundlePath("images");
        System.out.println("Bundle path set to: " + ImagePath.getBundlePath());

        testPatternLoading("Test 1");

        // Reset for next test
        ImagePath.reset();

        // Test 2: Set bundle path and add subdirectories (INCORRECT - what Brobot does)
        System.out.println("\n--- Test 2: Bundle path + subdirectory additions (INCORRECT) ---");
        ImagePath.setBundlePath("images");
        ImagePath.add("images/working");
        ImagePath.add("images/prompt");
        System.out.println("Bundle path: " + ImagePath.getBundlePath());

        testPatternLoading("Test 2");

        // Reset for next test
        ImagePath.reset();

        // Test 3: Set bundle path as absolute path (what our fix does)
        System.out.println("\n--- Test 3: Absolute bundle path (OUR FIX) ---");
        File imagesDir = new File("images");
        String absolutePath = imagesDir.getAbsolutePath();
        ImagePath.setBundlePath(absolutePath);
        ImagePath.add(absolutePath + "/working");
        ImagePath.add(absolutePath + "/prompt");
        System.out.println("Bundle path: " + ImagePath.getBundlePath());

        testPatternLoading("Test 3");
    }

    private void testPatternLoading(String testName) {
        String[] patterns = {
            "working/claude-icon-1.png",
            "working/claude-icon-1",
            "prompt/claude-prompt-1.png",
            "prompt/claude-prompt-1"
        };

        int successCount = 0;
        for (String pattern : patterns) {
            try {
                Pattern p = new Pattern(pattern);
                boolean loaded = p.getBImage() != null;
                System.out.println("  " + pattern + ": " + (loaded ? "✓ LOADED" : "✗ FAILED"));
                if (loaded) successCount++;
            } catch (Exception e) {
                System.out.println("  " + pattern + ": ✗ ERROR - " + e.getMessage());
            }
        }
        System.out.println(
                testName + " Result: " + successCount + "/" + patterns.length + " patterns loaded");
    }
}
