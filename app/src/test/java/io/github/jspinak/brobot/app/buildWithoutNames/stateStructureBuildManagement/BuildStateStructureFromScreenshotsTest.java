package io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.stateStructureBuildManagement.BuildStateStructureFromScreenshots;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Tag("manual")
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
                //.addImagesInScreenshotsFolder("floranext0", "floranext1") //, "floranext2")
                .addScenes(new Scene("floranext0"), new Scene("floranext1"))
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

    // if the image locations are correct, they should be in all 4 quadrants.
    @Test
    void imagesAreInAllFourQuadrants() {
        StateStructureConfiguration stateStructureConfiguration = new StateStructureConfiguration.Builder()
                //.addImagesInScreenshotsFolder("floranext0", "floranext1") //, "floranext2")
                .addScenes(new Scene("floranext0"), new Scene("floranext1"))
                .setBoundaryImages("topleft", "bottomR2")
                .setMinImageArea(100)
                .build();
        buildStateStructureFromScreenshots.build(stateStructureConfiguration);

        System.out.println("\nAll states saved in the database:");
        List<State> allStatesInDatabase = stateService.getAllStates();
        int minX = 1920, minY = 1200, maxX = 0, maxY = 0;
        for (State state : allStatesInDatabase) {
            System.out.println(state.getName());
            for (StateImage stateImage : state.getStateImages()) {
                Region r = stateImage.getPatterns().get(0).getRegion();
                minX = Math.min(minX, r.x());
                minY = Math.min(minY, r.y());
                maxX = Math.max(maxX, r.x());
                maxY = Math.max(maxY, r.y());
            }
        }
        System.out.println("minX:" + minX + " minY:" + minY + " maxX:" + maxX + " maxY:" + maxY);
                assertTrue(minX < 1920/2);
        assertTrue(maxX > 1920/2);
        assertTrue(minY < 600);
        assertTrue(maxY > 600);
    }

}