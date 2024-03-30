package io.github.jspinak.brobot.test.imageUtils;

import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.imageUtils.MatOps3d;
import io.github.jspinak.brobot.test.BrobotTestApplication;
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
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    MatOps3d matOps3d;

    @Test
    void makeTestMat3D() {
        Mat mat = matOps3d.makeMat3D(new short[]{0, 200, 200, 200});
        MatOps.printPartOfMat(mat, 3, 3, "test Mat");
        assertEquals(0, MatOps.getDouble(0,0,0, mat));
        assertEquals(200, MatOps.getDouble(0,1,0, mat));
        assertEquals(200, MatOps.getDouble(0,2,0, mat));
        assertEquals(200, MatOps.getDouble(1,0,0, mat));
    }

    @Test
    void makeTestMat2() {
        Mat mat = matOps3d.makeMat3D(new short[]{0, 0,   255, 255, 0, 0, 0,   255, 255});
        MatOps.printPartOfMat(mat, 3, 3, "test Mat");
        assertEquals(0, MatOps.getDouble(0,0,0, mat));
        assertEquals(0, MatOps.getDouble(0,1,0, mat));
        assertEquals(255, MatOps.getDouble(0,2,0, mat));
        assertEquals(255, MatOps.getDouble(1,0,0, mat));
        assertEquals(0, MatOps.getDouble(1,1,0, mat));
        assertEquals(0, MatOps.getDouble(1,2,0, mat));
        assertEquals(0, MatOps.getDouble(2,0,0, mat));
        assertEquals(255, MatOps.getDouble(2,1,0, mat));
        assertEquals(255, MatOps.getDouble(2,2,0, mat));
    }

    @Test
    void getGrayscale() {
        Mat mat = matOps3d.makeMat3D(new short[]{0, 200, 200, 200}, new short[]{0, 100, 100, 100}, new short[]{0, 100, 100, 200});
        mat = MatOps.getGrayscale(mat);
        MatOps.printPartOfMat(mat, 3, 3, "grayscale Mat");
        assertEquals(0, MatOps.getDouble(0,0,0, mat));
        assertNotEquals(0, MatOps.getDouble(0,1,0, mat));
    }
}