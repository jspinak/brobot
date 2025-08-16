package io.github.jspinak.brobot.action.internal.text;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.state.StateString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Hybrid text typer that supports both profile-based and runtime delegation.
 * 
 * Phase 3: Wrapper pattern implementation that enables:
 * - Profile-based dependency injection (preferred)
 * - Runtime delegation fallback (backward compatibility)
 * - Dynamic switching between mock and live modes
 * 
 * This component bridges the old runtime-check architecture with the new
 * profile-based architecture, allowing gradual migration and mixed-mode execution.
 */
@Component
@Primary
@ConditionalOnMissingBean(name = "textTyper")
@Slf4j
public class HybridTextTyper implements TextTyper {
    
    @Autowired(required = false)
    private MockTextTyper mockTyper;
    
    @Autowired(required = false)
    private LiveTextTyper liveTyper;
    
    @Autowired
    private DefaultTextTyper legacyWrapper; // Fallback to V2 implementation
    
    private volatile boolean useLegacyMode = false;
    
    /**
     * Types text using the appropriate implementation based on:
     * 1. Profile-specific beans (if available)
     * 2. Runtime settings (fallback)
     * 3. Legacy wrapper (ultimate fallback)
     */
    @Override
    public boolean type(StateString stateString, ActionConfig actionConfig) {
        // Try profile-based first
        if (mockTyper != null && FrameworkSettings.mock) {
            log.trace("Using profile-based mock typer");
            return mockTyper.type(stateString, actionConfig);
        }
        
        if (liveTyper != null && !FrameworkSettings.mock) {
            log.trace("Using profile-based live typer");
            return liveTyper.type(stateString, actionConfig);
        }
        
        // Check if we should use legacy mode for mixed execution
        if (useLegacyMode || (mockTyper == null && liveTyper == null)) {
            log.trace("Using legacy runtime-check typer");
            // Convert ActionConfig to TypeOptions if possible
            if (actionConfig instanceof TypeOptions) {
                return legacyWrapper.type(stateString, (TypeOptions) actionConfig);
            } else {
                // Fallback to default TypeOptions if not a TypeOptions instance
                return legacyWrapper.type(stateString, new TypeOptions.Builder().build());
            }
        }
        
        // This shouldn't happen, but provide a sensible default
        log.warn("No appropriate typer found, using legacy wrapper");
        if (actionConfig instanceof TypeOptions) {
            return legacyWrapper.type(stateString, (TypeOptions) actionConfig);
        } else {
            return legacyWrapper.type(stateString, new TypeOptions.Builder().build());
        }
    }
    
    /**
     * Enables mixed-mode execution by switching to runtime delegation.
     * This allows changing between mock and live modes within a single session.
     * 
     * @param enabled true to use runtime checks, false for profile-based
     */
    public void setLegacyMode(boolean enabled) {
        this.useLegacyMode = enabled;
        log.info("Text typer legacy mode: {}", enabled ? "ENABLED" : "DISABLED");
    }
    
    /**
     * Switches to mock mode dynamically.
     * Only works when legacy mode is enabled.
     */
    public void switchToMock() {
        if (useLegacyMode) {
            FrameworkSettings.mock = true;
            log.info("Switched to mock typing mode");
        } else {
            log.warn("Cannot switch to mock - legacy mode not enabled");
        }
    }
    
    /**
     * Switches to live mode dynamically.
     * Only works when legacy mode is enabled.
     */
    public void switchToLive() {
        if (useLegacyMode) {
            FrameworkSettings.mock = false;
            log.info("Switched to live typing mode");
        } else {
            log.warn("Cannot switch to live - legacy mode not enabled");
        }
    }
}