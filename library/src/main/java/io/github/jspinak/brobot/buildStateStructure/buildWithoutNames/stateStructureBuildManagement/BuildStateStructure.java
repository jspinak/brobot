package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.buildStateStructure.ScreenStateCreator;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.GetScreenObservation;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.GetTransitionImages;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.ScreenObservations;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.TransitionImageRepo;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenTtansitions.FindScreen;
import org.springframework.stereotype.Component;

@Component
public class BuildStateStructure {

    private final BuildStateStructureWithoutNames buildStateStructureWithoutNames;
    private final GetTransitionImages getTransitionImages;
    private final ScreenStateCreator screenStateCreator;
    private final GetScreenObservation getScreenObservation;
    private final ScreenObservations screenObservations;
    private final TransitionImageRepo transitionImageRepo;
    private final FindScreen findScreen;

    public BuildStateStructure(BuildStateStructureWithoutNames buildStateStructureWithoutNames,
                               GetTransitionImages getTransitionImages, ScreenStateCreator screenStateCreator,
                               GetScreenObservation getScreenObservation, ScreenObservations screenObservations,
                               TransitionImageRepo transitionImageRepo, FindScreen findScreen) {
        this.buildStateStructureWithoutNames = buildStateStructureWithoutNames;
        this.getTransitionImages = getTransitionImages;
        this.screenStateCreator = screenStateCreator;
        this.getScreenObservation = getScreenObservation;
        this.screenObservations = screenObservations;
        this.transitionImageRepo = transitionImageRepo;
        this.findScreen = findScreen;
    }

    public void execute(StateStructureTemplate stateStructureTemplate) {
        getTransitionImages.setMinWidthBetweenImages(stateStructureTemplate.getMinWidthBetweenImages());
        screenStateCreator.setSaveStateIllustrations(stateStructureTemplate.isSaveStateIllustrations());
        getScreenObservation.setSaveScreensWithMotionAndImages(stateStructureTemplate.isSaveScreensWithMotionAndImages());
        screenObservations.setSaveScreenshot(stateStructureTemplate.isSaveScreenshots());
        transitionImageRepo.setSaveMatchingImages(stateStructureTemplate.isSaveMatchingImages());
        findScreen.setSaveDecisionMat(stateStructureTemplate.isSaveDecisionMats());
        findScreen.setMinimumChangedPixelsForNewScreen(stateStructureTemplate.getMinimumChangedPixelsForNewScreen());
        buildStateStructureWithoutNames.automateStateStructure(
                stateStructureTemplate.getTopLeftBoundary(), stateStructureTemplate.getBottomRightBoundary());
    }
}
