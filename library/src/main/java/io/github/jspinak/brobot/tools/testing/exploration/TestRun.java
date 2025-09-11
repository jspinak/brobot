package io.github.jspinak.brobot.tools.testing.exploration;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single test run session with comprehensive metadata for tracking and analysis.
 *
 * <p>TestRun encapsulates all the essential information about a test execution session, serving as
 * a record of what was tested, when it was tested, and the outcomes achieved. This class is
 * fundamental to Brobot's strategy of systematic state exploration and comprehensive application
 * testing.
 *
 * <h2>Testing Philosophy</h2>
 *
 * <p>Brobot's testing strategy aims to achieve complete coverage by:
 *
 * <ul>
 *   <li>Systematically visiting all states in the application
 *   <li>Detecting missing images or broken visual elements
 *   <li>Identifying faulty transitions between states
 *   <li>Recording comprehensive metadata for analysis and debugging
 * </ul>
 *
 * <h2>Single vs Multiple Test Runs</h2>
 *
 * <p>Ideally, a single TestRun should be sufficient to test the entire application. However,
 * practical considerations may require multiple runs:
 *
 * <ul>
 *   <li><b>Single Run</b> - Possible when the state model includes cycles that allow returning to
 *       start states. This requires transitions that may close and reopen the application.
 *   <li><b>Multiple Runs</b> - Necessary when the state graph lacks sufficient cycles to reach all
 *       states from a single starting point. Each run explores different paths through the
 *       application.
 * </ul>
 *
 * <h2>Data Persistence and Analytics</h2>
 *
 * <p>TestRun instances can be persisted to various storage backends:
 *
 * <ul>
 *   <li>NoSQL databases like Elasticsearch for advanced querying and analytics
 *   <li>Time-series databases for performance trending
 *   <li>Traditional databases for structured reporting
 * </ul>
 *
 * <p>Key identifiers for tracking include:
 *
 * <ul>
 *   <li>Test iteration number (from BrobotSettings.testIteration)
 *   <li>Timestamp-based unique identifiers
 *   <li>Session-specific UUIDs
 * </ul>
 *
 * <h2>Recording Strategy</h2>
 *
 * <p>Each TestRun captures:
 *
 * <ul>
 *   <li>Start and end timestamps for duration calculation
 *   <li>Initial and final state sets showing navigation coverage
 *   <li>Video recording filename for visual verification
 *   <li>Descriptive information about the test purpose
 * </ul>
 *
 * <h2>Integration with Test Framework</h2>
 *
 * <p>TestRun works with:
 *
 * <ul>
 *   <li>{@link ExplorationOrchestrator} - Manages multiple test runs
 *   <li>{@link StateTraversalService} - Executes the exploration strategy
 *   <li>{@link ExplorationSessionRunner} - Performs individual test executions
 * </ul>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * // Create a new test run
 * Set<Long> startStates = stateMemory.getActiveStates();
 * TestRun testRun = new TestRun(
 *     "Comprehensive state coverage test",
 *     LocalDateTime.now(),
 *     startStates
 * );
 *
 * // After test execution
 * testRun.setEndTime(LocalDateTime.now());
 * testRun.setEndStates(stateMemory.getActiveStates());
 * testRun.setRecordingFilename("test_run_" + UUID.randomUUID() + ".mp4");
 *
 * // Persist for analysis
 * testRepository.save(testRun);
 * }</pre>
 *
 * @see ExplorationOrchestrator for orchestration of multiple test runs
 * @see StateTraversalService for the exploration implementation
 * @see StateVisit for individual state visit records
 * @author jspinak
 */
@Getter
@Setter
public class TestRun {

    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Set<Long> startStates;
    private Set<Long> endStates;
    private String recordingFilename;

    /**
     * Constructs a new TestRun with initial parameters.
     *
     * <p>Creates a test run instance capturing the starting conditions of the test. The end time,
     * end states, and recording filename should be set after the test completes to provide a
     * complete record of the test execution.
     *
     * <p>Note: Image files and their cloud storage locations should be managed by the backend
     * Spring Boot application, which can associate multiple PNG files with each Image object for
     * different resolutions or variations.
     *
     * @param description a human-readable description of the test purpose or scope. Should clearly
     *     indicate what aspect of the application is being tested.
     * @param startTime the timestamp when the test execution begins. Used for duration calculation
     *     and temporal correlation.
     * @param startStates the set of state IDs that are active at test start. Represents the initial
     *     application state from which exploration begins.
     * @throws NullPointerException if any parameter is null
     */
    public TestRun(String description, LocalDateTime startTime, Set<Long> startStates) {
        this.description = description;
        this.startTime = startTime;
        this.startStates = startStates;
    }
}
