package io.github.jspinak.brobot.util.image.debug;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.Finder;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Comprehensive debugging tool for screen capture issues.
 * Compares different capture methods and analyzes differences.
 */
@Slf4j
@Component
public class CaptureDebugger {
    
    private static final String DEBUG_DIR = "debug-captures";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    
    public CaptureDebugger() {
        // Create debug directory
        File dir = new File(DEBUG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Comprehensive debug capture that tests all methods and saves results.
     */
    public void debugCapture(Region region, String patternPath) {
        String timestamp = dateFormat.format(new Date());
        ConsoleReporter.println("\n" + "=".repeat(80));
        ConsoleReporter.println("CAPTURE DEBUG SESSION: " + timestamp);
        ConsoleReporter.println("=".repeat(80));
        
        // 1. Test different Screen creation methods
        ConsoleReporter.println("\n1. TESTING SCREEN CREATION METHODS:");
        ConsoleReporter.println("-".repeat(40));
        
        BufferedImage capture1 = captureWithNewScreen(region, timestamp + "_new-screen");
        BufferedImage capture2 = captureWithDefaultScreen(region, timestamp + "_default-screen");
        BufferedImage capture3 = captureWithRobot(region, timestamp + "_robot");
        BufferedImage capture4 = captureWithScreenshot(timestamp + "_screenshot");
        
        // 2. Compare captures
        ConsoleReporter.println("\n2. COMPARING CAPTURES:");
        ConsoleReporter.println("-".repeat(40));
        
        compareImages(capture1, capture2, "New Screen vs Default Screen");
        compareImages(capture1, capture3, "New Screen vs SikuliX Region");
        compareImages(capture2, capture3, "Default Screen vs SikuliX Region");
        
        // 3. Test pattern matching with each capture
        if (patternPath != null && new File(patternPath).exists()) {
            ConsoleReporter.println("\n3. PATTERN MATCHING TESTS:");
            ConsoleReporter.println("-".repeat(40));
            ConsoleReporter.println("Pattern: " + patternPath);
            
            try {
                Pattern pattern = new Pattern(patternPath);
                BufferedImage patternImg = pattern.getBImage();
                saveImage(patternImg, timestamp + "_pattern");
                
                testPatternMatch(capture1, pattern, "New Screen");
                testPatternMatch(capture2, pattern, "Default Screen");
                testPatternMatch(capture3, pattern, "SikuliX Region");
                
                // Analyze pattern
                analyzeImage(patternImg, "PATTERN");
                
            } catch (Exception e) {
                ConsoleReporter.println("ERROR loading pattern: " + e.getMessage());
            }
        }
        
        // 4. System information
        ConsoleReporter.println("\n4. SYSTEM INFORMATION:");
        ConsoleReporter.println("-".repeat(40));
        printSystemInfo();
        
        ConsoleReporter.println("\n" + "=".repeat(80));
        ConsoleReporter.println("Debug images saved to: " + new File(DEBUG_DIR).getAbsolutePath());
        ConsoleReporter.println("=".repeat(80) + "\n");
    }
    
    private BufferedImage captureWithNewScreen(Region region, String filename) {
        try {
            ConsoleReporter.println("\nMethod: new Screen()");
            Screen screen = new Screen();
            ConsoleReporter.println("  Screen ID: " + screen.getID());
            ConsoleReporter.println("  Screen bounds: " + screen.getBounds());
            
            BufferedImage captured = screen.capture(region.sikuli()).getImage();
            ConsoleReporter.println("  Captured: " + captured.getWidth() + "x" + captured.getHeight());
            
            saveImage(captured, filename);
            analyzeImage(captured, "NEW SCREEN");
            return captured;
            
        } catch (Exception e) {
            ConsoleReporter.println("  ERROR: " + e.getMessage());
            return null;
        }
    }
    
    private BufferedImage captureWithDefaultScreen(Region region, String filename) {
        try {
            ConsoleReporter.println("\nMethod: Screen.getPrimaryScreen() or Screen(0)");
            Screen screen = new Screen(0);
            ConsoleReporter.println("  Screen ID: " + screen.getID());
            ConsoleReporter.println("  Screen bounds: " + screen.getBounds());
            
            BufferedImage captured = screen.capture(region.sikuli()).getImage();
            ConsoleReporter.println("  Captured: " + captured.getWidth() + "x" + captured.getHeight());
            
            saveImage(captured, filename);
            analyzeImage(captured, "DEFAULT SCREEN");
            return captured;
            
        } catch (Exception e) {
            ConsoleReporter.println("  ERROR: " + e.getMessage());
            return null;
        }
    }
    
    private BufferedImage captureWithRobot(Region region, String filename) {
        try {
            ConsoleReporter.println("\nMethod: SikuliX Screen with region");
            Screen screen = new Screen();
            
            org.sikuli.script.Region sikuliRegion = new org.sikuli.script.Region(region.x(), region.y(), region.w(), region.h());
            BufferedImage captured = screen.capture(sikuliRegion).getImage();
            ConsoleReporter.println("  Captured: " + captured.getWidth() + "x" + captured.getHeight());
            
            saveImage(captured, filename);
            analyzeImage(captured, "SIKULI_REGION");
            return captured;
            
        } catch (Exception e) {
            ConsoleReporter.println("  ERROR: " + e.getMessage());
            return null;
        }
    }
    
    private BufferedImage captureWithScreenshot(String filename) {
        try {
            ConsoleReporter.println("\nMethod: Full Screenshot");
            Screen screen = new Screen();
            BufferedImage captured = screen.capture().getImage();
            ConsoleReporter.println("  Captured: " + captured.getWidth() + "x" + captured.getHeight());
            
            saveImage(captured, filename);
            return captured;
            
        } catch (Exception e) {
            ConsoleReporter.println("  ERROR: " + e.getMessage());
            return null;
        }
    }
    
    private void compareImages(BufferedImage img1, BufferedImage img2, String comparison) {
        if (img1 == null || img2 == null) {
            ConsoleReporter.println("\n" + comparison + ": Cannot compare (null image)");
            return;
        }
        
        ConsoleReporter.println("\n" + comparison + ":");
        ConsoleReporter.println("  Size: " + img1.getWidth() + "x" + img1.getHeight() + 
                                " vs " + img2.getWidth() + "x" + img2.getHeight());
        
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            ConsoleReporter.println("  WARNING: Different sizes!");
            return;
        }
        
        // Sample pixel comparison
        int differences = 0;
        int samples = 100;
        for (int i = 0; i < samples; i++) {
            int x = (img1.getWidth() * i) / samples;
            int y = img1.getHeight() / 2;
            
            if (x < img1.getWidth() && y < img1.getHeight()) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                
                if (rgb1 != rgb2) {
                    differences++;
                    if (differences <= 3) { // Show first 3 differences
                        ConsoleReporter.println(String.format("  Pixel diff at (%d,%d): #%06X vs #%06X",
                            x, y, rgb1 & 0xFFFFFF, rgb2 & 0xFFFFFF));
                    }
                }
            }
        }
        
        ConsoleReporter.println("  Pixel differences: " + differences + "/" + samples);
    }
    
