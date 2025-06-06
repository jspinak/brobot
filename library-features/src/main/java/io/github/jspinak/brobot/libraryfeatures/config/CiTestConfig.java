package io.github.jspinak.brobot.libraryfeatures.config;

import io.github.jspinak.brobot.libraryfeatures.captureAndReplay.capture.NativeHookDemo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.mockito.Mockito;

@Configuration
@Profile("ci")
public class CiTestConfig {

    //The NativeHookDemo class is trying to create a JFrame, but GitHub Actions is running in headless mode, which doesn't support GUI components.
    @Bean
    public NativeHookDemo nativeHookDemo() {
        // Return a mock instead of the real implementation
        return Mockito.mock(NativeHookDemo.class);
    }

    // Add other beans that need to be mocked in CI here
}
