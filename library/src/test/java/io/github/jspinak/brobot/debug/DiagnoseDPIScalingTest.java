package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.debug.DebugTestBase;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.core.FrameworkSettings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Diagnoses the DPI scaling issue by comparing pattern and capture dimensions.
 */
@DisabledInCI
public class DiagnoseDPIScalingTest extends DebugTestBase {
    
    @Test
    public void diagnoseDPIScaling() throws Exception {
        System.out.println("\n========== DPI SCALING DIAGNOSIS ==========\n");
        
        // 1. Check current settings
        System.out.println("1. CURRENT SETTINGS:");
        System.out.println("   Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println("   Settings.MinSimilarity: " + Settings.MinSimilarity);
        
        // 2. Check screen dimensions
        System.out.println("\n2. SCREEN DIMENSIONS:");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        System.out.println("   Logical screen size: " + screenSize.width + "x" + screenSize.height);
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        var transform = gc.getDefaultTransform();
        System.out.println("   Transform scale: " + transform.getScaleX() + "x" + transform.getScaleY());
        
        if (transform.getScaleX() > 1.0) {
            int physicalWidth = (int)(screenSize.width * transform.getScaleX());
            int physicalHeight = (int)(screenSize.height * transform.getScaleY());
            System.out.println("   Estimated physical: " + physicalWidth + "x" + physicalHeight);
            System.out.println("   Windows DPI scaling: " + (int)(transform.getScaleX() * 100) + "%");
        }
        
        // 3. Capture current screen
        System.out.println("\n3. SCREEN CAPTURE TEST:");
        Screen screen = new Screen();
        ScreenImage capture = screen.capture();
        BufferedImage capturedImage = capture.getImage();
        System.out.println("   Captured dimensions: " + capturedImage.getWidth() + "x" + capturedImage.getHeight());
        
        // Save the capture for inspection
        File captureFile = new File("test-capture.png");
        ImageIO.write(capturedImage, "png", captureFile);
        System.out.println("   Saved to: " + captureFile.getAbsolutePath());
        
        // 4. Check pattern dimensions
        System.out.println("\n4. PATTERN DIMENSIONS:");
        String[] patterns = {
            "images/prompt/claude-prompt-1.png",
            "images/working/working-1.png"
        };
        
        for (String patternPath : patterns) {
            File patternFile = new File(patternPath);
            if (patternFile.exists()) {
                BufferedImage patternImage = ImageIO.read(patternFile);
                System.out.println("   " + patternPath + ": " + 
                    patternImage.getWidth() + "x" + patternImage.getHeight());
                
                // Calculate what size it would be after resize
                if (Settings.AlwaysResize != 0 && Settings.AlwaysResize != 1.0f) {
                    int resizedWidth = (int)(patternImage.getWidth() * Settings.AlwaysResize);
                    int resizedHeight = (int)(patternImage.getHeight() * Settings.AlwaysResize);
                    System.out.println("     After resize (" + Settings.AlwaysResize + "): " + 
                        resizedWidth + "x" + resizedHeight);
                }
            } else {
                System.out.println("   " + patternPath + ": NOT FOUND");
            }
        }
        
        // 5. Test pattern matching with different resize factors
        System.out.println("\n5. TESTING PATTERN MATCHING:");
        
        File testPattern = new File("images/prompt/claude-prompt-1.png");
        if (testPattern.exists()) {
            BufferedImage patternImage = ImageIO.read(testPattern);
            System.out.println("   Pattern size: " + patternImage.getWidth() + "x" + patternImage.getHeight());
            
            // Test with different resize factors
            float[] testFactors = {1.0f, 0.8f, 0.667f, 1.25f, 1.5f};
            for (float factor : testFactors) {
                Settings.AlwaysResize = factor;
                Pattern pattern = new Pattern(patternImage).similar(0.70);
                
                // Calculate expected size after resize
                int expectedWidth = (int)(patternImage.getWidth() * factor);
                int expectedHeight = (int)(patternImage.getHeight() * factor);
                
                System.out.println("\n   Testing with AlwaysResize = " + factor);
                System.out.println("   Expected search size: " + expectedWidth + "x" + expectedHeight);
                
                try {
                    Match match = screen.find(pattern);
                    System.out.println("   ✓ FOUND at " + match.x + "," + match.y + 
                                     " (score: " + String.format("%.3f", match.getScore()) + ")");
                } catch (FindFailed e) {
                    System.out.println("   ✗ NOT FOUND");
                }
            }
        }
        
        // 6. Diagnosis
        System.out.println("\n6. DIAGNOSIS:");
        if (capturedImage.getWidth() == 1536 && capturedImage.getHeight() == 864) {
            System.out.println("   Screen is capturing at LOGICAL resolution (1536x864)");
            System.out.println("   This is 125% DPI scaling of 1920x1080");
            System.out.println("   Patterns captured at physical resolution need 0.8x scaling");
            System.out.println("   RECOMMENDATION: Set brobot.dpi.resize-factor=0.8");
        } else if (capturedImage.getWidth() == 1920 && capturedImage.getHeight() == 1080) {
            System.out.println("   Screen is capturing at PHYSICAL resolution (1920x1080)");
            System.out.println("   No scaling needed");
            System.out.println("   RECOMMENDATION: Set brobot.dpi.resize-factor=1.0");
        } else {
            System.out.println("   Unexpected resolution: " + capturedImage.getWidth() + "x" + capturedImage.getHeight());
            System.out.println("   May need custom scaling factor");
        }
        
        System.out.println("\n========================================\n");
    }
}