    private void testPatternMatch(BufferedImage scene, Pattern pattern, String method) {
        if (scene == null) {
            ConsoleReporter.println("\n" + method + " match: Cannot test (null scene)");
            return;
        }
        
        try {
            Finder finder = new Finder(scene);
            finder.findAll(pattern);
            
            int count = 0;
            double bestScore = 0;
            while (finder.hasNext()) {
                org.sikuli.script.Match match = finder.next();
                count++;
                if (match.getScore() > bestScore) {
                    bestScore = match.getScore();
                }
            }
            
            ConsoleReporter.println("\n" + method + " match: " + count + " matches, best score: " + 
                String.format("%.3f", bestScore));
            
            finder.destroy();
            
        } catch (Exception e) {
            ConsoleReporter.println("\n" + method + " match: ERROR - " + e.getMessage());
        }
    }
    
    private void analyzeImage(BufferedImage img, String label) {
        if (img == null) return;
        
        ConsoleReporter.println("\nImage Analysis - " + label + ":");
        ConsoleReporter.println("  Dimensions: " + img.getWidth() + "x" + img.getHeight());
        ConsoleReporter.println("  Type: " + getImageType(img.getType()));
        ConsoleReporter.println("  Color Model: " + img.getColorModel().getClass().getSimpleName());
        ConsoleReporter.println("  Has Alpha: " + img.getColorModel().hasAlpha());
        
        // Analyze content
        int blackCount = 0, whiteCount = 0;
        long totalR = 0, totalG = 0, totalB = 0;
        int sampleSize = Math.min(1000, img.getWidth() * img.getHeight());
        
        for (int i = 0; i < sampleSize; i++) {
            int x = (i * 7) % img.getWidth();
            int y = ((i * 13) / img.getWidth()) % img.getHeight();
            int rgb = img.getRGB(x, y);
            
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            
            totalR += r;
            totalG += g;
            totalB += b;
            
            if (r < 10 && g < 10 && b < 10) blackCount++;
            if (r > 245 && g > 245 && b > 245) whiteCount++;
        }
        
        ConsoleReporter.println(String.format("  Content: %.1f%% black, %.1f%% white",
            (blackCount * 100.0) / sampleSize, (whiteCount * 100.0) / sampleSize));
        ConsoleReporter.println(String.format("  Avg RGB: (%d, %d, %d)",
            (int)(totalR / sampleSize), (int)(totalG / sampleSize), (int)(totalB / sampleSize)));
        
        // Check edges for scaling artifacts
        checkEdges(img);
    }
    
