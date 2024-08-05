package io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    // I limit the amount of images due to heap issues.
    @Test
    void stateStructureFromScreenshots() {
        StateStructureConfiguration stateStructureConfiguration = new StateStructureConfiguration.Builder()
                .addImagesInScreenshotsFolder("floranext0", "floranext1") //, "floranext2")
                .setBoundaryImages("topleft", "bottomR2")
                .setMinImageArea(100)
                .build();
        buildStateStructureFromScreenshots.build(stateStructureConfiguration);

        System.out.println();
        System.out.println("All states saved in the database:");
        List<State> allStatesInDatabase = stateService.getAllStates();
        allStatesInDatabase.forEach(state -> System.out.println(state.getName()));
        assertTrue(allStatesInDatabase.size()>1); // the unknown state is always there
    }

}