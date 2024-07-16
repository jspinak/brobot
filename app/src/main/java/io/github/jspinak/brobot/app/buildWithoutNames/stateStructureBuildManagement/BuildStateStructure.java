package io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure.ScreenStateCreator;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.GetScreenObservation;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.GetStatelessImages;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.ScreenObservations;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.StatelessImageRepo;
import io.github.jspinak.brobot.app.buildWithoutNames.screenTransitions.FindScreen;
import org.springframework.stereotype.Component;

@Component
public class BuildStateStructure {

    private final BuildStateStructureFromGUI buildStateStructureFromGUI;
    private final GetStatelessImages getStatelessImages;
    private final ScreenStateCreator screenStateCreator;
    private final GetScreenObservation getScreenObservation;
    private final ScreenObservations screenObservations;
    private final StatelessImageRepo statelessImageRepo;
    private final FindScreen findScreen;
    private final BuildStateStructureFromScreenshots buildStateStructureFromScreenshots;
    private final GlobalStateStructureOptions globalStateStructureOptions;

    public BuildStateStructure(BuildStateStructureFromGUI buildStateStructureFromGUI,
                               GetStatelessImages getStatelessImages, ScreenStateCreator screenStateCreator,
                               GetScreenObservation getScreenObservation, ScreenObservations screenObservations,
                               StatelessImageRepo statelessImageRepo, FindScreen findScreen,
                               BuildStateStructureFromScreenshots buildStateStructureFromScreenshots,
                               GlobalStateStructureOptions globalStateStructureOptions) {
        this.buildStateStructureFromGUI = buildStateStructureFromGUI;
        this.getStatelessImages = getStatelessImages;
        this.screenStateCreator = screenStateCreator;
        this.getScreenObservation = getScreenObservation;
        this.screenObservations = screenObservations;
        this.statelessImageRepo = statelessImageRepo;
        this.findScreen = findScreen;
        this.buildStateStructureFromScreenshots = buildStateStructureFromScreenshots;
        this.globalStateStructureOptions = globalStateStructureOptions;
    }

    public void execute(StateStructureTemplate stateStructureTemplate) {
        // settings and preparation
        globalStateStructureOptions.setStateStructureTemplate(stateStructureTemplate);
        getStatelessImages.setMinWidthBetweenImages(stateStructureTemplate.getMinWidthBetweenImages());
        screenStateCreator.setSaveStateIllustrations(stateStructureTemplate.isSaveStateIllustrations());
        getScreenObservation.setSaveScreensWithMotionAndImages(stateStructureTemplate.isSaveScreensWithMotionAndImages());
        screenObservations.setSaveScreenshot(stateStructureTemplate.isSaveScreenshots());
        statelessImageRepo.setSaveMatchingImages(stateStructureTemplate.isSaveMatchingImages());
        findScreen.setSaveDecisionMat(stateStructureTemplate.isSaveDecisionMats());
        findScreen.setMinimumChangedPixelsForNewScreen(stateStructureTemplate.getMinimumChangedPixelsForNewScreen());
        // build the state structure
        buildStateStructureFromGUI.automateStateStructure(stateStructureTemplate);
    }
}
