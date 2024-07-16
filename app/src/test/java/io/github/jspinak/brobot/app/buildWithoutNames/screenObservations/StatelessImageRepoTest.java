package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class StatelessImageRepoTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    StatelessImageRepo statelessImageRepo;

    @Autowired
    GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;

    @Test
    void addSomeImagesToRepo() {
        getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern("../screenshots/floranext0"), 0);
        System.out.println("images found in screen observation = " + statelessImageRepo.getStatelessImages().size());
        assertFalse(statelessImageRepo.getStatelessImages().isEmpty());
    }

    @Test
    void duplicateImagesShouldNotBeAddedToRepo() {
        addImagesToRepo("floranext0", "floranext0");
        // # of images in floranext0 should be 97 (fused 20,10)
        assertTrue(statelessImageRepo.getStatelessImages().size() == 97);
    }

    @Test
    void addOnlyUniqueImagesToRepo() {
        addImagesToRepo("floranext0", "floranext1");
        // # of images in floranext0 should be 97
        // # of images in floranext1 should be 101
        assertTrue(statelessImageRepo.getStatelessImages().size() >= 101);
        assertTrue(statelessImageRepo.getStatelessImages().size() <= 198);
    }

    void addImagesToRepo(String filename1, String filename2) {
        String fullpath1 = "../screenshots/" + filename1;
        String fullpath2 = "../screenshots/" + filename2;
        ScreenObservation screenObservation0 = getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern(fullpath1), 0);
        ScreenObservation screenObservation1 = getScreenObservationFromScreenshot.getNewScreenObservationAndAddImagesToRepo(
                new Pattern(fullpath2), 1);
        System.out.println("# of images in screen 0 = " + screenObservation0.getImages().size());
        System.out.println("# of images in screen 1 = " + screenObservation1.getImages().size());
        System.out.println("# of unique images in the repo = " + statelessImageRepo.getStatelessImages().size());
    }
}