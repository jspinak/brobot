package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Create scaled versions of patterns at 0.8x for better matching
 */
@DisabledInCI
public class CreateScaledPatternsTest extends BrobotTestBase {
    
    @Test
    public void createScaledPatterns() {
        System.out.println("=== CREATING SCALED PATTERNS (0.8x) ===\n");
        
        try {
            // Patterns to scale
            String[] patterns = {
                "images/prompt/claude-prompt-1.png",
                "images/prompt/claude-prompt-2.png",
                "images/prompt/claude-prompt-3.png",
                "images/working/claude-icon-1.png",
                "images/working/claude-icon-2.png",
                "images/working/claude-icon-3.png",
                "images/working/claude-icon-4.png"
            };
            
            // Create output directory
            File outputDir = new File("images-scaled");
            File promptDir = new File(outputDir, "prompt");
            File workingDir = new File(outputDir, "working");
            promptDir.mkdirs();
            workingDir.mkdirs();
            
            double scale = 0.8;
            
            for (String patternPath : patterns) {
                File inputFile = new File(patternPath);
                
                if (!inputFile.exists()) {
                    System.out.println("Skipping (not found): " + patternPath);
                    continue;
                }
                
                System.out.println("Processing: " + patternPath);
                
                // Load original
                BufferedImage original = ImageIO.read(inputFile);
                System.out.println("  Original: " + original.getWidth() + "x" + original.getHeight());
                
                // Calculate new dimensions
                int newWidth = (int)(original.getWidth() * scale);
                int newHeight = (int)(original.getHeight() * scale);
                System.out.println("  Scaled:   " + newWidth + "x" + newHeight);
                
                // Create scaled version
                BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = scaled.createGraphics();
                
                // Use high quality rendering
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
                g2d.dispose();
                
                // Determine output path
                File outputFile;
                if (patternPath.contains("prompt")) {
                    outputFile = new File(promptDir, inputFile.getName());
                } else {
                    outputFile = new File(workingDir, inputFile.getName());
                }
                
                // Save scaled image
                ImageIO.write(scaled, "png", outputFile);
                System.out.println("  Saved to: " + outputFile.getPath());
                
                // Compare file sizes
                System.out.println("  Original size: " + inputFile.length() + " bytes");
                System.out.println("  Scaled size:   " + outputFile.length() + " bytes");
                System.out.println();
            }
            
            System.out.println("=== SCALING COMPLETE ===");
            System.out.println("\nScaled patterns saved to: " + outputDir.getAbsolutePath());
            System.out.println("\nTo use these patterns:");
            System.out.println("1. Update your code to use 'images-scaled' instead of 'images'");
            System.out.println("2. Or replace the original images with these scaled versions");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}