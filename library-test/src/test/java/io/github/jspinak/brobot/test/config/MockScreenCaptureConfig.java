package io.github.jspinak.brobot.test.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import io.github.jspinak.brobot.core.services.SikuliScreenCapture;

/**
 * Test configuration that provides a mock SikuliScreenCapture bean to avoid headless environment
 * issues during testing.
 *
 * <p>SikuliScreenCapture tries to initialize screens in its constructor, which fails in headless
 * test environments. This mock prevents that initialization from happening.
 */
@TestConfiguration
public class MockScreenCaptureConfig {

    @Bean
    @Primary
    public SikuliScreenCapture sikuliScreenCapture() {
        // Return a mock that doesn't initialize screens
        // This prevents: org.sikuli.script.SikuliXception: SikuliX: Init: running in headless
        // environment
        return Mockito.mock(SikuliScreenCapture.class);
    }
}
