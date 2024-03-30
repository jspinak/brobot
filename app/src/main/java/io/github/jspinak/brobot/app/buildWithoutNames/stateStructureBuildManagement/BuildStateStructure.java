package io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure.ScreenStateCreator;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.GetScreenObservation;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.GetTransitionImages;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.ScreenObservations;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.TransitionImageRepo;
import io.github.jspinak.brobot.app.buildWithoutNames.screenTransitions.FindScreen;
import org.springframework.stereotype.Component;

@Component
public class BuildStateStructure {

    private final BuildStateStructureFromGUI buildStateStructureFromGUI;
    private final GetTransitionImages getTransitionImages;
    private final ScreenStateCreator screenStateCreator;
    private final GetScreenObservation getScreenObservation;
    private final ScreenObservations screenObservations;
    private final TransitionImageRepo transitionImageRepo;
    private final FindScreen findScreen;
    private final BuildStateStructureFromScreenshots buildStateStructureFromScreenshots;
    private final GlobalStateStructureOptions globalStateStructureOptions;

    public BuildStateStructure(BuildStateStructureFromGUI buildStateStructureFromGUI,
                               GetTransitionImages getTransitionImages, ScreenStateCreator screenStateCreator,
                               GetScreenObservation getScreenObservation, ScreenObservations screenObservations,
                               TransitionImageRepo transitionImageRepo, FindScreen findScreen,
                               BuildStateStructureFromScreenshots buildStateStructureFromScreenshots,
                               GlobalStateStructureOptions globalStateStructureOptions) {
        this.buildStateStructureFromGUI = buildStateStructureFromGUI;
        this.getTransitionImages = getTransitionImages;
        this.screenStateCreator = screenStateCreator;
        this.getScreenObservation = getScreenObservation;
        this.screenObservations = screenObservations;
        this.transitionImageRepo = transitionImageRepo;
        this.findScreen = findScreen;
        this.buildStateStructureFromScreenshots = buildStateStructureFromScreenshots;
        this.globalStateStructureOptions = globalStateStructureOptions;
    }

    public void execute(StateStructureTemplate stateStructureTemplate) {
        globalStateStructureOptions.setStateStructureTemplate(stateStructureTemplate);

        getTransitionImages.setMinWidthBetweenImages(stateStructureTemplate.getMinWidthBetweenImages());
        screenStateCreator.setSaveStateIllustrations(stateStructureTemplate.isSaveStateIllustrations());
        getScreenObservation.setSaveScreensWithMotionAndImages(stateStructureTemplate.isSaveScreensWithMotionAndImages());
        screenObservations.setSaveScreenshot(stateStructureTemplate.isSaveScreenshots());
        transitionImageRepo.setSaveMatchingImages(stateStructureTemplate.isSaveMatchingImages());
        findScreen.setSaveDecisionMat(stateStructureTemplate.isSaveDecisionMats());
        findScreen.setMinimumChangedPixelsForNewScreen(stateStructureTemplate.getMinimumChangedPixelsForNewScreen());
        buildStateStructureFromGUI.automateStateStructure(stateStructureTemplate);
    }
}
