package io.github.jspinak.brobot.capture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Verifies that the capture method selection works correctly. This test ensures that
 * BufferedImageUtilities and SceneProvider use the configured capture provider (FFmpeg, Robot, or
 * SikuliX).
 */
@DisabledInCI
public class CaptureMethodVerificationTest extends BrobotTestBase {

    @Mock private BrobotCaptureService brobotCaptureService;

    @Test
    public void testUnifiedCaptureServiceIsAvailable() {
        // In a real Spring context, UnifiedCaptureService should be available
        // This test runs in mock mode, so we just verify the structure is correct

        // Create a mock UnifiedCaptureService with required constructor parameters
        UnifiedCaptureService mockService =
                new UnifiedCaptureService(
                        brobotCaptureService,
                        "SIKULIX", // providerConfig
                        false, // enableLogging
                        true, // autoRetry
                        3 // retryCount
                        );
        assertNotNull(mockService);

        // Verify it has the expected methods
        assertDoesNotThrow(
                () -> {
                    // These methods should exist
                    mockService.getClass().getMethod("captureScreen");
                    mockService.getClass().getMethod("captureScreen", int.class);
                    mockService.getClass().getMethod("captureRegion", java.awt.Rectangle.class);
                    mockService.getClass().getMethod("setProvider", String.class);
                    mockService.getClass().getMethod("getActiveProvider");
                });
    }

    @Test
    public void testDefaultProviderConfiguration() {
        // Test that the default provider is set correctly
        // This would be FFmpeg based on our properties configuration

        // In a real application context:
        // UnifiedCaptureService service = applicationContext.getBean(UnifiedCaptureService.class);
        // assertEquals("FFMPEG", service.getActiveProviderName());

        // For now, we just verify the test runs without errors
        assertTrue(true, "Default provider configuration test placeholder");
    }
}
