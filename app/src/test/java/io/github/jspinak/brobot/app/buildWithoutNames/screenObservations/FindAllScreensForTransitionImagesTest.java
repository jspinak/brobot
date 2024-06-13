package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.methods.basicactions.find.FindAll;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FindAllScreensForTransitionImagesTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }


    @Autowired
    GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;


    @Autowired
    FindAllScreensForTransitionImages findAllScreensForTransitionImages;

    @Autowired
    TransitionImageRepo transitionImageRepo;

    @Autowired
    ScreenObservations screenObservations;

    @Test
    void findScreens() {
        // populate ScreenObservations
        ScreenObservation screenObservation0 = getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern("../screenshots/floranext0"), 0);
        ScreenObservation screenObservation1 = getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern("../screenshots/floranext1"), 1);
        screenObservations.addScreenObservation(screenObservation0);
        screenObservations.addScreenObservation(screenObservation1);
        System.out.println("# of screens = "+screenObservations.getAll().size());

        // get image to find. this image appears on both screens
        TransitionImage transitionImage = new TransitionImage(new Match(new Region(0,0,20,50)), 0);
        transitionImage.setImage(new Pattern("topleft").getMat());
        transitionImageRepo.getImages().add(transitionImage);

        // find image in screens
        findAllScreensForTransitionImages.findScreens();
        System.out.println(transitionImage.getScreensFound());
        assertEquals(2, screenObservations.getAll().size());
        assertEquals(2, transitionImage.getScreensFound().size());
    }
}