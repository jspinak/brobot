package io.github.jspinak.brobot.capture.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * FFmpeg-based screen capture provider.
 * 
 * Captures screens at physical resolution, bypassing Java's DPI awareness.
 * This ensures consistent captures regardless of Java version or DPI settings.
 * 
 * @since 1.1.0
 */
@Component
public class FFmpegCaptureProvider implements CaptureProvider {
    
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS.contains("win");
    private static final boolean IS_MAC = OS.contains("mac");
    private static final boolean IS_LINUX = OS.contains("nux");
    
    @Value("${brobot.capture.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;
    
    @Value("${brobot.capture.ffmpeg.timeout:5}")
    private int captureTimeout;
    
    @Value("${brobot.capture.ffmpeg.format:png}")
    private String outputFormat;
    
    @Value("${brobot.capture.ffmpeg.log-level:error}")
    private String logLevel;
    
    private Boolean available = null;
    
    @Override
    public BufferedImage captureScreen() throws IOException {
        return captureScreen(0);
    }
    
    @Override
    public BufferedImage captureScreen(int screenId) throws IOException {
        String tmpFile = getTempFilePath();
        
        try {
            List<String> command = buildCaptureCommand(screenId, null, tmpFile);
            executeFFmpeg(command);
            
            BufferedImage image = loadImage(tmpFile);
            logCapture("Screen " + screenId, image.getWidth(), image.getHeight());
            
            return image;
            
        } finally {
            deleteTempFile(tmpFile);
        }
    }
    
    @Override
    public BufferedImage captureRegion(Rectangle region) throws IOException {
        // Always capture full screen for consistent behavior
        // Region filtering should happen at a higher level
        return captureScreen();
    }
    
    @Override
    public BufferedImage captureRegion(int screenId, Rectangle region) throws IOException {
        // Always capture full screen for consistent behavior
        // Region filtering should happen at a higher level
        return captureScreen(screenId);
    }
    
    /**
     * Builds the FFmpeg command for the current platform.
     */
    private List<String> buildCaptureCommand(int screenId, Rectangle region, String outputFile) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        
        // Common options
        command.add("-loglevel");
        command.add(logLevel);
        
        // Platform-specific input
        if (IS_WINDOWS) {
            addWindowsInput(command, screenId, region);
        } else if (IS_MAC) {
            addMacInput(command, screenId, region);
        } else if (IS_LINUX) {
            addLinuxInput(command, screenId, region);
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + OS);
        }
        
        // Common output options
        command.add("-frames:v");
        command.add("1");
        command.add("-y");  // Overwrite output
        command.add(outputFile);
        
        return command;
    }
    
    private void addWindowsInput(List<String> command, int screenId, Rectangle region) {
        command.add("-f");
        command.add("gdigrab");
        
        if (region != null) {
            command.add("-offset_x");
            command.add(String.valueOf(region.x));
            command.add("-offset_y");
            command.add(String.valueOf(region.y));
            command.add("-video_size");
            command.add(region.width + "x" + region.height);
        }
        
        command.add("-i");
        if (screenId == 0) {
            command.add("desktop");
        } else {
            // For multi-monitor, use display name
            command.add("title=Display " + screenId);
        }
    }
    
    private void addMacInput(List<String> command, int screenId, Rectangle region) {
        command.add("-f");
        command.add("avfoundation");
        
        // Get screen size for explicit video_size
        Dimension screenSize = getScreenSize(screenId);
        command.add("-video_size");
        command.add(screenSize.width + "x" + screenSize.height);
        
        command.add("-i");
        command.add(screenId + ":");  // Screen index for AVFoundation
        
        // Apply crop filter for regions
        if (region != null) {
            command.add("-vf");
            command.add(String.format("crop=%d:%d:%d:%d",
                region.width, region.height, region.x, region.y));
        }
    }
    
    private void addLinuxInput(List<String> command, int screenId, Rectangle region) {
        command.add("-f");
        command.add("x11grab");
        
        // Determine display
        String display = System.getenv("DISPLAY");
        if (display == null) {
            display = ":0." + screenId;
        } else if (screenId > 0) {
            // Append screen number if not primary
            if (display.contains(".")) {
                display = display.substring(0, display.lastIndexOf('.')) + "." + screenId;
            } else {
                display = display + "." + screenId;
            }
        }
        
        if (region != null) {
            command.add("-video_size");
            command.add(region.width + "x" + region.height);
            command.add("-i");
            command.add(display + "+" + region.x + "," + region.y);
        } else {
            // Full screen - detect size
            Dimension screenSize = getScreenSize(screenId);
            command.add("-video_size");
            command.add(screenSize.width + "x" + screenSize.height);
            command.add("-i");
            command.add(display);
        }
    }
    
    /**
     * Executes the FFmpeg command.
     */
    private void executeFFmpeg(List<String> command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectError(ProcessBuilder.Redirect.PIPE);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        
        try {
            Process process = pb.start();
            boolean completed = process.waitFor(captureTimeout, TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                throw new IOException("FFmpeg capture timed out after " + captureTimeout + " seconds");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                byte[] errorOutput = process.getErrorStream().readAllBytes();
                String error = new String(errorOutput);
                throw new IOException("FFmpeg failed with exit code " + exitCode + ": " + error);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("FFmpeg capture interrupted", e);
        }
    }
    
    /**
     * Gets screen size for the specified screen.
     */
    private Dimension getScreenSize(int screenId) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        
        if (screenId >= 0 && screenId < devices.length) {
            DisplayMode dm = devices[screenId].getDisplayMode();
            return new Dimension(dm.getWidth(), dm.getHeight());
        }
        
        // Fallback to primary screen
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
    
    /**
     * Generates a temporary file path for captures.
     */
    private String getTempFilePath() {
        return System.getProperty("java.io.tmpdir") + 
               File.separator + "brobot_ffmpeg_" + 
               System.currentTimeMillis() + "." + outputFormat;
    }
    
    /**
     * Loads an image from file.
     */
    private BufferedImage loadImage(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("FFmpeg did not create output file: " + path);
        }
        
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Failed to load captured image: " + path);
        }
        
        return image;
    }
    
    /**
     * Deletes temporary file.
     */
    private void deleteTempFile(String path) {
        try {
            new File(path).delete();
        } catch (Exception e) {
            // Log but don't fail
            System.err.println("[FFmpeg] Could not delete temp file: " + path);
        }
    }
    
    /**
     * Logs capture information.
     */
    private void logCapture(String target, int width, int height) {
        System.out.println(String.format("[FFmpeg] Captured %s at %dx%d (physical resolution)",
            target, width, height));
    }
    
    @Override
    public boolean isAvailable() {
        if (available == null) {
            available = checkFFmpegAvailable();
        }
        return available;
    }
    
    /**
     * Checks if FFmpeg is installed and accessible.
     */
    private boolean checkFFmpegAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(ffmpegPath, "-version");
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            
            Process process = pb.start();
            boolean completed = process.waitFor(2, TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                return false;
            }
            
            return process.exitValue() == 0;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getName() {
        return "FFmpeg";
    }
    
    @Override
    public ResolutionType getResolutionType() {
        return ResolutionType.PHYSICAL;
    }
}