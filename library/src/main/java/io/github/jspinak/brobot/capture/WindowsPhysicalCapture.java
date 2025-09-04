package io.github.jspinak.brobot.capture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Windows-specific physical resolution capture.
 * 
 * This implementation uses multiple strategies to capture at physical resolution:
 * 1. Robot with DPI scaling compensation
 * 2. FFmpeg gdigrab with forced resolution
 * 3. PowerShell screen capture
 * 
 * @since 1.1.0
 */
public class WindowsPhysicalCapture {
    
    /**
     * Captures the screen at physical resolution using Robot with scaling.
     * 
     * @return BufferedImage at physical resolution
     */
    public static BufferedImage captureWithScaledRobot() throws AWTException {
        // Get logical dimensions from AWT
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int logicalWidth = gd.getDisplayMode().getWidth();
        int logicalHeight = gd.getDisplayMode().getHeight();
        
        // Detect DPI scaling
        double dpiScale = 1.0;
        if (logicalWidth == 1536 && logicalHeight == 864) {
            // 125% scaling detected
            dpiScale = 1.25;
        } else if (logicalWidth == 1280 && logicalHeight == 720) {
            // 150% scaling detected
            dpiScale = 1.5;
        }
        
        // Calculate physical dimensions
        int physicalWidth = (int)(logicalWidth * dpiScale);
        int physicalHeight = (int)(logicalHeight * dpiScale);
        
        System.out.println("[WindowsPhysical] Logical: " + logicalWidth + "x" + logicalHeight);
        System.out.println("[WindowsPhysical] DPI Scale: " + dpiScale);
        System.out.println("[WindowsPhysical] Physical: " + physicalWidth + "x" + physicalHeight);
        
        // Capture at logical resolution
        Robot robot = new Robot();
        BufferedImage logicalCapture = robot.createScreenCapture(
            new Rectangle(0, 0, logicalWidth, logicalHeight)
        );
        
        // Scale up to physical resolution if needed
        if (dpiScale > 1.0) {
            BufferedImage physicalCapture = new BufferedImage(
                physicalWidth, physicalHeight, BufferedImage.TYPE_INT_RGB
            );
            
            Graphics2D g2d = physicalCapture.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(logicalCapture, 0, 0, physicalWidth, physicalHeight, null);
            g2d.dispose();
            
            System.out.println("[WindowsPhysical] Scaled capture to: " + 
                             physicalCapture.getWidth() + "x" + physicalCapture.getHeight());
            
            return physicalCapture;
        }
        
        return logicalCapture;
    }
    
    /**
     * Captures using PowerShell with .NET Framework.
     */
    public static BufferedImage captureWithPowerShell() throws IOException {
        String tmpFile = System.getProperty("java.io.tmpdir") + 
                        File.separator + "brobot_ps_" + 
                        System.currentTimeMillis() + ".png";
        
        // PowerShell script that captures at physical resolution
        String script = String.format(
            "[System.Reflection.Assembly]::LoadWithPartialName('System.Windows.Forms'); " +
            "[System.Reflection.Assembly]::LoadWithPartialName('System.Drawing'); " +
            "$bounds = [System.Windows.Forms.Screen]::PrimaryScreen.Bounds; " +
            "$bmp = New-Object System.Drawing.Bitmap($bounds.Width, $bounds.Height); " +
            "$graphics = [System.Drawing.Graphics]::FromImage($bmp); " +
            "$graphics.CopyFromScreen($bounds.Location, [System.Drawing.Point]::Empty, $bounds.Size); " +
            "$bmp.Save('%s', [System.Drawing.Imaging.ImageFormat]::Png); " +
            "$graphics.Dispose(); " +
            "$bmp.Dispose();",
            tmpFile.replace("\\", "\\\\")
        );
        
        ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", script);
        Process p = pb.start();
        
        try {
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                throw new IOException("PowerShell capture failed with exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("PowerShell capture interrupted", e);
        }
        
        File file = new File(tmpFile);
        if (!file.exists()) {
            throw new IOException("PowerShell did not create capture file");
        }
        
        BufferedImage image = ImageIO.read(file);
        file.delete();
        
        System.out.println("[WindowsPhysical] PowerShell captured: " + 
                         image.getWidth() + "x" + image.getHeight());
        
        return image;
    }
    
    /**
     * Captures using the best available method for physical resolution.
     */
    public static BufferedImage capture() {
        // Try JavaCV FFmpeg first (if available)
        try {
            if (JavaCVFFmpegCapture.isAvailable()) {
                BufferedImage image = JavaCVFFmpegCapture.capture();
                if (image.getWidth() >= 1920) {
                    return image;
                }
            }
        } catch (Exception e) {
            System.out.println("[WindowsPhysical] JavaCV FFmpeg failed: " + e.getMessage());
        }
        
        // Try external FFmpeg
        try {
            if (FFmpegPhysicalCapture.isAvailable()) {
                BufferedImage image = FFmpegPhysicalCapture.capture();
                if (image.getWidth() >= 1920) {
                    return image;
                }
            }
        } catch (Exception e) {
            System.out.println("[WindowsPhysical] External FFmpeg failed: " + e.getMessage());
        }
        
        // Try PowerShell
        try {
            BufferedImage image = captureWithPowerShell();
            if (image.getWidth() >= 1920) {
                return image;
            }
        } catch (Exception e) {
            System.out.println("[WindowsPhysical] PowerShell failed: " + e.getMessage());
        }
        
        // Fall back to scaled Robot
        try {
            return captureWithScaledRobot();
        } catch (Exception e) {
            System.out.println("[WindowsPhysical] All methods failed, using standard capture");
            try {
                Robot robot = new Robot();
                return robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            } catch (AWTException ex) {
                throw new RuntimeException("Failed to capture screen", ex);
            }
        }
    }
}