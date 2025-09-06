package io.github.jspinak.brobot.capture.provider;

import io.github.jspinak.brobot.capture.JavaCVFFmpegCapture;
import org.springframework.stereotype.Component;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * JavaCV FFmpeg-based capture provider that uses bundled FFmpeg libraries.
 * No external FFmpeg installation required.
 * 
 * This provider captures at physical resolution (1920x1080) bypassing DPI scaling.
 * It's ideal for matching patterns created with Windows or SikuliX IDE.
 * 
 * @since 1.1.0
 */
@Component
public class JavaCVFFmpegProvider implements CaptureProvider {
    
    private Boolean available = null;
    
    @Override
    public String getName() {
        return "JAVACV_FFMPEG";
    }
    
    @Override
    public BufferedImage captureScreen() throws IOException {
        try {
            return JavaCVFFmpegCapture.capture();
        } catch (Exception e) {
            throw new IOException("JavaCV FFmpeg capture failed", e);
        }
    }
    
    @Override
    public BufferedImage captureScreen(int screenId) throws IOException {
        // JavaCV FFmpeg doesn't support multiple screens yet, use primary
        return captureScreen();
    }
    
    @Override
    public BufferedImage captureRegion(Rectangle region) throws IOException {
        // Always capture full screen for physical resolution providers
        // Region filtering should happen at a higher level after capture
        return captureScreen();
    }
    
    @Override
    public BufferedImage captureRegion(int screenId, Rectangle region) throws IOException {
        // JavaCV FFmpeg doesn't support multiple screens yet
        return captureRegion(region);
    }
    
    @Override
    public boolean isAvailable() {
        if (available == null) {
            available = checkJavaCVAvailable();
        }
        return available;
    }
    
    @Override
    public ResolutionType getResolutionType() {
        return ResolutionType.PHYSICAL; // JavaCV FFmpeg always captures at physical resolution
    }
    
    private boolean checkJavaCVAvailable() {
        try {
            // Try to capture to see if JavaCV works
            BufferedImage test = JavaCVFFmpegCapture.capture();
            return test != null && test.getWidth() > 0;
        } catch (Throwable e) {
            // JavaCV not available or capture failed
            System.out.println("[JavaCVFFmpeg] Not available: " + e.getMessage());
            return false;
        }
    }
}