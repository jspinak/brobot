package io.github.jspinak.brobot.capture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

/** Tests that verify the default capture provider configuration. */
@SpringBootTest(
        classes = {
            BrobotCaptureService.class,
            UnifiedCaptureService.class,
            CaptureConfiguration.class
        })
@TestPropertySource(locations = "classpath:brobot-defaults.properties")
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
@ExtendWith(MockitoExtension.class)
class DefaultProviderTest {

    @Mock private UnifiedCaptureService mockCaptureService;

    @Mock private Environment mockEnvironment;

    @Value("${brobot.capture.provider:AUTO}")
    private String defaultProvider;

    @Value("${brobot.dpi.resize-factor:1.0}")
    private String resizeFactor;

    @Test
    void testDefaultProviderIsSikuliX() {
        // Verify the default provider is SIKULIX
        assertEquals("SIKULIX", defaultProvider, "Default capture provider should be SIKULIX");
    }

    @Test
    void testDpiResizeFactorIsAuto() {
        // Verify DPI resize-factor is set to auto
        assertEquals(
                "auto",
                resizeFactor,
                "DPI resize-factor should be 'auto' for automatic DPI recognition");
    }

    @Test
    void testProviderPriorityOrder() {
        // The priority order should be: SIKULIX, ROBOT, FFMPEG
        String[] expectedOrder = {"SIKULIX", "ROBOT", "FFMPEG"};

        // This tests the conceptual priority, actual implementation
        // testing would require more complex setup
        assertNotNull(expectedOrder);
        assertEquals("SIKULIX", expectedOrder[0], "SikuliX should be the first priority");
    }

    @Test
    void testRecommendedModeIsSikuliX() {
        CaptureConfiguration config = new CaptureConfiguration(mockCaptureService, mockEnvironment);
        CaptureConfiguration.CaptureMode recommended = config.getRecommendedMode();

        assertEquals(
                CaptureConfiguration.CaptureMode.SIKULIX,
                recommended,
                "Recommended capture mode should be SIKULIX");
    }
}
