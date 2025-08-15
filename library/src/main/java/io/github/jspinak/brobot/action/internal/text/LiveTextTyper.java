package io.github.jspinak.brobot.action.internal.text;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Live implementation of text typing for non-test profiles.
 * 
 * Phase 2 Refactoring: Profile-specific live implementation
 * that performs actual keyboard input via SikuliX.
 */
@Component
@Profile("!test")
@Slf4j
public class LiveTextTyper implements TextTyper {
    
    @Override
    public boolean type(StateString stateString, ActionConfig actionConfig) {
        String modifiers = "";
        if (actionConfig instanceof TypeOptions) {
            modifiers = ((TypeOptions) actionConfig).getModifiers();
        }
        
        log.debug("Live typing: '{}' with modifiers: '{}'", 
                  stateString.getString(), modifiers);
        
        try {
            Region region = new Region();
            
            if (modifiers.isEmpty()) {
                // Type without modifiers
                return region.sikuli().type(stateString.getString()) != 0;
            } else {
                // Type with modifiers (e.g., ctrl+a, shift+tab)
                return region.sikuli().type(stateString.getString(), 
                                          modifiers) != 0;
            }
        } catch (Exception e) {
            log.error("Failed to type text: {}", stateString.getString(), e);
            return false;
        }
    }
}