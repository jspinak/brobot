package io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure.ReplaceStateStructure;
import io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure.StateStructureInfo;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.GetScreenObservationFromScreenshot;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.SetUsableArea;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.StatelessImage;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BuildStateStructureFromScreenshots {

    private final SetUsableArea setUsableArea;
    private final GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;
    private final ReplaceStateStructure replaceStateStructure;
    private final StateStructureInfo stateStructureInfo;

    public BuildStateStructureFromScreenshots(SetUsableArea setUsableArea,
                                              GetScreenObservationFromScreenshot getScreenObservationFromScreenshot,
                                              ReplaceStateStructure replaceStateStructure,
                                              StateStructureInfo stateStructureInfo) {
        this.setUsableArea = setUsableArea;
        this.getScreenObservationFromScreenshot = getScreenObservationFromScreenshot;
        this.replaceStateStructure = replaceStateStructure;
        this.stateStructureInfo = stateStructureInfo;
    }

    public void build(StateStructureConfiguration config) {
        if (config.getScenes().isEmpty()) return;
        Region usableArea = setUsableArea.setArea(config);
        List<StatelessImage> statelessImages = new ArrayList<>();
        getScreenObservationFromScreenshot.getScreenObservations(config, statelessImages);
        replaceStateStructure.createNewStateStructure(usableArea, statelessImages);
        stateStructureInfo.printStateStructure();
    }
}
