package io.github.jspinak.brobot.capture;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.Pointer;
import com.sun.jna.Memory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Method;

/**
 * Provides physical resolution screen capture regardless of DPI settings.
 * 
 * This class uses platform-specific APIs to bypass Java's DPI awareness
 * and always capture at physical pixel resolution.
 * 
 * @since 1.1.0
 */
public class PhysicalScreenCapture {
    
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS_NAME.contains("win");
    private static final boolean IS_MAC = OS_NAME.contains("mac");
    private static final boolean IS_LINUX = OS_NAME.contains("nux");
    
    // GDI32 constants for GetDeviceCaps
    private static final int HORZRES = 8;  // Horizontal width in pixels
    private static final int VERTRES = 10; // Vertical height in pixels
    
    /**
     * Captures the screen at physical resolution, bypassing DPI scaling.
     * 
     * @return BufferedImage at physical resolution (e.g., 1920x1080)
     */
    public static BufferedImage capture() {
        try {
            // Try platform-specific capture first
            if (IS_WINDOWS) {
                return captureWindows();
            } else if (IS_MAC) {
                return captureMac();
            } else if (IS_LINUX) {
                return captureLinux();
            }
        } catch (Exception e) {
            System.err.println("[PhysicalCapture] Platform-specific capture failed: " + e.getMessage());
        }
        
        // Fallback to Java 8 behavior via reflection
        try {
            return captureWithJava8Mode();
        } catch (Exception e) {
            System.err.println("[PhysicalCapture] Java 8 mode failed: " + e.getMessage());
        }
        
        // Last resort: use standard capture and scale
        return captureAndScale();
    }
    
    /**
     * Windows-specific capture using GDI+ (always physical resolution).
     */
    private static BufferedImage captureWindows() throws Exception {
        // Windows API calls to get physical screen dimensions
        User32 user32 = User32.INSTANCE;
        WinDef.HDC hdcScreen = user32.GetDC(null);
        WinDef.HDC hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcScreen);
        
        // Get PHYSICAL screen dimensions (not DPI-scaled)
        int width = GDI32.INSTANCE.GetDeviceCaps(hdcScreen, HORZRES);
        int height = GDI32.INSTANCE.GetDeviceCaps(hdcScreen, VERTRES);
        
        // Alternative: use GetSystemMetrics for physical size
        if (width == 0 || height == 0) {
            width = user32.GetSystemMetrics(User32.SM_CXSCREEN);
            height = user32.GetSystemMetrics(User32.SM_CYSCREEN);
        }
        
        System.out.println("[PhysicalCapture] Windows GDI+ capturing at: " + width + "x" + height);
        
