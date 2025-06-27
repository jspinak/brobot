/**
 * Provides automated state space exploration and coverage verification capabilities.
 * 
 * <p>This package implements the systematic state exploration methodology described
 * in the Brobot paper. It enables comprehensive testing by automatically navigating
 * through all reachable states, verifying visual elements, and tracking coverage
 * metrics to ensure thorough application testing.
 * 
 * <h2>Core Exploration Strategy</h2>
 * <p>The exploration framework implements an intelligent traversal algorithm:
 * <ol>
 *   <li><strong>Adjacent-First</strong>: Prioritizes states directly reachable from
 *       the current position to minimize transitions</li>
 *   <li><strong>Shortest Path</strong>: Uses path-finding to reach distant unvisited
 *       states when no adjacent options exist</li>
 *   <li><strong>Failure Recovery</strong>: Continues exploration despite individual
 *       state failures, tracking unreachable states</li>
 *   <li><strong>Completion Criteria</strong>: Supports both state coverage and
 *       transition coverage goals</li>
 * </ol>
 * 
 * <h2>Key Components</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.exploration.StateTraversalService} - 
 *       Core engine orchestrating state exploration</li>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.exploration.ExplorationOrchestrator} - 
 *       High-level coordinator managing test execution</li>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.exploration.StateExplorationTracker} - 
 *       Tracks and prioritizes unvisited states</li>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.exploration.StateImageValidator} - 
 *       Verifies all visual elements within states</li>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.exploration.ExplorationSessionRunner} - 
 *       Executes individual exploration sessions</li>
 * </ul>
 * 
 * <h2>Data Collection</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.exploration.StateVisit} - 
 *       Records individual state visit attempts</li>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.exploration.TestRun} - 
 *       Captures complete test session metadata</li>
 * </ul>
 * 
 * <h2>Coverage Verification</h2>
 * <p>The framework ensures comprehensive testing through:
 * <ul>
 *   <li><strong>State Coverage</strong>: Verifies all states have been visited</li>
 *   <li><strong>Transition Coverage</strong>: Ensures all transitions are exercised</li>
 *   <li><strong>Image Coverage</strong>: Validates all visual elements are detectable</li>
 *   <li><strong>Failure Analysis</strong>: Identifies unreachable or problematic states</li>
 * </ul>
 * 
 * <h2>Exploration Process</h2>
 * <ol>
 *   <li>Initialize from known starting states</li>
 *   <li>Identify adjacent unvisited states</li>
 *   <li>Navigate to closest unvisited state</li>
 *   <li>Verify state arrival and optionally validate images</li>
 *   <li>Record visit success/failure</li>
 *   <li>Repeat until coverage goals are met</li>
 * </ol>
 * 
 * <h2>Failure Handling</h2>
 * <p>The exploration framework implements robust failure handling:
 * <ul>
 *   <li>Continues after individual state failures</li>
 *   <li>Tracks unreachable states for analysis</li>
 *   <li>Stops after configurable consecutive failures</li>
 *   <li>Provides detailed failure reporting</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Configure exploration parameters
 * ExplorationOrchestrator orchestrator = applicationContext.getBean(
 *     ExplorationOrchestrator.class);
 * 
 * // Run comprehensive state exploration
 * orchestrator.setMaxIterations(5);
 * orchestrator.exploreAllStates();
 * 
 * // Analyze results
 * List<TestRun> testRuns = orchestrator.getTestRuns();
 * Set<Long> unreachableStates = orchestrator.getUnreachableStates();
 * double stateCoverage = orchestrator.getStateCoverage();
 * 
 * // Generate report
 * orchestrator.generateCoverageReport();
 * }</pre>
 * 
 * <h2>Integration Points</h2>
 * <p>The exploration framework integrates with:
 * <ul>
 *   <li>State management for navigation and verification</li>
 *   <li>Adjacent states analysis for efficient traversal</li>
 *   <li>Action logging for comprehensive reporting</li>
 *   <li>Mock framework for rapid testing</li>
 * </ul>
 * 
 * <h2>Benefits</h2>
 * <ul>
 *   <li><strong>Automated</strong>: No manual test path creation needed</li>
 *   <li><strong>Comprehensive</strong>: Ensures complete coverage</li>
 *   <li><strong>Efficient</strong>: Optimized traversal strategy</li>
 *   <li><strong>Trackable</strong>: Detailed metrics and reporting</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.tools.testing.mock
 * @see io.github.jspinak.brobot.stateStructure.navigation
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.testing.exploration;