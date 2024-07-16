package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.GetScreenObservationFromScreenshot;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.StatelessImage;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.StatelessImageRepo;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
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
    StatelessImageRepo statelessImageRepo;

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
        statelessImageRepo.getStatelessImages().forEach(img -> System.out.println(img.getScreensFound()));
        for (StatelessImage statelessImage : statelessImageRepo.getStatelessImages()) {
            assertFalse(statelessImage.getScreensFound().isEmpty());
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
        imagesScreens.ifSameScreensAddImage(statelessImageRepo.getStatelessImages().get(1));
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
        imagesScreens.ifSameScreensAddImage(statelessImageRepo.getStatelessImages().get(1));
        imagesScreens.ifSameScreensAddImage(statelessImageRepo.getStatelessImages().get(4));
        System.out.println(imagesScreens);
        assertTrue(imagesScreens.getImages().size() == 3);
        assertTrue(imagesScreens.getScreens().size() == 1);
    }

    @Test
    void ifSameScreensAddImage_imageIsFoundOnMultipleScreens() {
        ImageSetsAndAssociatedScreens imagesScreens = createImagesScreens();
        StatelessImage statelessImage = new StatelessImage(new Match(new Region(0,0,30,30)), 0);
        statelessImage.getScreensFound().add(1);
        imagesScreens.ifSameScreensAddImage(statelessImage);
        System.out.println(imagesScreens);
        assertTrue(imagesScreens.getImages().size() == 2);
        assertTrue(imagesScreens.getScreens().size() == 2);
    }

}