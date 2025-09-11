package io.github.jspinak.brobot.tools.logging.mapper;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.logging.dto.ExecutionMetricsDTO;
import io.github.jspinak.brobot.tools.logging.dto.LogDataDTO;
import io.github.jspinak.brobot.tools.logging.dto.StateImageLogDTO;
import io.github.jspinak.brobot.tools.logging.model.ExecutionMetrics;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import io.github.jspinak.brobot.tools.logging.model.StateImageLogData;

/**
 * Maps between {@link LogData} domain models and {@link LogDataDTO} data transfer objects.
 *
 * <p>This mapper provides bidirectional conversion between the internal domain model used by the
 * logging system and the DTOs exposed to external consumers. The separation allows the internal
 * model to evolve independently of the external API contract.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li>Convert LogData to LogEntryDTO for external API responses
 *   <li>Convert LogEntryDTO to LogData for processing external requests
 *   <li>Handle null values and defensive copying of collections
 *   <li>Map nested objects (StateImageLogData, PerformanceMetricsData)
 *   <li>Provide default values for missing required fields
 * </ul>
 *
 * <p>Design decisions:
 *
 * <ul>
 *   <li>Creates new ArrayList instances to prevent external modification of internal collections
 *   <li>Handles null LogType by defaulting to ACTION for backward compatibility
 *   <li>Maps nested objects only when present to avoid unnecessary object creation
 *   <li>Uses method references and streams for clean, functional mapping
 * </ul>
 *
 * <p>Thread safety: This mapper is stateless and thread-safe. Multiple threads can safely use the
 * same instance concurrently.
 *
 * @see LogData
 * @see LogDataDTO
 * @see StateImageLogData
 * @see ExecutionMetrics
 */
@Component
public class LogDataMapper {
    /**
     * Converts a {@link LogDataDTO} to a {@link LogData} domain model.
     *
     * <p>This method performs a deep conversion, creating new instances for all collections and
     * nested objects. This ensures that changes to the DTO after conversion don't affect the domain
     * model.
     *
     * <p>Null handling:
     *
     * <ul>
     *   <li>Null DTO fields are set as null in the model
     *   <li>Null collections in the DTO result in empty collections in the model
     *   <li>The LogType enum is parsed from string representation
     * </ul>
     *
     * @param dto The data transfer object to convert. Must not be null.
     * @return A new LogData instance populated from the DTO
     * @throws IllegalArgumentException if the LogType string in the DTO is invalid
     * @throws NullPointerException if dto is null
     */
    public LogData fromDTO(LogDataDTO dto) {
        LogData log = new LogData();

        log.setProjectId(dto.getProjectId());
        log.setSessionId(dto.getSessionId());
        log.setType(LogEventType.valueOf(dto.getType()));
        log.setActionType(dto.getActionType());
        log.setDescription(dto.getDescription());
        log.setTimestamp(dto.getTimestamp());
        log.setSuccess(dto.isSuccess());
        log.setDuration(dto.getDuration());

        // Map action fields
        log.setApplicationUnderTest(dto.getApplicationUnderTest());
        log.setActionPerformed(dto.getActionPerformed());
        log.setErrorMessage(dto.getErrorMessage());
        log.setScreenshotPath(dto.getScreenshotPath());
        log.setVideoClipPath(dto.getVideoClipPath());
        log.setCurrentStateName(dto.getCurrentStateName());

        // Map transition fields
        log.setFromStates(dto.getFromStateName());
        log.setFromStateIds(dto.getFromStateIds());
        log.setToStateNames(new ArrayList<>(dto.getToStateNames()));
        log.setToStateIds(new ArrayList<>(dto.getToStateIds()));
        log.setBeforeStateNames(new ArrayList<>(dto.getBeforeStateNames()));
        log.setBeforeStateIds(new ArrayList<>(dto.getBeforeStateIds()));
        log.setAfterStateNames(new ArrayList<>(dto.getAfterStateNames()));
        log.setAfterStateIds(new ArrayList<>(dto.getAfterStateIds()));

        // Map state image logs
        if (dto.getStateImageLogs() != null) {
            log.setStateImageLogData(
                    dto.getStateImageLogs().stream()
                            .map(this::mapStateImageLog)
                            .collect(Collectors.toList()));
        }

        // Map performance metrics
        if (dto.getPerformance() != null) {
            ExecutionMetrics metrics = new ExecutionMetrics();
            metrics.setActionDuration(dto.getPerformance().getActionDuration());
            metrics.setPageLoadTime(dto.getPerformance().getPageLoadTime());
            metrics.setTransitionTime(dto.getPerformance().getTransitionTime());
            metrics.setTotalTestDuration(dto.getPerformance().getTotalTestDuration());
            log.setPerformance(metrics);
        }

        return log;
    }

