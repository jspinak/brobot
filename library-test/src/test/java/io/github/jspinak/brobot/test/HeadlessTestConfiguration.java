package io.github.jspinak.brobot.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration for headless environment testing.
 * Ensures tests can run without display/GUI requirements.
 */
@TestConfiguration
public class HeadlessTestConfiguration {
    
    static {
        // Set headless mode for AWT
        System.setProperty("java.awt.headless", "true");
    }
    
    @Bean
    @Primary
    public String headlessMode() {
        // This bean just serves as a marker that headless config is loaded
        return "headless";
    }
}