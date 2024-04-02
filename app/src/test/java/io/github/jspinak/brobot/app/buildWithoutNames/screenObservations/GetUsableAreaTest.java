package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.app.TestData;
import io.github.jspinak.brobot.app.services.ImageService;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GetUsableAreaTest {
    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    GetUsableArea getUsableArea;

    //@Autowired
    //ImageService imageService;

    @Test
    void defineInFile() {
        TestData testData = new TestData();
        Region usableArea = getUsableArea.defineInFile(testData.getScreenshot(), testData.getTopL(), testData.getBottomR());
        assertTrue(usableArea.isDefined());
    }
}