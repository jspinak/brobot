package com.brobot.app.buildWithoutNames.screenObservations;

import com.brobot.app.TestData;
import com.brobot.app.services.ImageService;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ComponentScan({"com.brobot.app","com.brobot.app.services,com.brobot.app.database.repositories," +
        "com.brobot.app.database.mappers"})
class GetUsableAreaTest {
    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    GetUsableArea getUsableArea;

    @Autowired
    ImageService imageService;

    @Test
    void defineInFile() {
        TestData testData = new TestData();
        Region usableArea = getUsableArea.defineInFile(testData.getScreenshot(), testData.getTopL(), testData.getBottomR());
        assertTrue(usableArea.isDefined());
    }
}