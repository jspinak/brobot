package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.analysis.motion.PixelChangeDetector;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
class PixelChangeDetectorTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    ColorMatrixUtilities matOps3d;

    @Test
    void getFinalMat() {
        Mat mat1 = matOps3d.makeMat3D(new short[]{0, 255, 255, 255, 0, 0, 255, 255});
        Mat mat2 = matOps3d.makeMat3D(new short[]{0, 0,   255, 255, 0, 0, 0,   255, 255});
        PixelChangeDetector pixelChangeDetector = new PixelChangeDetector.Builder()
                .addMats(mat1, mat2)
                .useGrayscale()
                //.useGaussianBlur(5, 5, 0)
                //.useDilation(5, 5, 1) // using dilation here causes a fatal error (EXCEPTION_ACCESS_VIOLATION)
                .useThreshold(50, 255)
                .build();
        pixelChangeDetector.print(3, 3, 3);
        Mat finalMat = pixelChangeDetector.getChangeMask();
        assertEquals(0, MatrixUtilities.getDouble(0,0,0, finalMat));
        assertEquals(255, MatrixUtilities.getDouble(0,1,0, finalMat));
    }
}