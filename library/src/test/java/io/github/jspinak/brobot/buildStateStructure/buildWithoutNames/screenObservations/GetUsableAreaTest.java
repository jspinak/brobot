package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.methods.basicactions.TestData;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class GetUsableAreaTest {
    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    GetUsableArea getUsableArea;

    @Test
    void defineInFile() {
        TestData testData = new TestData();
        Region usableArea  = getUsableArea.defineInFile(testData.getScreenshot(), testData.getTopL(), testData.getBottomR());
        assertTrue(usableArea.isDefined());
    }
}