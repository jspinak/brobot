package io.github.jspinak.brobot.capture.provider;

import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * SikuliX-based screen capture provider.
 * 
 * Uses the existing SikuliX API for screen capture.
 * In Java 21, this captures at logical/DPI-scaled resolution.
 * 
 * @since 1.1.0
 */
@Component
public class SikuliXCaptureProvider implements CaptureProvider {
    
    @Override
    public BufferedImage captureScreen() throws IOException {
        return captureScreen(0);
    }
    
    @Override
    public BufferedImage captureScreen(int screenId) throws IOException {
        try {
            Screen screen = new Screen(screenId);
            ScreenImage screenImage = screen.capture();
            BufferedImage image = screenImage.getImage();
            
            logCapture("Screen " + screenId, image.getWidth(), image.getHeight());
            return image;
            
        } catch (Exception e) {
            throw new IOException("SikuliX capture failed", e);
        }
    }
    
    @Override
    public BufferedImage captureRegion(Rectangle region) throws IOException {
        return captureRegion(0, region);
    }
    
    @Override
    public BufferedImage captureRegion(int screenId, Rectangle region) throws IOException {
        try {
            Screen screen = new Screen(screenId);
            ScreenImage screenImage = screen.capture(region);
            BufferedImage image = screenImage.getImage();
            
            logCapture(String.format("Region [%d,%d %dx%d]", 
                region.x, region.y, region.width, region.height),
                image.getWidth(), image.getHeight());
            
            return image;
            
        } catch (Exception e) {
            throw new IOException("SikuliX region capture failed", e);
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // Check if SikuliX classes are available
            Class.forName("org.sikuli.script.Screen");
            // Try a test capture
            new Screen(0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getName() {
        return "SikuliX";
    }
    
    @Override
    public ResolutionType getResolutionType() {
        // In Java 21, SikuliX captures at logical resolution
        // In Java 8, it captures at physical resolution
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1.8")) {
            return ResolutionType.PHYSICAL;
        }
        return ResolutionType.LOGICAL;
    }
    
    private void logCapture(String target, int width, int height) {
        String resType = getResolutionType() == ResolutionType.PHYSICAL ? "physical" : "logical";
        System.out.println(String.format("[SikuliX] Captured %s at %dx%d (%s resolution)",
            target, width, height, resType));
    }
}