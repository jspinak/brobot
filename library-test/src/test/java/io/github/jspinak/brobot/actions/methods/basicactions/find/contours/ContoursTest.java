package io.github.jspinak.brobot.actions.methods.basicactions.find.contours;

import static org.junit.jupiter.api.Assertions.*;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.analysis.compare.ContourExtractor;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;

/** Tests contour detection using Mat operations which work in headless mode. */
class ContoursTest {

    private static Mat mat;
    private static ContourExtractor contours;

    @BeforeAll
    public static void setVars() {
        mat =
                MatrixUtilities.make3x3Mat(
                        new short[] {
                            0, 255, 255, 0, 0, 0, 0, 0, 255, 255, 0, 0, 0, 0, 0, 0, 0, 0, 255, 255,
                            0, 0, 0, 0, 0, 255, 255, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0
                        });
        contours =
                new ContourExtractor.Builder()
                        .setBgrFromClassification2d(mat)
                        .setSearchRegions(new Region(0, 0, 7, 7))
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
