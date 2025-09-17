package io.github.jspinak.brobot.util.image.capture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.capture.BrobotCaptureService;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.testing.wrapper.TimeWrapper;
import io.github.jspinak.brobot.util.file.SaveToFile;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;
import io.github.jspinak.brobot.config.core.BrobotProperties;

/**
 * Manages continuous screenshot capture for various analysis and recording purposes.
 *
 * <p>This component provides two capture strategies:
 *
 * <ol>
 *   <li>Fixed-count capture: Takes a specific number of screenshots at regular intervals
 *   <li>Continuous capture: Runs indefinitely with scheduled intervals until stopped
 * </ol>
 *
 * <p>Primary use cases:
 *
 * <ul>
 *   <li><b>State structure building</b>: Capture UI states for automated model creation
 *   <li><b>Remote development</b>: Record UI behavior in different environments
 *   <li><b>Interaction analysis</b>: Study effects of user inputs over time
 *   <li><b>Machine learning</b>: Generate training data with regular time intervals
 *   <li><b>Documentation</b>: Create visual records of application behavior
 * </ul>
 *
 * <p>Time-based capture rationale:
 *
 * <ul>
 *   <li>Regular intervals enable smooth playback and analysis
 *   <li>Avoids screenshot explosion from rapid user interactions
 *   <li>Provides consistent data points for ML algorithms
 *   <li>Simplifies synchronization with input events
 * </ul>
 *
 * <p>Thread safety:
 *
 * <ul>
 *   <li>Uses single-threaded executor for sequential capture
 *   <li>Synchronized methods prevent concurrent start/stop conflicts
 *   <li>Volatile flag for cross-thread visibility (though unused)
 * </ul>
 *
 * <p>Resource management:
 *
 * <ul>
 *   <li>Graceful shutdown with 1-second timeout
 *   <li>Force shutdown if tasks don't complete
 *   <li>Single executor instance (not recreated after shutdown)
 * </ul>
 *
 * @see ScreenshotCapture
 * @see SaveToFile
 * @see TimeWrapper
 * @see BrobotProperties
 */
@Component
public class ScreenshotRecorder {

    @Autowired
    private BrobotProperties brobotProperties;

    private final ImageFileUtilities imageUtils;
    private final TimeWrapper timeWrapper;
    private final BrobotCaptureService captureService;

    /** Flag indicating capture status (currently unused but available for future enhancements). */
    private volatile boolean isCapturing = false;

    /**
     * Single-threaded executor for scheduled screenshot capture. Note: Once shut down, this
     * executor cannot be reused.
     */
    private final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    public ScreenshotRecorder(
            ImageFileUtilities imageUtils, TimeWrapper timeWrapper, BrobotCaptureService captureService) {
        this.imageUtils = imageUtils;
        this.timeWrapper = timeWrapper;
        this.captureService = captureService;
    }

    /**
     * Captures a fixed number of screenshots at regular intervals.
     *
     * <p>This blocking method takes screenshots for the specified duration, waiting between each
     * capture. Screenshots are saved to the path configured in BrobotProperties.
     *
     * <p>Calculation: numberOfScreenshots = secondsToCapture / captureFrequency
     *
     * <p>Example: capture(10, 0.5) takes 20 screenshots over 10 seconds
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Blocks the calling thread for the entire duration
     *   <li>Saves screenshots to BrobotProperties screenshot path
     *   <li>Uses full screen region (new Region())
     * </ul>
     *
     * @param secondsToCapture total duration for capturing screenshots
     * @param captureFrequency interval between captures in seconds (e.g., 0.5 = twice per second)
     */
    public void capture(int secondsToCapture, double captureFrequency) {
        int numberOfScreenshots = (int) (secondsToCapture / captureFrequency);
        for (int i = 0; i < numberOfScreenshots; i++) {
            timeWrapper.wait(captureFrequency);
            // Capture full screen (empty Region = full screen)
            imageUtils.saveRegionToFile(
                    new Region(),
                    brobotProperties.getScreenshot().getPath() + brobotProperties.getScreenshot().getFilename());
        }
    }

    /**
     * Starts continuous screenshot capture at fixed time intervals.
     *
     * <p>Initiates a scheduled task that captures screenshots indefinitely until stopCapturing() is
     * called. Uses time-based intervals rather than event-based triggers for several important
     * reasons:
     *
     * <p>Design rationale for time-based capture:
     *
     * <ol>
     *   <li><b>Prevents screenshot explosion</b>: Rapid user interactions (mouse movements, key
     *       presses) could generate excessive screenshots
     *   <li><b>Enables smooth playback</b>: Regular intervals allow consistent playback speed,
     *       including slow-motion analysis
     *   <li><b>ML compatibility</b>: Provides regularized input/output pairs where screenshots
     *       (inputs) map to user actions (outputs) within fixed time windows
     *   <li><b>Simplifies synchronization</b>: Fixed intervals make it easier to correlate
     *       screenshots with logged events
     * </ol>
     *
     * <p>Thread safety: Synchronized to prevent concurrent starts. However, note that the executor
     * cannot be restarted after shutdown.
     *
     * <p>Warning: The SCHEDULER field is final and shutdown is permanent. Multiple start/stop
     * cycles will not work with current implementation.
     *
     * @param saveToFile file saving implementation for captured screenshots
     * @param baseFilename base name for screenshot files (timestamp will be appended)
     * @param delayInMilliseconds interval between captures in milliseconds
     */
    public synchronized void startCapturing(
            SaveToFile saveToFile, String baseFilename, int delayInMilliseconds) {
        ScreenshotCapture captureScreenshot = new ScreenshotCapture(saveToFile, captureService);
        SCHEDULER.scheduleAtFixedRate(
                (() -> {
                    captureScreenshot.saveScreenshotWithDate(baseFilename);
                }),
                0,
                delayInMilliseconds,
                MILLISECONDS);
    }

    /**
     * Stops the continuous screenshot capture.
     *
     * <p>Gracefully shuts down the capture scheduler with a 1-second timeout. If tasks don't
     * complete within the timeout, forces immediate shutdown.
     *
     * <p>Shutdown sequence:
     *
     * <ol>
     *   <li>Initiates orderly shutdown (no new tasks accepted)
     *   <li>Waits up to 1 second for existing tasks to complete
     *   <li>Forces shutdown if timeout exceeded
     *   <li>Handles interruption by forcing immediate shutdown
     * </ol>
     *
     * <p>Important: Once stopped, the capture cannot be restarted with the current implementation
     * as the executor is permanently shut down.
     *
     * <p>Thread safety: Synchronized to coordinate with startCapturing().
     */
    public synchronized void stopCapturing() {
        try {
            SCHEDULER.shutdown();
            if (!SCHEDULER.awaitTermination(1, TimeUnit.SECONDS)) {
                SCHEDULER.shutdownNow();
            }
        } catch (InterruptedException e) {
            SCHEDULER.shutdownNow();
        }
    }
}
