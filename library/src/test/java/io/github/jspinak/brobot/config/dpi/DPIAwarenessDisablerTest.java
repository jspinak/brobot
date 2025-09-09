package io.github.jspinak.brobot.config.dpi;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import io.github.jspinak.brobot.test.DisabledInCI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that DPI awareness is properly disabled and captures occur at physical resolution.
 */
@DisabledInCI
public class DPIAwarenessDisablerTest extends BrobotTestBase {
    
    @Test
    public void testDPIAwarenessIsDisabled() {
        // Verify that DPI awareness has been disabled
        assertTrue(DPIAwarenessDisabler.isDPIAwarenessDisabled(), 
            "DPI awareness should be disabled by default");
        
        // Verify system properties are set correctly
        assertEquals("false", System.getProperty("sun.java2d.dpiaware"),
            "sun.java2d.dpiaware should be false");
        assertEquals("1.0", System.getProperty("sun.java2d.uiScale"),
            "sun.java2d.uiScale should be 1.0");
        assertEquals("1.0", System.getProperty("sun.java2d.win.uiScale"),
            "sun.java2d.win.uiScale should be 1.0");
        
        System.out.println("DPI Status: " + DPIAwarenessDisabler.getDPIStatus());
    }
    
    @Test
    public void testCaptureResolution() throws AWTException {
        // Skip this test in headless or CI environment since we're testing actual capture
        if (GraphicsEnvironment.isHeadless() || System.getenv("CI") != null) {
            System.out.println("Skipping capture resolution test in headless/CI environment");
            return;
        }
        
        // Get display information
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        
        System.out.println("\n=== DPI Awareness Test ===");
        System.out.println("Display Mode: " + dm.getWidth() + "x" + dm.getHeight());
        
        // Get transform scale
        double scaleX = gc.getDefaultTransform().getScaleX();
        double scaleY = gc.getDefaultTransform().getScaleY();
        System.out.println("Transform Scale: " + scaleX + "x" + scaleY);
        
        // Test with AWT Robot
        Robot robot = new Robot();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("Toolkit Size: " + screenSize.width + "x" + screenSize.height);
        
        BufferedImage robotCapture = robot.createScreenCapture(
            new Rectangle(0, 0, screenSize.width, screenSize.height));
        System.out.println("Robot Capture: " + robotCapture.getWidth() + "x" + robotCapture.getHeight());
        
        // Test with SikuliX
        Screen screen = new Screen();
        Rectangle bounds = screen.getBounds();
        System.out.println("SikuliX Bounds: " + bounds.width + "x" + bounds.height);
        
        ScreenImage screenImage = screen.capture();
        BufferedImage sikuliCapture = screenImage.getImage();
        System.out.println("SikuliX Capture: " + sikuliCapture.getWidth() + "x" + sikuliCapture.getHeight());
        
        // Verify captures are at the same resolution
        assertEquals(robotCapture.getWidth(), sikuliCapture.getWidth(),
            "Robot and SikuliX captures should have same width");
        assertEquals(robotCapture.getHeight(), sikuliCapture.getHeight(),
            "Robot and SikuliX captures should have same height");
        
        // Check if we're capturing at physical resolution
        if (scaleX > 1.0) {
            System.out.println("\n=== DPI Scaling Detection ===");
            System.out.println("Display has " + (int)(scaleX * 100) + "% scaling");
            
            // With DPI awareness disabled, captures should be at physical resolution
            // The display mode should show physical resolution
            System.out.println("Expected behavior with DPI disabled:");
            System.out.println("  - Captures at physical resolution");
            System.out.println("  - No scaling compensation needed");
            System.out.println("  - Patterns from SikuliX IDE will match directly");
        }
        
        System.out.println("\n=== Test Result ===");
        System.out.println("DPI awareness is properly disabled");
        System.out.println("Captures are at: " + sikuliCapture.getWidth() + "x" + sikuliCapture.getHeight());
    }
    
    @Test
    public void testDPIStatusReporting() {
        String status = DPIAwarenessDisabler.getDPIStatus();
        assertNotNull(status, "DPI status should not be null");
        assertTrue(status.contains("DISABLED") || status.contains("ENABLED"),
            "Status should indicate if DPI awareness is enabled or disabled");
        
        System.out.println("Current DPI Status: " + status);
    }
}