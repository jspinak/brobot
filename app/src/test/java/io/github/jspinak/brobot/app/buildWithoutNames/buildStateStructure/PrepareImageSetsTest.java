package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.buildStateStructure.PrepareImageSets;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.preliminaryStates.ImageSetsAndAssociatedScreens;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.GetScreenObservationFromScreenshot;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.StatelessImage;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class PrepareImageSetsTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    PrepareImageSets prepareImageSets;

    @Autowired
    GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;

    private StateStructureConfiguration config = new StateStructureConfiguration.Builder()
            .setBoundaryImages("topLeft", "BottomRight")
            .build();
    private List<StatelessImage> statelessImages = new ArrayList<>();

    @Test
    void defineStatesWithImages_fromOneObservation() {
        getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(
                new Scene("../screenshots/floranext0"), config, statelessImages);
        List<ImageSetsAndAssociatedScreens> imageSetsList = prepareImageSets.defineStatesWithImages(statelessImages);
        System.out.println("# of ImageSets = "+imageSetsList.size());
        System.out.println(imageSetsList);
        assertFalse(imageSetsList.isEmpty());
    }

    @Test
    void defineStatesWithImages_fromThreeObservations_oneScreenPerImage() {
        getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(
                new Scene("../screenshots/floranext0"), config, statelessImages);
        getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(
                new Scene("../screenshots/floranext1"), config, statelessImages);
        getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(
                new Scene("../screenshots/floranext2"), config, statelessImages);
        List<ImageSetsAndAssociatedScreens> imageSetsList = prepareImageSets.defineStatesWithImages(statelessImages);
        System.out.println("# of ImageSets = "+imageSetsList.size());
        System.out.println(imageSetsList);
        assertFalse(imageSetsList.isEmpty());
    }

    @Test
    void createState() {
    }

    @Test
    void createAndSaveStatesAndTransitions() {
    }

    @Test
    void setSaveStateIllustrations() {
    }
}