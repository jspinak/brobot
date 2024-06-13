package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TransitionImageRepoTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    TransitionImageRepo transitionImageRepo;

    @Autowired
    GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;

    @Test
    void addSomeImagesToRepo() {
        getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern("../screenshots/floranext0"), 0);
        System.out.println("images found in screen observation = " + transitionImageRepo.getImages().size());
        assertFalse(transitionImageRepo.getImages().isEmpty());
    }

    @Test
    void duplicateImagesShouldNotBeAddedToRepo() {
        addImagesToRepo("floranext0", "floranext0");
        // # of images in floranext0 should be 97
        assertTrue(transitionImageRepo.getImages().size() == 97);
    }

    @Test
    void addOnlyUniqueImagesToRepo() {
        addImagesToRepo("floranext0", "floranext1");
        // # of images in floranext0 should be 97
        // # of images in floranext1 should be 101
        assertTrue(transitionImageRepo.getImages().size() >= 101);
        assertTrue(transitionImageRepo.getImages().size() <= 198);
    }

    @Test
    void addImagesToRepo(String filename1, String filename2) {
        String fullpath1 = "../screenshots/" + filename1;
        String fullpath2 = "../screenshots/" + filename2;
        ScreenObservation screenObservation0 = getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern(fullpath1), 0);
        ScreenObservation screenObservation1 = getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern(fullpath2), 1);
        System.out.println("# of images in screen 0 = " + screenObservation0.getImages().size());
        System.out.println("# of images in screen 1 = " + screenObservation1.getImages().size());
        System.out.println("# of unique images in the repo = " + transitionImageRepo.getImages().size());
    }
}