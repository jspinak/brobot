package io.github.jspinak.brobot.tools.logging.dto;

import java.time.Instant;
import java.util.List;

import lombok.Data;

/**
 * Data Transfer Object for log entries in the Brobot automation framework. This DTO provides a
 * simplified, serialization-friendly representation of {@link
 * io.github.jspinak.brobot.tools.logging.model.LogData} for use in API responses, client-server
 * communication, and external integrations.
 *
 * <h3>Key Differences from LogData Model:</h3>
 *
 * <ul>
 *   <li><b>Type representation:</b> Uses String for LogType instead of the enum, providing
 *       flexibility for clients that may not have access to the enum definition
 *   <li><b>No default values:</b> Unlike the model, DTOs don't initialize fields with default
 *       values, allowing null checks to determine if data was provided
 *   <li><b>Simplified annotations:</b> Uses only {@code @Data} for a clean transfer object without
 *       Jackson-specific serialization hints
 *   <li><b>Decoupled from domain logic:</b> Pure data container without business logic or
 *       framework-specific dependencies
 * </ul>
 *
 * <h3>Why Both Model and DTO Exist:</h3>
 *
 * <p>The separation follows the DTO pattern to:
 *
 * <ul>
 *   <li>Provide a stable API contract independent of internal model changes
 *   <li>Enable different serialization strategies for different contexts
 *   <li>Protect internal domain models from external API constraints
 *   <li>Support versioning and backward compatibility in APIs
 * </ul>
 *
 * @see io.github.jspinak.brobot.tools.logging.model.LogData for the corresponding domain model
 * @see StateImageLogDTO for nested state image detection results
 * @see ExecutionMetricsDTO for nested performance metrics
 */
@Data
public class LogDataDTO {
    private Long id;
    private Long projectId;
    private String sessionId;

    /**
     * String representation of the LogType enum value. Using String instead of enum provides
     * flexibility for clients and avoids coupling to the specific enum implementation.
     */
    private String type;

    private String actionType;
    private String description;
    private Instant timestamp;
    private boolean success;
    private long duration;
    private String applicationUnderTest;
    private String actionPerformed;
    private String errorMessage;
    private String screenshotPath;
    private String videoClipPath;
    private String currentStateName;

    /**
     * Note: Field name differs from model (fromStates vs fromStateName). This maintains API
     * compatibility while the model uses a more accurate plural form.
     */
    private String fromStateName;

    private List<Long> fromStateIds;
    private List<String> toStateNames;
    private List<Long> toStateIds;
    private List<String> beforeStateNames;
    private List<Long> beforeStateIds;
    private List<String> afterStateNames;
    private List<Long> afterStateIds;

    /**
     * Note: Field name differs from model (stateImageLogData vs stateImageLogs). The DTO uses a
     * simpler, more conventional name for API clarity.
     */
    private List<StateImageLogDTO> stateImageLogs;

    private ExecutionMetricsDTO performance;
}
