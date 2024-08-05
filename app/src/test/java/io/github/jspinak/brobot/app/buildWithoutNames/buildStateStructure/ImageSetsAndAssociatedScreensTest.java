package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.buildWithoutNames.preliminaryStates.ImageSetsAndAssociatedScreens;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.GetScreenObservationFromScreenshot;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.StatelessImage;
import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class ImageSetsAndAssociatedScreensTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;

    Pattern flora0 = new Pattern("../screenshots/floranext0");
    Pattern flora1 = new Pattern("../screenshots/floranext1");
    Pattern flora2 = new Pattern("../screenshots/floranext2");
    StatelessImage statelessImageWithTwoScreens = new StatelessImage(new Match(), flora0);
    {
        statelessImageWithTwoScreens.addScreenFound(flora1);
    }

    StateStructureConfiguration config = new StateStructureConfiguration.Builder()
            .setBoundaryImages("topLeft", "BottomRight")
            .build();
    List<StatelessImage> statelessImages = new ArrayList<>();

    void populateStatelessImageRepo() {
        Pattern flora0 = new Pattern("../screenshots/floranext0");
        Pattern flora1 = new Pattern("../screenshots/floranext1");
        Pattern flora2 = new Pattern("../screenshots/floranext2");
        getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(flora0, config, statelessImages);
        getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(flora1, config, statelessImages);
        getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(flora2, config, statelessImages);
    }

    @Test
    void transitionImagesHaveAssociatedScreens() {
        populateStatelessImageRepo();
        statelessImages.forEach(img -> System.out.println(img.getScreensFound()));
        for (StatelessImage statelessImage : statelessImages) {
            assertFalse(statelessImage.getScreensFound().isEmpty());
        }
    }

    @Test
    void imageSetsAndAssociatedScreensObjectTest() {
        ImageSetsAndAssociatedScreens imagesScreens= new ImageSetsAndAssociatedScreens(statelessImageWithTwoScreens);
        assertEquals(1, imagesScreens.getImages().size());
        assertEquals(2, imagesScreens.getScreens().size());
    }

    @Test
    void ifSameScreensAddImage_imageNotAddedBecauseScreensDoNotMatch() {
        Pattern flora0 = new Pattern("../screenshots/floranext0");
        Pattern flora1 = new Pattern("../screenshots/floranext1");
        Pattern flora2 = new Pattern("../screenshots/floranext2");
        StateStructureConfiguration config = new StateStructureConfiguration.Builder()
                .setBoundaryImages("topLeft", "BottomRight")
                .build();
        List<StatelessImage> statelessImages = new ArrayList<>();
        getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(flora0, config, statelessImages);
        getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(flora1, config, statelessImages);
        getScreenObservationFromScreenshot.getNewScreenObservationAndProcessImages(flora2, config, statelessImages);
        ImageSetsAndAssociatedScreens imagesScreens= new ImageSetsAndAssociatedScreens(statelessImageWithTwoScreens);
        imagesScreens.ifSameScreensAddImage(statelessImages.get(1));
        System.out.println(imagesScreens);
        assertEquals(1, imagesScreens.getImages().size());
        assertEquals(2, imagesScreens.getScreens().size());
    }

    @Test
    void ifSameScreensAddImage_imagesAddedBecauseScreensMatch() {
        populateStatelessImageRepo(); // each of these (3 in total) has 1 screen
        ImageSetsAndAssociatedScreens imagesScreens = new ImageSetsAndAssociatedScreens(statelessImageWithTwoScreens); // has 2 screens
        imagesScreens.ifSameScreensAddImage(statelessImages.get(0));
        imagesScreens.ifSameScreensAddImage(statelessImages.get(1));
        System.out.println(imagesScreens);
        assertEquals(1, imagesScreens.getImages().size());
        assertEquals(2, imagesScreens.getScreens().size());
    }

    @Test
    void ifSameScreensAddImage_imageIsFoundOnMultipleScreens() {
        ImageSetsAndAssociatedScreens imagesScreens = new ImageSetsAndAssociatedScreens(statelessImageWithTwoScreens);
        StatelessImage statelessImage = new StatelessImage(new Match(new Region(0,0,30,30)), flora0);
        statelessImage.getScreensFound().add(flora1);
        imagesScreens.ifSameScreensAddImage(statelessImage);
        System.out.println(imagesScreens);
        assertEquals(2, imagesScreens.getImages().size());
        assertEquals(2, imagesScreens.getScreens().size());
    }

}