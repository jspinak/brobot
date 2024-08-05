package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.app.buildWithoutNames.buildLive.ScreenObservations;
import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class FindAllScreensForStatelessImagesTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;

    /**
     * I initialize a new StatelessImage with the "topLeft" Pattern.
     * This pattern should be found on screens "floranext0" and "floranext1".
     */
    @Test
    void getDoubleMatches() {
        StateStructureConfiguration config = new StateStructureConfiguration.Builder()
                .setBoundaryImages("topLeft", "BottomRight")
                .addImagesInScreenshotsFolder("../screenshots/floranext0", "../screenshots/floranext1")
                .setMaxSimilarityForUniqueImage(.99)
                .build();
        List<StatelessImage> statelessImages = new ArrayList<>();
        List<ScreenObservation> observations = getScreenObservationFromScreenshot.getScreenObservations(config, statelessImages);

        /*
         Running getScreenObservations twice should make each StatelessImage have at least 2 match objects.

         */
        getScreenObservationFromScreenshot.getScreenObservations(config, statelessImages);

        System.out.println("# of screens = "+observations.size());
        assertEquals(2, observations.size());
        statelessImages.forEach(img -> {
            System.out.print(img.getMatchList().size()+" ");
            assertTrue(img.getMatchList().size() > 1);
        });
    }
}