package io.github.jspinak.brobot.tools.testing.mock;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.testing.mock.action.MockFind;
import io.github.jspinak.brobot.tools.testing.mock.state.MockStateManagement;
import io.github.jspinak.brobot.tools.testing.mock.time.MockTime;

/**
 * Tracks mock operation counts for controlled testing scenarios.
 *
 * <p>MockStatus provides a simple counter mechanism to monitor the number of mock operations
 * performed during test execution. This is essential for creating deterministic test scenarios with
 * bounded execution, preventing infinite loops in mock environments, and ensuring tests complete
 * within reasonable limits. The counter enables tests to exit gracefully after a predetermined
 * number of mock operations.
 *
 * <p>Key capabilities:
 *
 * <ul>
 *   <li><b>Operation Counting</b>: Tracks total mock operations performed
 *   <li><b>Test Boundaries</b>: Enables setting maximum operation limits
 *   <li><b>Progress Monitoring</b>: Allows tests to check execution progress
 *   <li><b>Deterministic Exit</b>: Provides controlled test termination
 *   <li><b>Global State</b>: Singleton pattern for cross-component access
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Limiting test execution to prevent infinite loops
 *   <li>Creating reproducible test scenarios with fixed operation counts
 *   <li>Performance testing with specific operation quotas
 *   <li>Debugging by stopping at specific operation counts
 *   <li>Resource management in long-running mock tests
 * </ul>
 *
 * <p>Integration patterns:
 *
 * <pre>
 * // In test setup
 * int maxOperations = 1000;
 *
 * // In mock operations
 * mockStatus.addMockPerformed();
 * if (mockStatus.getMocksPerformed() >= maxOperations) {
 *     throw new TestLimitExceededException();
 * }
 * </pre>
 *
 * <p>Benefits for testing:
 *
 * <ul>
 *   <li>Prevents runaway tests consuming excessive resources
 *   <li>Ensures tests complete in reasonable time
 *   <li>Enables progress reporting during long tests
 *   <li>Facilitates debugging by limiting execution scope
 *   <li>Supports test timeout mechanisms
 * </ul>
 *
 * <p>Typical operation types counted:
 *
 * <ul>
 *   <li>Mock find operations
 *   <li>Simulated mouse clicks
 *   <li>Keyboard input simulations
 *   <li>State transitions
 *   <li>Wait operations
 * </ul>
 *
 * <p>Thread safety considerations:
 *
 * <ul>
 *   <li>Current implementation is not thread-safe
 *   <li>Suitable for single-threaded test execution
 *   <li>For concurrent tests, synchronization would be needed
 *   <li>Spring singleton ensures single instance per context
 * </ul>
 *
 * <p>In the model-based testing approach, MockStatus helps ensure that automated tests exploring
 * the state space remain bounded and predictable. This is crucial when testing complex state
 * machines where the number of possible paths could be very large or infinite without proper
 * constraints.
 *
 * @since 1.0
 * @see MockTime
 * @see MockFind
 * @see MockStateManagement
 */
@Component
public class MockStatus {

    private int mocksPerformed = 0;

    public void addMockPerformed() {
        mocksPerformed++;
    }

    public int getMocksPerformed() {
        return mocksPerformed;
    }
}
