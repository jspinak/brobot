package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class MatOps3dTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    ColorMatrixUtilities matOps3d;

    @Test
    void makeTestMat3D() {
        Mat mat = matOps3d.makeMat3D(new short[]{0, 200, 200, 200});
        MatrixUtilities.printPartOfMat(mat, 3, 3, "test Mat");
        assertEquals(0, MatrixUtilities.getDouble(0,0,0, mat));
        assertEquals(200, MatrixUtilities.getDouble(0,1,0, mat));
        assertEquals(200, MatrixUtilities.getDouble(0,2,0, mat));
        assertEquals(200, MatrixUtilities.getDouble(1,0,0, mat));
    }

    @Test
    void makeTestMat2() {
        Mat mat = matOps3d.makeMat3D(new short[]{0, 0,   255, 255, 0, 0, 0,   255, 255});
        MatrixUtilities.printPartOfMat(mat, 3, 3, "test Mat");
        assertEquals(0, MatrixUtilities.getDouble(0,0,0, mat));
        assertEquals(0, MatrixUtilities.getDouble(0,1,0, mat));
        assertEquals(255, MatrixUtilities.getDouble(0,2,0, mat));
        assertEquals(255, MatrixUtilities.getDouble(1,0,0, mat));
        assertEquals(0, MatrixUtilities.getDouble(1,1,0, mat));
        assertEquals(0, MatrixUtilities.getDouble(1,2,0, mat));
        assertEquals(0, MatrixUtilities.getDouble(2,0,0, mat));
        assertEquals(255, MatrixUtilities.getDouble(2,1,0, mat));
        assertEquals(255, MatrixUtilities.getDouble(2,2,0, mat));
    }

    @Test
    void getGrayscale() {
        Mat mat = matOps3d.makeMat3D(new short[]{0, 200, 200, 200}, new short[]{0, 100, 100, 100}, new short[]{0, 100, 100, 200});
        mat = MatrixUtilities.getGrayscale(mat);
        MatrixUtilities.printPartOfMat(mat, 3, 3, "grayscale Mat");
        assertEquals(0, MatrixUtilities.getDouble(0,0,0, mat));
        assertNotEquals(0, MatrixUtilities.getDouble(0,1,0, mat));
    }
}