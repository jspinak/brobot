package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GetScreenObservationFromScreenshotTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    GetScreenObservationFromScreenshot getScreenObservationFromScreenshot;

    @Test
    void findImagesWithWordsOnScreen_allMatchesHaveImages() {
        Region usableArea = new Region(0,77,1915,1032);
        Pattern screenshot = new Pattern("../screenshots/floranext0");
        int index = 0;
        List<StatelessImage> statelessImages = getScreenObservationFromScreenshot.findImagesWithWordsOnScreen(
                usableArea, screenshot, index);
        statelessImages.forEach(img -> assertTrue(img.getMatch().getImage() != null));
    }
}