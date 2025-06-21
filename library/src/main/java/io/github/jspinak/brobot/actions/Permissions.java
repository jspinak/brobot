package io.github.jspinak.brobot.actions;

import org.springframework.stereotype.Component;

/**
 * Controls execution permissions and modes in the Brobot framework.
 * 
 * <p>Permissions acts as a centralized decision point for determining how actions should 
 * be executed based on the current configuration. It interprets global settings to enable 
 * different execution modes, particularly distinguishing between real GUI interaction and 
 * simulated (mock) execution.</p>
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li><b>Mock Mode Detection</b>: Determines when actions should run in simulated mode 
 *       without actual GUI interaction</li>
 *   <li><b>Screenshot Override</b>: Respects screenshot-based testing configurations that 
 *       override mock mode</li>
 *   <li><b>Execution Context</b>: Provides a single source of truth for action execution 
 *       permissions across the framework</li>
 * </ul>
 * </p>
 * 
 * <p>Mock mode behavior:
 * <ul>
 *   <li>Enabled when BrobotSettings.mock is true AND no test screenshots are configured</li>
 *   <li>When screenshots are present, they take precedence over mock mode for testing</li>
 *   <li>Mock mode simulates action execution with configurable timing delays</li>
 *   <li>Useful for development, testing, and demonstrations without GUI access</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Permissions enables the framework to seamlessly switch 
 * between different execution contexts. This flexibility is crucial for supporting various 
 * development and deployment scenarios, from unit testing to production automation, without 
 * requiring code changes in action implementations.</p>
 * 
 * @since 1.0
 * @see BrobotSettings
 * @see ActionInterface
 */
@Component
public class Permissions {

    public boolean isMock() {
        return BrobotSettings.mock && BrobotSettings.screenshots.isEmpty();
    }
}