    /**
     * Maps a single {@link StateImageLogDTO} to {@link StateImageLogData}.
     *
     * <p>This helper method is used to convert state image log entries when converting from DTO to
     * domain model. It creates a new instance to ensure immutability.
     *
     * @param dto The state image DTO to convert
     * @return A new StateImageLogData instance
     */
    private StateImageLogData mapStateImageLog(StateImageLogDTO dto) {
        StateImageLogData log = new StateImageLogData();
        log.setStateImageId(dto.getStateImageId());
        log.setFound(dto.isFound());
        return log;
    }

    /**
     * Converts a {@link LogData} domain model to a {@link LogDataDTO}.
     *
     * <p>This method performs a deep conversion suitable for external API responses. It handles
     * null values gracefully and provides defaults where necessary for backward compatibility.
     *
     * <p>Special handling:
     *
     * <ul>
     *   <li>Null LogType defaults to "ACTION" to prevent API breaking changes
     *   <li>Collections are defensively copied to prevent external modification
     *   <li>Nested objects are only created when present in the source
     *   <li>The ID field is transferred to support persistence layer requirements
     * </ul>
     *
     * @param log The domain model to convert. Must not be null.
     * @return A new LogEntryDTO instance populated from the domain model
     * @throws NullPointerException if log is null
     */
    public LogDataDTO toDTO(LogData log) {
        LogDataDTO dto = new LogDataDTO();

        dto.setId(log.getId());
        dto.setProjectId(log.getProjectId());
        dto.setSessionId(log.getSessionId());
        if (log.getType() == null) dto.setType(LogEventType.ACTION.toString());
        else dto.setType(log.getType().name());
        dto.setActionType(log.getActionType());
        dto.setDescription(log.getDescription());
        dto.setTimestamp(log.getTimestamp());
        dto.setSuccess(log.isSuccess());
        dto.setDuration(log.getDuration());

        // Map action fields
        dto.setApplicationUnderTest(log.getApplicationUnderTest());
        dto.setActionPerformed(log.getActionPerformed());
        dto.setErrorMessage(log.getErrorMessage());
        dto.setScreenshotPath(log.getScreenshotPath());
        dto.setVideoClipPath(log.getVideoClipPath());
        dto.setCurrentStateName(log.getCurrentStateName());

        // Map transition fields
        dto.setFromStateName(log.getFromStates());
        dto.setFromStateIds(log.getFromStateIds());
        dto.setToStateNames(new ArrayList<>(log.getToStateNames()));
        dto.setToStateIds(new ArrayList<>(log.getToStateIds()));
        dto.setBeforeStateNames(new ArrayList<>(log.getBeforeStateNames()));
        dto.setBeforeStateIds(new ArrayList<>(log.getBeforeStateIds()));
        dto.setAfterStateNames(new ArrayList<>(log.getAfterStateNames()));
        dto.setAfterStateIds(new ArrayList<>(log.getAfterStateIds()));

        // Map state image logs
        if (log.getStateImageLogData() != null) {
            dto.setStateImageLogs(
                    log.getStateImageLogData().stream()
                            .map(this::mapStateImageLogToDTO)
                            .collect(Collectors.toList()));
        }

        // Map performance metrics
        if (log.getPerformance() != null) {
            ExecutionMetricsDTO metrics = new ExecutionMetricsDTO();
            metrics.setActionDuration(log.getPerformance().getActionDuration());
            metrics.setPageLoadTime(log.getPerformance().getPageLoadTime());
            metrics.setTransitionTime(log.getPerformance().getTransitionTime());
            metrics.setTotalTestDuration(log.getPerformance().getTotalTestDuration());
            dto.setPerformance(metrics);
        }

        return dto;
    }

    /**
     * Maps a single {@link StateImageLogData} to {@link StateImageLogDTO}.
     *
     * <p>This helper method is used to convert state image log entries when converting from domain
     * model to DTO. It creates a new instance to ensure the DTO layer cannot modify the domain
     * model.
     *
     * @param log The state image domain model to convert
     * @return A new StateImageLogDTO instance
     */
    private StateImageLogDTO mapStateImageLogToDTO(StateImageLogData log) {
        StateImageLogDTO dto = new StateImageLogDTO();
        dto.setStateImageId(log.getStateImageId());
        dto.setFound(log.isFound());
        return dto;
    }
}
