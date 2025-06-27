/**
 * Provides mock implementations of GUI actions for deterministic testing.
 * 
 * <p>This package contains mock versions of all Brobot actions (find, click, drag,
 * type, etc.) that execute without actual GUI interaction. These mocks use historical
 * match data and configured behaviors to provide realistic but deterministic results,
 * enabling rapid and reliable automated testing.
 * 
 * <h2>Core Mock Actions</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.mock.action.MockFind} - 
 *       Returns snapshots from Match History based on current state</li>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.mock.action.MockDrag} - 
 *       Simulates drag operations with configurable delays</li>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.mock.action.MockText} - 
 *       Provides mock text extraction using snapshot data</li>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.mock.action.MockHistogram} - 
 *       Generates simulated histogram matches</li>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.mock.action.MockColor} - 
 *       Creates synthetic scenes for color-based operations</li>
 * </ul>
 * 
 * <h2>Execution Control</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.mock.action.ExecutionModeController} - 
 *       Central coordinator for switching between mock and live execution</li>
 * </ul>
 * 
 * <h2>Mock Find Strategy</h2>
 * <p>MockFind is the most sophisticated mock action, implementing:
 * <ul>
 *   <li>Snapshot retrieval from Match History</li>
 *   <li>State-aware match selection</li>
 *   <li>Configurable success probabilities</li>
 *   <li>Support for both images and regions</li>
 * </ul>
 * 
 * <h2>Deterministic Behavior</h2>
 * <p>All mock actions ensure deterministic execution by:
 * <ul>
 *   <li>Using stored data rather than live screenshots</li>
 *   <li>Applying configured probabilities consistently</li>
 *   <li>Simulating delays without actual waiting</li>
 *   <li>Returning predictable results for same inputs</li>
 * </ul>
 * 
 * <h2>Configuration Options</h2>
 * <p>Mock behavior can be controlled through:
 * <ul>
 *   <li>Success probabilities per state</li>
 *   <li>Action duration settings</li>
 *   <li>Match score thresholds</li>
 *   <li>Failure simulation patterns</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Configure mock mode
 * ExecutionModeController.setMockTrue();
 * 
 * // Mock find operation
 * MockFind mockFind = context.getBean(MockFind.class);
 * Matches matches = mockFind.findSnapshots(stateImage);
 * 
 * // Mock drag operation
 * MockDrag mockDrag = context.getBean(MockDrag.class);
 * mockDrag.drag(fromLocation, toLocation);
 * 
 * // Mock text extraction
 * MockText mockText = context.getBean(MockText.class);
 * String text = mockText.getText(region);
 * }</pre>
 * 
 * <h2>Integration with Actions</h2>
 * <p>Mock actions integrate seamlessly with the action framework:
 * <ul>
 *   <li>Actions check ExecutionModeController for mock/live mode</li>
 *   <li>In mock mode, actions delegate to appropriate mock implementation</li>
 *   <li>Mock results are formatted identically to live results</li>
 *   <li>Action chains work unchanged in mock mode</li>
 * </ul>
 * 
 * <h2>Benefits for Testing</h2>
 * <ul>
 *   <li><strong>Speed</strong>: No GUI interaction delays</li>
 *   <li><strong>Reliability</strong>: Consistent results eliminate flakiness</li>
 *   <li><strong>Coverage</strong>: Can test all paths including failures</li>
 *   <li><strong>Debugging</strong>: Easier to isolate issues without GUI complexity</li>
 * </ul>
 * 
 * <h2>Limitations</h2>
 * <p>Mock actions have some inherent limitations:
 * <ul>
 *   <li>Require pre-existing snapshots in Match History</li>
 *   <li>Cannot detect new UI changes</li>
 *   <li>May not catch timing-dependent issues</li>
 *   <li>Should be supplemented with live validation tests</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action
 * @see io.github.jspinak.brobot.matchHistory
 * @see io.github.jspinak.brobot.tools.testing.mock.state
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.testing.mock.action;