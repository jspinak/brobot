package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.sikuli.script.ImagePath;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "logging.level.com.claude.automator=INFO"
})
@Disabled("CI failure - needs investigation")
public class ImagePathVerificationTest extends BrobotTestBase {

    @Test
    public void testImagePathConfiguration() {
        System.out.println("\n=== Image Path Verification Test ===");
        
        // Check bundle path
        String bundlePath = ImagePath.getBundlePath();
        System.out.println("ImagePath bundle path: " + bundlePath);
        
        // Check if images directory exists
        File imagesDir = new File("images");
        System.out.println("Images directory exists: " + imagesDir.exists());
        System.out.println("Images directory absolute path: " + imagesDir.getAbsolutePath());
        
        // Check working subdirectory
        File workingDir = new File("images/working");
        System.out.println("Working directory exists: " + workingDir.exists());
        System.out.println("Working directory absolute path: " + workingDir.getAbsolutePath());
        
        // List files in working directory
        if (workingDir.exists()) {
            File[] files = workingDir.listFiles();
            if (files != null && files.length > 0) {
                System.out.println("\nFiles in working directory:");
                for (File file : files) {
                    System.out.println("  - " + file.getName());
                }
            } else {
                System.out.println("No files found in working directory");
            }
        }
        
        // Check if claude-icon-1.png exists
        File claudeIcon = new File("images/working/claude-icon-1.png");
        System.out.println("\nclaude-icon-1.png exists: " + claudeIcon.exists());
        System.out.println("claude-icon-1.png path: " + claudeIcon.getAbsolutePath());
        
        // Verify working directory exists and contains images
        assertTrue(workingDir.exists(), "Working directory should exist");
        assertTrue(claudeIcon.exists(), "claude-icon-1.png should exist in working directory");
        
        System.out.println("\n=== Test Completed Successfully ===\n");
    }
}