package io.github.jspinak.brobot.test;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

/**
 * Factory for creating mock Mat objects for testing. Helps tests that need OpenCV Mat objects work
 * in headless environments.
 */
public class MockMatFactory {

    /** Create a mock Mat object with specified dimensions */
    public static Mat createMockMat(int width, int height, int type) {
        Mat mockMat = mock(Mat.class);
        when(mockMat.cols()).thenReturn(width);
        when(mockMat.rows()).thenReturn(height);
        when(mockMat.type()).thenReturn(type);
        when(mockMat.channels()).thenReturn(CV_MAT_CN(type));
        when(mockMat.depth()).thenReturn(CV_MAT_DEPTH(type));
        when(mockMat.empty()).thenReturn(false);
        when(mockMat.total()).thenReturn((long) (width * height));

        Size size = new Size(width, height);
        when(mockMat.size()).thenReturn(size);

        return mockMat;
    }

    /** Create a mock Mat for a standard RGB image */
    public static Mat createMockRGBMat(int width, int height) {
        return createMockMat(width, height, CV_8UC3);
    }

    /** Create a mock Mat for a grayscale image */
    public static Mat createMockGrayMat(int width, int height) {
        return createMockMat(width, height, CV_8UC1);
    }

    /** Create a real Mat object if OpenCV is available, otherwise return mock */
    public static Mat createSafeMat(int width, int height, int type) {
        try {
            // Try to create real Mat
            Mat mat = new Mat(height, width, type);
            // Initialize mat with zeros
            // mat.setTo(new Scalar(0, 0, 0, 0)); // This method signature may not be available
            return mat;
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            // OpenCV native libraries not available, return mock
            return createMockMat(width, height, type);
        }
    }

    /** Convert BufferedImage to Mat safely (returns mock if native libs unavailable) */
    public static Mat bufferedImageToMat(BufferedImage image) {
        if (image == null) {
            return null;
        }

        try {
            // Try real conversion
            int type = image.getType() == BufferedImage.TYPE_BYTE_GRAY ? CV_8UC1 : CV_8UC3;
            Mat mat = new Mat(image.getHeight(), image.getWidth(), type);

            byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            mat.data().put(pixels);

            return mat;
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            // Return mock Mat with correct dimensions
            int type = image.getType() == BufferedImage.TYPE_BYTE_GRAY ? CV_8UC1 : CV_8UC3;
            return createMockMat(image.getWidth(), image.getHeight(), type);
        }
    }

    /** Check if OpenCV native libraries are available */
    public static boolean isOpenCVAvailable() {
        try {
            // Try to create a small Mat
            Mat test = new Mat(1, 1, CV_8UC1);
            test.release();
            return true;
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            return false;
        }
    }

    /** Create a mock Mat that simulates being empty */
    public static Mat createEmptyMockMat() {
        Mat mockMat = mock(Mat.class);
        when(mockMat.empty()).thenReturn(true);
        when(mockMat.cols()).thenReturn(0);
        when(mockMat.rows()).thenReturn(0);
        when(mockMat.total()).thenReturn(0L);
        return mockMat;
    }
}
