package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.GetScreenObservationFromScreenshot;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.StatelessImage;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GetScreenObservationFromScreenshotTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;

    private StateStructureConfiguration config = new StateStructureConfiguration.Builder()
            //.setBoundaryImages("topLeft", "BottomRight")
            .setUsableArea(new Region(0,77,1915,1032))
            .build();
    private List<StatelessImage> statelessImages = new ArrayList<>();

    @Test
    void findImagesWithWordsOnScreen_allMatchesHaveImages() {
        Scene screenshot = new Scene("../screenshots/floranext0");
        List<StatelessImage> statelessImages = getScreenObservationFromScreenshot.findImagesWithWordsOnScreen(
                config, screenshot);
        statelessImages.forEach(img -> assertTrue(img.getMatchList().get(0).getImage() != null));
    }
}