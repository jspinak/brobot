package io.github.jspinak.brobot.test.mock;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.config.core.FrameworkInitializer;
import io.github.jspinak.brobot.startup.orchestration.FrameworkLifecycleManager;

/**
 * Mock implementation of BrobotStartupLifecycle for tests. This prevents the actual startup
 * lifecycle from running and attempting to initialize SikuliX in headless environments.
 */
@Component
@Primary
public class MockBrobotStartupLifecycle extends FrameworkLifecycleManager {

    private boolean running = false;

    public MockBrobotStartupLifecycle(FrameworkInitializer initService) {
        super(initService, new BrobotProperties());
    }

    @Override
    public void start() {
        // Skip SikuliX initialization in tests
        System.out.println("Mock Brobot startup lifecycle - skipping SikuliX initialization");
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
