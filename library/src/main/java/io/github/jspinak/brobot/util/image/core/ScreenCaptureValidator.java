package io.github.jspinak.brobot.util.image.core;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Validates screen capture functionality in different environments.
 * Detects headless mode, validates captured images, and provides diagnostics.
 */
@Component
public class ScreenCaptureValidator {
    
    @Autowired(required = false)
    private BrobotLogger logger;
    
    /**
     * Result of screen capture validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final BufferedImage capturedImage;
        private final boolean isHeadless;
        private final boolean hasNonBlackPixels;
        
        public ValidationResult(boolean valid, String message, BufferedImage capturedImage,
                               boolean isHeadless, boolean hasNonBlackPixels) {
            this.valid = valid;
            this.message = message;
            this.capturedImage = capturedImage;
            this.isHeadless = isHeadless;
            this.hasNonBlackPixels = hasNonBlackPixels;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public BufferedImage getCapturedImage() { return capturedImage; }
        public boolean isHeadless() { return isHeadless; }
        public boolean hasNonBlackPixels() { return hasNonBlackPixels; }
    }
    
    /**
     * Validate screen capture functionality
     */
    public ValidationResult validateScreenCapture() {
        boolean isHeadless = detectHeadlessMode();
        
        if (isHeadless) {
            return new ValidationResult(
                false, 
                "Screen capture not available in headless mode",
                null,
                true,
                false
            );
        }
        
        BufferedImage capturedImage = null;
        try {
            capturedImage = captureScreen();
            
            if (capturedImage == null) {
                return new ValidationResult(
                    false,
                    "Screen capture returned null",
                    null,
                    isHeadless,
                    false
                );
            }
            
            boolean hasNonBlackPixels = checkPixelValues(capturedImage);
            
            if (!hasNonBlackPixels) {
                return new ValidationResult(
                    false,
                    "Screen capture returned all black image",
                    capturedImage,
                    isHeadless,
                    false
                );
            }
            
            return new ValidationResult(
                true,
                "Screen capture validated successfully",
                capturedImage,
                isHeadless,
                true
            );
            
        } catch (Exception e) {
            if (logger != null) {
                logger.log()
                    .level(LogEvent.Level.ERROR)
                    .message("Error during screen capture validation")
                    .error(e)
                    .log();
            }
            return new ValidationResult(
                false,
                "Exception during screen capture: " + e.getMessage(),
                capturedImage,
                isHeadless,
                false
            );
        }
    }
    
    /**
     * Detect if running in headless mode
     */
    public boolean detectHeadlessMode() {
        // Check Java system property
        boolean isHeadlessProperty = "true".equals(System.getProperty("java.awt.headless"));
        
        // Check GraphicsEnvironment
        boolean isGraphicsHeadless = GraphicsEnvironment.isHeadless();
        
        // Check for WSL environment
        boolean isWSL = isRunningInWSL();
        
        // Check for SSH session
        boolean isSSH = System.getenv("SSH_CLIENT") != null || 
                       System.getenv("SSH_TTY") != null;
        
        boolean isHeadless = isHeadlessProperty || isGraphicsHeadless || (isWSL && !hasDisplay());
        
        if (isHeadless) {
            if (logger != null) {
                logger.log()
                    .level(LogEvent.Level.INFO)
                    .message("Headless mode detected")
                    .metadata("headlessProperty", isHeadlessProperty)
                    .metadata("graphicsHeadless", isGraphicsHeadless)
                    .metadata("isWSL", isWSL)
                    .metadata("isSSH", isSSH)
                    .log();
            }
        }
        
        return isHeadless;
    }
    
    /**
     * Check if running in WSL
     */
    private boolean isRunningInWSL() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        boolean isLinux = osName.contains("linux");
        
        if (!isLinux) {
            return false;
        }
        
        // Check for WSL-specific environment variables
        String wslDistro = System.getenv("WSL_DISTRO_NAME");
        String wslInterop = System.getenv("WSL_INTEROP");
        
        // Check for Microsoft in kernel version
        String kernelVersion = System.getProperty("os.version", "").toLowerCase();
        boolean hasMicrosoft = kernelVersion.contains("microsoft");
        
        return wslDistro != null || wslInterop != null || hasMicrosoft;
    }
    
    /**
     * Check if DISPLAY environment variable is set (for X11)
     */
    private boolean hasDisplay() {
        String display = System.getenv("DISPLAY");
        return display != null && !display.isEmpty();
    }
    
    /**
     * Capture a screen image
     */
    private BufferedImage captureScreen() throws AWTException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        return robot.createScreenCapture(screenRect);
    }
    
    /**
     * Check if image has non-black pixels
     */
    public boolean checkPixelValues(BufferedImage image) {
        if (image == null) {
            return false;
        }
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Sample pixels across the image
        int sampleRate = Math.max(1, Math.min(width, height) / 100);
        
        for (int x = 0; x < width; x += sampleRate) {
            for (int y = 0; y < height; y += sampleRate) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                
                // If any pixel is not black, return true
                if (red > 0 || green > 0 || blue > 0) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Get diagnostic information about the environment
     */
    public String getDiagnosticInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== Screen Capture Diagnostics ===\n");
        info.append("OS: ").append(System.getProperty("os.name")).append("\n");
        info.append("OS Version: ").append(System.getProperty("os.version")).append("\n");
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("Headless Property: ").append(System.getProperty("java.awt.headless")).append("\n");
        info.append("Graphics Headless: ").append(GraphicsEnvironment.isHeadless()).append("\n");
        info.append("DISPLAY: ").append(System.getenv("DISPLAY")).append("\n");
        info.append("WSL: ").append(isRunningInWSL()).append("\n");
        info.append("SSH Session: ").append(System.getenv("SSH_CLIENT") != null).append("\n");
        
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            info.append("Screen Size: ").append(screenSize.width).append("x").append(screenSize.height).append("\n");
        } catch (Exception e) {
            info.append("Screen Size: Unable to determine (").append(e.getMessage()).append(")\n");
        }
        
        return info.toString();
    }
    
    /**
     * Test screen capture and return result
     */
    public boolean testScreenCapture() {
        ValidationResult result = validateScreenCapture();
        
        if (result.isValid()) {
            if (logger != null) {
                logger.log()
                    .level(LogEvent.Level.INFO)
                    .message("Screen capture test PASSED")
                    .log();
            }
        } else {
            if (logger != null) {
                logger.log()
                    .level(LogEvent.Level.WARNING)
                    .message("Screen capture test FAILED: " + result.getMessage())
                    .metadata("diagnosticInfo", getDiagnosticInfo())
                    .log();
            }
        }
        
        return result.isValid();
    }
}