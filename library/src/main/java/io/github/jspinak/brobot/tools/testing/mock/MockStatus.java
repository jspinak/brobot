package io.github.jspinak.brobot.tools.testing.mock;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.testing.mock.action.MockFind;
import io.github.jspinak.brobot.tools.testing.mock.state.MockStateManagement;
import io.github.jspinak.brobot.tools.testing.mock.time.MockTime;

/**
 * Tracks mock operation counts for controlled testing scenarios.
 * 
 * <p>MockStatus provides a simple counter mechanism to monitor the number of mock 
 * operations performed during test execution. This is essential for creating 
 * deterministic test scenarios with bounded execution, preventing infinite loops 
 * in mock environments, and ensuring tests complete within reasonable limits. 
 * The counter enables tests to exit gracefully after a predetermined number 
 * of mock operations.</p>
 * 
 * <p>Key capabilities:
 * <ul>
 *   <li><b>Operation Counting</b>: Tracks total mock operations performed</li>
 *   <li><b>Test Boundaries</b>: Enables setting maximum operation limits</li>
 *   <li><b>Progress Monitoring</b>: Allows tests to check execution progress</li>
 *   <li><b>Deterministic Exit</b>: Provides controlled test termination</li>
 *   <li><b>Global State</b>: Singleton pattern for cross-component access</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Limiting test execution to prevent infinite loops</li>
 *   <li>Creating reproducible test scenarios with fixed operation counts</li>
 *   <li>Performance testing with specific operation quotas</li>
 *   <li>Debugging by stopping at specific operation counts</li>
 *   <li>Resource management in long-running mock tests</li>
 * </ul>
 * </p>
 * 
 * <p>Integration patterns:
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
 * </p>
 * 
 * <p>Benefits for testing:
 * <ul>
 *   <li>Prevents runaway tests consuming excessive resources</li>
 *   <li>Ensures tests complete in reasonable time</li>
 *   <li>Enables progress reporting during long tests</li>
 *   <li>Facilitates debugging by limiting execution scope</li>
 *   <li>Supports test timeout mechanisms</li>
 * </ul>
 * </p>
 * 
 * <p>Typical operation types counted:
 * <ul>
 *   <li>Mock find operations</li>
 *   <li>Simulated mouse clicks</li>
 *   <li>Keyboard input simulations</li>
 *   <li>State transitions</li>
 *   <li>Wait operations</li>
 * </ul>
 * </p>
 * 
 * <p>Thread safety considerations:
 * <ul>
 *   <li>Current implementation is not thread-safe</li>
 *   <li>Suitable for single-threaded test execution</li>
 *   <li>For concurrent tests, synchronization would be needed</li>
 *   <li>Spring singleton ensures single instance per context</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based testing approach, MockStatus helps ensure that automated 
 * tests exploring the state space remain bounded and predictable. This is crucial 
 * when testing complex state machines where the number of possible paths could 
 * be very large or infinite without proper constraints.</p>
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
