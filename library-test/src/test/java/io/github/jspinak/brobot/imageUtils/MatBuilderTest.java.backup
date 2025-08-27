package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import io.github.jspinak.brobot.util.image.visualization.MatBuilder;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Mat operations which work in headless mode.
 */
class MatBuilderTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    private MatBuilder getMatBuilder() {
        MatBuilder matBuilder = new MatBuilder();
        Region r = new Region();
        matBuilder.setMat(new Mat(r.h(), r.w(), 24));
        return matBuilder;
    }

    @Test
    void setMat() {
        Mat mat = getMatBuilder().build();
        assertEquals(1920, mat.cols());
    }

    @Test
    void addSubMat() {
        MatBuilder matBuilder = getMatBuilder();
        Mat submat = MatrixUtilities.makeMat(new Size(20,20), 24, 100);
        matBuilder.addSubMat(new Location(100,100), submat);
        Mat mat = matBuilder.build();
        Double d = MatrixUtilities.getDouble(101, 101, 0, mat);
        assertEquals(100, d);
    }

    @Test
    void build() {
        Mat mat = getMatBuilder().build();
        assertNotNull(mat);
    }
}