package io.github.jspinak.brobot.capture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Cross-platform physical resolution capture using FFmpeg.
 * 
 * FFmpeg captures at physical resolution on all platforms, bypassing Java's DPI awareness.
 * 
 * Requirements:
 * - FFmpeg must be installed and in PATH
 * - Windows: Download from https://ffmpeg.org/download.html
 * - macOS: brew install ffmpeg
 * - Linux: apt-get install ffmpeg or yum install ffmpeg
 * 
 * @since 1.1.0
 */
public class FFmpegPhysicalCapture {
    
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS.contains("win");
    private static final boolean IS_MAC = OS.contains("mac");
    private static final boolean IS_LINUX = OS.contains("nux");
    
    /**
     * Captures the screen at physical resolution using FFmpeg.
     * 
     * @return BufferedImage at physical resolution
     * @throws IOException if capture fails
     */
    public static BufferedImage capture() throws IOException {
        String tmpFile = System.getProperty("java.io.tmpdir") + 
                        File.separator + "brobot_ffmpeg_" + 
                        System.currentTimeMillis() + ".png";
        
        try {
            String command = buildFFmpegCommand(tmpFile);
            
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new IOException("FFmpeg capture failed with exit code: " + exitCode);
            }
            
            File file = new File(tmpFile);
            if (!file.exists()) {
                throw new IOException("FFmpeg did not create output file");
            }
            
            BufferedImage image = ImageIO.read(file);
            file.delete();
            
            System.out.println("[FFmpeg] Captured at: " + 
                             image.getWidth() + "x" + image.getHeight() + 
                             " (physical resolution)");
            
            return image;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("FFmpeg capture interrupted", e);
        } catch (IOException e) {
            throw new IOException("FFmpeg capture failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Builds the FFmpeg command for the current platform.
     */
    private static String buildFFmpegCommand(String outputFile) {
        if (IS_WINDOWS) {
            // Windows: Use GDI grab (captures physical resolution)
            return String.format(
                "ffmpeg -f gdigrab -framerate 1 -frames:v 1 -i desktop -y \"%s\"",
                outputFile
            );
        } else if (IS_MAC) {
            // macOS: Use AVFoundation (captures physical/Retina resolution)
            // List devices: ffmpeg -f avfoundation -list_devices true -i ""
            return String.format(
                "ffmpeg -f avfoundation -video_size 1920x1080 -framerate 1 -frames:v 1 -i \"1:\" -y \"%s\"",
                outputFile
            );
        } else if (IS_LINUX) {
            // Linux: Use x11grab (captures physical resolution)
            // Detect display size first if needed
            String display = System.getenv("DISPLAY");
            if (display == null) display = ":0";
            
            return String.format(
                "ffmpeg -f x11grab -video_size 1920x1080 -framerate 1 -frames:v 1 -i %s -y \"%s\"",
                display, outputFile
            );
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + OS);
        }
    }
    
    /**
     * Captures a specific region at physical resolution.
     */
    public static BufferedImage captureRegion(int x, int y, int width, int height) throws IOException {
        String tmpFile = System.getProperty("java.io.tmpdir") + 
                        File.separator + "brobot_ffmpeg_region_" + 
                        System.currentTimeMillis() + ".png";
        
        try {
            String command = buildRegionCommand(x, y, width, height, tmpFile);
            
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new IOException("FFmpeg region capture failed");
            }
            
            BufferedImage image = ImageIO.read(new File(tmpFile));
            new File(tmpFile).delete();
            
            return image;
            
        } catch (Exception e) {
            throw new IOException("FFmpeg region capture failed", e);
        }
    }
    
    /**
     * Builds FFmpeg command for region capture.
     */
    private static String buildRegionCommand(int x, int y, int width, int height, String output) {
        if (IS_WINDOWS) {
            // Windows GDI grab with offset
            return String.format(
                "ffmpeg -f gdigrab -offset_x %d -offset_y %d -video_size %dx%d " +
                "-framerate 1 -frames:v 1 -i desktop -y \"%s\"",
                x, y, width, height, output
            );
        } else if (IS_LINUX) {
            // Linux x11grab with geometry
            String display = System.getenv("DISPLAY");
            if (display == null) display = ":0";
            
            return String.format(
                "ffmpeg -f x11grab -video_size %dx%d -framerate 1 -frames:v 1 " +
                "-i %s+%d,%d -y \"%s\"",
                width, height, display, x, y, output
            );
        } else if (IS_MAC) {
            // macOS - crop after capture (AVFoundation doesn't support direct region)
            return String.format(
                "ffmpeg -f avfoundation -framerate 1 -frames:v 1 -i \"1:\" " +
                "-vf \"crop=%d:%d:%d:%d\" -y \"%s\"",
                width, height, x, y, output
            );
        }
        
        throw new UnsupportedOperationException("Region capture not supported on: " + OS);
    }
    
    /**
     * Checks if FFmpeg is installed and available.
     */
    public static boolean isAvailable() {
        try {
            Process process = Runtime.getRuntime().exec("ffmpeg -version");
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets FFmpeg version information.
     */
    public static String getVersion() {
        try {
            Process process = Runtime.getRuntime().exec("ffmpeg -version");
            byte[] output = process.getInputStream().readAllBytes();
            String version = new String(output);
            
            // Extract first line (version info)
            int newline = version.indexOf('\n');
            if (newline > 0) {
                return version.substring(0, newline);
            }
            return version;
        } catch (Exception e) {
            return "FFmpeg not available";
        }
    }
}