package io.github.jspinak.brobot.action.internal.text;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Mock implementation of text typing for test profile.
 * 
 * Phase 2 Refactoring: Profile-specific mock implementation
 * that replaces runtime checks in TypeTextWrapper.
 */
@Component
@Profile("test")
@Slf4j
public class MockTextTyper implements TextTyper {
    
    @Override
    public boolean type(StateString stateString, ActionConfig actionConfig) {
        String modifiers = "";
        if (actionConfig instanceof TypeOptions) {
            modifiers = ((TypeOptions) actionConfig).getModifiers();
        }
        
        log.debug("Mock typing: '{}' with modifiers: '{}'", 
                  stateString.getString(), modifiers);
        
        // Log to console for visibility
        ConsoleReporter.print(modifiers);
        ConsoleReporter.print(stateString.getString());
        
        // Simulate typing delay using click time (since there's no specific type time)
        if (FrameworkSettings.mockTimeClick > 0) {
            try {
                Thread.sleep((long)(FrameworkSettings.mockTimeClick * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Mock typing interrupted", e);
            }
        }
        
        return true; // Always successful in mock mode
    }
}