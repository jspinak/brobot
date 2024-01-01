package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.recorder.SaveToFile;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Saves a series of screenshots.
 * Can be used for:
 *   1. Using the BuildStructure class to automate building the State structure
 *      with real MatchSnapshots.
 *   2. Remote application creation, where the client uses a different environment
 *      that may differ from the developer's environment.
 *   3. Analyzing the effects of inputs (keyboard and mouse) on the environment.
 */
@Component
public class CaptureScreenshots {

    private final ImageUtils imageUtils;
    private final Time time;

    private final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    public CaptureScreenshots(ImageUtils imageUtils, Time time) {
        this.imageUtils = imageUtils;
        this.time = time;
    }

    /**
     * Takes and saves screenshots repeatedly in the folder 'path'.
     * @param secondsToCapture The duration to capture screenshots.
     * @param captureFrequency The frequency per capture in seconds.
     */
    public void capture(int secondsToCapture, double captureFrequency) {
        int numberOfScreenshots = (int) (secondsToCapture / captureFrequency);
        for (int i=0; i<numberOfScreenshots; i++) {
            time.wait(captureFrequency);
            imageUtils.saveRegionToFile(new Region(),
                    BrobotSettings.screenshotPath + BrobotSettings.screenshotFilename);
        }
    }

    /**
     * Repeats the capture with a delay, during which other calls to this method are ignored.
     * Screenshots are taken with a time interval (i.e. every second)
     *   and not based on user interaction (mouse movement, key press, etc.). There may be a number
     *   of user interactions between two screenshots. Time intervals are used for the following reasons:
     *   - User interactions can take place in short time intervals, which would result in a large number of screenshots.
     *   - Playback, especially slow-motion playback, is much easier to implement if the screenshots are taken
     *     at regular time intervals.
     *   - Screenshots could be used for a machine learning algorithm, in which the outputs are the user's
     *     interactions for a given time interval and the inputs are screenshots. Since we
     *     need to match screenshots with future interactions, and interactions do not take place at regular time intervals,
     *     user interactions do not provide any useful information to help us determine when to take screenshots.
     *     We need to have some regularization of our inputs and outputs, and time intervals is a practical choice.
     */
    public synchronized void startCapturing(SaveToFile saveToFile, String baseFilename, int delayInMilliseconds) {
        CaptureScreenshot captureScreenshot = new CaptureScreenshot(saveToFile);
        SCHEDULER.scheduleAtFixedRate((() -> {
            captureScreenshot.saveScreenshot(baseFilename);
        }), 0, delayInMilliseconds, MILLISECONDS);
    }

    public synchronized void stopCapturing() {
        SCHEDULER.shutdown();
    }

}
