package io.github.jspinak.brobot.config.environment;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import org.springframework.stereotype.Component;

/**
 * Controls execution permissions and modes in the Brobot framework.
 * 
 * <p>ExecutionMode acts as a centralized decision point for determining how actions should 
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
 * <p>In the model-based approach, ExecutionMode enables the framework to seamlessly switch 
 * between different execution contexts. This flexibility is crucial for supporting various 
 * development and deployment scenarios, from unit testing to production automation, without 
 * requiring code changes in action implementations.</p>
 * 
 * @since 1.0
 * @see FrameworkSettings
 * @see ActionInterface
 */
@Component
public class ExecutionMode {

    /**
     * Determines whether the framework should execute in mock mode.
     * <p>
     * Mock mode is active when both conditions are met:
     * <ol>
     * <li>{@link FrameworkSettings#mock} is set to true</li>
     * <li>No test screenshots are configured ({@link FrameworkSettings#screenshots} is empty)</li>
     * </ol>
     * <p>
     * The presence of test screenshots overrides mock mode because screenshots provide
     * a more realistic testing environment. This allows tests to use actual screen
     * captures while still avoiding live GUI interaction.
     * <p>
     * <strong>Usage context:</strong>
     * <p>
     * Action implementations check this method to determine execution strategy:
     * <pre>{@code
     * if (permissions.isMock()) {
     *     // Simulate action with mock timing
     *     Thread.sleep((long)(BrobotSettings.mockTimeClick * 1000));
     *     return mockResult;
     * } else {
     *     // Perform real GUI interaction
     *     return performRealClick();
     * }
     * }</pre>
     *
     * @return true if mock mode is active and no screenshots are configured;
     *         false if real execution should occur or screenshots are available
     * @see FrameworkSettings#mock
     * @see FrameworkSettings#screenshots
     */
    public boolean isMock() {
        return FrameworkSettings.mock && FrameworkSettings.screenshots.isEmpty();
    }
}
