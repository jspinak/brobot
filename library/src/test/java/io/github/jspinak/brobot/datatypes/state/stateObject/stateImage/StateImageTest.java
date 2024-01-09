package io.github.jspinak.brobot.datatypes.state.stateObject.stateImage;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.MatOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateImageTest {

    StateImage getImage() {
        Pattern pattern = new Pattern.Builder()
                .setFixedRegion(new Region(0, 0, 10, 10))
                .setMat(MatOps.make3x3Mat(new short[]{255, 255, 255}))
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
        assertEquals(10, region.w);
    }

    @Test
    void getDefinedFixedRegions() {
        StateImage stateImage = getImage();
        assertEquals(1, stateImage.getDefinedFixedRegions().size());
    }
}