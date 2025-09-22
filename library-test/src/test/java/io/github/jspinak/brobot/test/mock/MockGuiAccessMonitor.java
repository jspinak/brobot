package io.github.jspinak.brobot.test.mock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.environment.HeadlessDetector;
import io.github.jspinak.brobot.logging.BrobotLogger;

// GuiAccessConfig removed
// GuiAccessMonitor removed

/**
 * Mock GUI access monitor for headless test environment. Always reports GUI as accessible to allow
 * tests to run without a display.
 */
@Component
@Primary
@ConditionalOnMissingBean(name = "guiAccessMonitor")
public class MockGuiAccessMonitor extends GuiAccessMonitor {

    public MockGuiAccessMonitor(
            BrobotLogger logger, GuiAccessConfig config, HeadlessDetector headlessDetector) {
        super(logger, config, headlessDetector);
    }

    @Override
    public boolean checkGuiAccess() {
        // Always return false in test environment (no GUI access in tests)
        return false;
    }
}
