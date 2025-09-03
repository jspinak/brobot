package io.github.jspinak.brobot.action.internal.text;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Primary text typing implementation that handles both mock and live
 * environments.
 * Automatically switches between mock and live typing based on
 * FrameworkSettings.mock.
 */
@Component
@Primary
@Slf4j
public class DefaultTextTyper implements TextTyper {

    @Override
    public boolean type(StateString stateString, ActionConfig actionConfig) {
        // Extract modifiers from config if it's TypeOptions
        String modifiers = "";
        if (actionConfig instanceof TypeOptions) {
            modifiers = ((TypeOptions) actionConfig).getModifiers();
        }

        // Handle null or empty string
        String textToType = stateString != null && stateString.getString() != null
                ? stateString.getString()
                : "";

        if (FrameworkSettings.mock) {
            return mockType(textToType, modifiers);
        } else {
            return liveType(textToType, modifiers);
        }
    }

    private boolean mockType(String text, String modifiers) {
        log.debug("Mock typing: '{}' with modifiers: '{}'", text, modifiers);

        // Log to console for visibility
        if (modifiers != null && !modifiers.isEmpty()) {
            ConsoleReporter.print(modifiers);
        }
        ConsoleReporter.print(text);

        // Simulate typing delay
        if (FrameworkSettings.mockTimeClick > 0) {
            try {
                Thread.sleep((long) (FrameworkSettings.mockTimeClick * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Mock typing interrupted", e);
            }
        }

        return true; // Always successful in mock mode
    }

    private boolean liveType(String text, String modifiers) {
        log.debug("Live typing: '{}' with modifiers: '{}'", text, modifiers);

        try {
            Region region = new Region();

            if (modifiers == null || modifiers.isEmpty()) {
                // Type without modifiers
                return region.sikuli().type(text) != 0;
            } else {
                // Type with modifiers (e.g., ctrl+a, shift+tab)
                return region.sikuli().type(text, modifiers) != 0;
            }
        } catch (Exception e) {
            log.error("Failed to type text: {}", text, e);
            return false;
        }
    }
}