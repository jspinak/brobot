package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import static org.junit.jupiter.api.Assertions.*;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import io.github.jspinak.brobot.analysis.motion.MotionDetector;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;

@SpringBootTest(
        classes = io.github.jspinak.brobot.BrobotTestApplication.class,
        properties = {
            "brobot.gui-access.continue-on-error=true",
            "brobot.gui-access.check-on-startup=false",
            "java.awt.headless=true",
            "spring.main.allow-bean-definition-overriding=true",
            "brobot.test.type=unit",
            "brobot.capture.physical-resolution=false",
            "brobot.mock.enabled=true"
        })
@Import({
    MockScreenConfig.class,
    io.github.jspinak.brobot.test.config.TestApplicationConfiguration.class
})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
class DetectMotionTest {

    private Mat mat1;
    private Mat mat2;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired ColorMatrixUtilities matOps3d;

    @Autowired MotionDetector detectMotion;

    private Mat getDynamicPixels() {
        mat1 = matOps3d.makeMat3D(new short[] {0, 255, 255, 255, 0, 0, 255, 255});
        mat2 = matOps3d.makeMat3D(new short[] {0, 0, 255, 255, 0, 0, 0, 255, 255});
        Mat dynamicPixels = detectMotion.getDynamicPixelMask(new MatVector(mat1, mat2));
        MatrixUtilities.printPartOfMat(dynamicPixels, 3, 3, "dynamic");
        System.out.println();
        return dynamicPixels;
    }

    @Test
    void getDynamicPixelsFromTopMethod() {
        mat1 = matOps3d.makeMat3D(new short[] {0, 255, 255, 255, 0, 0, 255, 255});
        mat2 = matOps3d.makeMat3D(new short[] {0, 0, 255, 255, 0, 0, 0, 255, 255});
        Mat absdiff = detectMotion.getDynamicPixelMask(mat1, mat2);
        MatrixUtilities.printPartOfMat(absdiff, 3, 3, "absdiff");
        assertEquals(255, MatrixUtilities.getDouble(0, 0, 0, absdiff));
    }

    @Test
    void getDynamicRegions() {
        Mat dynamicPixels = getDynamicPixels();
        assertEquals(0, MatrixUtilities.getDouble(0, 1, 0, dynamicPixels));
    }

    @Test
    void getDynamicRegions2() {
        Mat dynamicPixels = getDynamicPixels();
        assertEquals(0, MatrixUtilities.getDouble(0, 2, 0, dynamicPixels));
    }

    @Test
    void getDynamicRegions3() {
        Mat dynamicPixels = getDynamicPixels();
        assertEquals(0, MatrixUtilities.getDouble(1, 0, 0, dynamicPixels));
    }

    @Test
    void getDynamicRegions4() {
        Mat dynamicPixels = getDynamicPixels();
        assertEquals(0, MatrixUtilities.getDouble(1, 1, 0, dynamicPixels));
    }

    @Test
    void getDynamicRegions5() {
        Mat dynamicPixels = getDynamicPixels();
        assertEquals(0, MatrixUtilities.getDouble(1, 2, 0, dynamicPixels));
    }
}
