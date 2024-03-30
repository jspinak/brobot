package io.github.jspinak.brobot.test.actions.methods.basicactions.find.contours;

import io.github.jspinak.brobot.actions.methods.basicactions.find.contours.Contours;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.MatOps;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContoursTest {

    private static Mat mat;
    private static Contours contours;

    @BeforeAll
    public static void setVars() {
        mat = MatOps.make3x3Mat(new short[]{0, 255, 255, 0, 0,     0, 0,
                                             0, 255, 255, 0, 0,     0, 0,
                                             0, 0,     0, 0, 255, 255, 0,
                                             0, 0,     0, 0, 255, 255, 0,
                                             0, 0,     0, 0,   0,   0, 0,
                                             0, 0,     0, 0,   0,   0, 0,
                                             0, 0,     0, 0,   0,   0, 0});
        contours = new Contours.Builder()
                .setBgrFromClassification2d(mat)
                .setSearchRegions(new Region(0,0,7,7))
                .build();
    }


    @Test
    void getMatches() {
        System.out.println("matches size = " + contours.getMatchList().size());
        assertEquals(2, contours.getMatchList().size());
    }

    @Test
    void getContours() {
        assertEquals(2, contours.getContours().size());
    }
}