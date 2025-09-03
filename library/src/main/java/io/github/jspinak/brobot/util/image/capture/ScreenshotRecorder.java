package io.github.jspinak.brobot.util.image.capture;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import io.github.jspinak.brobot.util.file.SaveToFile;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;

import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Manages continuous screenshot capture for various analysis and recording
 * purposes.
 * <p>
 * This component provides two capture strategies:
 * <ol>
 * <li>Fixed-count capture: Takes a specific number of screenshots at regular
 * intervals</li>
 * <li>Continuous capture: Runs indefinitely with scheduled intervals until
 * stopped</li>
 * </ol>
 * <p>
 * Primary use cases:
 * <ul>
 * <li><b>State structure building</b>: Capture UI states for automated model
 * creation</li>
 * <li><b>Remote development</b>: Record UI behavior in different
 * environments</li>
 * <li><b>Interaction analysis</b>: Study effects of user inputs over time</li>
 * <li><b>Machine learning</b>: Generate training data with regular time
 * intervals</li>
 * <li><b>Documentation</b>: Create visual records of application behavior</li>
 * </ul>
 * <p>
 * Time-based capture rationale:
 * <ul>
 * <li>Regular intervals enable smooth playback and analysis</li>
 * <li>Avoids screenshot explosion from rapid user interactions</li>
 * <li>Provides consistent data points for ML algorithms</li>
 * <li>Simplifies synchronization with input events</li>
 * </ul>
 * <p>
 * Thread safety:
 * <ul>
 * <li>Uses single-threaded executor for sequential capture</li>
 * <li>Synchronized methods prevent concurrent start/stop conflicts</li>
 * <li>Volatile flag for cross-thread visibility (though unused)</li>
 * </ul>
 * <p>
 * Resource management:
 * <ul>
 * <li>Graceful shutdown with 1-second timeout</li>
 * <li>Force shutdown if tasks don't complete</li>
 * <li>Single executor instance (not recreated after shutdown)</li>
 * </ul>
 *
 * @see ScreenshotCapture
 * @see SaveToFile
 * @see TimeProvider
 * @see FrameworkSettings
 */
@Component
public class ScreenshotRecorder {

    private final ImageFileUtilities imageUtils;
    private final TimeProvider time;

    /**
     * Flag indicating capture status (currently unused but available for future
     * enhancements).
     */
    private volatile boolean isCapturing = false;

    /**
     * Single-threaded executor for scheduled screenshot capture.
     * Note: Once shut down, this executor cannot be reused.
     */
    private final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    public ScreenshotRecorder(ImageFileUtilities imageUtils, TimeProvider time) {
        this.imageUtils = imageUtils;
        this.time = time;
    }

    /**
     * Captures a fixed number of screenshots at regular intervals.
     * <p>
     * This blocking method takes screenshots for the specified duration,
     * waiting between each capture. Screenshots are saved to the path
     * configured in BrobotSettings.
     * <p>
     * Calculation: numberOfScreenshots = secondsToCapture / captureFrequency
     * <p>
     * Example: capture(10, 0.5) takes 20 screenshots over 10 seconds
     * <p>
     * Side effects:
     * <ul>
     * <li>Blocks the calling thread for the entire duration</li>
     * <li>Saves screenshots to BrobotSettings.screenshotPath</li>
     * <li>Uses full screen region (new Region())</li>
     * </ul>
     *
     * @param secondsToCapture total duration for capturing screenshots
     * @param captureFrequency interval between captures in seconds (e.g., 0.5 =
     *                         twice per second)
     */
    public void capture(int secondsToCapture, double captureFrequency) {
        int numberOfScreenshots = (int) (secondsToCapture / captureFrequency);
        for (int i = 0; i < numberOfScreenshots; i++) {
            time.wait(captureFrequency);
            // Capture full screen (empty Region = full screen)
            imageUtils.saveRegionToFile(new Region(),
                    FrameworkSettings.screenshotPath + FrameworkSettings.screenshotFilename);
        }
    }

    /**
     * Starts continuous screenshot capture at fixed time intervals.
     * <p>
     * Initiates a scheduled task that captures screenshots indefinitely
     * until stopCapturing() is called. Uses time-based intervals rather
     * than event-based triggers for several important reasons:
     * <p>
     * Design rationale for time-based capture:
     * <ol>
     * <li><b>Prevents screenshot explosion</b>: Rapid user interactions
     * (mouse movements, key presses) could generate excessive screenshots</li>
     * <li><b>Enables smooth playback</b>: Regular intervals allow consistent
     * playback speed, including slow-motion analysis</li>
     * <li><b>ML compatibility</b>: Provides regularized input/output pairs
     * where screenshots (inputs) map to user actions (outputs) within
     * fixed time windows</li>
     * <li><b>Simplifies synchronization</b>: Fixed intervals make it easier
     * to correlate screenshots with logged events</li>
     * </ol>
     * <p>
     * Thread safety: Synchronized to prevent concurrent starts. However,
     * note that the executor cannot be restarted after shutdown.
     * <p>
     * Warning: The SCHEDULER field is final and shutdown is permanent.
     * Multiple start/stop cycles will not work with current implementation.
     *
     * @param saveToFile          file saving implementation for captured
     *                            screenshots
     * @param baseFilename        base name for screenshot files (timestamp will be
     *                            appended)
     * @param delayInMilliseconds interval between captures in milliseconds
     */
    public synchronized void startCapturing(SaveToFile saveToFile, String baseFilename, int delayInMilliseconds) {
        ScreenshotCapture captureScreenshot = new ScreenshotCapture(saveToFile);
        SCHEDULER.scheduleAtFixedRate((() -> {
            captureScreenshot.saveScreenshotWithDate(baseFilename);
        }), 0, delayInMilliseconds, MILLISECONDS);
    }

    /**
     * Stops the continuous screenshot capture.
     * <p>
     * Gracefully shuts down the capture scheduler with a 1-second timeout.
     * If tasks don't complete within the timeout, forces immediate shutdown.
     * <p>
     * Shutdown sequence:
     * <ol>
     * <li>Initiates orderly shutdown (no new tasks accepted)</li>
     * <li>Waits up to 1 second for existing tasks to complete</li>
     * <li>Forces shutdown if timeout exceeded</li>
     * <li>Handles interruption by forcing immediate shutdown</li>
     * </ol>
     * <p>
     * Important: Once stopped, the capture cannot be restarted with the
     * current implementation as the executor is permanently shut down.
     * <p>
     * Thread safety: Synchronized to coordinate with startCapturing().
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
