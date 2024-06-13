package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.GetScreenObservationFromScreenshot;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class ScreenStateCreatorTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    ScreenStateCreator screenStateCreator;

    @Autowired
    GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;

    @Test
    void defineStatesWithImages_fromOneObservation() {
        getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern("../screenshots/floranext0"), 0);
        List<ImageSetsAndAssociatedScreens> imageSetsList = screenStateCreator.defineStatesWithImages();
        System.out.println("# of ImageSets = "+imageSetsList.size());
        System.out.println(imageSetsList);
        assertFalse(imageSetsList.isEmpty());
    }

    @Test
    void defineStatesWithImages_fromThreeObservations_oneScreenPerImage() {
        getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern("../screenshots/floranext0"), 0);
        getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern("../screenshots/floranext1"), 1);
        getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern("../screenshots/floranext2"), 2);
        List<ImageSetsAndAssociatedScreens> imageSetsList = screenStateCreator.defineStatesWithImages();
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