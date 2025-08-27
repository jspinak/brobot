package io.github.jspinak.brobot.model.state;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

import static org.junit.jupiter.api.Assertions.*;

// SpringBootTest is necessary to load the JavaCV dependencies
@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class,
    properties = {
        "brobot.gui-access.continue-on-error=true",
        "brobot.gui-access.check-on-startup=false",
        "java.awt.headless=true",
        "spring.main.allow-bean-definition-overriding=true",
        "brobot.test.type=unit",
        "brobot.capture.physical-resolution=false",
        "brobot.mock.enabled=true"
    })
@Import({MockGuiAccessConfig.class, MockGuiAccessMonitor.class, MockScreenConfig.class})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
class StateImageTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    StateImage getImage() {
        Pattern pattern = new Pattern.Builder()
                .setFixedRegion(new Region(0, 0, 10, 10))
                .setMat(MatrixUtilities.make3x3Mat(new short[]{255, 255, 255}))
                .build();
        return new StateImage.Builder()
                .addPattern(pattern)
                .setName("test image")
                .build();
    }

    @Test
    void getLargestDefinedFixedRegionOrNewRegion() {
        StateImage stateImage = getImage();
        Region region = stateImage.getLargestDefinedFixedRegionOrNewRegion();
        System.out.println(region);
        assertEquals(10, region.w());
    }

    @Test
    void getDefinedFixedRegions() {
        StateImage stateImage = getImage();
        assertEquals(1, stateImage.getDefinedFixedRegions().size());
    }
}