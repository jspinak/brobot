package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.imageUtils.MatVisualize;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FindScreen {

    private final GetScreenObservation getScreenObservation;
    private final ScreenObservations screenObservations;
    private final TransitionImageRepo transitionImageRepo;
    private final MatVisualize matVisualize;
    private final DecisionMatBuilder decisionMatBuilder;

    private final int minimumChangedPixelsForNewScreen = 200000;

    public FindScreen(GetScreenObservation getScreenObservation, ScreenObservations screenObservations,
                      TransitionImageRepo transitionImageRepo, MatVisualize matVisualize,
                      DecisionMatBuilder decisionMatBuilder) {
        this.getScreenObservation = getScreenObservation;
        this.screenObservations = screenObservations;
        this.transitionImageRepo = transitionImageRepo;
        this.matVisualize = matVisualize;
        this.decisionMatBuilder = decisionMatBuilder;
    }

    /**
     * Takes a new screenshot and compares with the screenshots in the repo. If new, adds it to the repo.
     * If in the repo, returns the screen's id.
     *
     * @return the id of the current screen
     */
    public int findCurrentScreen(int nextId, double minSimilarityImages) {
        ScreenObservation newObservation = getScreenObservation.takeScreenshotAndGetImages(nextId); // get a new screenshot, set the screenshot and fixed-pixel-mask
        int screenId = getScreenId(newObservation); // compare to previous screenshots
        if (screenId < 0) { // screen hasn't been seen before
            screenObservations.addScreenObservation(newObservation); // add screen to repo
            transitionImageRepo.addUniqueImagesToRepo(newObservation, minSimilarityImages);
            return newObservation.getId();
        }
        return screenId; // screen is not new
    }

    /**
     * The new screenshot taken is compared to all other screenshots taken.
     * @param newObservation the new screenshot taken, to be analyzed
     * @return active screen's id if found; otherwise -1
     */
    private int getScreenId(ScreenObservation newObservation) {
        Optional<ScreenObservation> optObs = screenObservations.get(0);
        if (optObs.isEmpty()) return -1; // there are no screens in the repo
        int mostSimilarScreen = -1;
        int leastChangedPixels = -1;
        int changedPixels;
        for (ScreenObservation screenObservation : screenObservations.getAll()) {
            DecisionMat decisionMat = decisionMatBuilder
                    .colorChangedPixels(newObservation.getScreenshot(), newObservation.getDynamicPixelMask(),
                            screenObservation.getId(), screenObservation.getScreenshot(), screenObservation.getDynamicPixelMask())
                    .build();
            changedPixels = decisionMat.getNumberOfChangedPixels();
            if (leastChangedPixels < 0 || changedPixels < leastChangedPixels) {
                mostSimilarScreen = optObs.get().getId();
                leastChangedPixels = changedPixels;
            }
            matVisualize.writeMatToHistory(decisionMat.getCombinedMats(), decisionMat.getFilename());
        }
        if (leastChangedPixels < minimumChangedPixelsForNewScreen) return mostSimilarScreen;
        return -1; // screen is new
    }
}
