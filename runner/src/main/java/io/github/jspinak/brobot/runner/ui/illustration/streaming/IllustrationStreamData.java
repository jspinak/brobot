package io.github.jspinak.brobot.runner.ui.illustration.streaming;

import java.time.LocalDateTime;

import org.bytedeco.opencv.opencv_core.Mat;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.tools.history.configuration.IllustrationContext;

import lombok.Builder;
import lombok.Data;

/**
 * Internal data structure for queued illustration stream data.
 *
 * <p>This class holds the raw data from the library before it's processed and converted to
 * JavaFX-compatible formats.
 *
 * @see IllustrationStreamService
 */
@Data
@Builder
public class IllustrationStreamData {

    /** Unique identifier for this stream item. */
    private final String id;

    /** Timestamp when this data was received. */
    private final LocalDateTime timestamp;

    /** The OpenCV Mat containing the illustration image. */
    private final Mat mat;

    /** The action result associated with this illustration. */
    private final ActionResult actionResult;

    /** The illustration context containing additional metadata. */
    private final IllustrationContext context;

    /** Priority for processing (higher priority processed first). */
    @Builder.Default private final int priority = 0;
}
