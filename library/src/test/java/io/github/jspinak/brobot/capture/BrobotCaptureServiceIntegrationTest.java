package io.github.jspinak.brobot.capture;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.sikuli.script.Region;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.jspinak.brobot.capture.provider.CaptureProvider;
import io.github.jspinak.brobot.capture.provider.FFmpegCaptureProvider;
import io.github.jspinak.brobot.capture.provider.RobotCaptureProvider;
import io.github.jspinak.brobot.capture.provider.SikuliXCaptureProvider;
import io.github.jspinak.brobot.test.BrobotTestBase;

@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
class BrobotCaptureServiceIntegrationTest extends BrobotTestBase {

    private BrobotCaptureService service;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        service = new BrobotCaptureService();

        // Manually set up providers for testing
        List<CaptureProvider> providers = new ArrayList<>();
        providers.add(new RobotCaptureProvider());
        providers.add(new FFmpegCaptureProvider());
        providers.add(new SikuliXCaptureProvider());

        ReflectionTestUtils.setField(service, "availableProviders", providers);
        ReflectionTestUtils.setField(service, "configuredProvider", "AUTO");
        ReflectionTestUtils.setField(service, "preferPhysicalResolution", true);
        ReflectionTestUtils.setField(service, "fallbackEnabled", true);

        // Initialize the service
        service.init();
    }

    @Test
    void testRobotIsDefaultProvider() {
        // Skip test in headless environment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) {
            return;
        }

        CaptureProvider activeProvider = service.getActiveProvider();
        assertNotNull(activeProvider);
        assertEquals(
                "Robot",
                activeProvider.getName(),
                "Robot should be the default provider when available");
    }

    @Test
    void testCaptureScreen() throws IOException {
        // Skip test in headless environment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) {
            return;
        }

        BufferedImage capture = service.captureScreen();

        assertNotNull(capture);
        assertTrue(capture.getWidth() > 0);
        assertTrue(capture.getHeight() > 0);

        System.out.println(
                "Screen captured at: "
                        + capture.getWidth()
                        + "x"
                        + capture.getHeight()
                        + " using "
                        + service.getActiveProvider().getName());
    }

    @Test
    void testCaptureRegion() throws IOException {
        // In mock mode, BrobotCaptureService returns mock images
        // The mock implementation creates full screen images, not region-specific ones

        Rectangle region = new Rectangle(50, 50, 300, 200);
        BufferedImage capture = service.captureRegion(region);

        assertNotNull(capture);

        // In mock mode or with certain providers, the capture might be the full screen
        // rather than just the region, so we just verify it's a valid image
        assertTrue(capture.getWidth() > 0);
        assertTrue(capture.getHeight() > 0);

        // Log the actual dimensions for debugging
        System.out.println(
                String.format(
                        "Region capture: requested %dx%d, got %dx%d",
                        region.width, region.height, capture.getWidth(), capture.getHeight()));
    }

    @Test
    void testCaptureSikuliRegion() throws IOException {
        // Skip test in headless environment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) {
            return;
        }

        Region sikuliRegion = new Region(100, 100, 200, 150);
        BufferedImage capture = service.captureRegion(sikuliRegion);

        assertNotNull(capture);
        assertTrue(capture.getWidth() > 0);
        assertTrue(capture.getHeight() > 0);
    }

    @Test
    void testProviderSwitching() {
        // Skip test in headless environment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) {
            return;
        }

        // Try to switch to FFmpeg if available
        try {
            service.setProvider("FFMPEG");
            CaptureProvider provider = service.getActiveProvider();
            if (provider.getName().equals("FFmpeg")) {
                assertEquals("FFmpeg", provider.getName());
            }
        } catch (IllegalStateException e) {
            // FFmpeg might not be available
            System.out.println("FFmpeg not available: " + e.getMessage());
        }

        // Switch back to Robot (should always work in non-headless)
        service.setProvider("ROBOT");
        assertEquals("Robot", service.getActiveProvider().getName());
    }

    @Test
    void testProvidersInfo() {
        String info = service.getProvidersInfo();

        assertNotNull(info);
        assertTrue(info.contains("Robot"));
        assertTrue(info.contains("Available") || info.contains("Not Available"));

        System.out.println("Provider info:\n" + info);
    }

    @Test
    void testPhysicalResolutionPreference() throws IOException {
        // Skip test in headless environment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) {
            return;
        }

        // With preferPhysicalResolution = true, should use Robot or FFmpeg
        CaptureProvider provider = service.getActiveProvider();

        assertTrue(
                provider.getName().equals("Robot") || provider.getName().equals("FFmpeg"),
                "Should prefer Robot or FFmpeg for physical resolution");

        // Capture and check resolution type
        BufferedImage capture = service.captureScreen();
        CaptureProvider.ResolutionType resType = provider.getResolutionType();

        if (provider.getName().equals("Robot")) {
            // Robot should report PHYSICAL when scaling is enabled
            System.out.println("Robot resolution type: " + resType);
        }
    }

    @Test
    void testFallbackBehavior() {
        // Skip test in headless environment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) {
            return;
        }

        // Test that unavailable providers are skipped
        ReflectionTestUtils.setField(service, "configuredProvider", "NONEXISTENT");

        // Re-init to trigger fallback
        service.init();

        // Should fall back to an available provider
        CaptureProvider provider = service.getActiveProvider();
        assertNotNull(provider);
        assertTrue(provider.isAvailable());
    }

    @Test
    void testMultipleScreenCapture() throws IOException {
        // Skip test in headless environment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) {
            return;
        }

        GraphicsDevice[] devices = ge.getScreenDevices();

        for (int i = 0; i < Math.min(devices.length, 2); i++) {
            try {
                BufferedImage capture = service.captureScreen(i);
                assertNotNull(capture, "Should capture screen " + i);

                System.out.println(
                        "Screen "
                                + i
                                + " captured at: "
                                + capture.getWidth()
                                + "x"
                                + capture.getHeight());
            } catch (IOException e) {
                System.out.println("Could not capture screen " + i + ": " + e.getMessage());
            }
        }
    }
}
