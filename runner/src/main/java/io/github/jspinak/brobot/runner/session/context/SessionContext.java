package io.github.jspinak.brobot.runner.session.context;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Immutable context object containing all information about a session.
 *
 * <p>This class captures the complete context of a session, including identifiers, timing,
 * configuration, and metadata.
 *
 * @since 1.0.0
 */
@Getter
@Builder
@ToString
public class SessionContext {

    /** Unique identifier for this session */
    @Builder.Default private final String sessionId = UUID.randomUUID().toString();

    /** Human-readable name for the session */
    private final String sessionName;

    /** Project name associated with this session */
    private final String projectName;

    /** Path to configuration file */
    private final String configPath;

    /** Path to image resources */
    private final String imagePath;

    /** Time when the session started */
    @Builder.Default private final LocalDateTime startTime = LocalDateTime.now();

    /** Session options including autosave settings */
    private final SessionOptions options;

    /** Additional metadata for the session */
    private final Map<String, Object> metadata;

    /** Correlation ID for tracing this session across components */
    private final String correlationId;

    /** Check if the session has been active for longer than the specified duration */
    public boolean hasBeenActiveFor(long minutes) {
        return startTime.plusMinutes(minutes).isBefore(LocalDateTime.now());
    }

    /** Get a descriptive label for the session */
    public String getLabel() {
        return sessionName != null ? sessionName : "Session " + sessionId.substring(0, 8);
    }
}
