package io.github.jspinak.brobot.tools.diagnostics;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Verifies the actual resolution that SikuliX uses for screen capture
 * and tests how Settings.AlwaysResize affects capture dimensions.
 */
public class ScreenResolutionVerifier {
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SCREEN RESOLUTION VERIFIER");
        System.out.println("=".repeat(80));
        
        // Get display information
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        
        System.out.println("\n1. JAVA DISPLAY INFORMATION:");
        System.out.println("   Display Mode: " + dm.getWidth() + "x" + dm.getHeight());
        System.out.println("   Refresh Rate: " + dm.getRefreshRate() + " Hz");
        System.out.println("   Bit Depth: " + dm.getBitDepth());
        
        // Get transform scale
        double scaleX = gc.getDefaultTransform().getScaleX();
        double scaleY = gc.getDefaultTransform().getScaleY();
        System.out.println("   Transform Scale: " + scaleX + "x" + scaleY);
        
        // Get screen bounds
        Rectangle bounds = gc.getBounds();
        System.out.println("   Screen Bounds: " + bounds);
        
        // Initialize SikuliX
        Screen screen = new Screen();
        
        System.out.println("\n2. SIKULIX SCREEN INFORMATION:");
        System.out.println("   Number of screens: " + Screen.getNumberScreens());
        System.out.println("   Screen ID: " + screen.getID());
        Rectangle sikuliBounds = screen.getBounds();
        System.out.println("   SikuliX Bounds: " + sikuliBounds);
        System.out.println("   SikuliX Size: " + sikuliBounds.width + "x" + sikuliBounds.height);
        
        // Test captures with different AlwaysResize settings
        System.out.println("\n3. TESTING CAPTURES WITH DIFFERENT Settings.AlwaysResize:");
        System.out.println("-".repeat(70));
        
        float[] resizeValues = {0.0f, 1.0f, 0.8f, 0.67f, 1.25f};
        
        for (float resize : resizeValues) {
            Settings.AlwaysResize = resize;
            
            System.out.println("\n   Settings.AlwaysResize = " + resize);
            
            // Capture full screen
            ScreenImage screenImage = screen.capture();
            BufferedImage capture = screenImage.getImage();
            
            System.out.println("   Capture dimensions: " + capture.getWidth() + "x" + capture.getHeight());
            
            // Calculate ratios
            double ratioToDisplay = (double)capture.getWidth() / dm.getWidth();
            double ratioToSikuli = (double)capture.getWidth() / sikuliBounds.width;
            
            System.out.println("   Ratio to display mode: " + String.format("%.3f", ratioToDisplay));
            System.out.println("   Ratio to SikuliX bounds: " + String.format("%.3f", ratioToSikuli));
            
            // Save the capture
            saveCapture(capture, "resize_" + String.format("%.2f", resize).replace(".", "_"));
        }
        
        // Test region capture
        System.out.println("\n4. TESTING REGION CAPTURE:");
        System.out.println("-".repeat(70));
        
        Settings.AlwaysResize = 1.0f; // Reset to no resize
        
        // Define a 200x100 region at center
        int centerX = sikuliBounds.width / 2 - 100;
        int centerY = sikuliBounds.height / 2 - 50;
        Region testRegion = new Region(centerX, centerY, 200, 100);
        
        System.out.println("   Requested region: " + testRegion);
        System.out.println("   Requested size: 200x100");
        
        BufferedImage regionCapture = screen.capture(testRegion).getImage();
        System.out.println("   Captured size: " + regionCapture.getWidth() + "x" + regionCapture.getHeight());
        
        if (regionCapture.getWidth() != 200 || regionCapture.getHeight() != 100) {
            System.out.println("   ⚠️ WARNING: Captured size doesn't match requested!");
            double regionScale = (double)regionCapture.getWidth() / 200;
            System.out.println("   Scale factor: " + String.format("%.3f", regionScale));
        } else {
            System.out.println("   ✓ Captured size matches requested");
        }
        
        saveCapture(regionCapture, "region_200x100");
        
        // Test with AWT Robot for comparison
        System.out.println("\n5. COMPARING WITH AWT ROBOT:");
        System.out.println("-".repeat(70));
        
        try {
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            System.out.println("   AWT Screen Size: " + screenSize.width + "x" + screenSize.height);
            
            BufferedImage robotCapture = robot.createScreenCapture(
                new Rectangle(0, 0, screenSize.width, screenSize.height));
            System.out.println("   Robot capture: " + robotCapture.getWidth() + "x" + robotCapture.getHeight());
            
            saveCapture(robotCapture, "awt_robot");
            
            // Compare with SikuliX capture
            Settings.AlwaysResize = 1.0f;
            BufferedImage sikuliCapture = screen.capture().getImage();
            
            System.out.println("\n   COMPARISON:");
            System.out.println("   SikuliX: " + sikuliCapture.getWidth() + "x" + sikuliCapture.getHeight());
            System.out.println("   AWT Robot: " + robotCapture.getWidth() + "x" + robotCapture.getHeight());
            
            if (sikuliCapture.getWidth() == robotCapture.getWidth()) {
                System.out.println("   ✓ Same resolution - Both use same capture method");
            } else {
                double ratio = (double)sikuliCapture.getWidth() / robotCapture.getWidth();
                System.out.println("   ⚠️ Different resolution! Ratio: " + String.format("%.3f", ratio));
                System.out.println("   This explains pattern matching issues!");
            }
            
        } catch (AWTException e) {
            System.err.println("   Could not create AWT Robot: " + e.getMessage());
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CONCLUSIONS:");
        System.out.println("=".repeat(80));
        
        // Analyze results
        Settings.AlwaysResize = 1.0f;
        BufferedImage testCapture = screen.capture().getImage();
        double captureRatio = (double)testCapture.getWidth() / dm.getWidth();
        
        if (Math.abs(captureRatio - 0.8) < 0.01) {
            System.out.println("\n⚠️ CRITICAL FINDING:");
            System.out.println("SikuliX is capturing at 80% of display resolution!");
            System.out.println("This means:");
            System.out.println("  - Screen captures are at 0.8x scale");
            System.out.println("  - Patterns at 100% size won't match well");
            System.out.println("  - Pre-scaled 80% patterns will match best");
            System.out.println("\nRECOMMENDATION:");
            System.out.println("  Use Settings.AlwaysResize = 1.0 with 80% pre-scaled patterns");
            System.out.println("  OR");
            System.out.println("  Use Settings.AlwaysResize = 1.25 with original patterns");
        } else if (Math.abs(captureRatio - 1.0) < 0.01) {
            System.out.println("\n✓ SikuliX captures at logical resolution");
            System.out.println("Patterns should match without scaling");
        } else {
            System.out.println("\n❓ Unexpected capture ratio: " + String.format("%.3f", captureRatio));
            System.out.println("Additional investigation needed");
        }
        
        System.out.println("\n" + "=".repeat(80));
    }
    
    private static void saveCapture(BufferedImage image, String prefix) {
        try {
            File debugDir = new File("debug_captures");
            if (!debugDir.exists()) debugDir.mkdirs();
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File(debugDir, prefix + "_" + timestamp + ".png");
            ImageIO.write(image, "png", file);
            System.out.println("   Saved: " + file.getName());
        } catch (Exception e) {
            System.err.println("   Failed to save: " + e.getMessage());
        }
    }
}