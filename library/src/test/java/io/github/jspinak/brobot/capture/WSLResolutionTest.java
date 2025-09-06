package io.github.jspinak.brobot.capture;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for resolution handling in WSL and Windows environments.
 * This test simulates the DPI scaling issues encountered on Windows with 125% scaling.
 */
public class WSLResolutionTest extends BrobotTestBase {
    
    @Test
    @EnabledOnOs({OS.LINUX, OS.WINDOWS})
    public void testResolutionDetection() {
        // Get screen dimensions
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        
        System.out.println("=== Resolution Detection Test ===");
        System.out.println("Operating System: " + System.getProperty("os.name"));
        System.out.println("Logical Screen Size: " + screenSize.width + "x" + screenSize.height);
        
        // Check if we're in WSL
        boolean isWSL = System.getenv("WSL_DISTRO_NAME") != null;
        System.out.println("Running in WSL: " + isWSL);
        
        if (isWSL) {
            System.out.println("WSL Distribution: " + System.getenv("WSL_DISTRO_NAME"));
        }
        
        // In WSL, we expect logical and physical to be the same (no DPI scaling)
        // On Windows with DPI scaling, they would differ
        assertNotNull(screenSize);
        assertTrue(screenSize.width > 0);
        assertTrue(screenSize.height > 0);
    }
    
    @Test
    public void testCoordinateScaling() {
        // Simulate a scenario with 125% DPI scaling
        int logicalWidth = 1536;
        int logicalHeight = 864;
        int physicalWidth = 1920;
        int physicalHeight = 1080;
        
        double scaleX = (double) physicalWidth / logicalWidth;
        double scaleY = (double) physicalHeight / logicalHeight;
        
        System.out.println("=== Coordinate Scaling Test ===");
        System.out.println("Scale Factor X: " + scaleX + " (expected: 1.25)");
        System.out.println("Scale Factor Y: " + scaleY + " (expected: 1.25)");
        
        // Test region scaling
        int regionX = 0;
        int regionY = 432;
        int regionW = 768;
        int regionH = 432;
        
        // Scale to physical coordinates
        int physicalX = (int) Math.round(regionX * scaleX);
        int physicalY = (int) Math.round(regionY * scaleY);
        int physicalW = (int) Math.round(regionW * scaleX);
        int physicalH = (int) Math.round(regionH * scaleY);
        
        System.out.println("Logical Region: [" + regionX + "," + regionY + " " + regionW + "x" + regionH + "]");
        System.out.println("Physical Region: [" + physicalX + "," + physicalY + " " + physicalW + "x" + physicalH + "]");
        
        // Verify scaling
        assertEquals(0, physicalX);
        assertEquals(540, physicalY);  // 432 * 1.25 = 540
        assertEquals(960, physicalW);  // 768 * 1.25 = 960
        assertEquals(540, physicalH);  // 432 * 1.25 = 540
        
        assertEquals(1.25, scaleX, 0.01);
        assertEquals(1.25, scaleY, 0.01);
    }
    
    @Test
    public void testImageCroppingWithScaling() {
        // Create a mock physical resolution image (1920x1080)
        BufferedImage physicalImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        
        // Fill with test pattern
        Graphics2D g = physicalImage.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 1920, 1080);
        g.setColor(Color.RED);
        g.fillRect(960, 540, 100, 100);  // Red square at physical center
        g.dispose();
        
        // Logical region coordinates
        int logicalX = 0;
        int logicalY = 432;
        int logicalW = 768;
        int logicalH = 432;
        
        // Calculate scale
        double scaleX = 1920.0 / 1536.0;  // 1.25
        double scaleY = 1080.0 / 864.0;   // 1.25
        
        // Scale to physical coordinates
        int physicalX = (int) Math.round(logicalX * scaleX);
        int physicalY = (int) Math.round(logicalY * scaleY);
        int physicalW = (int) Math.round(logicalW * scaleX);
        int physicalH = (int) Math.round(logicalH * scaleY);
        
        System.out.println("=== Image Cropping Test ===");
        System.out.println("Physical image size: " + physicalImage.getWidth() + "x" + physicalImage.getHeight());
        System.out.println("Cropping region: [" + physicalX + "," + physicalY + " " + physicalW + "x" + physicalH + "]");
        
        // Crop the image
        BufferedImage cropped = physicalImage.getSubimage(physicalX, physicalY, physicalW, physicalH);
        
        System.out.println("Cropped image size: " + cropped.getWidth() + "x" + cropped.getHeight());
        
        // Verify crop dimensions
        assertEquals(960, cropped.getWidth());   // 768 * 1.25
        assertEquals(540, cropped.getHeight());  // 432 * 1.25
        
        // The red square should be visible in the cropped region
        // It's at (960, 540) in physical coords, which becomes (960, 0) in the cropped image
        // since we cropped starting at y=540
        int redPixel = cropped.getRGB(960 - physicalX, 540 - physicalY);
        assertEquals(Color.RED.getRGB(), redPixel, "Red square should be visible in cropped region");
    }
    
    @Test
    @EnabledOnOs(OS.LINUX)
    public void testWSLEnvironment() {
        // Test WSL-specific behavior
        String wslDistro = System.getenv("WSL_DISTRO_NAME");
        String wslInterop = System.getenv("WSL_INTEROP");
        
        System.out.println("=== WSL Environment Test ===");
        System.out.println("WSL_DISTRO_NAME: " + wslDistro);
        System.out.println("WSL_INTEROP: " + wslInterop);
        
        if (wslDistro != null) {
            System.out.println("Running in WSL distribution: " + wslDistro);
            
            // In WSL, screen capture typically doesn't work without X server
            // But coordinate calculations should still work
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            
            // WSL typically reports a default resolution if no X server
            System.out.println("WSL Screen Size: " + screenSize.width + "x" + screenSize.height);
            
            // Note: In WSL without X server, this might be a default size
            // With X server (like VcXsrv), it would report the actual display size
        }
    }
}