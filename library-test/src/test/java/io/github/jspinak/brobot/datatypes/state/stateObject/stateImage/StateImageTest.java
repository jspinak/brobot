package io.github.jspinak.brobot.datatypes.state.stateObject.stateImage;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

// SpringBootTest is necessary to load the JavaCV dependencies
@SpringBootTest(classes = BrobotTestApplication.class)
class StateImageTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
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