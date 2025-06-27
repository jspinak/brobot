package io.github.jspinak.brobot.tools.testing.exploration;

import org.springframework.stereotype.Component;

/**
 * Manages and orchestrates comprehensive testing of the automation application.
 * 
 * <p>ExplorationOrchestrator serves as the central coordinator for the testing exploration framework,
 * responsible for defining test completion criteria and managing test execution strategies.
 * It provides high-level control over the testing process, determining when comprehensive
 * coverage has been achieved.</p>
 * 
 * <h2>Test Completion Strategies</h2>
 * <p>The manager supports two primary completion criteria:</p>
 * <ol>
 *   <li><b>State Coverage</b> - Test is complete when all states have been visited at least once.
 *       This ensures basic coverage of all application screens/states.</li>
 *   <li><b>Transition Coverage</b> - Test is complete when all possible transitions between
 *       states have been executed. This is more comprehensive as it verifies all navigation
 *       paths and implicitly covers all states.</li>
 * </ol>
 * 
 * <h2>Testing Philosophy</h2>
 * <p>The framework adopts a systematic approach to ensure thorough testing:</p>
 * <ul>
 *   <li>Automated exploration of all reachable states</li>
 *   <li>Verification of state transitions and navigation paths</li>
 *   <li>Detection of broken images or faulty transitions</li>
 *   <li>Comprehensive logging and reporting of test progress</li>
 * </ul>
 * 
 * <h2>Integration with Testing Components</h2>
 * <p>ExplorationOrchestrator coordinates with:</p>
 * <ul>
 *   <li>{@link StateTraversalService} - Executes the state exploration strategy</li>
 *   <li>{@link StateExplorationTracker} - Tracks and prioritizes unvisited states</li>
 *   <li>{@link ExplorationSessionRunner} - Executes individual test scenarios</li>
 *   <li>{@link TestRun} - Records metadata about test execution sessions</li>
 * </ul>
 * 
 * <h2>Future Enhancements</h2>
 * <p>Planned features include:</p>
 * <ul>
 *   <li>Configurable completion criteria selection</li>
 *   <li>Support for partial test runs with checkpointing</li>
 *   <li>Integration with CI/CD pipelines</li>
 *   <li>Advanced reporting and analytics</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Initialize test manager
 * ExplorationOrchestrator explorationOrchestrator = new ExplorationOrchestrator();
 * 
 * // Future API might look like:
 * explorationOrchestrator.setCompletionCriteria(CompletionCriteria.ALL_TRANSITIONS);
 * explorationOrchestrator.startComprehensiveTest();
 * 
 * // Check if testing is complete
 * if (explorationOrchestrator.isTestComplete()) {
 *     Report report = explorationOrchestrator.generateReport();
 * }
 * }</pre>
 * 
 * @see StateTraversalService for the exploration execution
 * @see TestRun for test session metadata
 * @see StateExplorationTracker for coverage tracking
 * @author jspinak
 */
@Component
public class ExplorationOrchestrator {

    /**
     * Tracks the currently active test instance.
     * This can be used to differentiate between multiple test runs
     * and correlate results across different testing sessions.
     */
    private int activeTest = 1;

    /**
     * Increments the active test counter to start a new test session.
     * 
     * <p>This method should be called when initiating a new comprehensive
     * test run to ensure proper tracking and separation of test results.</p>
     * 
     * @return the new active test number
     */
    public int startNewTest() {
        return ++activeTest;
    }

    /**
     * Gets the current active test number.
     * 
     * @return the current test instance identifier
     */
    public int getActiveTest() {
        return activeTest;
    }

    /**
     * Resets the test counter to begin a fresh testing cycle.
     * 
     * <p>Use with caution as this will reset the test numbering sequence.</p>
     */
    public void resetTestCounter() {
        activeTest = 1;
    }
}
