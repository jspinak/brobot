package io.github.jspinak.brobot.startup;

import io.github.jspinak.brobot.model.state.StateEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for Brobot startup behavior.
 * 
 * <p>Allows declarative configuration of initial state verification
 * through application properties or YAML files.</p>
 * 
 * <p>Example application.yml:
 * <pre>{@code
 * brobot:
 *   startup:
 *     verify-initial-states: true
 *     initial-states:
 *       - HOME
 *       - LOGIN
 *     fallback-search: true
 *     activate-first-only: false
 * }</pre>
 * </p>
 * 
 * @since 1.1.0
 */
@Component
@ConfigurationProperties(prefix = "brobot.startup")
@Getter
@Setter
public class BrobotStartupConfiguration {
    
    /**
     * Whether to automatically verify initial states on startup.
     */
    private boolean verifyInitialStates = false;
    
    /**
     * List of state names to verify at startup.
     */
    private List<String> initialStates = new ArrayList<>();
    
    /**
     * Whether to search all states if specified states are not found.
     */
    private boolean fallbackSearch = false;
    
    /**
     * Whether to activate only the first found state.
     */
    private boolean activateFirstOnly = false;
    
    /**
     * Delay in seconds before initial state verification.
     */
    private int startupDelay = 0;
}