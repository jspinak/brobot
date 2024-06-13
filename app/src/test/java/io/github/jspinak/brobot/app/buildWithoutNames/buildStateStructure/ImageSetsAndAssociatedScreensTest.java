package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.GetScreenObservationFromScreenshot;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.TransitionImage;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.TransitionImageRepo;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageSetsAndAssociatedScreensTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;

    @Autowired
    TransitionImageRepo transitionImageRepo;

    void populateTransitionImageRepo() {
        getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern("../screenshots/floranext0"), 0);
        getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern("../screenshots/floranext1"), 1);
        getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern("../screenshots/floranext2"), 2);
    }

    @Test
    void transitionImagesHaveAssociatedScreens() {
        populateTransitionImageRepo();
        transitionImageRepo.getImages().forEach(img -> System.out.println(img.getScreensFound()));
        for (TransitionImage transitionImage : transitionImageRepo.getImages()) {
            assertFalse(transitionImage.getScreensFound().isEmpty());
        }
    }

    ImageSetsAndAssociatedScreens createImagesScreens() {
        Set<Integer> screens = new HashSet<>();
        screens.add(0);
        screens.add(1);
        return new ImageSetsAndAssociatedScreens(0, screens);
    }
    @Test
    void imageSetsAndAssociatedScreensObjectTest() {
        ImageSetsAndAssociatedScreens imagesScreens = createImagesScreens();
        assertTrue(imagesScreens.getImages().size() == 1);
        assertTrue(imagesScreens.getScreens().size() == 2);
    }

    @Test
    void ifSameScreensAddImage_imageNotAddedBecauseScreensDoNotMatch() {
        ImageSetsAndAssociatedScreens imagesScreens = createImagesScreens();
        populateTransitionImageRepo();
        imagesScreens.ifSameScreensAddImage(transitionImageRepo.getImages().get(1));
        System.out.println(imagesScreens);
        assertTrue(imagesScreens.getImages().size() == 1);
        assertTrue(imagesScreens.getScreens().size() == 2);
    }

    @Test
    void ifSameScreensAddImage_imagesAddedBecauseScreensMatch() {
        Set<Integer> screens = new HashSet<>();
        screens.add(0);
        ImageSetsAndAssociatedScreens imagesScreens = new ImageSetsAndAssociatedScreens(0, screens);
        populateTransitionImageRepo();
        imagesScreens.ifSameScreensAddImage(transitionImageRepo.getImages().get(1));
        imagesScreens.ifSameScreensAddImage(transitionImageRepo.getImages().get(4));
        System.out.println(imagesScreens);
        assertTrue(imagesScreens.getImages().size() == 3);
        assertTrue(imagesScreens.getScreens().size() == 1);
    }

    @Test
    void ifSameScreensAddImage_imageIsFoundOnMultipleScreens() {
        ImageSetsAndAssociatedScreens imagesScreens = createImagesScreens();
        TransitionImage transitionImage = new TransitionImage(new Match(new Region(0,0,30,30)), 0);
        transitionImage.getScreensFound().add(1);
        imagesScreens.ifSameScreensAddImage(transitionImage);
        System.out.println(imagesScreens);
        assertTrue(imagesScreens.getImages().size() == 2);
        assertTrue(imagesScreens.getScreens().size() == 2);
    }

}