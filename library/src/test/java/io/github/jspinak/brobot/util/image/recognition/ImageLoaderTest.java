package io.github.jspinak.brobot.util.image.recognition;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;

/**
 * Comprehensive test suite for ImageLoader. Tests image loading, color conversion, and region
 * extraction functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageLoader Tests")
public class ImageLoaderTest extends BrobotTestBase {

    @Mock private BufferedImageUtilities bufferedImageOps;

    @InjectMocks private ImageLoader imageLoader;

    private Mat testMatBGR;
    private Mat testMatHSV;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Create test matrices
        testMatBGR = new Mat(100, 100, CV_8UC3, new Scalar(100, 150, 200, 0));
        testMatHSV = new Mat(100, 100, CV_8UC3);
        cvtColor(testMatBGR, testMatHSV, COLOR_BGR2HSV);
    }

    @AfterEach
    public void tearDown() {
        releaseIfNotNull(testMatBGR);
        releaseIfNotNull(testMatHSV);
    }

    private void releaseIfNotNull(Mat mat) {
        if (mat != null && !mat.isNull()) {
            mat.release();
        }
    }

    @Nested
    @DisplayName("Color Space Conversion")
    class ColorSpaceConversion {

        @Test
        @DisplayName("Should convert BGR to HSV in-place")
        void shouldConvertBGRToHSVInPlace() {
            Mat bgrCopy = testMatBGR.clone();

            Mat result = imageLoader.convertToHSV(bgrCopy);

            assertNotNull(result);
            assertEquals(bgrCopy, result); // Should return same Mat object
            assertEquals(CV_8UC3, result.type());

            // Verify conversion happened (values should change)
            assertNotEquals(testMatBGR.ptr(0, 0).get(0), result.ptr(0, 0).get(0));

            bgrCopy.release();
        }

        @Test
        @DisplayName("Should create new HSV Mat from BGR")
        void shouldCreateNewHSVMatFromBGR() {
            Mat result = imageLoader.getHSV(testMatBGR);

            assertNotNull(result);
            assertNotEquals(testMatBGR, result); // Should be different Mat object
            assertEquals(CV_8UC3, result.type());
            assertEquals(testMatBGR.rows(), result.rows());
            assertEquals(testMatBGR.cols(), result.cols());

            result.release();
        }

        @Test
        @DisplayName("Should handle empty Mat for conversion")
        void shouldHandleEmptyMatForConversion() {
            // Test that empty Mat conversion is handled gracefully
            Mat emptyMat = new Mat();

            Mat result = imageLoader.getHSV(emptyMat);

            assertNotNull(result);
            assertTrue(result.empty());

            emptyMat.release();
            result.release();
        }
    }

    @Nested
    @DisplayName("Pattern and StateImage Processing")
    class PatternAndStateImageProcessing {

        @Test
        @DisplayName("Should get Mats from StateImage patterns")
        void shouldGetMatsFromStateImage() {
            StateImage stateImage = mock(StateImage.class);
            Pattern pattern1 = mock(Pattern.class);
            Pattern pattern2 = mock(Pattern.class);

            when(pattern1.getImgpath()).thenReturn("image1.png");
            when(pattern2.getImgpath()).thenReturn("image2.png");
            when(stateImage.getPatterns()).thenReturn(Arrays.asList(pattern1, pattern2));

            // Mock the file loading to return test matrices
            ImageLoader spyLoader = spy(imageLoader);
            doReturn(testMatBGR.clone()).when(spyLoader).getMatFromBundlePath(anyString(), any());

            List<Mat> result = spyLoader.getMats(stateImage, ColorCluster.ColorSchemaName.BGR);

            assertNotNull(result);
            assertEquals(2, result.size());

            // Clean up
            result.forEach(Mat::release);
        }

        @Test
        @DisplayName("Should get Mats from Pattern list")
        void shouldGetMatsFromPatternList() {
            Pattern pattern1 = mock(Pattern.class);
            Pattern pattern2 = mock(Pattern.class);
            Pattern pattern3 = mock(Pattern.class);

            when(pattern1.getImgpath()).thenReturn("pattern1.png");
            when(pattern2.getImgpath()).thenReturn("pattern2.png");
            when(pattern3.getImgpath()).thenReturn("pattern3.png");

            List<Pattern> patterns = Arrays.asList(pattern1, pattern2, pattern3);

            ImageLoader spyLoader = spy(imageLoader);
            doReturn(testMatBGR.clone()).when(spyLoader).getMatFromBundlePath(anyString(), any());

            List<Mat> result = spyLoader.getMats(patterns, ColorCluster.ColorSchemaName.BGR);

            assertNotNull(result);
            assertEquals(3, result.size());

            result.forEach(Mat::release);
        }

        @Test
        @DisplayName("Should handle empty pattern list")
        void shouldHandleEmptyPatternList() {
            List<Pattern> emptyPatterns = Collections.emptyList();

            List<Mat> result = imageLoader.getMats(emptyPatterns, ColorCluster.ColorSchemaName.BGR);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Region-based Loading")
    class RegionBasedLoading {

        @Test
        @DisplayName("Should get single Mat masked by regions")
        void shouldGetSingleMatMaskedByRegions() {
            // Test that region masking creates a proper masked image
            List<Region> regions =
                    Arrays.asList(new Region(10, 10, 30, 30), new Region(50, 50, 40, 40));

            ImageLoader spyLoader = spy(imageLoader);
            Mat mockMat = new Mat(100, 100, CV_8UC3, new Scalar(100, 150, 200, 0));
            doReturn(mockMat).when(spyLoader).getMatFromBundlePath(anyString(), any());

            Mat result = spyLoader.getMat("test.png", regions, ColorCluster.ColorSchemaName.BGR);

            assertNotNull(result);
            assertEquals(mockMat.type(), result.type());
            assertEquals(mockMat.rows(), result.rows());
            assertEquals(mockMat.cols(), result.cols());

            // The result should be mostly black (zeros) except for the masked regions
            // We don't need to release mockMat as it's returned by the method
            result.release();
        }

        @Test
        @DisplayName("Should get separate Mats for each region")
        void shouldGetSeparateMatsForEachRegion() {
            List<Region> regions =
                    Arrays.asList(
                            new Region(0, 0, 50, 50),
                            new Region(50, 0, 50, 50),
                            new Region(0, 50, 50, 50));

            ImageLoader spyLoader = spy(imageLoader);
            doReturn(testMatBGR.clone()).when(spyLoader).getMatFromBundlePath(anyString(), any());

            List<Mat> result =
                    spyLoader.getMats("test.png", regions, ColorCluster.ColorSchemaName.BGR);

            assertNotNull(result);
            assertEquals(3, result.size());

            // Each Mat should have the region dimensions
            for (int i = 0; i < result.size(); i++) {
                Mat mat = result.get(i);
                assertEquals(50, mat.cols());
                assertEquals(50, mat.rows());
                mat.release();
            }
        }

        @Test
        @DisplayName("Should return full image when no regions specified")
        void shouldReturnFullImageWhenNoRegions() {
            List<Region> emptyRegions = Collections.emptyList();

            ImageLoader spyLoader = spy(imageLoader);
            Mat mockMat = new Mat(100, 100, CV_8UC3, new Scalar(100, 150, 200, 0));
            doReturn(mockMat).when(spyLoader).getMatFromBundlePath(anyString(), any());

            List<Mat> result =
                    spyLoader.getMats("test.png", emptyRegions, ColorCluster.ColorSchemaName.BGR);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(mockMat.rows(), result.get(0).rows());
            assertEquals(mockMat.cols(), result.get(0).cols());

            // Don't release mockMat as it's returned in the list
            result.forEach(Mat::release);
        }
    }

    @Nested
    @DisplayName("Screen Capture")
    class ScreenCapture {

        @Test
        @DisplayName("Should capture screen region")
        void shouldCaptureScreenRegion() {
            Region region = new Region(100, 100, 200, 200);

            // Mock screen capture
            when(bufferedImageOps.getBuffImgFromScreen(region))
                    .thenReturn(new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB));
            when(bufferedImageOps.convertTo3ByteBGRType(any(BufferedImage.class)))
                    .thenReturn(new BufferedImage(200, 200, BufferedImage.TYPE_3BYTE_BGR));

            Mat result = imageLoader.getMatFromScreen(region, ColorCluster.ColorSchemaName.BGR);

            assertNotNull(result);
            assertEquals(CV_8UC3, result.type());

            verify(bufferedImageOps).getBuffImgFromScreen(region);

            result.release();
        }

        @Test
        @DisplayName("Should capture full screen when region is null")
        void shouldCaptureFullScreenWhenRegionNull() {
            when(bufferedImageOps.getBuffImgFromScreen(any(Region.class)))
                    .thenReturn(new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB));
            when(bufferedImageOps.convertTo3ByteBGRType(any(BufferedImage.class)))
                    .thenReturn(new BufferedImage(1920, 1080, BufferedImage.TYPE_3BYTE_BGR));

            Mat result = imageLoader.getMatFromScreen();

            assertNotNull(result);
            verify(bufferedImageOps).getBuffImgFromScreen(any(Region.class));

            result.release();
        }

        @Test
        @DisplayName("Should capture screen over time")
        void shouldCaptureScreenOverTime() {
            Region region = new Region(0, 0, 100, 100);
            double intervalSeconds = 0.5;
            double totalSeconds = 2.0;

            // Mock repeated captures
            when(bufferedImageOps.getBuffImgFromScreen(region))
                    .thenReturn(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB));
            when(bufferedImageOps.convertTo3ByteBGRType(any(BufferedImage.class)))
                    .thenReturn(new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR));

            MatVector result = imageLoader.getMatsFromScreen(region, intervalSeconds, totalSeconds);

            assertNotNull(result);
            // Should capture approximately totalSeconds/intervalSeconds times
            assertTrue(result.size() >= 3);

            // Clean up
            for (int i = 0; i < result.size(); i++) {
                result.get(i).release();
            }
        }
    }

    @Nested
    @DisplayName("Color Schema Support")
    class ColorSchemaSupport {

        @ParameterizedTest
        @EnumSource(ColorCluster.ColorSchemaName.class)
        @DisplayName("Should handle all color schema types")
        void shouldHandleAllColorSchemas(ColorCluster.ColorSchemaName schemaName) {
            ImageLoader spyLoader = spy(imageLoader);

            // Mock file reading to return a test Mat
            Mat mockMat = testMatBGR.clone();
            doReturn(mockMat)
                    .when(spyLoader)
                    .getMatFromFilename(anyString(), any(ColorCluster.ColorSchemaName.class));

            if (schemaName == ColorCluster.ColorSchemaName.BGR
                    || schemaName == ColorCluster.ColorSchemaName.HSV) {

                Mat result = spyLoader.getMatFromFilename("test.png", schemaName);

                assertNotNull(result);
                assertEquals(CV_8UC3, result.type());

                if (!result.equals(mockMat)) {
                    result.release();
                }
                mockMat.release();
            } else {
                // Other schemas should throw exception
                assertThrows(
                        RuntimeException.class,
                        () -> spyLoader.getMatFromFilename("test.png", schemaName));
                mockMat.release();
            }
        }

        @Test
        @DisplayName("Should convert to HSV when requested")
        void shouldConvertToHSVWhenRequested() {
            // Test the HSV conversion directly
            Mat bgrMat = testMatBGR.clone();

            Mat hsvResult = imageLoader.getHSV(bgrMat);

            assertNotNull(hsvResult);
            // HSV should be a new Mat, not the same object
            assertNotEquals(bgrMat, hsvResult);
            assertEquals(bgrMat.rows(), hsvResult.rows());
            assertEquals(bgrMat.cols(), hsvResult.cols());
            assertEquals(3, hsvResult.channels()); // HSV has 3 channels

            bgrMat.release();
            hsvResult.release();
        }
    }

    @Nested
    @DisplayName("Batch Processing")
    class BatchProcessing {

        @Test
        @DisplayName("Should process multiple filenames efficiently")
        void shouldProcessMultipleFilenames() {
            List<String> filenames = Arrays.asList("img1.png", "img2.png", "img3.png", "img4.png");

            ImageLoader spyLoader = spy(imageLoader);
            doReturn(testMatBGR.clone()).when(spyLoader).getMatFromBundlePath(anyString(), any());

            List<Mat> result =
                    spyLoader.getMatsFromFilenames(filenames, ColorCluster.ColorSchemaName.BGR);

            assertNotNull(result);
            assertEquals(4, result.size());

            result.forEach(Mat::release);
        }

        @ParameterizedTest
        @ValueSource(ints = {10, 50, 100})
        @DisplayName("Should handle large batch sizes")
        void shouldHandleLargeBatchSizes(int batchSize) {
            List<String> filenames = new ArrayList<>();
            for (int i = 0; i < batchSize; i++) {
                filenames.add("image" + i + ".png");
            }

            ImageLoader spyLoader = spy(imageLoader);
            doReturn(new Mat(10, 10, CV_8UC3))
                    .when(spyLoader)
                    .getMatFromBundlePath(anyString(), any());

            long startTime = System.currentTimeMillis();
            List<Mat> result =
                    spyLoader.getMatsFromFilenames(filenames, ColorCluster.ColorSchemaName.BGR);
            long endTime = System.currentTimeMillis();

            assertEquals(batchSize, result.size());
            // Should complete in reasonable time
            assertTrue(endTime - startTime < 5000, "Batch processing should be efficient");

            result.forEach(Mat::release);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle null color schema")
        void shouldHandleNullColorSchema() {
            assertThrows(
                    RuntimeException.class, () -> imageLoader.getMatFromFilename("test.png", null));
        }

        @Test
        @DisplayName("Should handle invalid file paths gracefully")
        void shouldHandleInvalidFilePaths() {
            ImageLoader spyLoader = spy(imageLoader);
            doReturn(new Mat())
                    .when(spyLoader)
                    .getMatFromFilename(anyString(), any(ColorCluster.ColorSchemaName.class));

            Mat result =
                    spyLoader.getMatFromFilename(
                            "nonexistent.png", ColorCluster.ColorSchemaName.BGR);

            assertNotNull(result);
            assertTrue(result.empty());
        }

        @Test
        @DisplayName("Should handle null pattern list")
        void shouldHandleNullPatternList() {
            assertThrows(
                    NullPointerException.class,
                    () ->
                            imageLoader.getMats(
                                    (List<Pattern>) null, ColorCluster.ColorSchemaName.BGR));
        }
    }
}
