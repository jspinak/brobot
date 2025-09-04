package io.github.jspinak.brobot.capture;

import io.github.jspinak.brobot.capture.provider.CaptureProvider;
import io.github.jspinak.brobot.capture.provider.FFmpegCaptureProvider;
import io.github.jspinak.brobot.capture.provider.SikuliXCaptureProvider;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FFmpeg-based screen capture.
 * 
 * Compares FFmpeg captures with SikuliX captures to verify physical resolution.
 */
public class FFmpegCaptureTest extends BrobotTestBase {
    
    @Autowired(required = false)
    private BrobotCaptureService captureService;
    
    @Autowired(required = false)
    private FFmpegCaptureProvider ffmpegProvider;
    
    @Autowired(required = false)
    private SikuliXCaptureProvider sikuliProvider;
    
    @Test
    public void testFFmpegAvailability() {
        if (ffmpegProvider == null) {
            ffmpegProvider = new FFmpegCaptureProvider();
        }
        
        boolean available = ffmpegProvider.isAvailable();
        System.out.println("FFmpeg available: " + available);
        
        if (available) {
            System.out.println("FFmpeg provider ready for use");
            assertEquals(CaptureProvider.ResolutionType.PHYSICAL, 
                        ffmpegProvider.getResolutionType());
        } else {
            System.out.println("FFmpeg not installed - skipping tests");
            System.out.println("To install FFmpeg:");
            System.out.println("  Windows: Download from https://ffmpeg.org/download.html");
            System.out.println("  macOS: brew install ffmpeg");
            System.out.println("  Linux: apt-get install ffmpeg");
        }
    }
    
    @Test
    public void testCaptureComparison() throws IOException {
        // Skip in mock mode
        Assumptions.assumeFalse(isInMockMode(), "Skipping in mock mode");
        
        if (ffmpegProvider == null) {
            ffmpegProvider = new FFmpegCaptureProvider();
        }
        if (sikuliProvider == null) {
            sikuliProvider = new SikuliXCaptureProvider();
        }
        
        // Check if FFmpeg is available
        Assumptions.assumeTrue(ffmpegProvider.isAvailable(), "FFmpeg not available");
        
        System.out.println("\n=== CAPTURE COMPARISON TEST ===");
        
        // Capture with both providers
        BufferedImage ffmpegCapture = ffmpegProvider.captureScreen();
        BufferedImage sikuliCapture = sikuliProvider.captureScreen();
        
        System.out.println("FFmpeg capture: " + 
            ffmpegCapture.getWidth() + "x" + ffmpegCapture.getHeight() + 
            " (" + ffmpegProvider.getResolutionType() + ")");
        
        System.out.println("SikuliX capture: " + 
            sikuliCapture.getWidth() + "x" + sikuliCapture.getHeight() + 
            " (" + sikuliProvider.getResolutionType() + ")");
        
        // Compare resolutions
        if (ffmpegCapture.getWidth() != sikuliCapture.getWidth()) {
            double ratio = (double) ffmpegCapture.getWidth() / sikuliCapture.getWidth();
            System.out.println("Resolution ratio: " + String.format("%.2f", ratio));
            
            if (Math.abs(ratio - 1.25) < 0.01) {
                System.out.println("→ Detected 125% DPI scaling");
                System.out.println("→ FFmpeg captures at physical resolution (as expected)");
                System.out.println("→ SikuliX captures at logical resolution in Java 21");
            }
        } else {
            System.out.println("→ Both capture at same resolution");
        }
        
        // Save captures for visual comparison
        saveCapture(ffmpegCapture, "test_ffmpeg_capture.png");
        saveCapture(sikuliCapture, "test_sikuli_capture.png");
    }
    
    @Test
    public void testRegionCapture() throws IOException {
        Assumptions.assumeFalse(isInMockMode(), "Skipping in mock mode");
        
        if (ffmpegProvider == null) {
            ffmpegProvider = new FFmpegCaptureProvider();
        }
        
        Assumptions.assumeTrue(ffmpegProvider.isAvailable(), "FFmpeg not available");
        
        System.out.println("\n=== REGION CAPTURE TEST ===");
        
        // Capture a 200x100 region at center
        Rectangle region = new Rectangle(100, 100, 200, 100);
        
        BufferedImage regionCapture = ffmpegProvider.captureRegion(region);
        
        System.out.println("Requested region: " + region);
        System.out.println("Captured size: " + 
            regionCapture.getWidth() + "x" + regionCapture.getHeight());
        
        assertEquals(200, regionCapture.getWidth(), "Width should match requested");
        assertEquals(100, regionCapture.getHeight(), "Height should match requested");
        
        saveCapture(regionCapture, "test_region_capture.png");
    }
    
    @Test
    public void testCaptureService() {
        if (captureService == null) {
            System.out.println("CaptureService not available in test context");
            return;
        }
        
        System.out.println("\n=== CAPTURE SERVICE TEST ===");
        System.out.println(captureService.getProvidersInfo());
        
        CaptureProvider active = captureService.getActiveProvider();
        assertNotNull(active, "Should have an active provider");
        
        System.out.println("Active provider: " + active.getName());
        System.out.println("Resolution type: " + active.getResolutionType());
    }
    
    @Test
    public void testProviderSwitching() throws IOException {
        if (captureService == null) {
            return;
        }
        
        System.out.println("\n=== PROVIDER SWITCHING TEST ===");
        
        // Try to switch to FFmpeg
        try {
            captureService.setProvider("FFMPEG");
            System.out.println("Switched to FFmpeg");
            
            BufferedImage capture = captureService.captureScreen();
            System.out.println("FFmpeg capture: " + 
                capture.getWidth() + "x" + capture.getHeight());
        } catch (Exception e) {
            System.out.println("Could not switch to FFmpeg: " + e.getMessage());
        }
        
        // Switch to SikuliX
        try {
            captureService.setProvider("SIKULIX");
            System.out.println("Switched to SikuliX");
            
            BufferedImage capture = captureService.captureScreen();
            System.out.println("SikuliX capture: " + 
                capture.getWidth() + "x" + capture.getHeight());
        } catch (Exception e) {
            System.out.println("Could not switch to SikuliX: " + e.getMessage());
        }
    }
    
    private void saveCapture(BufferedImage image, String filename) {
        try {
            File file = new File(filename);
            ImageIO.write(image, "png", file);
            System.out.println("Saved: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Could not save image: " + e.getMessage());
        }
    }
}