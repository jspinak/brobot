package io.github.jspinak.brobot.test.mock;

import io.github.jspinak.brobot.BrobotStartupLifecycle;
import io.github.jspinak.brobot.services.Init;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Mock implementation of BrobotStartupLifecycle for tests.
 * This prevents the actual startup lifecycle from running and
 * attempting to initialize SikuliX in headless environments.
 */
@Component
@Primary
public class MockBrobotStartupLifecycle extends BrobotStartupLifecycle {
    
    private boolean running = false;
    
    public MockBrobotStartupLifecycle(Init initService) {
        super(initService);
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