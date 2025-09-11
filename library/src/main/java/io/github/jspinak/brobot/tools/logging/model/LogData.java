package io.github.jspinak.brobot.tools.logging.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a comprehensive log entry in the Brobot automation framework's logging system. This
 * model captures various types of events that occur during automated testing, including user
 * actions, state transitions, performance metrics, and error conditions.
 *
 * <p>The LogData model is designed to be flexible enough to accommodate different types of log
 * entries while maintaining a consistent structure. It uses optional fields to support various
 * logging scenarios without requiring separate models for each type.
 *
 * <p>Key design decisions:
 *
 * <ul>
 *   <li>Uses {@link JsonInclude} annotation to exclude null fields during serialization, reducing
 *       payload size and improving readability
 *   <li>Employs ArrayList as the default List implementation for predictable iteration order and
 *       efficient random access
 *   <li>Includes both state names and IDs to support both human-readable logs and efficient
 *       database queries
 *   <li>Defaults timestamp to the current time for immediate capture of event timing
 * </ul>
 *
 * @see LogEventType for the different types of log entries this model can represent
 * @see StateImageLogData for detailed state image detection results
 * @see ExecutionMetrics for performance-related measurements
 * @see io.github.jspinak.brobot.tools.logging.dto.LogDataDTO for the corresponding DTO used in API
 *     responses
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogData {
    /** Unique identifier for this log entry, typically assigned by the persistence layer. */
    private Long id;

    /**
     * Identifier for the project this log entry belongs to. Defaults to 0L for backward
     * compatibility. Allows multi-project support within the same logging infrastructure.
     */
    private Long projectId = 0L;

    /**
     * Unique identifier for the testing session. All log entries from a single test run share the
     * same sessionId, enabling correlation and analysis of related events.
     */
    private String sessionId;

    /**
     * The type of event being logged. This field is required and determines which optional fields
     * are relevant for this log entry.
     */
    private LogEventType type;

    /**
     * For ACTION type logs, specifies the specific action performed (e.g., "CLICK", "TYPE",
     * "FIND"). Optional field primarily used when type is ACTION.
     */
    private String actionType;

    /**
     * Human-readable description of the logged event. Provides context and details that aren't
     * captured in structured fields.
     */
    private String description;

    /**
     * When this event occurred. Defaults to the current time to ensure accurate timing even if the
     * log entry is created before being persisted.
     */
    private Instant timestamp = Instant.now();

    /**
     * Indicates whether the logged operation completed successfully. For actions: whether the
     * action was performed successfully. For transitions: whether the state transition completed as
     * expected.
     */
    private boolean success;

    /**
     * Duration of the operation in milliseconds. Captures execution time for performance analysis
     * and optimization.
     */
    private long duration;

    // Action-specific fields
    /**
     * Name or identifier of the application being tested. Used for multi-application test scenarios
     * and reporting.
     */
    private String applicationUnderTest;

    /** Detailed description of the action performed, often including target element details. */
    private String actionPerformed;

    /** Error message if the operation failed. Null when success is true. */
    private String errorMessage;

    /**
     * File path to a screenshot captured during or after the event. Used for visual verification
     * and debugging.
     */
    private String screenshotPath;

    /**
     * File path to a video clip of the operation. Typically used for complex actions or failures
     * requiring detailed analysis.
     */
    private String videoClipPath;

    /**
     * The name of the current state when this event occurred. Provides context for understanding
     * the application's state during the operation.
     */
    private String currentStateName;

    // Transition-specific fields
    /**
     * For TRANSITION logs: the name of the state from which the transition originated. This
     * represents the starting point of a state transition.
     */
    private String fromStates;

    /** Database IDs corresponding to fromStates, enabling efficient queries and joins. */
    private List<Long> fromStateIds;

    /**
     * For TRANSITION logs: the target states that the transition is expected to activate. A
     * transition may activate multiple states simultaneously.
     */
    private List<String> toStateNames = new ArrayList<>();

    /** Database IDs corresponding to toStateNames. */
    private List<Long> toStateIds = new ArrayList<>();

    /**
     * The complete set of active states before the transition began. Used for state validation and
     * debugging unexpected state configurations.
     */
    private List<String> beforeStateNames = new ArrayList<>();

    /** Database IDs corresponding to beforeStateNames. */
    private List<Long> beforeStateIds = new ArrayList<>();

    /**
     * The complete set of active states after the transition completed. Comparison with
     * beforeStateNames reveals the actual state changes.
     */
    private List<String> afterStateNames = new ArrayList<>();

    /** Database IDs corresponding to afterStateNames. */
    private List<Long> afterStateIds = new ArrayList<>();

    /**
     * Detailed results of state image detection during this operation. Each entry represents an
     * attempt to find a specific state image.
     */
    private List<StateImageLogData> stateImageLogData = new ArrayList<>();

    /**
     * Performance metrics captured during this operation. Optional field typically populated for
     * operations where performance monitoring is enabled.
     */
    private ExecutionMetrics performance;

    // Constructors
    public LogData() {}

    public LogData(String sessionId, LogEventType logType, String description) {
        this.sessionId = sessionId;
        this.type = logType;
        this.description = description;
    }
}
