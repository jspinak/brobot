package io.github.jspinak.brobot.integration.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import org.junit.jupiter.api.Disabled;

/**
 * Run diagnostics on the claude-prompt pattern matching issue.
 */
@Disabled("Missing ClaudeAutomatorApplication and ImageMatchingDiagnostics dependencies")
@SpringBootTest // (classes = ClaudeAutomatorApplication.class)
public class RunImageDiagnostics {
    
    // @Autowired
    // private ImageMatchingDiagnostics diagnostics;
    
    // @Autowired
    // private Object imageNormalizer; // TODO: ImageNormalizer not available
    
    @Autowired
    private BufferedImageUtilities bufferedImageUtilities;
    
    @Test
    public void diagnoseClaudePromptPattern() throws Exception {
        System.out.println("\n=== Running Image Matching Diagnostics for claude-prompt ===\n");
        
        // Load the claude-prompt pattern
        Pattern pattern = new Pattern("claude-prompt");
        
        // Capture current screen as scene
        Region screenRegion = new Region(); // Full screen
        BufferedImage screenCapture = BufferedImageUtilities.getBufferedImageFromScreen(screenRegion);
        Pattern scenePattern = new Pattern(screenCapture);
        scenePattern.setName("current-screen");
        Scene scene = new Scene(scenePattern);
        
        // Run diagnostics
        ImageMatchingDiagnostics.DiagnosticReport report = 
            diagnostics.runDiagnostics(pattern, scene);
        
        // Print the report
        report.print();
        
        // Save diagnostic images
        diagnostics.saveDiagnosticImages(pattern, scene, "diagnostics/claude-prompt");
        
        // Additional manual diagnostics
        System.out.println("\n=== Additional Image Analysis ===\n");
        
        BufferedImage patternImg = pattern.getBImage();
        if (patternImg != null) {
            imageNormalizer.diagnoseImage(patternImg, "claude-prompt pattern");
            
            // Check if normalizing helps
            BufferedImage normalized = imageNormalizer.normalizeToRGB(patternImg);
            System.out.println("\nAfter normalization:");
            imageNormalizer.diagnoseImage(normalized, "claude-prompt normalized");
        }
        
        System.out.println("\n=== Diagnostics Complete ===\n");
    }
    
    @Test
    public void comparePatternFiles() throws Exception {
        System.out.println("\n=== Comparing Pattern Image Files ===\n");
        
        // Compare the saved match file with the original pattern
        File savedMatch = new File("working/claude-prompt.png");  // Adjust path as needed
        File originalPattern = new File("src/main/resources/images/claude-prompt.png");
        
        if (savedMatch.exists() && originalPattern.exists()) {
            BufferedImage savedImg = ImageIO.read(savedMatch);
            BufferedImage originalImg = ImageIO.read(originalPattern);
            
            System.out.println("Saved Match Image:");
            imageNormalizer.diagnoseImage(savedImg, "Saved Match");
            
            System.out.println("\nOriginal Pattern Image:");
            imageNormalizer.diagnoseImage(originalImg, "Original Pattern");
            
            // Check compatibility
            boolean compatible = imageNormalizer.areFormatsCompatible(savedImg, originalImg);
            System.out.println("\nImages are compatible: " + compatible);
            
            if (!compatible) {
                System.out.println("\nNormalizing both images to RGB...");
                
                BufferedImage normalizedSaved = imageNormalizer.normalizeToRGB(savedImg);
                BufferedImage normalizedOriginal = imageNormalizer.normalizeToRGB(originalImg);
                
                // Save normalized versions
                File outputDir = new File("diagnostics/normalized");
                outputDir.mkdirs();
                
                imageNormalizer.saveNormalizedImage(savedImg, 
                    new File(outputDir, "claude-prompt-saved-normalized.png"));
                imageNormalizer.saveNormalizedImage(originalImg, 
                    new File(outputDir, "claude-prompt-original-normalized.png"));
                
                System.out.println("Normalized images saved to diagnostics/normalized/");
                
                boolean nowCompatible = imageNormalizer.areFormatsCompatible(
                    normalizedSaved, normalizedOriginal);
                System.out.println("After normalization, images are compatible: " + nowCompatible);
            }
        } else {
            System.out.println("Pattern files not found:");
            System.out.println("  Saved match exists: " + savedMatch.exists());
            System.out.println("  Original pattern exists: " + originalPattern.exists());
        }
    }
}