    private void checkEdges(BufferedImage img) {
        // Check if edges have interpolation artifacts (sign of scaling)
        int edgeVariance = 0;
        int checks = 10;
        
        for (int i = 0; i < checks; i++) {
            int x = (img.getWidth() * i) / checks;
            
            // Check top edge
            if (img.getHeight() > 1) {
                int rgb1 = img.getRGB(x, 0);
                int rgb2 = img.getRGB(x, 1);
                if (Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF)) > 50) {
                    edgeVariance++;
                }
            }
        }
        
        if (edgeVariance > checks / 2) {
            ConsoleReporter.println("  WARNING: High edge variance - possible scaling artifacts!");
        }
    }
    
    private void printSystemInfo() {
        // Display information
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        
        ConsoleReporter.println("Display Mode: " + dm.getWidth() + "x" + dm.getHeight() + 
            " @ " + dm.getRefreshRate() + "Hz, " + dm.getBitDepth() + "-bit");
        
        // Toolkit screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        ConsoleReporter.println("Toolkit Screen Size: " + screenSize.width + "x" + screenSize.height);
        
        // Screen resolution (DPI)
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        ConsoleReporter.println("Screen DPI: " + dpi);
        
        // Graphics configuration
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        AffineTransform transform = gc.getDefaultTransform();
        ConsoleReporter.println("Graphics Transform: scaleX=" + transform.getScaleX() + 
            ", scaleY=" + transform.getScaleY());
        
        // Java properties
        ConsoleReporter.println("\nJava Properties:");
        ConsoleReporter.println("  java.awt.headless: " + System.getProperty("java.awt.headless"));
        ConsoleReporter.println("  sun.java2d.dpiaware: " + System.getProperty("sun.java2d.dpiaware"));
        ConsoleReporter.println("  sun.java2d.uiScale: " + System.getProperty("sun.java2d.uiScale"));
        ConsoleReporter.println("  sun.java2d.win.uiScale: " + System.getProperty("sun.java2d.win.uiScale"));
        
        // SikuliX Settings
        ConsoleReporter.println("\nSikuliX Settings:");
        ConsoleReporter.println("  MinSimilarity: " + org.sikuli.basics.Settings.MinSimilarity);
        ConsoleReporter.println("  AutoWaitTimeout: " + org.sikuli.basics.Settings.AutoWaitTimeout);
        
        // OS info
        ConsoleReporter.println("\nOS Information:");
        ConsoleReporter.println("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        ConsoleReporter.println("  Architecture: " + System.getProperty("os.arch"));
    }
    
    private void saveImage(BufferedImage img, String filename) {
        if (img == null) return;
        
        try {
            File file = new File(DEBUG_DIR, filename + ".png");
            ImageIO.write(img, "png", file);
            ConsoleReporter.println("  Saved: " + file.getName());
        } catch (IOException e) {
            ConsoleReporter.println("  Failed to save: " + e.getMessage());
        }
    }
    
    private String getImageType(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB: return "RGB";
            case BufferedImage.TYPE_INT_ARGB: return "ARGB";
            case BufferedImage.TYPE_3BYTE_BGR: return "BGR";
            case BufferedImage.TYPE_BYTE_GRAY: return "GRAY";
            default: return "Type" + type;
        }
    }
}