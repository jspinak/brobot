package io.github.jspinak.brobot.runner.ui.illustration.streaming;

import java.time.LocalDateTime;
import javafx.scene.image.Image;

import io.github.jspinak.brobot.runner.ui.illustration.IllustrationMetadata;

import lombok.Builder;
import lombok.Data;

/**
 * Event fired when a new illustration is available in the stream.
 *
 * <p>This event is posted to the EventBus and consumed by UI components that display real-time
 * illustration updates.
 *
 * @see IllustrationStreamService
 * @see IllustrationStreamListener
 */
@Data
@Builder
public class IllustrationStreamEvent {

    /** Unique identifier for this stream event. */
    private final String id;

    /** Timestamp when the illustration was created. */
    private final LocalDateTime timestamp;

    /** The JavaFX Image containing the illustration. */
    private final Image image;

    /** Metadata about the illustrated action. */
    private final IllustrationMetadata metadata;

    /** Current size of the stream queue. */
    private final int queueSize;

    /** Processing time in milliseconds. */
    @Builder.Default private final long processingTimeMs = 0;

    /**
     * Checks if this is a high-priority event.
     *
     * @return true if high priority (errors, first occurrences, etc.)
     */
    public boolean isHighPriority() {
        if (metadata == null) return false;

        // High priority for failures
        if (!metadata.isSuccess()) return true;

        // High priority for specific action types
        String actionType = metadata.getActionType();
        return "CLICK".equals(actionType) || "TYPE".equals(actionType);
    }
}
