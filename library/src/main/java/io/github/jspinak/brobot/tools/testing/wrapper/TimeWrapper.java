package io.github.jspinak.brobot.tools.testing.wrapper;

import io.github.jspinak.brobot.config.ExecutionMode;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.tools.testing.mock.time.MockTime;
import io.github.jspinak.brobot.model.element.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Wrapper for Time operations that routes to mock or live implementation.
 * 
 * This wrapper provides a stable API for time-related operations while
 * allowing the underlying implementation to switch between mock and live modes.
 */
@Slf4j
@Component
public class TimeWrapper {
    
    private final ExecutionMode executionMode;
    private final MockTime mockTime;
    
    public TimeWrapper(ExecutionMode executionMode, MockTime mockTime) {
        this.executionMode = executionMode;
        this.mockTime = mockTime;
    }
    
    /**
     * Returns the current time, using either simulated or real system time.
     */
    public LocalDateTime now() {
        if (executionMode.isMock()) {
            return mockTime.now();
        }
        return LocalDateTime.now();
    }
    
    /**
     * Pauses execution for the specified duration, using mock or real waiting.
     */
    public void wait(double seconds) {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        if (executionMode.isMock() || env.shouldSkipSikuliX()) {
            mockTime.wait(seconds);
        } else {
            try {
                org.sikuli.script.Region sikuliRegion = new Region().sikuli();
                if (sikuliRegion != null) {
                    sikuliRegion.wait(seconds);
                } else {
                    // Fallback to Thread.sleep when SikuliX not available
                    try {
                        Thread.sleep((long) (seconds * 1000));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (org.sikuli.script.SikuliXception e) {
                // SikuliX failed (likely headless environment) - fallback to thread sleep
                log.warn("SikuliX failed in wait operation ({}), falling back to Thread.sleep", e.getMessage());
                try {
                    Thread.sleep((long) (seconds * 1000));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}