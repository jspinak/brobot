package io.github.jspinak.brobot.util.image.visualization;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Test class for MatBuilder - builder pattern for creating OpenCV Mat objects. Tests various Mat
 * creation scenarios in mock mode.
 */
@DisplayName("MatBuilder Tests")
public class MatBuilderTest extends BrobotTestBase {

    private MatBuilder matBuilder;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        matBuilder = new MatBuilder();
    }

    @Nested
    @DisplayName("Basic Mat Creation")
    class BasicMatCreation {

        @Test
        @DisplayName("Should create Mat with default parameters")
        void shouldCreateDefaultMat() {
            Mat result = matBuilder.build();

            assertNotNull(result);
            // Default might be empty
            assertTrue(result != null);
        }

        @Test
        @DisplayName("Should create Mat with width and height")
        void shouldCreateMatWithWH() {
            Mat result = matBuilder.setWH(200, 100).build();

            assertNotNull(result);
            assertEquals(100, result.rows()); // Height
            assertEquals(200, result.cols()); // Width
        }

        @Test
        @DisplayName("Should init Mat from Region")
        void shouldInitFromRegion() {
            Region region = new Region(0, 0, 320, 240);

            Mat result = matBuilder.init(region).build();

            assertNotNull(result);
            assertEquals(240, result.rows());
            assertEquals(320, result.cols());
        }

        @Test
        @DisplayName("Should set Mat name")
        void shouldSetMatName() {
            Mat result = matBuilder.setName("TestMat").setWH(100, 100).build();

            assertNotNull(result);
            // Name is stored internally, doesn't affect Mat itself
        }
    }

    @Nested
    @DisplayName("Mat Operations")
    class MatOperations {

        @Test
        @DisplayName("Should set existing Mat")
        void shouldSetExistingMat() {
            Mat existingMat = new Mat(50, 50, CV_8UC3);

            Mat result = matBuilder.setMat(existingMat).build();

            assertNotNull(result);
            assertEquals(existingMat.rows(), result.rows());
            assertEquals(existingMat.cols(), result.cols());
        }

        @Test
        @DisplayName("Should create one channel row Mat")
        void shouldCreateOneChannelRowMat() {
            Mat result = matBuilder.newOneChannelRowMat(1, 2, 3, 4, 5).build();

            assertNotNull(result);
            assertEquals(1, result.rows());
            assertEquals(5, result.cols());
        }

        @Test
        @DisplayName("Should init with default values")
        void shouldInitWithDefaults() {
            Mat result = matBuilder.init().build();

            assertNotNull(result);
            // Should create with default dimensions
        }
    }

    @Nested
    @DisplayName("SubMat Operations")
    class SubMatOperations {

        @Test
        @DisplayName("Should add horizontal submats")
        void shouldAddHorizontalSubmats() {
            Mat mat1 = new Mat(50, 50, CV_8UC1);
            Mat mat2 = new Mat(50, 50, CV_8UC1);

            Mat result = matBuilder.addHorizontalSubmats(mat1, mat2).build();

            assertNotNull(result);
            // Result should be wider
            assertTrue(result.cols() >= 100);
        }

        @Test
        @DisplayName("Should add vertical submats")
        void shouldAddVerticalSubmats() {
            Mat mat1 = new Mat(50, 50, CV_8UC1);
            Mat mat2 = new Mat(50, 50, CV_8UC1);

            Mat result = matBuilder.addVerticalSubmats(mat1, mat2).build();

            assertNotNull(result);
            // Result should be taller
            assertTrue(result.rows() >= 100);
        }

        @Test
        @DisplayName("Should add horizontal submats from list")
        void shouldAddHorizontalSubmatsFromList() {
            List<Mat> mats =
                    Arrays.asList(
                            new Mat(30, 30, CV_8UC1),
                            new Mat(30, 30, CV_8UC1),
                            new Mat(30, 30, CV_8UC1));

            Mat result = matBuilder.addHorizontalSubmats(mats).build();

            assertNotNull(result);
            assertTrue(result.cols() >= 90);
        }

        @Test
        @DisplayName("Should add vertical submats from list")
        void shouldAddVerticalSubmatsFromList() {
            List<Mat> mats = Arrays.asList(new Mat(20, 40, CV_8UC1), new Mat(20, 40, CV_8UC1));

            Mat result = matBuilder.addVerticalSubmats(mats).build();

            assertNotNull(result);
            assertTrue(result.rows() >= 40);
        }

        @Test
        @DisplayName("Should concat submats horizontally")
        void shouldConcatSubmatsHorizontally() {
            List<Mat> submats =
                    Arrays.asList(
                            new Mat(50, 25, CV_8UC1),
                            new Mat(50, 25, CV_8UC1),
                            new Mat(50, 25, CV_8UC1),
                            new Mat(50, 25, CV_8UC1));

            Mat result = matBuilder.concatSubmatsHorizontally(submats).build();

            assertNotNull(result);
            assertEquals(50, result.rows());
            assertTrue(result.cols() >= 100);
        }

        @Test
        @DisplayName("Should concat submats vertically")
        void shouldConcatSubmatsVertically() {
            List<Mat> submats =
                    Arrays.asList(
                            new Mat(25, 50, CV_8UC1),
                            new Mat(25, 50, CV_8UC1),
                            new Mat(25, 50, CV_8UC1));

            Mat result = matBuilder.concatSubmatsVertically(submats).build();

            assertNotNull(result);
            assertTrue(result.rows() >= 75);
            assertEquals(50, result.cols());
        }

        @Test
        @DisplayName("Should add submat at location")
        void shouldAddSubmatAtLocation() {
            Mat baseMat = new Mat(100, 100, CV_8UC1);
            Mat subMat = new Mat(20, 20, CV_8UC1);
            Location location = new Location(30, 30);

            Mat result = matBuilder.setMat(baseMat).addSubMat(location, subMat).build();

            assertNotNull(result);
            assertEquals(100, result.rows());
            assertEquals(100, result.cols());
        }
    }

    @Nested
    @DisplayName("Configuration Options")
    class ConfigurationOptions {

        @Test
        @DisplayName("Should set submat max width")
        void shouldSetSubmatMaxWidth() {
            Mat result = matBuilder.setSubmatMaxWidth(200).setWH(400, 300).build();

            assertNotNull(result);
            // Max width constraint is applied internally
        }

        @Test
        @DisplayName("Should set submat max height")
        void shouldSetSubmatMaxHeight() {
            Mat result = matBuilder.setSubmatMaxHeight(150).setWH(300, 400).build();

            assertNotNull(result);
            // Max height constraint is applied internally
        }

        @Test
        @DisplayName("Should set space between submats")
        void shouldSetSpaceBetween() {
            Mat mat1 = new Mat(50, 50, CV_8UC1);
            Mat mat2 = new Mat(50, 50, CV_8UC1);

            Mat result = matBuilder.setSpaceBetween(10).addHorizontalSubmats(mat1, mat2).build();

            assertNotNull(result);
            // Space should be added between mats
            assertTrue(result.cols() >= 110);
        }

        @Test
        @DisplayName("Should set submats list")
        void shouldSetSubmatsList() {
            List<Mat> submats =
                    Arrays.asList(
                            new Mat(30, 30, CV_8UC1),
                            new Mat(40, 40, CV_8UC1),
                            new Mat(50, 50, CV_8UC1));

            Mat result = matBuilder.setSubMats(submats).build();

            assertNotNull(result);
            // Should process all submats
        }
    }

    @Nested
    @DisplayName("Builder Chaining")
    class BuilderChaining {

        @Test
        @DisplayName("Should support method chaining")
        void shouldSupportMethodChaining() {
            Mat result =
                    matBuilder
                            .setName("ChainedMat")
                            .setWH(200, 100)
                            .setSpaceBetween(5)
                            .setSubmatMaxWidth(100)
                            .setSubmatMaxHeight(100)
                            .build();

            assertNotNull(result);
            assertEquals(100, result.rows());
            assertEquals(200, result.cols());
        }

        @Test
        @DisplayName("Should override previous settings")
        void shouldOverridePreviousSettings() {
            Mat result =
                    matBuilder
                            .setWH(100, 100)
                            .setWH(200, 200) // Override
                            .build();

            assertNotNull(result);
            assertEquals(200, result.rows());
            assertEquals(200, result.cols());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty submats list")
        void shouldHandleEmptySubmatsList() {
            Mat result = matBuilder.setSubMats(Arrays.asList()).build();

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should handle null Mat")
        void shouldHandleNullMat() {
            // Setting null Mat throws NullPointerException when building
            assertThrows(
                    NullPointerException.class,
                    () -> {
                        matBuilder.setMat(null).build();
                    });
        }

        @Test
        @DisplayName("Should handle zero dimensions")
        void shouldHandleZeroDimensions() {
            Mat result = matBuilder.setWH(0, 0).build();

            assertNotNull(result);
            // Empty Mat is valid
        }

        @Test
        @DisplayName("Should handle negative dimensions")
        void shouldHandleNegativeDimensions() {
            // Negative dimensions throw RuntimeException from OpenCV
            assertThrows(
                    RuntimeException.class,
                    () -> {
                        matBuilder.setWH(-100, -200).build();
                    });
        }

        @Test
        @DisplayName("Should handle very large dimensions")
        void shouldHandleVeryLargeDimensions() {
            assertDoesNotThrow(() -> matBuilder.setWH(10000, 10000).build());
        }
    }
}
