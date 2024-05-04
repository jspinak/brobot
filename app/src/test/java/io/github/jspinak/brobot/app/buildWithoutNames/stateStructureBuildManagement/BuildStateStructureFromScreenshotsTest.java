package io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BuildStateStructureFromScreenshotsTest {
    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    BuildStateStructureFromScreenshots buildStateStructureFromScreenshots;

    @Autowired
    StateService stateService;

    @Test
    void stateStructureFromScreenshots() {
        StateStructureTemplate stateStructureTemplate = new StateStructureTemplate.Builder()
                .addImagesInScreenshotsFolder("floranext0", "floranext1", "floranext2")
                .setBoundaryImages("topleft", "bottomR2")
                .setSaveStateIllustrations(false)
                .setSaveScreenshots(false)
                .setSaveDecisionMats(false)
                .setSaveMatchingImages(false)
                .setSaveScreenWithMotionAndImages(false)
                .build();
        buildStateStructureFromScreenshots.build(stateStructureTemplate);

        System.out.println();
        System.out.println("All states saved in the database:");
        List<State> allStatesInDatabase = stateService.getAllStates();
        allStatesInDatabase.forEach(state -> System.out.println(state.getName()));
        assertTrue(allStatesInDatabase.size()>1); // the unknown state is always there
    }

}