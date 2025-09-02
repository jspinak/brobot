package io.github.jspinak.brobot.test.config;

import io.github.jspinak.brobot.config.BrobotConfig;
import io.github.jspinak.brobot.config.MockModeManager;
import io.github.jspinak.brobot.core.services.ScreenCaptureService;
import io.github.jspinak.brobot.test.TestLoggingConfig;
import io.github.jspinak.brobot.test.mock.MockBrobotLoggerConfig;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.awt.image.BufferedImage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Minimal integration test configuration that avoids bean conflicts.
 * This configuration provides only the essential beans needed for integration tests.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@ComponentScan(
    basePackages = "io.github.jspinak.brobot",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TestApplication"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TestConfiguration"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*MinimalTestConfig"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*BrobotTestConfiguration")
    }
)
@Import({
    BrobotConfig.class,
    TestLoggingConfig.class,
    MockBrobotLoggerConfig.class
})
public class IntegrationTestConfig {
    
    static {
        // Enable mock mode before Spring context loads
        MockModeManager.setMockMode(true);
        System.setProperty("java.awt.headless", "true");
        System.setProperty("brobot.core.mock", "true");
        System.setProperty("brobot.mock.enabled", "true");
        System.setProperty("brobot.framework.mock", "true");
    }
    
    /**
     * Mock ScreenCaptureService for tests
     */
    @Bean
    @Primary
    public ScreenCaptureService mockScreenCaptureService() {
        ScreenCaptureService service = mock(ScreenCaptureService.class);
        BufferedImage mockImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        when(service.captureScreen()).thenReturn(mockImage);
        when(service.captureRegion(org.mockito.ArgumentMatchers.any())).thenReturn(mockImage);
        return service;
    }
}