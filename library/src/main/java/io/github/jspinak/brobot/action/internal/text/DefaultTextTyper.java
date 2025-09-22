package io.github.jspinak.brobot.action.internal.text;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateString;

import lombok.extern.slf4j.Slf4j;

/**
 * Primary text typing implementation that handles both mock and live environments. Automatically
 * switches between mock and live typing based on brobotProperties.getCore().isMock().
 */
@Component
@Primary
@Slf4j
public class DefaultTextTyper implements TextTyper {

    private final BrobotProperties brobotProperties;

    @Autowired
    public DefaultTextTyper(BrobotProperties brobotProperties) {
        this.brobotProperties = brobotProperties;
    }

    @Override
    public boolean type(StateString stateString, ActionConfig actionConfig) {
        // Extract modifiers from config if it's TypeOptions
        String modifiers = "";
        if (actionConfig instanceof TypeOptions) {
            modifiers = ((TypeOptions) actionConfig).getModifiers();
        }

        // Handle null or empty string
        String textToType =
                stateString != null && stateString.getString() != null
                        ? stateString.getString()
                        : "";

        if (brobotProperties.getCore().isMock()) {
            return mockType(textToType, modifiers);
        } else {
            return liveType(textToType, modifiers);
        }
    }

    private boolean mockType(String text, String modifiers) {
        log.debug("Mock typing: '{}' with modifiers: '{}'", text, modifiers);

        // Log to console for visibility
        if (modifiers != null && !modifiers.isEmpty()) {}

        // Simulate typing delay
        if (brobotProperties.getMock().getTimeClick() > 0) {
            try {
                Thread.sleep((long) (brobotProperties.getMock().getTimeClick() * 1000));
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
