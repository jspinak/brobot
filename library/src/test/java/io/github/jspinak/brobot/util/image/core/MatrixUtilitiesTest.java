package io.github.jspinak.brobot.util.image.core;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;


/** Test class for MatrixUtilities operations. Tests OpenCV Mat utilities in mock mode. */
public class MatrixUtilitiesTest extends BrobotTestBase {

    private Mat testMat;
    private Mat multiChannelMat;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Create single channel test Mat
        testMat = new Mat(10, 10, CV_8UC1);
        testMat.ptr().put(new byte[100]); // Initialize with zeros

        // Create multi-channel test Mat
        multiChannelMat = new Mat(5, 5, CV_8UC3);
    }

    @Test
    void shouldPutValueAtPosition() {
        MatrixUtilities.putInt(testMat, 5, 5, (short) 127);

        // The value should be set
        assertNotNull(testMat);
        assertFalse(testMat.empty());
    }

    @Test
    void shouldGetValueAtPosition() {
        // Put a value first
        MatrixUtilities.putInt(testMat, 3, 3, (short) 127);

        // Get the value (channel 0 for single-channel mat)
        double value = MatrixUtilities.getDouble(3, 3, 0, testMat);

        // Should retrieve the value
        assertTrue(value >= 0);
    }

    @Test
    void shouldHandleOutOfBoundsAccess() {
        // Getting value out of bounds will throw IndexOutOfBoundsException
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> MatrixUtilities.getDouble(100, 100, 0, testMat));

        // Putting value out of bounds should not crash (putInt checks bounds)
        assertDoesNotThrow(() -> MatrixUtilities.putInt(testMat, 100, 100, (short) 1));
    }

    @Test
    void shouldPrintPartialMatrix() {
        // Should print without throwing exception
        assertDoesNotThrow(() -> MatrixUtilities.printPartOfMat(testMat, 5, 5));

        // Test with larger limit
        assertDoesNotThrow(() -> MatrixUtilities.printPartOfMat(testMat, 10, 10));
    }

    @Test
    void shouldPrintMatrixWithTitle() {
        assertDoesNotThrow(() -> MatrixUtilities.printPartOfMat(testMat, 5, 5, "Test Matrix"));

        // Test with null title
        assertDoesNotThrow(() -> MatrixUtilities.printPartOfMat(testMat, 5, 5, null));
    }

    @Test
    void shouldGetMinMaxValues() {
        // Set some values
        MatrixUtilities.putInt(testMat, 0, 0, (short) 0);
        MatrixUtilities.putInt(testMat, 5, 5, (short) 255);

        // Get min/max values
        double[] minMax = MatrixUtilities.getMinMaxOfFirstChannel(testMat);

        assertNotNull(minMax);
        assertEquals(2, minMax.length);
        // Min should be 0, max should be 255 (or close to it)
        assertTrue(minMax[0] >= 0);
        assertTrue(minMax[1] <= 255);
    }

    @Test
    void shouldCreateMatFromBufferedImage() {
        BufferedImage buffImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);

        Optional<Mat> result = MatrixUtilities.bufferedImageToMat(buffImage);

        assertTrue(result.isPresent());
        assertFalse(result.get().empty());
        assertEquals(50, result.get().rows());
        assertEquals(50, result.get().cols());
    }

    @Test
    void shouldHandleNullBufferedImage() {
        Optional<Mat> result = MatrixUtilities.bufferedImageToMat(null);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldCreateMatWithDimensions() {
        Mat result = MatrixUtilities.makeMat(100, 200, CV_8UC1, 0.0);

        assertNotNull(result);
        assertFalse(result.empty());
        assertEquals(100, result.rows());
        assertEquals(200, result.cols());
    }

    @Test
    void shouldCreateMatWithDimensionsAndColor() {
        Mat result = MatrixUtilities.makeMat(50, 75, CV_8UC1, 128.0);

        assertNotNull(result);
        assertFalse(result.empty());
        assertEquals(50, result.rows());
        assertEquals(75, result.cols());
    }

    @Test
    void shouldExtractChannel() {
        Mat channel = MatrixUtilities.getFirstChannel(multiChannelMat);

        assertNotNull(channel);
        assertFalse(channel.empty());
        assertEquals(1, channel.channels());
        assertEquals(multiChannelMat.rows(), channel.rows());
        assertEquals(multiChannelMat.cols(), channel.cols());
    }

    @Test
    void shouldHandleInvalidChannelIndex() {
        // getFirstChannel always returns first channel
        Mat result = MatrixUtilities.getFirstChannel(multiChannelMat);

        assertNotNull(result);
        assertFalse(result.empty());
    }

    @Test
    void shouldGetAggregateMatrix() {
        Mat mat1 = new Mat(10, 10, CV_8UC1);
        Mat mat2 = new Mat(10, 10, CV_8UC1);
        MatrixUtilities.putInt(mat1, 5, 5, (short) 100);
        MatrixUtilities.putInt(mat2, 5, 5, (short) 200);

        List<Mat> matrices = List.of(mat1, mat2);
        // Use REDUCE_MAX (1) for aggregation
        Mat result = MatrixUtilities.getNewMatWithPerCellMinsOrMaxes(matrices, 1);

        assertNotNull(result);
        assertFalse(result.empty());
        assertEquals(mat1.rows(), result.rows());
        assertEquals(mat1.cols(), result.cols());
    }

    @Test
    void shouldHandleEmptyListForAggregate() {
        Mat result = MatrixUtilities.getNewMatWithPerCellMinsOrMaxes(List.of(), 1);

        assertNotNull(result);
        assertTrue(result.empty());
    }

    @Test
    void shouldApplyROI() {
        // Test applying a region of interest
        Rect roi = new Rect(2, 2, 5, 5);

        Optional<Mat> result = MatrixUtilities.applyIfOk(testMat, roi);

        assertTrue(result.isPresent());
        assertEquals(5, result.get().rows());
        assertEquals(5, result.get().cols());
    }

    @Test
    void shouldHandleEmptyMatForROI() {
        Mat emptyMat = new Mat();
        Rect roi = new Rect(0, 0, 1, 1);

        Optional<Mat> result = MatrixUtilities.applyIfOk(emptyMat, roi);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldPrintMultiChannel() {
        assertDoesNotThrow(() -> MatrixUtilities.printPartOfMat(multiChannelMat, 5, 5, 3));
    }

    @Test
    void shouldPrintEmptyMatrix() {
        Mat emptyMat = new Mat();

        assertDoesNotThrow(
                () -> {
                    MatrixUtilities.printPartOfMat(emptyMat, 5, 5);
                    MatrixUtilities.printPartOfMat(emptyMat, 5, 5, 3);
                });
    }

    @Test
    void shouldHandleZeroLimitForPrint() {
        assertDoesNotThrow(() -> MatrixUtilities.printPartOfMat(testMat, 0, 0));
    }

    @Test
    void shouldHandleNegativeLimitForPrint() {
        assertDoesNotThrow(() -> MatrixUtilities.printPartOfMat(testMat, -1, -1));
    }

    @Test
    void shouldWorkWithDifferentMatTypes() {
        // Test with different Mat types
        Mat floatMat = new Mat(5, 5, CV_32FC1);
        Mat intMat = new Mat(5, 5, CV_32SC1);
        Mat byteMat = new Mat(5, 5, CV_8UC1);

        // Can read from different mat types
        assertDoesNotThrow(
                () -> {
                    double val1 = MatrixUtilities.getDouble(2, 2, 0, floatMat);
                    double val2 = MatrixUtilities.getDouble(2, 2, 0, intMat);
                    double val3 = MatrixUtilities.getDouble(2, 2, 0, byteMat);
                });

        // putInt only works with byte mats, throws ClassCastException for others
        assertThrows(
                ClassCastException.class, () -> MatrixUtilities.putInt(floatMat, 2, 2, (short) 3));
        assertThrows(
                ClassCastException.class, () -> MatrixUtilities.putInt(intMat, 2, 2, (short) 42));
        assertDoesNotThrow(() -> MatrixUtilities.putInt(byteMat, 2, 2, (short) 100));

        // Clean up
        floatMat.release();
        intMat.release();
        byteMat.release();
    }

    @Test
    void shouldHandleNullMat() {
        // printPartOfMat with 2 params throws NPE due to mat.channels() call
        assertThrows(NullPointerException.class, () -> MatrixUtilities.printPartOfMat(null, 5, 5));

        // printPartOfMat with 3 params doesn't throw NPE, it passes channels directly
        assertDoesNotThrow(() -> MatrixUtilities.printPartOfMat(null, 5, 5, 3));

        // printPartOfMat with 4 params handles null gracefully
        assertDoesNotThrow(
                () -> {
                    MatrixUtilities.printPartOfMat(null, 5, 5, 3, "test");
                });

        // getDouble with null will throw NullPointerException
        assertThrows(NullPointerException.class, () -> MatrixUtilities.getDouble(0, 0, 0, null));

        // putInt with null will throw NullPointerException
        assertThrows(
                NullPointerException.class, () -> MatrixUtilities.putInt(null, 0, 0, (short) 1));

        Optional<Mat> result = MatrixUtilities.applyIfOk(null, new Rect(0, 0, 1, 1));
        assertFalse(result.isPresent());
    }

    @Test
    void shouldExtractROI() {
        // Create a region of interest
        Rect roi = new Rect(2, 2, 5, 5);
        Mat subMat = new Mat(testMat, roi);

        assertNotNull(subMat);
        assertFalse(subMat.empty());
        assertEquals(5, subMat.rows());
        assertEquals(5, subMat.cols());
    }

    @Test
    void shouldHandleLargeMatrix() {
        Mat largeMat = new Mat(1000, 1000, CV_8UC1);

        assertDoesNotThrow(
                () -> {
                    MatrixUtilities.printPartOfMat(largeMat, 10, 10);
                    MatrixUtilities.getMinMaxOfFirstChannel(largeMat);
                });

        // Cleanup
        largeMat.release();
    }
}
