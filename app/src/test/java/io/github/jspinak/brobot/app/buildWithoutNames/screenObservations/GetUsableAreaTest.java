package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.app.TestData;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
        usableArea.print();

        // top-left image should match at 0,34 107x43
        // bottom-right image should match at 1856,1032 59x36
        // the defined region should be 0.77.1915.1032
        assertTrue(usableArea.isDefined());
        assertTrue(usableArea.getY() == 77);
        assertTrue(usableArea.getX() + usableArea.getW() == 1915);
    }
}