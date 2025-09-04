package io.github.jspinak.brobot.tools.diagnostics;

import io.github.jspinak.brobot.config.dpi.DPIAwarenessDisabler;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Diagnostic tool to verify that DPI awareness disabling is working correctly.
 * 
 * <p>This tool captures screens and reports the resolution to help verify
 * that captures are occurring at physical resolution (e.g., 1920x1080)
 * rather than logical resolution (e.g., 1536x864).</p>
 * 
 * @since 1.1.0
 */
public class DPIVerificationTool {
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("BROBOT DPI VERIFICATION TOOL");
        System.out.println("=".repeat(80));
        
        // Report Java version
        System.out.println("\n1. JAVA ENVIRONMENT:");
        System.out.println("   Java Version: " + System.getProperty("java.version"));
        System.out.println("   Java Vendor: " + System.getProperty("java.vendor"));
        
        // Report DPI awareness status
        System.out.println("\n2. DPI AWARENESS STATUS:");
        System.out.println("   " + DPIAwarenessDisabler.getDPIStatus());
        System.out.println("   sun.java2d.dpiaware: " + System.getProperty("sun.java2d.dpiaware"));
        System.out.println("   sun.java2d.uiScale: " + System.getProperty("sun.java2d.uiScale"));
        System.out.println("   sun.java2d.win.uiScale: " + System.getProperty("sun.java2d.win.uiScale"));
        
        // Get display information
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        
        System.out.println("\n3. DISPLAY INFORMATION:");
        System.out.println("   Display Mode: " + dm.getWidth() + "x" + dm.getHeight());
        System.out.println("   Refresh Rate: " + dm.getRefreshRate() + " Hz");
        System.out.println("   Bit Depth: " + dm.getBitDepth());
        
        // Get transform scale
        double scaleX = gc.getDefaultTransform().getScaleX();
        double scaleY = gc.getDefaultTransform().getScaleY();
        System.out.println("   Transform Scale: " + scaleX + "x" + scaleY);
        
        if (scaleX > 1.0) {
            System.out.println("   → Display has " + (int)(scaleX * 100) + "% DPI scaling");
        }
        
        // Test AWT Robot capture
        System.out.println("\n4. AWT ROBOT CAPTURE:");
        Robot robot = new Robot();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("   Toolkit Screen Size: " + screenSize.width + "x" + screenSize.height);
        
        BufferedImage robotCapture = robot.createScreenCapture(
            new Rectangle(0, 0, screenSize.width, screenSize.height));
        System.out.println("   Captured Size: " + robotCapture.getWidth() + "x" + robotCapture.getHeight());
        
        // Test SikuliX capture
        System.out.println("\n5. SIKULIX CAPTURE:");
        Screen screen = new Screen();
        Rectangle bounds = screen.getBounds();
        System.out.println("   Screen Bounds: " + bounds.width + "x" + bounds.height);
        
        ScreenImage screenImage = screen.capture();
        BufferedImage sikuliCapture = screenImage.getImage();
        System.out.println("   Captured Size: " + sikuliCapture.getWidth() + "x" + sikuliCapture.getHeight());
        
        // Analysis
        System.out.println("\n6. ANALYSIS:");
        System.out.println("-".repeat(70));
        
        boolean capturesMatch = (robotCapture.getWidth() == sikuliCapture.getWidth()) &&
                               (robotCapture.getHeight() == sikuliCapture.getHeight());
        
        if (capturesMatch) {
            System.out.println("   ✓ Robot and SikuliX captures match: " + 
                             sikuliCapture.getWidth() + "x" + sikuliCapture.getHeight());
        } else {
            System.out.println("   ⚠ Captures don't match!");
            System.out.println("     Robot: " + robotCapture.getWidth() + "x" + robotCapture.getHeight());
            System.out.println("     SikuliX: " + sikuliCapture.getWidth() + "x" + sikuliCapture.getHeight());
        }
        
        // Determine if we're capturing at physical or logical resolution
        if (scaleX > 1.0) {
            // System has DPI scaling
            int expectedPhysicalWidth = dm.getWidth();
            int expectedLogicalWidth = (int)(dm.getWidth() / scaleX);
            
            if (Math.abs(sikuliCapture.getWidth() - expectedPhysicalWidth) < 10) {
                System.out.println("\n   ✓ SUCCESS: Capturing at PHYSICAL resolution!");
                System.out.println("   This is the desired behavior for SikuliX IDE compatibility.");
                System.out.println("   Patterns captured in SikuliX IDE will match directly.");
            } else if (Math.abs(sikuliCapture.getWidth() - expectedLogicalWidth) < 10) {
                System.out.println("\n   ⚠ WARNING: Capturing at LOGICAL resolution!");
                System.out.println("   DPI awareness may not be properly disabled.");
                System.out.println("   Patterns from SikuliX IDE may not match well.");
                System.out.println("\n   To fix this:");
                System.out.println("   1. Ensure brobot.dpi.disable=true in application.properties");
                System.out.println("   2. Or set environment variable: BROBOT_DISABLE_DPI=true");
                System.out.println("   3. Or use JVM argument: -Dbrobot.dpi.disable=true");
            } else {
                System.out.println("\n   ❓ Unexpected capture resolution");
                System.out.println("   Expected physical: " + expectedPhysicalWidth);
                System.out.println("   Expected logical: " + expectedLogicalWidth);
                System.out.println("   Actual: " + sikuliCapture.getWidth());
            }
        } else {
            System.out.println("\n   ✓ No DPI scaling detected");
            System.out.println("   Captures are at native resolution: " + 
                             sikuliCapture.getWidth() + "x" + sikuliCapture.getHeight());
        }
        
        // Save test captures
        System.out.println("\n7. SAVING TEST CAPTURES:");
        File debugDir = new File("dpi_verification");
        if (!debugDir.exists()) {
            debugDir.mkdirs();
        }
        
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        
        File robotFile = new File(debugDir, "robot_" + timestamp + ".png");
        ImageIO.write(robotCapture, "png", robotFile);
        System.out.println("   Saved Robot capture: " + robotFile.getName());
        
        File sikuliFile = new File(debugDir, "sikuli_" + timestamp + ".png");
        ImageIO.write(sikuliCapture, "png", sikuliFile);
        System.out.println("   Saved SikuliX capture: " + sikuliFile.getName());
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("VERIFICATION COMPLETE");
        System.out.println("=".repeat(80));
    }
}