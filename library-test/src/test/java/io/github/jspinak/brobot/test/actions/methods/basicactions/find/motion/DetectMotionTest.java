package io.github.jspinak.brobot.test.actions.methods.basicactions.find.motion;

import io.github.jspinak.brobot.actions.methods.basicactions.find.motion.DetectMotion;
import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.imageUtils.MatOps3d;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class DetectMotionTest {

    private Mat mat1;
    private Mat mat2;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    MatOps3d matOps3d;

    @Autowired
    DetectMotion detectMotion;

    private Mat getDynamicPixels() {
        mat1 = matOps3d.makeMat3D(new short[]{0, 255, 255, 255, 0, 0, 255, 255});
        mat2 = matOps3d.makeMat3D(new short[]{0, 0,   255, 255, 0, 0, 0,   255, 255});
        Mat dynamicPixels = detectMotion.getDynamicPixelMask(new MatVector(mat1, mat2));
        MatOps.printPartOfMat(dynamicPixels, 3, 3, "dynamic");
        System.out.println();
        return dynamicPixels;
    }

    @Test
    void getDynamicPixelsFromTopMethod() {
        mat1 = matOps3d.makeMat3D(new short[]{0, 255, 255, 255, 0, 0, 255, 255});
        mat2 = matOps3d.makeMat3D(new short[]{0, 0,   255, 255, 0, 0, 0,   255, 255});
        Mat absdiff = detectMotion.getDynamicPixelMask(mat1, mat2);
        MatOps.printPartOfMat(absdiff, 3, 3, "absdiff");
        assertEquals(255, MatOps.getDouble(0,0,0, absdiff));
    }

    @Test
    void getDynamicRegions() {
        Mat dynamicPixels = getDynamicPixels();
        assertEquals(0, MatOps.getDouble(0,1,0, dynamicPixels));
    }

    @Test
    void getDynamicRegions2() {
        Mat dynamicPixels = getDynamicPixels();
        assertEquals(0, MatOps.getDouble(0,2,0, dynamicPixels));
    }

    @Test
    void getDynamicRegions3() {
        Mat dynamicPixels = getDynamicPixels();
        assertEquals(0, MatOps.getDouble(1,0,0, dynamicPixels));
    }

    @Test
    void getDynamicRegions4() {
        Mat dynamicPixels = getDynamicPixels();
        assertEquals(0, MatOps.getDouble(1,1,0, dynamicPixels));
    }

    @Test
    void getDynamicRegions5() {
        Mat dynamicPixels = getDynamicPixels();
        assertEquals(0, MatOps.getDouble(1,2,0, dynamicPixels));
    }
}