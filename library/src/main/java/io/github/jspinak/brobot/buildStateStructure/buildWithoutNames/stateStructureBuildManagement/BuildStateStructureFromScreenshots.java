package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.buildStateStructure.ScreenStateCreator;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.buildStateStructure.StateStructureInfo;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.GetScreenObservation;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.GetScreenObservationFromScreenshot;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.GetUsableArea;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
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

    public BuildStateStructureFromScreenshots(GetUsableArea getUsableArea,
                                              GetScreenObservation getScreenObservation,
                                              GetScreenObservationFromScreenshot getScreenObservationFromScreenshot,
                                              ScreenStateCreator screenStateCreator,
                                              StateStructureInfo stateStructureInfo) {
        this.getUsableArea = getUsableArea;
        this.getScreenObservation = getScreenObservation;
        this.getScreenObservationFromScreenshot = getScreenObservationFromScreenshot;
        this.screenStateCreator = screenStateCreator;
        this.stateStructureInfo = stateStructureInfo;
    }

    public void build(List<Image> screenshots, Image topLeftBoundary, Image bottomRightBoundary) {
        if (screenshots.isEmpty()) return;
        Region usableArea = getUsableArea.getFromFile(
                screenshots.get(0).getFirstFilename(), topLeftBoundary, bottomRightBoundary);
        getScreenObservation.setUsableArea(usableArea);
        screenshots.forEach(getScreenObservationFromScreenshot::getNewScreenObservation);
        screenStateCreator.createAndSaveStatesAndTransitions();
        stateStructureInfo.printStateStructure();
    }
}
