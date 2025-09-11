package io.github.jspinak.brobot.analysis.histogram;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.*;

import io.github.jspinak.brobot.model.element.Grid;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for HistogramRegions. Tests spatial region management for
 * histogram-based image analysis.
 */
@DisplayName("HistogramRegions Tests")
public class HistogramRegionsTest extends BrobotTestBase {

    private Mat testImage;
    private Mat testImage2;
    private List<Mat> testImages;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Create test images
        testImage = new Mat(200, 300, CV_8UC3, new Scalar(100, 150, 200, 0)); // BGR values

        testImage2 = new Mat(150, 250, CV_8UC3, new Scalar(50, 100, 150, 0));

        testImages = new ArrayList<>();
        testImages.add(testImage);
        testImages.add(testImage2);
    }

    @AfterEach
    public void tearDown() {
        // Release OpenCV Mat objects
        if (testImage != null && !testImage.isNull()) testImage.release();
        if (testImage2 != null && !testImage2.isNull()) testImage2.release();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should construct with single image")
        void shouldConstructWithSingleImage() {
            HistogramRegions regions = new HistogramRegions(testImage);

            assertNotNull(regions);
            assertEquals(1, regions.getImages().size());
            assertEquals(testImage, regions.getImages().get(0));
            assertEquals(1, regions.getImageSizes().size());
            assertEquals(300, regions.getImageSizes().get(0).w());
            assertEquals(200, regions.getImageSizes().get(0).h());
        }

        @Test
        @DisplayName("Should construct with multiple images")
        void shouldConstructWithMultipleImages() {
            HistogramRegions regions = new HistogramRegions(testImages);

            assertNotNull(regions);
            assertEquals(2, regions.getImages().size());
            assertEquals(testImage, regions.getImages().get(0));
            assertEquals(testImage2, regions.getImages().get(1));
            assertEquals(2, regions.getImageSizes().size());
        }

        @Test
        @DisplayName("Should initialize all regions")
        void shouldInitializeAllRegions() {
            HistogramRegions regions = new HistogramRegions(testImage);

            assertNotNull(regions.getTopLeft());
            assertNotNull(regions.getTopRight());
            assertNotNull(regions.getBottomLeft());
            assertNotNull(regions.getBottomRight());
            assertNotNull(regions.getEllipse());
            assertNotNull(regions.getCombined());
        }
    }

    @Nested
    @DisplayName("Region Initialization")
    class RegionInitialization {

        @Test
        @DisplayName("Should create masks for all regions")
        void shouldCreateMasksForAllRegions() {
            HistogramRegions regions = new HistogramRegions(testImage);

            // Each region should have one mask for the single image
            assertEquals(1, regions.getTopLeft().getMasks().size());
            assertEquals(1, regions.getTopRight().getMasks().size());
            assertEquals(1, regions.getBottomLeft().getMasks().size());
            assertEquals(1, regions.getBottomRight().getMasks().size());
            assertEquals(1, regions.getEllipse().getMasks().size());
        }

        @Test
        @DisplayName("Should create correct mask dimensions")
        void shouldCreateCorrectMaskDimensions() {
            HistogramRegions regions = new HistogramRegions(testImage);

            Mat topLeftMask = regions.getTopLeft().getMasks().get(0);
            assertEquals(200, topLeftMask.rows());
            assertEquals(300, topLeftMask.cols());
            assertEquals(CV_8UC1, topLeftMask.type());
        }

        @Test
        @DisplayName("Should create masks for multiple images")
        void shouldCreateMasksForMultipleImages() {
            HistogramRegions regions = new HistogramRegions(testImages);

            // Each region should have two masks for two images
            assertEquals(2, regions.getTopLeft().getMasks().size());
            assertEquals(2, regions.getTopRight().getMasks().size());
            assertEquals(2, regions.getBottomLeft().getMasks().size());
            assertEquals(2, regions.getBottomRight().getMasks().size());
            assertEquals(2, regions.getEllipse().getMasks().size());
        }
    }

    @Nested
    @DisplayName("Grid Division")
    class GridDivision {

        @Test
        @DisplayName("Should create 2x2 grid for each image")
        void shouldCreate2x2GridForEachImage() {
            HistogramRegions regions = new HistogramRegions(testImage);

            assertEquals(1, regions.getGrids().size());
            Grid grid = regions.getGrids().get(0);
            assertEquals(2, grid.getRows());
            assertEquals(2, grid.getCols());
            assertEquals(4, grid.getGridRegions().size());
        }

        @Test
        @DisplayName("Should create correct grid regions")
        void shouldCreateCorrectGridRegions() {
            HistogramRegions regions = new HistogramRegions(testImage);

            Grid grid = regions.getGrids().get(0);
            List<Region> gridRegions = grid.getGridRegions();

            // Top-left quadrant
            Region topLeft = gridRegions.get(0);
            assertEquals(0, topLeft.x());
            assertEquals(0, topLeft.y());
            assertEquals(150, topLeft.w());
            assertEquals(100, topLeft.h());

            // Top-right quadrant
            Region topRight = gridRegions.get(1);
            assertEquals(150, topRight.x());
            assertEquals(0, topRight.y());
            assertEquals(150, topRight.w());
            assertEquals(100, topRight.h());
        }
    }

    @Nested
    @DisplayName("Ellipse Mask")
    class EllipseMask {

        @Test
        @DisplayName("Should create ellipse mask with correct dimensions")
        void shouldCreateEllipseMaskWithCorrectDimensions() {
            HistogramRegions regions = new HistogramRegions(testImage);

            Mat ellipseMask = regions.getEllipse().getMasks().get(0);
            assertEquals(200, ellipseMask.rows());
            assertEquals(300, ellipseMask.cols());
            assertEquals(CV_8UC1, ellipseMask.type());
        }

        @Test
        @DisplayName("Should have non-zero ellipse mask")
        void shouldHaveNonZeroEllipseMask() {
            HistogramRegions regions = new HistogramRegions(testImage);

            Mat ellipseMask = regions.getEllipse().getMasks().get(0);
            double sum = sumElems(ellipseMask).get();

            // Ellipse mask should have white pixels (255)
            assertTrue(sum > 0, "Ellipse mask should have non-zero pixels");
        }

        @Test
        @DisplayName("Should center ellipse in image")
        void shouldCenterEllipseInImage() {
            HistogramRegions regions = new HistogramRegions(testImage);

            Mat ellipseMask = regions.getEllipse().getMasks().get(0);

            // Check center pixel should be white (inside ellipse)
            int centerX = 150;
            int centerY = 100;
            byte centerValue = ellipseMask.ptr(centerY, centerX).get();
            assertEquals((byte) 255, centerValue, "Center should be inside ellipse");

            // Check corner pixels should be black (outside ellipse)
            byte cornerValue = ellipseMask.ptr(0, 0).get();
            assertEquals((byte) 0, cornerValue, "Corner should be outside ellipse");
        }
    }

    @Nested
    @DisplayName("Corner Masks")
    class CornerMasks {

        @Test
        @DisplayName("Should create non-overlapping corner masks")
        void shouldCreateNonOverlappingCornerMasks() {
            HistogramRegions regions = new HistogramRegions(testImage);

            Mat topLeftMask = regions.getTopLeft().getMasks().get(0);
            Mat ellipseMask = regions.getEllipse().getMasks().get(0);

            // Create a combined mask to check for overlap
            Mat combined = new Mat();
            bitwise_and(topLeftMask, ellipseMask, combined);

            double overlap = sumElems(combined).get();
            assertEquals(0.0, overlap, "Corner and ellipse masks should not overlap");

            combined.release();
        }

        @Test
        @DisplayName("Should have four distinct corner masks")
        void shouldHaveFourDistinctCornerMasks() {
            HistogramRegions regions = new HistogramRegions(testImage);

            Mat topLeftMask = regions.getTopLeft().getMasks().get(0);
            Mat topRightMask = regions.getTopRight().getMasks().get(0);
            Mat bottomLeftMask = regions.getBottomLeft().getMasks().get(0);
            Mat bottomRightMask = regions.getBottomRight().getMasks().get(0);

            // Each corner mask should have non-zero pixels
            assertTrue(sumElems(topLeftMask).get() > 0);
            assertTrue(sumElems(topRightMask).get() > 0);
            assertTrue(sumElems(bottomLeftMask).get() > 0);
            assertTrue(sumElems(bottomRightMask).get() > 0);

            // Masks should be different
            Mat diff = new Mat();
            absdiff(topLeftMask, topRightMask, diff);
            assertTrue(sumElems(diff).get() > 0, "Top-left and top-right should be different");
            diff.release();
        }
    }

    @Nested
    @DisplayName("Histogram Initialization")
    class HistogramInitialization {

        @Test
        @DisplayName("Should initialize empty histograms for all regions")
        void shouldInitializeEmptyHistogramsForAllRegions() {
            HistogramRegions regions = new HistogramRegions(testImage);

            assertNotNull(regions.getTopLeft().getHistogram());
            assertNotNull(regions.getTopRight().getHistogram());
            assertNotNull(regions.getBottomLeft().getHistogram());
            assertNotNull(regions.getBottomRight().getHistogram());
            assertNotNull(regions.getEllipse().getHistogram());
        }

        @Test
        @DisplayName("Should have empty histogram lists initially")
        void shouldHaveEmptyHistogramListsInitially() {
            HistogramRegions regions = new HistogramRegions(testImage);

            // Histogram lists should be empty until histograms are calculated
            assertEquals(0, regions.getTopLeft().getHistograms().size());
            assertEquals(0, regions.getTopRight().getHistograms().size());
            assertEquals(0, regions.getBottomLeft().getHistograms().size());
            assertEquals(0, regions.getBottomRight().getHistograms().size());
            assertEquals(0, regions.getEllipse().getHistograms().size());
        }
    }

    @Nested
    @DisplayName("Combined Histograms")
    class CombinedHistograms {

        @Test
        @DisplayName("Should combine histograms when populated")
        void shouldCombineHistogramsWhenPopulated() {
            HistogramRegions regions = new HistogramRegions(testImage);

            // Simulate adding computed histograms
            Mat testHist = new Mat(256, 1, CV_32F);
            for (int i = 0; i < 256; i++) {
                testHist.ptr(i, 0).putFloat(i / 256.0f);
            }

            regions.getTopLeft().getHistograms().add(testHist);
            regions.getTopRight().getHistograms().add(testHist);
            regions.getBottomLeft().getHistograms().add(testHist);
            regions.getBottomRight().getHistograms().add(testHist);
            regions.getEllipse().getHistograms().add(testHist);

            regions.setCombinedHistograms();

            assertNotNull(regions.getCombined().getHistogram());
            assertEquals(1, regions.getCombined().getHistograms().size());

            testHist.release();
        }

        @Test
        @DisplayName("Should handle empty image list for combined histograms")
        void shouldHandleEmptyImageListForCombinedHistograms() {
            HistogramRegions regions = new HistogramRegions(new ArrayList<>());

            regions.setCombinedHistograms();

            // Should handle gracefully without throwing exception
            assertNotNull(regions.getCombined());
        }

        @Test
        @DisplayName("Should combine histograms from multiple images")
        void shouldCombineHistogramsFromMultipleImages() {
            HistogramRegions regions = new HistogramRegions(testImages);

            // Simulate adding computed histograms for both images
            Mat testHist1 = new Mat(256, 1, CV_32F);
            Mat testHist2 = new Mat(256, 1, CV_32F);

            for (int i = 0; i < 256; i++) {
                testHist1.ptr(i, 0).putFloat(i / 256.0f);
                testHist2.ptr(i, 0).putFloat((255 - i) / 256.0f);
            }

            // Add histograms for first image
            regions.getTopLeft().getHistograms().add(testHist1);
            regions.getTopRight().getHistograms().add(testHist1);
            regions.getBottomLeft().getHistograms().add(testHist1);
            regions.getBottomRight().getHistograms().add(testHist1);
            regions.getEllipse().getHistograms().add(testHist1);

            // Add histograms for second image
            regions.getTopLeft().getHistograms().add(testHist2);
            regions.getTopRight().getHistograms().add(testHist2);
            regions.getBottomLeft().getHistograms().add(testHist2);
            regions.getBottomRight().getHistograms().add(testHist2);
            regions.getEllipse().getHistograms().add(testHist2);

            regions.setCombinedHistograms();

            assertNotNull(regions.getCombined().getHistogram());
            assertEquals(2, regions.getCombined().getHistograms().size());

            testHist1.release();
            testHist2.release();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle very small images")
        void shouldHandleVerySmallImages() {
            Mat smallImage = new Mat(10, 10, CV_8UC3);

            try {
                HistogramRegions regions = new HistogramRegions(smallImage);

                assertNotNull(regions);
                assertEquals(1, regions.getImages().size());
                assertEquals(1, regions.getEllipse().getMasks().size());

                // Should create masks even for tiny images
                Mat ellipseMask = regions.getEllipse().getMasks().get(0);
                assertEquals(10, ellipseMask.rows());
                assertEquals(10, ellipseMask.cols());
            } finally {
                smallImage.release();
            }
        }

        @Test
        @DisplayName("Should handle non-square images")
        void shouldHandleNonSquareImages() {
            Mat wideImage = new Mat(100, 500, CV_8UC3);
            Mat tallImage = new Mat(500, 100, CV_8UC3);

            try {
                HistogramRegions wideRegions = new HistogramRegions(wideImage);
                HistogramRegions tallRegions = new HistogramRegions(tallImage);

                // Should handle both wide and tall images
                assertEquals(1, wideRegions.getEllipse().getMasks().size());
                assertEquals(1, tallRegions.getEllipse().getMasks().size());

                // Ellipse should adapt to image dimensions
                Mat wideMask = wideRegions.getEllipse().getMasks().get(0);
                Mat tallMask = tallRegions.getEllipse().getMasks().get(0);

                assertEquals(100, wideMask.rows());
                assertEquals(500, wideMask.cols());
                assertEquals(500, tallMask.rows());
                assertEquals(100, tallMask.cols());
            } finally {
                wideImage.release();
                tallImage.release();
            }
        }

        @Test
        @DisplayName("Should handle images with different dimensions")
        void shouldHandleImagesWithDifferentDimensions() {
            Mat image1 = new Mat(100, 100, CV_8UC3);
            Mat image2 = new Mat(200, 300, CV_8UC3);
            Mat image3 = new Mat(50, 75, CV_8UC3);

            List<Mat> mixedImages = List.of(image1, image2, image3);

            try {
                HistogramRegions regions = new HistogramRegions(mixedImages);

                assertEquals(3, regions.getImages().size());
                assertEquals(3, regions.getImageSizes().size());
                assertEquals(3, regions.getGrids().size());

                // Each region should have masks for all three images
                assertEquals(3, regions.getTopLeft().getMasks().size());
                assertEquals(3, regions.getEllipse().getMasks().size());

                // Verify each mask matches its corresponding image dimensions
                for (int i = 0; i < 3; i++) {
                    Mat mask = regions.getEllipse().getMasks().get(i);
                    Region imageSize = regions.getImageSizes().get(i);
                    assertEquals(imageSize.h(), mask.rows());
                    assertEquals(imageSize.w(), mask.cols());
                }
            } finally {
                image1.release();
                image2.release();
                image3.release();
            }
        }
    }
}
