package io.github.jspinak.brobot;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.annotations.DisabledInHeadlessEnvironment;

@DisabledInHeadlessEnvironment("Requires image files for path testing")
public class ImagePathDebugTest extends BrobotTestBase {

    @Test
    public void testImagePathConfiguration() {
        System.out.println("=== Image Path Debug Test ===");
        System.out.println("Current working directory: " + System.getProperty("user.dir"));

        // Check if images directory exists
        File imagesDir = new File("images");
        System.out.println("Images directory exists: " + imagesDir.exists());
        System.out.println("Images directory absolute path: " + imagesDir.getAbsolutePath());

        // Check subdirectories
        File workingDir = new File("images/working");
        File promptDir = new File("images/prompt");
        System.out.println("images/working exists: " + workingDir.exists());
        System.out.println("images/prompt exists: " + promptDir.exists());

        // Check specific image files
        File[] testFiles = {
            new File("images/working/claude-icon-1.png"),
            new File("images/prompt/claude-prompt-1.png")
        };

        for (File f : testFiles) {
            System.out.println(f.getPath() + " exists: " + f.exists());
        }

        // Set ImagePath and test
        System.out.println("\n=== Setting ImagePath ===");
        ImagePath.setBundlePath("images");
        System.out.println("Bundle path set to: " + ImagePath.getBundlePath());

        // Add subdirectories
        ImagePath.add("images/working");
        ImagePath.add("images/prompt");

        // Test pattern loading
        String[] patterns = {"working/claude-icon-1.png", "prompt/claude-prompt-1.png"};

        for (String pattern : patterns) {
            try {
                org.sikuli.script.Pattern p = new org.sikuli.script.Pattern(pattern);
                System.out.println("Pattern '" + pattern + "' loaded: " + (p.getBImage() != null));
            } catch (Exception e) {
                System.out.println("Failed to load pattern '" + pattern + "': " + e.getMessage());
            }
        }
    }
}
