package io.github.jspinak.brobot.capture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Cross-platform physical resolution capture using ImageMagick.
 * 
 * ImageMagick captures at physical resolution, bypassing Java's DPI awareness.
 * 
 * Requirements:
 * - ImageMagick must be installed
 * - Windows: Download from https://imagemagick.org/script/download.php#windows
 * - macOS: brew install imagemagick
 * - Linux: apt-get install imagemagick or yum install imagemagick
 * 
 * @since 1.1.0
 */
public class ImageMagickPhysicalCapture {
    
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS.contains("win");
    private static final boolean IS_MAC = OS.contains("mac");
    
    /**
     * Captures the screen at physical resolution using ImageMagick.
     */
    public static BufferedImage capture() throws IOException {
        String tmpFile = System.getProperty("java.io.tmpdir") + 
                        File.separator + "brobot_magick_" + 
                        System.currentTimeMillis() + ".png";
        
        try {
            String command = buildCommand(tmpFile);
            
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                // Try reading error stream
                byte[] error = process.getErrorStream().readAllBytes();
                throw new IOException("ImageMagick failed: " + new String(error));
            }
            
            File file = new File(tmpFile);
            if (!file.exists()) {
                throw new IOException("ImageMagick did not create output file");
            }
            
            BufferedImage image = ImageIO.read(file);
            file.delete();
            
            System.out.println("[ImageMagick] Captured at: " + 
                             image.getWidth() + "x" + image.getHeight() + 
                             " (physical resolution)");
            
            return image;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("ImageMagick capture interrupted", e);
        }
    }
    
    /**
     * Builds the ImageMagick command for the current platform.
     */
    private static String buildCommand(String outputFile) {
        if (IS_WINDOWS) {
            // Windows: Use screenshot: device
            return String.format("magick import -window root \"%s\"", outputFile);
        } else if (IS_MAC) {
            // macOS: Use screencapture (ImageMagick may not have screen access)
            // Falls back to native screencapture
            return String.format("screencapture -x \"%s\"", outputFile);
        } else {
            // Linux: Use import with root window
            return String.format("import -window root \"%s\"", outputFile);
        }
    }
    
    /**
     * Captures a specific region.
     */
    public static BufferedImage captureRegion(int x, int y, int width, int height) throws IOException {
        String tmpFile = System.getProperty("java.io.tmpdir") + 
                        File.separator + "brobot_magick_region_" + 
                        System.currentTimeMillis() + ".png";
        
        String command;
        if (IS_WINDOWS) {
            // Windows: Capture full then crop
            command = String.format(
                "magick import -window root -crop %dx%d+%d+%d \"%s\"",
                width, height, x, y, tmpFile
            );
        } else if (IS_MAC) {
            // macOS: Use screencapture with region
            command = String.format(
                "screencapture -x -R%d,%d,%d,%d \"%s\"",
                x, y, width, height, tmpFile
            );
        } else {
            // Linux: Use import with geometry
            command = String.format(
                "import -window root -crop %dx%d+%d+%d \"%s\"",
                width, height, x, y, tmpFile
            );
        }
        
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            
            BufferedImage image = ImageIO.read(new File(tmpFile));
            new File(tmpFile).delete();
            
            return image;
        } catch (Exception e) {
            throw new IOException("ImageMagick region capture failed", e);
        }
    }
    
    /**
     * Checks if ImageMagick is installed.
     */
    public static boolean isAvailable() {
        try {
            String command = IS_WINDOWS ? "magick -version" : "convert -version";
            Process process = Runtime.getRuntime().exec(command);
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}