package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.Wait;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

/**
 * Saves a series of screenshots.
 * Can be used for:
 *   1. Using the BuildStructure class to automate building the State structure
 *      with real MatchSnapshots.
 *   2. Remote application creation, where the client uses a different environment
 *      that may differ from the developer's environment.
 */
@Component
public class CaptureScreenshots {

    private ImageUtils imageUtils;
    private Wait wait;

    public CaptureScreenshots(ImageUtils imageUtils, Wait wait) {
        this.imageUtils = imageUtils;
        this.wait = wait;
    }

    /**
     * Takes and saves screenshots repeatedly in the folder 'path'.
     * @param secondsToCapture The duration to capture screenshots.
     * @param captureFrequency The frequency per capture in seconds.
     */
    public void capture(int secondsToCapture, double captureFrequency) {
        int numberOfScreenshots = (int) (secondsToCapture / captureFrequency);
        for (int i=0; i<numberOfScreenshots; i++) {
            wait.wait(captureFrequency);
            imageUtils.saveRegionToFile(new Region(),
                    BrobotSettings.screenshotPath + BrobotSettings.screenshotFilename);
        }
    }
}
