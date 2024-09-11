package io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.buildLive.screenTransitions;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.buildLive.ScreenObservations;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.*;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Setter
public class FindScreen {

    private final GetScreenObservation getScreenObservation;
    private final ScreenObservations screenObservations;
    private final StatelessImageOps statelessImageOps;
    private final ScreenObservationManager screenObservationManager;
    private final Action action;

    private double maxSimilarityForNewScreen = .95; // below this it's a new screen

    public FindScreen(GetScreenObservation getScreenObservation, ScreenObservations screenObservations,
                      StatelessImageOps statelessImageOps, ScreenObservationManager screenObservationManager,
                      Action action) {
        this.getScreenObservation = getScreenObservation;
        this.screenObservations = screenObservations;
        this.statelessImageOps = statelessImageOps;
        this.screenObservationManager = screenObservationManager;
        this.action = action;
    }

    /**
     * If live, takes a new screenshot and compares with the screenshots in the repo. If new, adds it to the repo.
     * This can also be used with screenshots.
     * If in the repo, returns the screen's id.
     */
    public void findCurrentScreenAndSaveIfNew(StateStructureConfiguration config,
                                              List<ScreenObservation> observations, List<StatelessImage> statelessImages) {
        int nextUnassignedId = screenObservationManager.getNextUnassignedScreenId();
        /*
        Get a new screenshot and its images. If the screen is new, add it to the repo and update the current id.
         */
        ScreenObservation newObservation = getScreenObservation.takeScreenshotAndGetImages(config);
        int screenId = getScreenId(newObservation, config, observations); // compare to previous screenshots
        screenObservationManager.setCurrentScreenId(screenId);
        if (screenId == nextUnassignedId) { // screen hasn't been seen before
            processNewScreen(newObservation.getScene(), observations, config, statelessImages);
            screenObservationManager.setNextUnassignedScreenId(screenId+1);
            System.out.println("FindScreen: new screen = " + screenId);
        }
        screenObservations.get(screenId, observations).ifPresent(screenObservationManager::setCurrentScreenObservation);
    }

    private void processNewScreen(Scene screenshot, List<ScreenObservation> observations,
                                  StateStructureConfiguration config, List<StatelessImage> statelessImages) {
        ScreenObservation sO = screenObservations.addScreenObservation(screenshot, observations, config); // add screen to repo
        statelessImageOps.addOrMergeStatelessImages(sO, statelessImages, config);
    }

    /**
     * The new screenshot taken is compared to all other screenshots taken.
     * @param newObservation the new screenshot taken, to be analyzed
     * @return active screen's id if found; otherwise -1
     */
    private int getScreenId(ScreenObservation newObservation, StateStructureConfiguration config,
                            List<ScreenObservation> observations) {
        StateImage newObs = new StateImage.Builder()
                .addPattern(newObservation.getScene().getPattern())
                .build();
        ObjectCollection objColl1 = new ObjectCollection.Builder()
                .withImages(newObs)
                .build();
        ObjectCollection objColl2 = new ObjectCollection.Builder()
                .withImages(screenObservations.getAllAsImages(observations))
                .build();
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.SIMILAR_IMAGES)
                .build();
        Matches matches = action.perform(actionOptions, objColl1, objColl2);
        Optional<Match> bestMatch = getBestMatch(newObservation, matches);
        if (bestMatch.isEmpty() || bestMatch.get().getScore() <= config.getMaxSimilarityForUniqueImage())
            return newObservation.getId();
        return matches.getMatchList().indexOf(bestMatch.get());

        /*
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
         */
    }

    private Optional<Match> getBestMatch(ScreenObservation newObservation, Matches matches) {
        Optional<Match> bestMatch = matches.getBestMatch();
        bestMatch.ifPresent(match -> {
            System.out.println("screenshotName: " + newObservation.getScene().getPattern().getName());
            System.out.println("screen: " + newObservation.getId());
            System.out.println("bestMatch: " + match.getScore());
            System.out.println("bestMatchIndex: " + matches.getMatchList().indexOf(match));
        });
        return bestMatch;
    }
}
