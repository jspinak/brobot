package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenTtansitions;

import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.*;
import io.github.jspinak.brobot.imageUtils.MatVisualize;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Setter
public class FindScreen {

    private final GetScreenObservation getScreenObservation;
    private final ScreenObservations screenObservations;
    private final TransitionImageRepo transitionImageRepo;
    private final MatVisualize matVisualize;
    private final DecisionMatBuilder decisionMatBuilder;
    private final ScreenObservationManager screenObservationManager;

    private int minimumChangedPixelsForNewScreen = 20000;
    private boolean saveDecisionMat;

    public FindScreen(GetScreenObservation getScreenObservation, ScreenObservations screenObservations,
                      TransitionImageRepo transitionImageRepo, MatVisualize matVisualize,
                      DecisionMatBuilder decisionMatBuilder, ScreenObservationManager screenObservationManager) {
        this.getScreenObservation = getScreenObservation;
        this.screenObservations = screenObservations;
        this.transitionImageRepo = transitionImageRepo;
        this.matVisualize = matVisualize;
        this.decisionMatBuilder = decisionMatBuilder;
        this.screenObservationManager = screenObservationManager;
    }

    /**
     * Takes a new screenshot and compares with the screenshots in the repo. If new, adds it to the repo.
     * If in the repo, returns the screen's id.
     *
     * @return the id of the current screen
     */
    public void findCurrentScreenAndSaveIfNew() {
        int nextUnassignedId = screenObservationManager.getNextUnassignedScreenId();
        /*
        Get a new screenshot and its images. If the screen is new, add it to the repo and update the current id.
         */
        ScreenObservation newObservation = getScreenObservation.takeScreenshotAndGetImages();
        int screenId = getScreenId(newObservation); // compare to previous screenshots
        screenObservationManager.setCurrentScreenId(screenId);
        if (screenId == nextUnassignedId) { // screen hasn't been seen before
            screenObservations.addScreenObservation(newObservation); // add screen to repo
            transitionImageRepo.addUniqueImagesToRepo(newObservation);
            screenObservationManager.setNextUnassignedScreenId(screenId+1);
            System.out.println("FindScreen: new screen = " + screenId);
        }
        screenObservations.get(screenId).ifPresent(screenObservationManager::setCurrentScreenObservation);
    }

    /**
     * The new screenshot taken is compared to all other screenshots taken.
     * @param newObservation the new screenshot taken, to be analyzed
     * @return active screen's id if found; otherwise -1
     */
    private int getScreenId(ScreenObservation newObservation) {
        int mostSimilarScreen = newObservation.getId();
        int leastChangedPixels = -1;
        int changedPixels;
        for (ScreenObservation screenObservation : screenObservations.getAll()) {
            DecisionMat decisionMat = decisionMatBuilder
                    .colorChangedPixels(newObservation.getScreenshot(), newObservation.getDynamicPixelMask(),
                            screenObservation.getId(), screenObservation.getScreenshot(), screenObservation.getDynamicPixelMask())
                    .build();
            changedPixels = decisionMat.getNumberOfChangedPixels();
            if (leastChangedPixels < 0 || changedPixels < leastChangedPixels) {
                mostSimilarScreen = screenObservation.getId();
                leastChangedPixels = changedPixels;
            }
            if (saveDecisionMat) matVisualize.writeMatToHistory(decisionMat.getCombinedMats(), decisionMat.getFilename());
        }
        if (leastChangedPixels < minimumChangedPixelsForNewScreen) return mostSimilarScreen;
        return newObservation.getId(); // screen is new
    }
}
