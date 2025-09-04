package io.github.jspinak.brobot.capture;

import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Intelligent screen capture that handles DPI scaling and application scaling issues.
 * 
 * The core problem: Even if we capture at physical resolution, applications can have
 * their own scaling that we cannot detect. This class provides strategies to handle this.
 * 
 * @since 1.1.0
 */
@Component
public class BrobotScreenCapture {
    
    @Value("${brobot.capture.strategy:ADAPTIVE}")
    private CaptureStrategy strategy;
    
    @Value("${brobot.capture.force-physical:true}")
    private boolean forcePhysical;
    
    @Value("${brobot.capture.expected-width:1920}")
    private int expectedPhysicalWidth;
    
    @Value("${brobot.capture.expected-height:1080}")
    private int expectedPhysicalHeight;
    
    private double detectedScaleFactor = 1.0;
    private boolean scalingDetected = false;
    
    public enum CaptureStrategy {
        /**
         * Always capture at whatever resolution Java provides (DPI-aware in Java 21).
         */
        NATIVE,
        
        /**
         * Force physical resolution using platform-specific methods or scaling.
         */
        PHYSICAL,
        
        /**
         * Detect scaling and adapt automatically.
         */
        ADAPTIVE,
        
        /**
         * Use external screenshot tool (most reliable but slower).
         */
        EXTERNAL
    }
    
    @PostConstruct
    public void init() {
        detectScaling();
        reportConfiguration();
    }
    
    /**
     * Captures the screen using the configured strategy.
     */
    public BufferedImage capture() {
        switch (strategy) {
            case PHYSICAL:
                return capturePhysical();
            case EXTERNAL:
                return captureExternal();
            case ADAPTIVE:
                return captureAdaptive();
            case NATIVE:
            default:
                return captureNative();
        }
    }
    
    /**
     * Captures a specific region using the configured strategy.
     */
    public BufferedImage capture(Rectangle region) {
        BufferedImage fullCapture = capture();
        
        // Adjust region coordinates if scaling is detected
        if (scalingDetected && strategy == CaptureStrategy.PHYSICAL) {
            region = new Rectangle(
                (int)(region.x * detectedScaleFactor),
                (int)(region.y * detectedScaleFactor),
                (int)(region.width * detectedScaleFactor),
                (int)(region.height * detectedScaleFactor)
            );
        }
        
        // Crop to region
        return fullCapture.getSubimage(region.x, region.y, region.width, region.height);
    }
    
    /**
     * Native Java capture (DPI-aware in Java 21).
     */
    private BufferedImage captureNative() {
        try {
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            return robot.createScreenCapture(new Rectangle(screenSize));
        } catch (AWTException e) {
            throw new RuntimeException("Failed to capture screen", e);
        }
    }
    
    /**
     * Captures at physical resolution, scaling if necessary.
     */
    private BufferedImage capturePhysical() {
        BufferedImage capture = captureNative();
        
        // Check if we need to scale
        if (capture.getWidth() != expectedPhysicalWidth) {
            System.out.println(String.format(
                "[BrobotCapture] Scaling from %dx%d to expected %dx%d",
                capture.getWidth(), capture.getHeight(),
                expectedPhysicalWidth, expectedPhysicalHeight
            ));
            
            return scaleToPhysical(capture);
        }
        
        return capture;
    }
    
    /**
     * Uses external tool for most reliable capture.
     */
    private BufferedImage captureExternal() {
        String os = System.getProperty("os.name").toLowerCase();
        String tmpFile = System.getProperty("java.io.tmpdir") + 
                        "/brobot_capture_" + System.currentTimeMillis() + ".png";
        
        try {
            Process process;
            
            if (os.contains("win")) {
                // Use PowerShell screenshot
                String command = String.format(
                    "powershell -Command \"Add-Type -AssemblyName System.Windows.Forms; " +
                    "$bmp = New-Object System.Drawing.Bitmap([System.Windows.Forms.Screen]::PrimaryScreen.Bounds.Width, " +
                    "[System.Windows.Forms.Screen]::PrimaryScreen.Bounds.Height); " +
                    "$graphics = [System.Drawing.Graphics]::FromImage($bmp); " +
                    "$graphics.CopyFromScreen(0, 0, 0, 0, $bmp.Size); " +
                    "$bmp.Save('%s', [System.Drawing.Imaging.ImageFormat]::Png)\"",
                    tmpFile.replace("\\", "\\\\")
                );
                process = Runtime.getRuntime().exec(command);
            } else if (os.contains("mac")) {
                process = Runtime.getRuntime().exec(new String[]{
                    "screencapture", "-x", "-T", "0", tmpFile
                });
            } else {
                // Linux - try gnome-screenshot first, then scrot
                process = Runtime.getRuntime().exec(new String[]{
                    "gnome-screenshot", "-f", tmpFile
                });
            }
            
            process.waitFor();
            
            File file = new File(tmpFile);
            if (file.exists()) {
                BufferedImage image = ImageIO.read(file);
                file.delete();
                
                System.out.println("[BrobotCapture] External capture: " + 
                                 image.getWidth() + "x" + image.getHeight());
                
                return image;
            }
            
        } catch (Exception e) {
            System.err.println("[BrobotCapture] External capture failed: " + e.getMessage());
        }
        
        // Fallback to native
        return captureNative();
    }
    
