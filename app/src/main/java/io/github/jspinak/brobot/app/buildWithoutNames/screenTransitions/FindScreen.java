package io.github.jspinak.brobot.app.buildWithoutNames.screenTransitions;

import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.*;
import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.GlobalStateStructureOptions;
import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.StateStructureTemplate;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
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
    private final Action action;
    private final GlobalStateStructureOptions stateStructureOptions;

    private int minimumChangedPixelsForNewScreen = 20000;
    private double maxSimilarityForNewScreen = .95; // below this it's a new screen
    private boolean saveDecisionMat;

    public FindScreen(GetScreenObservation getScreenObservation, ScreenObservations screenObservations,
                      TransitionImageRepo transitionImageRepo, MatVisualize matVisualize,
                      DecisionMatBuilder decisionMatBuilder, ScreenObservationManager screenObservationManager,
                      Action action, GlobalStateStructureOptions stateStructureOptions) {
        this.getScreenObservation = getScreenObservation;
        this.screenObservations = screenObservations;
        this.transitionImageRepo = transitionImageRepo;
        this.matVisualize = matVisualize;
        this.decisionMatBuilder = decisionMatBuilder;
        this.screenObservationManager = screenObservationManager;
        this.action = action;
        this.stateStructureOptions = stateStructureOptions;
    }

    /**
     * Takes a new screenshot and compares with the screenshots in the repo. If new, adds it to the repo.
     * If in the repo, returns the screen's id.
     */
    public void findCurrentScreenAndSaveIfNew(StateStructureTemplate stateStructureTemplate) {
        int nextUnassignedId = screenObservationManager.getNextUnassignedScreenId();
        /*
        Get a new screenshot and its images. If the screen is new, add it to the repo and update the current id.
         */
        ScreenObservation newObservation = getScreenObservation.takeScreenshotAndGetImages(stateStructureTemplate);
        int screenId = getScreenId(newObservation); // compare to previous screenshots
        screenObservationManager.setCurrentScreenId(screenId);
        if (screenId == nextUnassignedId) { // screen hasn't been seen before
            processNewScreen(newObservation);
            screenObservationManager.setNextUnassignedScreenId(screenId+1);
            System.out.println("FindScreen: new screen = " + screenId);
            if (saveDecisionMat) matVisualize.writeMatToHistory(newObservation.getScreenshot(), "unique screen");
        }
        screenObservations.get(screenId).ifPresent(screenObservationManager::setCurrentScreenObservation);
    }

    private void processNewScreen(ScreenObservation newObservation) {
        screenObservations.addScreenObservation(newObservation); // add screen to repo
        transitionImageRepo.addUniqueImagesToRepo(newObservation);
    }

    /**
     * The new screenshot taken is compared to all other screenshots taken.
     * @param newObservation the new screenshot taken, to be analyzed
     * @return active screen's id if found; otherwise -1
     */
    private int getScreenId(ScreenObservation newObservation) {
        StateImage newObs = new StateImage.Builder()
                .addPattern(newObservation.getPattern())
                .build();
        ObjectCollection objColl1 = new ObjectCollection.Builder()
                .withImages(newObs)
                .build();
        ObjectCollection objColl2 = new ObjectCollection.Builder()
                .withImages(screenObservations.getAllAsImages())
                .build();
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.SIMILAR_IMAGES)
                .build();
        Matches matches = action.perform(actionOptions, objColl1, objColl2);
        Optional<Match> bestMatch = getBestMatch(newObservation, matches);
        if (bestMatch.isEmpty() || bestMatch.get().getScore() <= maxSimilarityForNewScreen) return newObservation.getId();
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
            System.out.println("screenshotName: " + newObservation.getPattern().getName());
            System.out.println("screen: " + newObservation.getId());
            System.out.println("bestMatch: " + match.getScore());
            System.out.println("bestMatchIndex: " + matches.getMatchList().indexOf(match));
            System.out.println("size of saved screens: " + screenObservations.getAll().size());
        });
        return bestMatch;
    }
}
