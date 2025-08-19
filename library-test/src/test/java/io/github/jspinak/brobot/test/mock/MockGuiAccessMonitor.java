package io.github.jspinak.brobot.test.mock;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessConfig;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessMonitor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Mock GUI access monitor for headless test environment.
 * Always reports GUI as accessible to allow tests to run without a display.
 */
@Component
@Primary
public class MockGuiAccessMonitor extends GuiAccessMonitor {
    
    public MockGuiAccessMonitor(BrobotLogger logger, GuiAccessConfig config) {
        super(logger, config);
    }
    
    @Override
    public boolean checkGuiAccess() {
        // Always return true in test environment
        return true;
    }
}