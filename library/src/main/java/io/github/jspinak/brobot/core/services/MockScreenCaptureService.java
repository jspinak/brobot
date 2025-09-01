package io.github.jspinak.brobot.core.services;

import io.github.jspinak.brobot.model.element.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

/**
 * Mock implementation of ScreenCaptureService for testing and headless environments.
 * 
 * This implementation returns blank images without requiring a display,
 * allowing tests to run in CI/CD pipelines and headless environments.
 */
@Component
@Primary
@ConditionalOnProperty(name = "brobot.mock", havingValue = "true")
@Slf4j
public class MockScreenCaptureService implements ScreenCaptureService {
    
    private static final int DEFAULT_WIDTH = 1920;
    private static final int DEFAULT_HEIGHT = 1080;
    
    public MockScreenCaptureService() {
        log.info("[MOCK] MockScreenCaptureService initialized - no display required");
    }
    
    @Override
    public BufferedImage captureScreen() {
        log.debug("[MOCK] Capturing mock screen");
        return createMockImage(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    @Override
    public BufferedImage captureRegion(int x, int y, int width, int height) {
        log.debug("[MOCK] Capturing mock region: x={}, y={}, w={}, h={}", x, y, width, height);
        return createMockImage(width, height);
    }
    
    @Override
    public BufferedImage captureActiveScreen() {
        log.debug("[MOCK] Capturing mock active screen");
        return captureScreen();
    }
    
    @Override
    public BufferedImage captureMonitor(int monitorIndex) {
        log.debug("[MOCK] Capturing mock monitor {}", monitorIndex);
        return captureScreen();
    }
    
    @Override
    public int getMonitorCount() {
        return 1; // Mock environment has one virtual monitor
    }
    
    @Override
    public Region getMonitorBounds(int monitorIndex) {
        if (monitorIndex == 0) {
            return new Region(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
        return null;
    }
    
    @Override
    public Region getVirtualDesktopBounds() {
        return new Region(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    @Override
    public boolean isAvailable() {
        return true; // Mock is always available
    }
    
    @Override
    public String getImplementationName() {
        return "Mock Screen Capture";
    }
    
    private BufferedImage createMockImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Fill with a light gray color to simulate a screen
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, 0xCCCCCC);
            }
        }
        return image;
    }
}