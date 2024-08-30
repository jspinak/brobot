package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.GetScreenObservationFromScreenshot;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.ScreenObservation;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.StatelessImage;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.StatelessImageOps;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StatelessImageOpsTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    StatelessImageOps statelessImageOps;

    @Autowired
    GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;

    private StateStructureConfiguration config = new StateStructureConfiguration.Builder()
            .setBoundaryImages("topLeft", "BottomRight")
            .build();
    private List<StatelessImage> statelessImages = new ArrayList<>();

    @Test
    void addSomeImagesToRepo() {
        getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(
                new Scene("../screenshots/floranext0"), config, statelessImages);
        System.out.println("images found in screen observation = " + statelessImages.size());
        assertFalse(statelessImages.isEmpty());
    }

    @Test
    void duplicateImagesShouldNotBeAddedToRepo() {
        addImagesToRepo("floranext0");
        int sizeOfOneRepo = statelessImages.size();
        addImagesToRepo("floranext0");
        assertTrue(statelessImages.size() == sizeOfOneRepo);
    }

    /**
     * There are two screens searched. Images are saved in the repo.
     * Duplicate images will not be saved twice. These will have 2 screens.
     * The total number of images found in screen0 and screen1 should equal
     * the sum of images found in each screen minus the images that are similar.
     */
    @Test
    void addOnlyUniqueImagesToRepo() {
        int size0 = addImagesToRepo("floranext0");
        int size1 = addImagesToRepo("floranext1");
        System.out.println("# images in screenObservations: " + size0 + " " + size1 + ", duplicate images: " +
                statelessImageOps.getDuplicateImagesFound());
        int sizeOfRepo = size0 + size1 - statelessImageOps.getDuplicateImagesFound();
        assertEquals(sizeOfRepo, statelessImages.size());
    }

    void addImagesToRepo(String filename1, String filename2) {
        String fullpath1 = "../screenshots/" + filename1;
        String fullpath2 = "../screenshots/" + filename2;
        ScreenObservation screenObservation0 = getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(
                new Scene(fullpath1), config, statelessImages);
        ScreenObservation screenObservation1 = getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(
                new Scene(fullpath2), config, statelessImages);
        System.out.println("# of images in screen 0 = " + screenObservation0.getImages().size());
        System.out.println("# of images in screen 1 = " + screenObservation1.getImages().size());
        System.out.println("# of unique images in the repo = " + statelessImages.size());
    }

    int addImagesToRepo(String filename) {
        String fullpath = "../screenshots/" + filename;
        ScreenObservation screenObservation = getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(
                new Scene(fullpath), config, statelessImages);
        return screenObservation.getImages().size();
    }
}