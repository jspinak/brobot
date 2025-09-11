package io.github.jspinak.brobot.tools.logging.dto;

import lombok.Data;

/**
 * Data Transfer Object for performance metrics in the Brobot automation framework. This DTO
 * provides a client-friendly representation of {@link
 * io.github.jspinak.brobot.tools.logging.model.ExecutionMetrics} for use in API responses,
 * reporting, and performance analysis tools.
 *
 * <p>Like its model counterpart, all time values are in milliseconds, providing a consistent unit
 * of measurement across the API. This standardization simplifies client-side calculations and
 * visualizations.
 *
 * <h3>Key Differences from ExecutionMetrics Model:</h3>
 *
 * <ul>
 *   <li><b>No toString() override:</b> DTOs rely on Lombok's generated toString() for consistency
 *       and maintainability
 *   <li><b>Pure data container:</b> No business logic or custom formatting, leaving presentation
 *       concerns to the client
 * </ul>
 *
 * <h3>Usage in Performance Analysis:</h3>
 *
 * <p>These metrics support various performance analysis scenarios:
 *
 * <ul>
 *   <li><b>Bottleneck identification:</b> Compare actionDuration across different operations
 *   <li><b>Load time monitoring:</b> Track pageLoadTime trends over time
 *   <li><b>Transition optimization:</b> Analyze transitionTime to improve state detection
 *   <li><b>End-to-end performance:</b> Use totalTestDuration for overall test efficiency
 * </ul>
 *
 * @see io.github.jspinak.brobot.tools.logging.model.ExecutionMetrics for the corresponding model
 * @see LogDataDTO for the parent DTO that may contain these metrics
 */
@Data
public class ExecutionMetricsDTO {
    /** Time taken to execute a specific action in milliseconds. */
    private long actionDuration;

    /**
     * Time taken for page/screen content to load in milliseconds. May be 0 if not applicable to the
     * operation.
     */
    private long pageLoadTime;

    /** Time taken to complete a state transition in milliseconds. */
    private long transitionTime;

    /** Total time for the entire operation or test in milliseconds. */
    private long totalTestDuration;
}
