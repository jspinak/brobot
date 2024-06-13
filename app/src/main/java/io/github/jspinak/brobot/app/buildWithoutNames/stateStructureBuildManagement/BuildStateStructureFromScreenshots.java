package io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure.ScreenStateCreator;
import io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure.StateStructureInfo;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.*;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BuildStateStructureFromScreenshots {

    private final GetUsableArea getUsableArea;
    private final GetScreenObservation getScreenObservation;
    private final GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;
    private final ScreenStateCreator screenStateCreator;
    private final StateStructureInfo stateStructureInfo;
    private final ScreenObservations screenObservations;
    private final FindAllScreensForTransitionImages findAllScreensForTransitionImages;

    public BuildStateStructureFromScreenshots(GetUsableArea getUsableArea,
                                              GetScreenObservation getScreenObservation,
                                              GetScreenObservationFromScreenshot getScreenObservationFromScreenshot,
                                              ScreenStateCreator screenStateCreator,
                                              StateStructureInfo stateStructureInfo,
                                              ScreenObservations screenObservations,
                                              FindAllScreensForTransitionImages findAllScreensForTransitionImages) {
        this.getUsableArea = getUsableArea;
        this.getScreenObservation = getScreenObservation;
        this.getScreenObservationFromScreenshot = getScreenObservationFromScreenshot;
        this.screenStateCreator = screenStateCreator;
        this.stateStructureInfo = stateStructureInfo;
        this.screenObservations = screenObservations;
        this.findAllScreensForTransitionImages = findAllScreensForTransitionImages;
    }

    public void build(List<Pattern> screenshots, Pattern topLeftBoundary, Pattern bottomRightBoundary) {
        if (screenshots.isEmpty()) return;
        Region usableArea = getUsableArea.defineInFile(
                screenshots.get(0), topLeftBoundary, bottomRightBoundary);
        getScreenObservation.setUsableArea(usableArea);
        screenshots.forEach(screenshot -> screenObservations.addScreenObservation(
                getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(screenshot, screenshots.indexOf(screenshot))));
        findAllScreensForTransitionImages.findScreens();
        screenStateCreator.createAndSaveStatesAndTransitions();
        stateStructureInfo.printStateStructure();
    }

    public void build(StateStructureTemplate stateStructureTemplate) {
        build(stateStructureTemplate.getScreenshots(), stateStructureTemplate.getTopLeftBoundary(),
                stateStructureTemplate.getBottomRightBoundary());
    }
}
