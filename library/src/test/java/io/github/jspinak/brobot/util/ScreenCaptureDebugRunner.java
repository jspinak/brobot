package io.github.jspinak.brobot.util;

import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class ScreenCaptureDebugRunner {
    public static void main(String[] args) {
        try {
            // Set headless to false
            System.setProperty("java.awt.headless", "false");
            
            // Check GraphicsEnvironment
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            System.out.println("=== Graphics Environment ===");
            System.out.println("GraphicsEnvironment.isHeadless: " + ge.isHeadless());
            System.out.println("java.awt.headless property: " + System.getProperty("java.awt.headless"));
            
            // Build ExecutionEnvironment with explicit settings
            ExecutionEnvironment env = ExecutionEnvironment.builder()
                    .mockMode(false)
                    .forceHeadless(false)
                    .allowScreenCapture(true)
                    .fromEnvironment()
                    .build();
            ExecutionEnvironment.setInstance(env);
            
            System.out.println("\n=== Execution Environment ===");
            System.out.println("ExecutionEnvironment.hasDisplay: " + env.hasDisplay());
            System.out.println("ExecutionEnvironment.canCaptureScreen: " + env.canCaptureScreen());
            System.out.println("ExecutionEnvironment.isMockMode: " + env.isMockMode());
            System.out.println("ExecutionEnvironment.useRealFiles: " + env.useRealFiles());
            
            if (!env.canCaptureScreen()) {
                System.out.println("\nERROR: ExecutionEnvironment says cannot capture screen!");
                return;
            }
            
            // Try screen capture with BufferedImageUtilities instance
            System.out.println("\n=== Attempting Screen Capture ===");
            BufferedImageUtilities utils = new BufferedImageUtilities();
            Region fullScreen = new Region(); // Default is full screen
            BufferedImage screenshot = utils.getBuffImgFromScreen(fullScreen);
            
            if (screenshot == null) {
                System.out.println("ERROR: Screenshot is null!");
                return;
            }
            
            System.out.println("Screenshot captured successfully!");
            System.out.println("Dimensions: " + screenshot.getWidth() + "x" + screenshot.getHeight());
            
            // Check if the image is not all black
            boolean hasNonBlackPixels = false;
            int blackPixelCount = 0;
            int totalPixelsChecked = 0;
            int maxCheck = Math.min(100, screenshot.getWidth());
            int maxY = Math.min(100, screenshot.getHeight());
            
            for (int x = 0; x < maxCheck; x++) {
                for (int y = 0; y < maxY; y++) {
                    totalPixelsChecked++;
                    int rgb = screenshot.getRGB(x, y);
                    if (rgb == 0xFF000000) { // Pure black
                        blackPixelCount++;
                    } else {
                        hasNonBlackPixels = true;
                    }
                }
            }
            
            System.out.println("Has non-black pixels: " + hasNonBlackPixels);
            System.out.println("Black pixels: " + blackPixelCount + " out of " + totalPixelsChecked + " checked");
            
            // Save the screenshot to verify
            File outputFile = new File("test-screenshot.png");
            ImageIO.write(screenshot, "png", outputFile);
            System.out.println("Screenshot saved to: " + outputFile.getAbsolutePath());
            
            // Try static method
            System.out.println("\n=== Testing Static Method ===");
            BufferedImage staticScreenshot = BufferedImageUtilities.getBufferedImageFromScreen(fullScreen);
            if (staticScreenshot != null) {
                System.out.println("Static method also works!");
                System.out.println("Dimensions: " + staticScreenshot.getWidth() + "x" + staticScreenshot.getHeight());
            } else {
                System.out.println("ERROR: Static method returned null!");
            }
            
            System.out.println("\n=== Summary ===");
            if (hasNonBlackPixels) {
                System.out.println("✓ Screen capture is working correctly - image contains non-black pixels");
            } else {
                System.out.println("✗ Screen capture produces all-black images - this is the problem!");
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}