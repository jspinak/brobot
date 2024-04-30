package io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.database.data.AllStatesInProject;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BuildStateStructureTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    BuildStateStructure buildStateStructure;

    @Autowired
    AllStatesInProjectService allStatesInProjectService;

    @Autowired
    StateService stateService;

    @Test
    public void buildStateStructure() {
        StateStructureTemplate stateStructureTemplate = new StateStructureTemplate.Builder()
                .addImagesInScreenshotsFolder("floranext0", "floranext1", "floranext2")
                .setBoundaryImages("bottomR", "topleft")
                .setSaveStateIllustrations(false)
                .setSaveScreenshots(false)
                .setSaveDecisionMats(false)
                .setSaveMatchingImages(false)
                .setSaveScreenWithMotionAndImages(false)
                .build();
        buildStateStructure.execute(stateStructureTemplate);

        Set<String> states = allStatesInProjectService.getAllStateNames();
        System.out.println("All states saved in the library module:");
        states.forEach(System.out::println);

        System.out.println();
        System.out.println("All states saved in the database:");
        List<State> allStatesInDatabase = stateService.getAllStates();
        allStatesInDatabase.forEach(state -> System.out.println(state.getName()));
        assertTrue(allStatesInDatabase.size()>1); // the unknown state is always there
    }

}