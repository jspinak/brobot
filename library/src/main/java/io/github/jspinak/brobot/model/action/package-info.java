/**
 * Action execution history and tracking models.
 * 
 * <p>This package provides data structures for recording and analyzing action
 * execution history. These models enable debugging, performance analysis, and
 * behavioral understanding of automation sequences by maintaining detailed
 * records of what actions were performed, when they occurred, and their outcomes.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.action.ActionRecord} - 
 *       Immutable record of a single action execution</li>
 *   <li>{@link io.github.jspinak.brobot.model.action.ActionHistory} - 
 *       Temporal collection of action records with analysis capabilities</li>
 * </ul>
 * 
 * <h2>Action Recording</h2>
 * 
 * <p>Every action execution can be recorded for later analysis:</p>
 * <ul>
 *   <li><b>Action Type</b> - What kind of action was performed</li>
 *   <li><b>Target</b> - What element or location was acted upon</li>
 *   <li><b>Result</b> - Success/failure and any matches found</li>
 *   <li><b>Timing</b> - When it started, ended, and duration</li>
 *   <li><b>Context</b> - Current state and environment</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Recording Actions</h3>
 * <pre>{@code
 * // Create action record from execution
 * ActionRecord record = ActionRecord.builder()
 *     .actionType(ActionType.CLICK)
 *     .target(loginButton)
 *     .startTime(Instant.now())
 *     .duration(Duration.ofMillis(250))
 *     .success(true)
 *     .resultMatches(Arrays.asList(buttonMatch))
 *     .fromState("LoginScreen")
 *     .toState("Dashboard")
 *     .build();
 * 
 * // Add to history
 * actionHistory.add(record);
 * }</pre>
 * 
 * <h3>Analyzing History</h3>
 * <pre>{@code
 * ActionHistory history = automation.getActionHistory();
 * 
 * // Get recent actions
 * List<ActionRecord> recentActions = history.getRecent(10);
 * 
 * // Find all clicks on specific element
 * List<ActionRecord> buttonClicks = history.stream()
 *     .filter(r -> r.getActionType() == ActionType.CLICK)
 *     .filter(r -> r.getTarget().equals(loginButton))
 *     .collect(Collectors.toList());
 * 
 * // Calculate success rate
 * double successRate = history.getSuccessRate();
 * 
 * // Get actions in time range
 * List<ActionRecord> lastMinute = history.getInTimeRange(
 *     Instant.now().minus(1, ChronoUnit.MINUTES),
 *     Instant.now()
 * );
 * }</pre>
 * 
 * <h3>Performance Analysis</h3>
 * <pre>{@code
 * // Find slow actions
 * List<ActionRecord> slowActions = history.stream()
 *     .filter(r -> r.getDuration().compareTo(Duration.ofSeconds(5)) > 0)
 *     .sorted(Comparator.comparing(ActionRecord::getDuration).reversed())
 *     .collect(Collectors.toList());
 * 
 * // Average duration by action type
 * Map<ActionType, Double> avgDurations = history.stream()
 *     .collect(Collectors.groupingBy(
 *         ActionRecord::getActionType,
 *         Collectors.averagingLong(r -> r.getDuration().toMillis())
 *     ));
 * }</pre>
 * 
 * <h2>Action Record Structure</h2>
 * 
 * <p>ActionRecord captures comprehensive execution details:</p>
 * <pre>{@code
 * public class ActionRecord {
 *     // Identification
 *     private final String id;
 *     private final ActionType actionType;
 *     
 *     // Target information
 *     private final StateObject target;
 *     private final Location targetLocation;
 *     
 *     // Execution details
 *     private final Instant startTime;
 *     private final Instant endTime;
 *     private final Duration duration;
 *     
 *     // Results
 *     private final boolean success;
 *     private final List<Match> resultMatches;
 *     private final String errorMessage;
 *     
 *     // State context
 *     private final String fromState;
 *     private final String toState;
 *     
 *     // Additional metadata
 *     private final Map<String, Object> metadata;
 * }
 * }</pre>
 * 
 * <h2>History Management</h2>
 * 
 * <h3>Memory Management</h3>
 * <pre>{@code
 * ActionHistory history = new ActionHistory();
 * 
 * // Set maximum records to keep
 * history.setMaxRecords(1000);
 * 
 * // Set maximum age
 * history.setMaxAge(Duration.ofHours(24));
 * 
 * // Automatic cleanup of old records
 * history.cleanup();
 * }</pre>
 * 
 * <h3>Persistence</h3>
 * <pre>{@code
 * // Save history to file
 * history.saveTo("automation_history.json");
 * 
 * // Load from file
 * ActionHistory loaded = ActionHistory.loadFrom("automation_history.json");
 * 
 * // Export for analysis
 * history.exportToCsv("action_report.csv");
 * }</pre>
 * 
 * <h2>Analysis Capabilities</h2>
 * 
 * <h3>Pattern Detection</h3>
 * <pre>{@code
 * // Find repeated action sequences
 * List<ActionSequence> patterns = history.findRepeatingPatterns(3);
 * 
 * // Detect action loops
 * List<ActionLoop> loops = history.detectLoops();
 * 
 * // Find common failure points
 * Map<StateObject, Long> failuresByTarget = history.stream()
 *     .filter(r -> !r.isSuccess())
 *     .collect(Collectors.groupingBy(
 *         ActionRecord::getTarget,
 *         Collectors.counting()
 *     ));
 * }</pre>
 * 
 * <h3>State Transition Analysis</h3>
 * <pre>{@code
 * // Build transition graph from history
 * Map<String, Map<String, Long>> transitions = history.stream()
 *     .filter(r -> r.getToState() != null)
 *     .collect(Collectors.groupingBy(
 *         ActionRecord::getFromState,
 *         Collectors.groupingBy(
 *             ActionRecord::getToState,
 *             Collectors.counting()
 *         )
 *     ));
 * 
 * // Find most common paths
 * List<StatePath> commonPaths = history.findCommonPaths(5);
 * }</pre>
 * 
 * <h2>Integration Uses</h2>
 * 
 * <h3>Debugging</h3>
 * <pre>{@code
 * // Replay actions that led to failure
 * ActionRecord failure = history.getLastFailure();
 * List<ActionRecord> context = history.getBefore(failure, 10);
 * 
 * for (ActionRecord action : context) {
 *     logger.debug("Action: {} on {} at {}", 
 *         action.getActionType(),
 *         action.getTarget(),
 *         action.getStartTime()
 *     );
 * }
 * }</pre>
 * 
 * <h3>Optimization</h3>
 * <pre>{@code
 * // Identify optimization opportunities
 * public class ActionOptimizer {
 *     public List<Suggestion> analyze(ActionHistory history) {
 *         List<Suggestion> suggestions = new ArrayList<>();
 *         
 *         // Find redundant actions
 *         findRedundantActions(history).forEach(actions -> 
 *             suggestions.add(new Suggestion(
 *                 "Remove redundant actions",
 *                 actions
 *             ))
 *         );
 *         
 *         // Find slow paths
 *         findSlowPaths(history).forEach(path ->
 *             suggestions.add(new Suggestion(
 *                 "Optimize slow path",
 *                 path
 *             ))
 *         );
 *         
 *         return suggestions;
 *     }
 * }
 * }</pre>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 *   <li>Record all significant actions for complete history</li>
 *   <li>Include relevant metadata for better analysis</li>
 *   <li>Set appropriate retention policies to manage memory</li>
 *   <li>Use immutable records to ensure data integrity</li>
 *   <li>Export history periodically for long-term analysis</li>
 *   <li>Include error details for failed actions</li>
 * </ol>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>ActionRecord is immutable and thread-safe. ActionHistory uses
 * concurrent collections for safe multi-threaded access. Builders
 * should be used by single threads only.</p>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.action.ActionResult
 * @see io.github.jspinak.brobot.action.ActionOptions
 * @see io.github.jspinak.brobot.ConsoleReporter.Report
 */
package io.github.jspinak.brobot.model.action;