    /**
     * Adaptive capture that detects and compensates for scaling.
     */
    private BufferedImage captureAdaptive() {
        BufferedImage capture = captureNative();
        
        // First capture - detect scaling
        if (!scalingDetected) {
            detectScalingFromCapture(capture);
        }
        
        // If scaling detected and we want physical, scale up
        if (scalingDetected && forcePhysical) {
            return scaleToPhysical(capture);
        }
        
        return capture;
    }
    
    /**
     * Detects DPI scaling from system properties and environment.
     */
    private void detectScaling() {
        try {
            // Get graphics configuration
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            
            // Check transform scale
            double scaleX = gc.getDefaultTransform().getScaleX();
            if (scaleX > 1.0) {
                detectedScaleFactor = scaleX;
                scalingDetected = true;
                return;
            }
            
            // Check via Robot capture
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            BufferedImage testCapture = robot.createScreenCapture(new Rectangle(0, 0, 100, 100));
            
            // Common scenarios
            if (screenSize.width == 1536 && expectedPhysicalWidth == 1920) {
                detectedScaleFactor = 1.25; // 125% scaling
                scalingDetected = true;
            } else if (screenSize.width == 1280 && expectedPhysicalWidth == 1920) {
                detectedScaleFactor = 1.5; // 150% scaling
                scalingDetected = true;
            }
            
        } catch (Exception e) {
            System.err.println("[BrobotCapture] Could not detect scaling: " + e.getMessage());
        }
    }
    
    /**
     * Detects scaling from an actual capture.
     */
    private void detectScalingFromCapture(BufferedImage capture) {
        int width = capture.getWidth();
        
        if (width != expectedPhysicalWidth) {
            detectedScaleFactor = (double) expectedPhysicalWidth / width;
            scalingDetected = true;
            
            System.out.println(String.format(
                "[BrobotCapture] Scaling detected: %.2fx (captured %d, expected %d)",
                detectedScaleFactor, width, expectedPhysicalWidth
            ));
        }
    }
    
    /**
     * Scales image to physical resolution.
     */
    private BufferedImage scaleToPhysical(BufferedImage source) {
        if (!scalingDetected) {
            return source;
        }
        
        int targetWidth = (int)(source.getWidth() * detectedScaleFactor);
        int targetHeight = (int)(source.getHeight() * detectedScaleFactor);
        
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, source.getType());
        Graphics2D g2d = scaled.createGraphics();
        
        // High-quality scaling
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        
        return scaled;
    }
    
    /**
     * Reports current configuration and detected settings.
     */
    private void reportConfiguration() {
        System.out.println("\n=== Brobot Screen Capture Configuration ===");
        System.out.println("Strategy: " + strategy);
        System.out.println("Force Physical: " + forcePhysical);
        System.out.println("Expected Resolution: " + expectedPhysicalWidth + "x" + expectedPhysicalHeight);
        
        if (scalingDetected) {
            System.out.println("Scaling Detected: " + String.format("%.2fx", detectedScaleFactor));
            System.out.println("Logical Resolution: " + 
                             (int)(expectedPhysicalWidth / detectedScaleFactor) + "x" + 
                             (int)(expectedPhysicalHeight / detectedScaleFactor));
        } else {
            System.out.println("Scaling: None detected");
        }
        
        System.out.println("Java DPI Aware: " + !"false".equals(System.getProperty("sun.java2d.dpiaware")));
        System.out.println("=========================================\n");
    }
    
    /**
     * Gets information about current capture configuration.
     */
    public String getInfo() {
        return String.format(
            "Capture Strategy: %s, Scaling: %.2fx, Physical: %dx%d",
            strategy, detectedScaleFactor, expectedPhysicalWidth, expectedPhysicalHeight
        );
    }
}