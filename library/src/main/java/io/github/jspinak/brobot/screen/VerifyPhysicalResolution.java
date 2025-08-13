package io.github.jspinak.brobot.screen;

import io.github.jspinak.brobot.startup.BrobotStartup;
import org.sikuli.basics.Settings;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility to verify that physical resolution capture is working correctly.
 * Run this to check if Brobot is capturing at the same resolution as SikuliX IDE.
 */
public class VerifyPhysicalResolution {
    
    public static void main(String[] args) throws IOException {
        System.out.println("\n========================================");
        System.out.println("  Brobot Physical Resolution Verifier");
        System.out.println("========================================\n");
        
        // Initialize Brobot startup
        new BrobotStartup();
        
        // Check system information
        System.out.println("System Information:");
        System.out.println("  OS: " + System.getProperty("os.name"));
        System.out.println("  Java Version: " + System.getProperty("java.version"));
        System.out.println("  Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println();
        
        // Check DPI settings
        System.out.println("DPI Settings:");
        System.out.println("  sun.java2d.dpiaware: " + System.getProperty("sun.java2d.dpiaware"));
        System.out.println("  sun.java2d.uiScale: " + System.getProperty("sun.java2d.uiScale"));
        System.out.println();
        
        // Get resolution information
        GraphicsDevice device = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice();
        
        DisplayMode mode = device.getDisplayMode();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        int physicalWidth = mode.getWidth();
        int physicalHeight = mode.getHeight();
        int toolkitWidth = (int) screenSize.getWidth();
        int toolkitHeight = (int) screenSize.getHeight();
        
        System.out.println("Resolution Information:");
        System.out.println("  Physical Resolution: " + physicalWidth + "x" + physicalHeight);
        System.out.println("  Toolkit Resolution:  " + toolkitWidth + "x" + toolkitHeight);
        
        if (physicalWidth != toolkitWidth || physicalHeight != toolkitHeight) {
            float scale = (float) physicalWidth / toolkitWidth;
            System.out.println("  DPI Scaling Factor: " + scale + " (" + (int)(scale * 100) + "%)");
        } else {
            System.out.println("  DPI Scaling: None detected");
        }
        System.out.println();
        
        // Test standard Screen capture
        System.out.println("Testing Standard Screen Capture:");
        Screen standardScreen = new Screen();
        ScreenImage standardCapture = standardScreen.capture();
        BufferedImage standardImage = standardCapture.getImage();
        System.out.println("  Standard capture size: " + standardImage.getWidth() + "x" + standardImage.getHeight());
        
        // Test PhysicalScreen capture
        System.out.println("\nTesting Physical Screen Capture:");
        PhysicalScreen physicalScreen = new PhysicalScreen();
        ScreenImage physicalCapture = physicalScreen.capture();
        BufferedImage physicalImage = physicalCapture.getImage();
        System.out.println("  Physical capture size: " + physicalImage.getWidth() + "x" + physicalImage.getHeight());
        
        // Check Settings.AlwaysResize
        System.out.println("\nSikuliX Settings:");
        System.out.println("  Settings.AlwaysResize: " + Settings.AlwaysResize);
        
        // Save screenshots for comparison
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File outputDir = new File("brobot-screenshots");
        outputDir.mkdirs();
        
        File standardFile = new File(outputDir, "standard_" + timestamp + ".png");
        File physicalFile = new File(outputDir, "physical_" + timestamp + ".png");
        
        ImageIO.write(standardImage, "PNG", standardFile);
        ImageIO.write(physicalImage, "PNG", physicalFile);
        
        System.out.println("\nScreenshots saved:");
        System.out.println("  Standard: " + standardFile.getAbsolutePath());
        System.out.println("  Physical: " + physicalFile.getAbsolutePath());
        
        // Analysis and recommendation
        System.out.println("\n========================================");
        System.out.println("  Analysis & Recommendations");
        System.out.println("========================================\n");
        
        if (physicalImage.getWidth() == physicalWidth && physicalImage.getHeight() == physicalHeight) {
            System.out.println("✓ SUCCESS: Physical resolution capture is working!");
            System.out.println("  Brobot is capturing at " + physicalWidth + "x" + physicalHeight);
            System.out.println("  This matches the SikuliX IDE behavior.");
            System.out.println("  Pattern matching should achieve ~0.99 similarity.");
        } else if (physicalImage.getWidth() == toolkitWidth && physicalImage.getHeight() == toolkitHeight) {
            System.out.println("⚠ WARNING: Capturing at logical resolution");
            System.out.println("  Current: " + toolkitWidth + "x" + toolkitHeight);
            System.out.println("  Expected: " + physicalWidth + "x" + physicalHeight);
            System.out.println("\n  To fix, ensure you run with:");
            System.out.println("  java -Dsun.java2d.dpiaware=false -jar your-app.jar");
        } else {
            System.out.println("❌ ERROR: Unexpected capture resolution");
            System.out.println("  Captured: " + physicalImage.getWidth() + "x" + physicalImage.getHeight());
            System.out.println("  Physical: " + physicalWidth + "x" + physicalHeight);
            System.out.println("  Logical: " + toolkitWidth + "x" + toolkitHeight);
        }
        
        // Pattern matching recommendation
        System.out.println("\nPattern Matching Configuration:");
        if (Math.abs(Settings.AlwaysResize) < 0.01) {
            System.out.println("✓ Settings.AlwaysResize = 0 (correct for physical capture)");
        } else {
            System.out.println("⚠ Settings.AlwaysResize = " + Settings.AlwaysResize);
            System.out.println("  Consider setting to 0 since we're using physical capture");
        }
        
        System.out.println("\n========================================\n");
    }
}