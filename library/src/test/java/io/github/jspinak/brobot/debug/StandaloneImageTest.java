package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Standalone test for basic image loading - no Spring required
 */
public class StandaloneImageTest {
    
    // Update this to your actual image path
    private static final String TEST_IMAGE_PATH = "prompt/claude-prompt-3.png";
    
    public static void main(String[] args) {
        System.out.println("=== Standalone Image Test ===");
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        
        // Test 1: File exists?
        File imageFile = new File(TEST_IMAGE_PATH);
        System.out.println("\n1. File Check:");
        System.out.println("   Path: " + imageFile.getAbsolutePath());
        System.out.println("   Exists: " + imageFile.exists());
        
        if (!imageFile.exists()) {
            System.out.println("\nERROR: File not found!");
            System.out.println("Make sure the image path is correct.");
            
            // Try to find images directory
            File currentDir = new File(".");
            System.out.println("\nCurrent directory contents:");
            String[] files = currentDir.list();
            if (files != null) {
                for (String file : files) {
                    System.out.println("  " + file);
                }
            }
            
            // Check common image locations
            String[] possiblePaths = {
                "images/" + TEST_IMAGE_PATH,
                "src/test/resources/" + TEST_IMAGE_PATH,
                "../app/" + TEST_IMAGE_PATH,
                "../claude-automator/" + TEST_IMAGE_PATH,
                "../claude-automator/src/main/resources/" + TEST_IMAGE_PATH
            };
            
            System.out.println("\nChecking other possible locations:");
            for (String path : possiblePaths) {
                File f = new File(path);
                System.out.println("  " + path + " -> " + (f.exists() ? "EXISTS" : "not found"));
            }
            
            return;
        }
        
        // Test 2: Load as BufferedImage
        try {
            System.out.println("\n2. BufferedImage Loading:");
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            System.out.println("   Size: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());
            System.out.println("   Type: " + bufferedImage.getType());
            System.out.println("   Success!");
        } catch (Exception e) {
            System.out.println("   ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test 3: Create Pattern
        try {
            System.out.println("\n3. Pattern Creation:");
            Pattern pattern = new Pattern(TEST_IMAGE_PATH);
            System.out.println("   Pattern name: " + pattern.getName());
            System.out.println("   Has imgpath: " + (pattern.getImgpath() != null));
            System.out.println("   Has image: " + (pattern.getImage() != null));
            
            if (pattern.getImage() != null) {
                BufferedImage img = pattern.getImage().getBufferedImage();
                System.out.println("   Image size: " + img.getWidth() + "x" + img.getHeight());
            }
            System.out.println("   Success!");
        } catch (Exception e) {
            System.out.println("   ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test 4: Create StateImage
        try {
            System.out.println("\n4. StateImage Creation:");
            Pattern pattern = new Pattern(TEST_IMAGE_PATH);
            StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .setName("test-state-image")
                .build();
            
            System.out.println("   StateImage name: " + stateImage.getName());
            System.out.println("   Pattern count: " + stateImage.getPatterns().size());
            System.out.println("   Success!");
        } catch (Exception e) {
            System.out.println("   ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Test Complete ===");
    }
}