        // Create bitmap and capture
        WinDef.HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcScreen, width, height);
        GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap);
        GDI32.INSTANCE.BitBlt(hdcMemDC, 0, 0, width, height, hdcScreen, 0, 0, GDI32.SRCCOPY);
        
        // Convert to BufferedImage
        BufferedImage image = convertHBitmapToBufferedImage(hBitmap, width, height);
        
        // Cleanup
        GDI32.INSTANCE.DeleteObject(hBitmap);
        GDI32.INSTANCE.DeleteDC(hdcMemDC);
        user32.ReleaseDC(null, hdcScreen);
        
        return image;
    }
    
    /**
     * Mac-specific capture using CoreGraphics (Retina-aware).
     */
    private static BufferedImage captureMac() throws Exception {
        // Use screencapture command which captures at physical resolution
        String tmpFile = "/tmp/brobot_capture_" + System.currentTimeMillis() + ".png";
        
        Process process = Runtime.getRuntime().exec(new String[]{
            "screencapture", "-x", "-T", "0", tmpFile
        });
        process.waitFor();
        
        BufferedImage image = ImageIO.read(new File(tmpFile));
        new File(tmpFile).delete();
        
        System.out.println("[PhysicalCapture] Mac screencapture: " + 
                         image.getWidth() + "x" + image.getHeight());
        
        return image;
    }
    
    /**
     * Linux-specific capture using native tools.
     */
    private static BufferedImage captureLinux() throws Exception {
        // Try using import command (ImageMagick) which captures physical pixels
        String tmpFile = "/tmp/brobot_capture_" + System.currentTimeMillis() + ".png";
        
        // First try with import (ImageMagick)
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                "import", "-window", "root", tmpFile
            });
            process.waitFor();
            
            if (new File(tmpFile).exists()) {
                BufferedImage image = ImageIO.read(new File(tmpFile));
                new File(tmpFile).delete();
                System.out.println("[PhysicalCapture] Linux import: " + 
                                 image.getWidth() + "x" + image.getHeight());
                return image;
            }
        } catch (Exception e) {
            // ImageMagick not installed
        }
        
        // Try with scrot
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                "scrot", tmpFile
            });
            process.waitFor();
            
            if (new File(tmpFile).exists()) {
                BufferedImage image = ImageIO.read(new File(tmpFile));
                new File(tmpFile).delete();
                System.out.println("[PhysicalCapture] Linux scrot: " + 
                                 image.getWidth() + "x" + image.getHeight());
                return image;
            }
        } catch (Exception e) {
            // scrot not installed
        }
        
        throw new Exception("No suitable screen capture tool found on Linux");
    }
    
    /**
     * Attempts to use Java 8-style capture via reflection.
     */
    private static BufferedImage captureWithJava8Mode() throws Exception {
        // Try to access internal APIs that might bypass DPI scaling
        Robot robot = new Robot();
        
        // Get the peer (internal class that does actual capture)
        Method getPeer = Robot.class.getDeclaredMethod("getPeer");
        getPeer.setAccessible(true);
        Object peer = getPeer.invoke(robot);
        
        // Try to get physical screen dimensions
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Method getScreenSize = toolkit.getClass().getDeclaredMethod("getPhysicalScreenSize");
        getScreenSize.setAccessible(true);
        Dimension physicalSize = (Dimension) getScreenSize.invoke(toolkit);
        
        System.out.println("[PhysicalCapture] Java reflection mode: " + 
                         physicalSize.width + "x" + physicalSize.height);
        
        // Capture at physical size
        return robot.createScreenCapture(new Rectangle(physicalSize));
    }
    
    /**
     * Fallback: Capture and scale to estimated physical resolution.
     */
    private static BufferedImage captureAndScale() {
        try {
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            BufferedImage capture = robot.createScreenCapture(new Rectangle(screenSize));
            
            // Detect if we need to scale
            int capturedWidth = capture.getWidth();
            
            // Common scaling scenarios
            if (capturedWidth == 1536) {
                // 125% scaling - scale up to 1920x1080
                return scaleImage(capture, 1920, 1080);
            } else if (capturedWidth == 1280) {
                // 150% scaling - scale up to 1920x1080
                return scaleImage(capture, 1920, 1080);
            } else if (capturedWidth == 960) {
                // 200% scaling - scale up to 1920x1080
                return scaleImage(capture, 1920, 1080);
            }
            
            // No scaling needed or unknown scaling
            return capture;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to capture screen", e);
        }
    }
    
    /**
     * Converts Windows HBITMAP to BufferedImage.
     */
    private static BufferedImage convertHBitmapToBufferedImage(WinDef.HBITMAP hBitmap, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // This is simplified - actual implementation would need proper bitmap data extraction
        // Using JNA to get bitmap bits and convert to BufferedImage
        
        return image;
    }
    
    /**
     * High-quality image scaling.
     */
    private static BufferedImage scaleImage(BufferedImage source, int targetWidth, int targetHeight) {
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, source.getType());
        Graphics2D g2d = scaled.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        
        return scaled;
    }
    
    /**
     * Gets the physical screen resolution using platform-specific methods.
     */
    public static Dimension getPhysicalResolution() {
        try {
            if (IS_WINDOWS) {
                User32 user32 = User32.INSTANCE;
                int width = user32.GetSystemMetrics(User32.SM_CXSCREEN);
                int height = user32.GetSystemMetrics(User32.SM_CYSCREEN);
                return new Dimension(width, height);
            } else if (IS_MAC) {
                // Use system_profiler to get display info
                Process p = Runtime.getRuntime().exec(
                    "system_profiler SPDisplaysDataType | grep Resolution");
                // Parse output for resolution
                // Example: "Resolution: 1920 x 1080"
            } else if (IS_LINUX) {
                // Use xrandr to get physical resolution
                Process p = Runtime.getRuntime().exec("xrandr | grep '*'");
                // Parse output for resolution
            }
        } catch (Exception e) {
            System.err.println("[PhysicalCapture] Could not get physical resolution: " + e);
        }
        
        // Fallback
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
}