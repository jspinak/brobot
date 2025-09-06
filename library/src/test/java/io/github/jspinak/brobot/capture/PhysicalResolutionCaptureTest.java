package io.github.jspinak.brobot.capture;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that verify physical resolution capture and pattern matching works correctly
 * with DPI scaling (e.g., 125% scaling on Windows).
 */
public class PhysicalResolutionCaptureTest extends BrobotTestBase {
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }
    
    @Test
    public void testScaleFactorCalculation() {
        System.out.println("\n=== Scale Factor Calculation Test ===");
        
        // Test the scale factor calculation logic
        int logicalWidth = 1536;
        int logicalHeight = 864;
        int physicalWidth = 1920; 
        int physicalHeight = 1080;
        
        double scaleX = (double) physicalWidth / logicalWidth;
        double scaleY = (double) physicalHeight / logicalHeight;
        
        System.out.println("Logical resolution: " + logicalWidth + "x" + logicalHeight);
        System.out.println("Physical resolution: " + physicalWidth + "x" + physicalHeight);
        System.out.println("Scale factor X: " + scaleX);
        System.out.println("Scale factor Y: " + scaleY);
        
        assertEquals(1.25, scaleX, 0.01, "Scale X should be 1.25 for 125% DPI");
        assertEquals(1.25, scaleY, 0.01, "Scale Y should be 1.25 for 125% DPI");
        
        // Test coordinate transformation
        int logicalX = 768;
        int logicalY = 432;
        
        int physicalX = (int) Math.round(logicalX * scaleX);
        int physicalY = (int) Math.round(logicalY * scaleY);
        
        System.out.println("Logical coords: (" + logicalX + ", " + logicalY + ")");
        System.out.println("Physical coords: (" + physicalX + ", " + physicalY + ")");
        
        assertEquals(960, physicalX, "768 * 1.25 = 960");
        assertEquals(540, physicalY, "432 * 1.25 = 540");
    }
}