package io.github.jspinak.brobot.integration.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

// import io.github.jspinak.brobot.util.image.capture.DPIAwareCapture;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import io.github.jspinak.brobot.model.element.Region;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Disabled;

/**
 * Test DPI-aware capture functionality.
 */
@Disabled("Missing ClaudeAutomatorApplication and DPIAwareCapture dependencies")
@SpringBootTest // (classes = ClaudeAutomatorApplication.class)
public class DPIAwareCaptureTest extends BrobotTestBase {
    
    // @Autowired
    // private DPIAwareCapture dpiAwareCapture;
    
    @Test
    public void testDPIDetection() {
        System.out.println("\n=== DPI Detection Test ===\n");
        
        double scaleFactor = dpiAwareCapture.getDisplayScaleFactor();
        System.out.println("Display scale factor: " + (int)(scaleFactor * 100) + "%");
        
        if (dpiAwareCapture.isScalingActive()) {
            System.out.println("Display scaling is ACTIVE");
            System.out.println("This means captured images need to be normalized for pattern matching");
        } else {
            System.out.println("Display scaling is NOT active (100% scale)");
            System.out.println("No normalization needed for pattern matching");
        }
        
        System.out.println("\n=== Test Complete ===\n");
    }
    
    @Test
    public void testDPIAwareCapture() throws IOException {
        System.out.println("\n=== DPI-Aware Capture Test ===\n");
        
        // Test capturing a small region
        int x = 100, y = 100, width = 200, height = 150;
        
        System.out.println("Capturing region: " + width + "x" + height + " at (" + x + "," + y + ")");
        
        // Capture with DPI awareness
        BufferedImage captured = dpiAwareCapture.captureDPIAware(x, y, width, height);
        
        System.out.println("Captured image dimensions: " + 
            captured.getWidth() + "x" + captured.getHeight());
        
        // The captured image should always be at logical resolution
        assert captured.getWidth() == width : "Width mismatch: expected " + width + " but got " + captured.getWidth();
        assert captured.getHeight() == height : "Height mismatch: expected " + height + " but got " + captured.getHeight();
        
        // Save the captured image for inspection
        File outputDir = new File("diagnostics/dpi-test");
        outputDir.mkdirs();
        File outputFile = new File(outputDir, "dpi-aware-capture.png");
        ImageIO.write(captured, "png", outputFile);
        System.out.println("Saved captured image to: " + outputFile.getPath());
        
        System.out.println("\n=== Test Complete ===\n");
    }
    
    @Test
    public void compareStandardVsDPIAwareCapture() throws IOException {
        System.out.println("\n=== Standard vs DPI-Aware Capture Comparison ===\n");
        
        Region region = new Region(100, 100, 300, 200);
        
        // Capture using standard method
        System.out.println("Capturing with standard method...");
        BufferedImage standardCapture = BufferedImageUtilities.getBufferedImageFromScreen(region);
        
        // Capture using DPI-aware method
        System.out.println("Capturing with DPI-aware method...");
        BufferedImage dpiAwareCapture = this.dpiAwareCapture.captureDPIAware(
            region.x(), region.y(), region.w(), region.h()
        );
        
        System.out.println("\nComparison:");
        System.out.println("Standard capture: " + 
            standardCapture.getWidth() + "x" + standardCapture.getHeight());
        System.out.println("DPI-aware capture: " + 
            dpiAwareCapture.getWidth() + "x" + dpiAwareCapture.getHeight());
        
        // Save both for comparison
        File outputDir = new File("diagnostics/dpi-comparison");
        outputDir.mkdirs();
        
        ImageIO.write(standardCapture, "png", new File(outputDir, "standard-capture.png"));
        ImageIO.write(dpiAwareCapture, "png", new File(outputDir, "dpi-aware-capture.png"));
        
        System.out.println("\nImages saved to diagnostics/dpi-comparison/");
        
        // Check if dimensions differ (indicating scaling was applied)
        if (standardCapture.getWidth() != dpiAwareCapture.getWidth() ||
            standardCapture.getHeight() != dpiAwareCapture.getHeight()) {
            System.out.println("\n⚠ WARNING: Capture dimensions differ!");
            System.out.println("This indicates display scaling is affecting captures.");
            System.out.println("DPI-aware capture should be used for consistent pattern matching.");
        } else {
            System.out.println("\n✓ Capture dimensions match");
        }
        
        System.out.println("\n=== Test Complete ===\n");
    }
}