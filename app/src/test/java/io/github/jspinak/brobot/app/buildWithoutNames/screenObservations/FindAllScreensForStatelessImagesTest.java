package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class FindAllScreensForStatelessImagesTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;

    @Autowired
    FindAllScreensForStatelessImages findAllScreensForStatelessImages;

    @Autowired
    StatelessImageRepo statelessImageRepo;

    @Autowired
    ScreenObservations screenObservations;

    /**
     * I initialize a new StatelessImage with the "topLeft" Pattern.
     * This pattern should be found on screens "floranext0" and "floranext1".
     */
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
        Match match = new Match.Builder()
                .setName("topLeft")
                .setImage(new Pattern("topleft").getImage())
                .setRegion(new Region(0,0,20,50))
                .build();
        // include the image in screenObservation0
        StatelessImage statelessTopLeft = new StatelessImage(match, 0);
        // add the StatelessImage to the repo
        statelessImageRepo.getStatelessImages().add(statelessTopLeft);

        // find all StatelessImages in both screens
        findAllScreensForStatelessImages.findScreens();
        System.out.println(statelessTopLeft.getScreensFound());
        assertEquals(2, screenObservations.getAll().size());
        assertEquals(2, statelessTopLeft.getScreensFound().size());
    }
}