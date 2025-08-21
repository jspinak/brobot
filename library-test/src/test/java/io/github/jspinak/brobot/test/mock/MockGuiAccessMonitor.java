package io.github.jspinak.brobot.test.mock;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessConfig;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessMonitor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Mock GUI access monitor for headless test environment.
 * Always reports GUI as accessible to allow tests to run without a display.
 */
@Component
@Primary
@ConditionalOnMissingBean(name = "guiAccessMonitor")
public class MockGuiAccessMonitor extends GuiAccessMonitor {
    
    public MockGuiAccessMonitor(BrobotLogger logger, GuiAccessConfig config) {
        super(logger, config);
    }
    
    @Override
    public boolean checkGuiAccess() {
        // Always return false in test environment (no GUI access in tests)
        return false;
    }
}