/**
 * Provides comprehensive testing framework for automated GUI testing with Brobot.
 * 
 * <p>This package implements the sophisticated testing methodology described in the
 * Brobot paper, offering both mock-based and live testing capabilities. The framework
 * enables systematic exploration of application state spaces, comprehensive coverage
 * verification, and deterministic test execution through advanced mocking strategies.
 * 
 * <h2>Testing Philosophy</h2>
 * <p>The Brobot testing framework embodies several key principles:
 * <ul>
 *   <li><strong>Model-Based Testing</strong>: Tests are driven by the state model,
 *       ensuring systematic coverage of all states and transitions</li>
 *   <li><strong>Dual-Mode Execution</strong>: Seamless switching between mock and
 *       live environments enables both rapid testing and real validation</li>
 *   <li><strong>Deterministic Behavior</strong>: Mock mode provides reproducible
 *       results using historical match data (snapshots)</li>
 *   <li><strong>Comprehensive Coverage</strong>: Automated exploration ensures all
 *       reachable states and visual elements are tested</li>
 * </ul>
 * 
 * <h2>Architecture Overview</h2>
 * <p>The testing framework is organized into two main subpackages:
 * <ul>
 *   <li><strong>exploration</strong> - State space exploration and coverage verification</li>
 *   <li><strong>mock</strong> - Mock implementations for deterministic testing</li>
 * </ul>
 * 
 * <h2>Mock Testing Approach</h2>
 * <p>The mock framework enables:
 * <ul>
 *   <li>Rapid test execution without GUI interaction delays</li>
 *   <li>Deterministic results using historical match data</li>
 *   <li>Controlled failure scenarios for robustness testing</li>
 *   <li>Time manipulation for testing time-dependent behaviors</li>
 * </ul>
 * 
 * <h2>State Exploration</h2>
 * <p>The exploration framework provides:
 * <ul>
 *   <li>Systematic traversal of all application states</li>
 *   <li>Adjacent-first navigation strategy for efficiency</li>
 *   <li>Image verification within each state</li>
 *   <li>Detailed coverage reporting and analysis</li>
 * </ul>
 * 
 * <h2>Key Benefits</h2>
 * <ul>
 *   <li><strong>Speed</strong>: Mock tests execute orders of magnitude faster than live tests</li>
 *   <li><strong>Reliability</strong>: Deterministic execution eliminates flaky tests</li>
 *   <li><strong>Coverage</strong>: Automated exploration ensures comprehensive testing</li>
 *   <li><strong>Debugging</strong>: Detailed logging and state tracking aid troubleshooting</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Configure for mock testing
 * ExecutionModeController.setMockTrue();
 * 
 * // Run comprehensive state exploration
 * ExplorationOrchestrator orchestrator = new ExplorationOrchestrator(...);
 * orchestrator.exploreAllStates();
 * 
 * // Analyze results
 * Set<Long> visitedStates = orchestrator.getVisitedStates();
 * double coverage = orchestrator.getStateCoverage();
 * }</pre>
 * 
 * <h2>Integration with Brobot Framework</h2>
 * <p>The testing framework integrates seamlessly with:
 * <ul>
 *   <li>State management system for navigation and verification</li>
 *   <li>Action execution framework for GUI interactions</li>
 *   <li>Match history for snapshot-based testing</li>
 *   <li>Logging system for comprehensive test reporting</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.tools.testing.exploration
 * @see io.github.jspinak.brobot.tools.testing.mock
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.testing;