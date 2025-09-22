package io.github.jspinak.brobot.util.image.debug;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.sikuli.script.Finder;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Region;

import lombok.extern.slf4j.Slf4j;

/**
 * Comprehensive debugging tool for screen capture issues. Compares different capture methods and
 * analyzes differences.
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

    /** Comprehensive debug capture that tests all methods and saves results. */
    public void debugCapture(Region region, String patternPath) {
        String timestamp = dateFormat.format(new Date());

        // 1. Test different Screen creation methods

        BufferedImage capture1 = captureWithNewScreen(region, timestamp + "_new-screen");
        BufferedImage capture2 = captureWithDefaultScreen(region, timestamp + "_default-screen");
        BufferedImage capture3 = captureWithRobot(region, timestamp + "_robot");
        BufferedImage capture4 = captureWithScreenshot(timestamp + "_screenshot");

        // 2. Compare captures

        compareImages(capture1, capture2, "New Screen vs Default Screen");
        compareImages(capture1, capture3, "New Screen vs SikuliX Region");
        compareImages(capture2, capture3, "Default Screen vs SikuliX Region");

        // 3. Test pattern matching with each capture
        if (patternPath != null && new File(patternPath).exists()) {

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
            }
        }

        // 4. System information
        printSystemInfo();
    }

    private BufferedImage captureWithNewScreen(Region region, String filename) {
        try {
            Screen screen = new Screen();

            BufferedImage captured = screen.capture(region.sikuli()).getImage();
            saveImage(captured, filename);
            analyzeImage(captured, "NEW SCREEN");
            return captured;

        } catch (Exception e) {
            return null;
        }
    }

    private BufferedImage captureWithDefaultScreen(Region region, String filename) {
        try {
            Screen screen = new Screen(0);

            BufferedImage captured = screen.capture(region.sikuli()).getImage();
            saveImage(captured, filename);
            analyzeImage(captured, "DEFAULT SCREEN");
            return captured;

        } catch (Exception e) {
            return null;
        }
    }

    private BufferedImage captureWithRobot(Region region, String filename) {
        try {
            Screen screen = new Screen();

            org.sikuli.script.Region sikuliRegion =
                    new org.sikuli.script.Region(region.x(), region.y(), region.w(), region.h());
            BufferedImage captured = screen.capture(sikuliRegion).getImage();
            saveImage(captured, filename);
            analyzeImage(captured, "SIKULI_REGION");
            return captured;

        } catch (Exception e) {
            return null;
        }
    }

    private BufferedImage captureWithScreenshot(String filename) {
        try {
            Screen screen = new Screen();
            BufferedImage captured = screen.capture().getImage();
            saveImage(captured, filename);
            return captured;

        } catch (Exception e) {
            return null;
        }
    }

    private void compareImages(BufferedImage img1, BufferedImage img2, String comparison) {
        if (img1 == null || img2 == null) {
            return;
        }

        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
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
                    }
                }
            }
        }
    }

    private void testPatternMatch(BufferedImage scene, Pattern pattern, String method) {
        if (scene == null) {
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
            finder.destroy();

        } catch (Exception e) {
        }
    }

    private void analyzeImage(BufferedImage img, String label) {
        if (img == null) return;

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

        if (edgeVariance > checks / 2) {}
    }

    private void printSystemInfo() {
        // Display information
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // Check if we're in a headless environment
        if (ge.isHeadlessInstance()) {
            System.out.println("Running in headless mode - screen information not available");
            return;
        }

        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        // Toolkit screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // Screen resolution (DPI)
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();

        // Graphics configuration
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        AffineTransform transform = gc.getDefaultTransform();
        // Java properties
        // SikuliX Settings

        // OS info
    }

    private void saveImage(BufferedImage img, String filename) {
        if (img == null) return;

        try {
            File file = new File(DEBUG_DIR, filename + ".png");
            ImageIO.write(img, "png", file);
        } catch (IOException e) {
        }
    }

    private String getImageType(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB:
                return "RGB";
            case BufferedImage.TYPE_INT_ARGB:
                return "ARGB";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "BGR";
            case BufferedImage.TYPE_BYTE_GRAY:
                return "GRAY";
            default:
                return "Type" + type;
        }
    }
}
