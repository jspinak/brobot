package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.debug.DebugTestBase;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.core.FrameworkSettings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Investigates why screenshots are blurry compared to SikuliX IDE patterns.
 * Tests different capture methods and DPI/scaling behavior.
 */
@DisabledInCI
public class ScreenCaptureInvestigationTest extends DebugTestBase {
    
    @Test
    public void investigateScreenCapture() throws Exception {
        System.out.println("=== SCREEN CAPTURE INVESTIGATION ===\n");
        
        // 1. Check Java version
        System.out.println("1. JAVA VERSION:");
        System.out.println("   Java version: " + System.getProperty("java.version"));
        System.out.println("   Java vendor: " + System.getProperty("java.vendor"));
        System.out.println("   Java home: " + System.getProperty("java.home"));
        
        // 2. Check display configuration
        System.out.println("\n2. DISPLAY CONFIGURATION:");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        System.out.println("   Display mode: " + dm.getWidth() + "x" + dm.getHeight());
        System.out.println("   Refresh rate: " + dm.getRefreshRate() + " Hz");
        System.out.println("   Bit depth: " + dm.getBitDepth() + " bits");
        
        // 3. Check DPI scaling
        System.out.println("\n3. DPI SCALING:");
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        AffineTransform transform = gc.getDefaultTransform();
        System.out.println("   Scale X: " + transform.getScaleX());
        System.out.println("   Scale Y: " + transform.getScaleY());
        
        Rectangle bounds = gc.getBounds();
        System.out.println("   Graphics config bounds: " + bounds.width + "x" + bounds.height);
        
        // 4. Check Toolkit screen size
        System.out.println("\n4. TOOLKIT SCREEN SIZE:");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("   Toolkit size: " + screenSize.width + "x" + screenSize.height);
        
        // 5. Test Robot capture
        System.out.println("\n5. ROBOT CAPTURE TEST:");
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(screenSize);
        BufferedImage robotCapture = robot.createScreenCapture(screenRect);
        System.out.println("   Robot captured: " + robotCapture.getWidth() + "x" + robotCapture.getHeight());
        System.out.println("   Image type: " + getImageType(robotCapture.getType()));
        saveImage(robotCapture, "robot-capture.png");
        
        // 6. Test Robot with GraphicsDevice
        System.out.println("\n6. ROBOT WITH GRAPHICS DEVICE:");
        Robot gdRobot = new Robot(gd);
        BufferedImage gdCapture = gdRobot.createScreenCapture(bounds);
        System.out.println("   GD Robot captured: " + gdCapture.getWidth() + "x" + gdCapture.getHeight());
        System.out.println("   Image type: " + getImageType(gdCapture.getType()));
        saveImage(gdCapture, "gd-robot-capture.png");
        
        // 7. Test SikuliX Screen capture
        System.out.println("\n8. SIKULIX SCREEN CAPTURE:");
        Screen screen = new Screen();
        ScreenImage screenImage = screen.capture();
        BufferedImage screenCapture = screenImage.getImage();
        System.out.println("   Screen captured: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());
        System.out.println("   Image type: " + getImageType(screenCapture.getType()));
        saveImage(screenCapture, "screen-capture.png");
        
        // 9. Compare capture methods
        System.out.println("\n9. CAPTURE METHOD COMPARISON:");
        System.out.println("   Robot:        " + robotCapture.getWidth() + "x" + robotCapture.getHeight());
        System.out.println("   GD Robot:     " + gdCapture.getWidth() + "x" + gdCapture.getHeight());
        System.out.println("   Screen:       " + screenCapture.getWidth() + "x" + screenCapture.getHeight());
        
        // 10. Check for scaling differences
        System.out.println("\n10. SCALING ANALYSIS:");
        double widthRatio = (double) dm.getWidth() / screenCapture.getWidth();
        double heightRatio = (double) dm.getHeight() / screenCapture.getHeight();
        System.out.println("   Display/Capture width ratio: " + widthRatio);
        System.out.println("   Display/Capture height ratio: " + heightRatio);
        
        if (Math.abs(widthRatio - 1.25) < 0.01) {
            System.out.println("   → Detected 125% Windows scaling!");
        } else if (Math.abs(widthRatio - 1.5) < 0.01) {
            System.out.println("   → Detected 150% Windows scaling!");
        } else if (Math.abs(widthRatio - 1.0) < 0.01) {
            System.out.println("   → No scaling detected (100%)");
        } else {
            System.out.println("   → Custom scaling factor: " + (widthRatio * 100) + "%");
        }
        
        // 11. Test high-DPI awareness
        System.out.println("\n11. HIGH-DPI AWARENESS:");
        System.out.println("   sun.java2d.dpiaware: " + System.getProperty("sun.java2d.dpiaware"));
        System.out.println("   sun.java2d.uiScale: " + System.getProperty("sun.java2d.uiScale"));
        System.out.println("   sun.java2d.win.uiScale: " + System.getProperty("sun.java2d.win.uiScale"));
        
        System.out.println("\n12. IMAGES SAVED:");
        System.out.println("   Check these files for sharpness comparison:");
        System.out.println("   - robot-capture.png");
        System.out.println("   - gd-robot-capture.png");
        System.out.println("   - screen-capture.png");
        
        System.out.println("\n=== INVESTIGATION COMPLETE ===");
    }
    
    private void saveImage(BufferedImage image, String filename) {
        try {
            File file = new File(filename);
            ImageIO.write(image, "png", file);
            System.out.println("   Saved: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("   Failed to save: " + e.getMessage());
        }
    }
    
    private String getImageType(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: return "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB: return "TYPE_INT_ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "TYPE_INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR: return "TYPE_INT_BGR";
            case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR: return "TYPE_4BYTE_ABGR";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "TYPE_4BYTE_ABGR_PRE";
            default: return "Type " + type;
        }
    }
}