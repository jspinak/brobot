package io.github.jspinak.brobot.test.utils;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Utility class for creating and managing OpenCV Mat objects in tests.
 * Provides safe Mat creation, validation, and cleanup methods to prevent
 * JVM crashes from invalid Mat operations.
 */
public class MatTestUtils {
    
    private static final Random random = new Random();
    
    /**
     * Creates a valid Mat with specified dimensions and type, initialized with zeros.
     * Guarantees the Mat is not empty and has valid data.
     * 
     * @param rows Height of the Mat
     * @param cols Width of the Mat
     * @param type OpenCV type (e.g., CV_8UC3)
     * @return A properly initialized Mat
     */
    public static Mat createSafeMat(int rows, int cols, int type) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Mat dimensions must be positive: " + rows + "x" + cols);
        }
        Mat mat = Mat.zeros(rows, cols, type).asMat();
        validateMat(mat, "createSafeMat");
        return mat;
    }
    
    /**
     * Creates a Mat filled with a specific color/value.
     * 
     * @param rows Height of the Mat
     * @param cols Width of the Mat  
     * @param type OpenCV type
     * @param scalar Fill value
     * @return A Mat filled with the specified value
     */
    public static Mat createFilledMat(int rows, int cols, int type, Scalar scalar) {
        Mat mat = createSafeMat(rows, cols, type);
        mat.setTo(new Mat(scalar));
        validateMat(mat, "createFilledMat");
        return mat;
    }
    
    /**
     * Creates a grayscale Mat with a specific gray value.
     * 
     * @param rows Height of the Mat
     * @param cols Width of the Mat
     * @param grayValue Gray level (0-255)
     * @return A single-channel grayscale Mat
     */
    public static Mat createGrayMat(int rows, int cols, int grayValue) {
        if (grayValue < 0 || grayValue > 255) {
            throw new IllegalArgumentException("Gray value must be 0-255: " + grayValue);
        }
        return createFilledMat(rows, cols, CV_8UC1, new Scalar(grayValue));
    }
    
    /**
     * Creates a color Mat (BGR) with specified color values.
     * 
     * @param rows Height of the Mat
     * @param cols Width of the Mat
     * @param blue Blue channel value (0-255)
     * @param green Green channel value (0-255)
     * @param red Red channel value (0-255)
     * @return A 3-channel color Mat
     */
    public static Mat createColorMat(int rows, int cols, int blue, int green, int red) {
        return createFilledMat(rows, cols, CV_8UC3, new Scalar(blue, green, red, 0));
    }
    
    /**
     * Creates a Mat with a checkerboard pattern for testing.
     * Useful for testing image processing operations.
     * 
     * @param rows Height of the Mat
     * @param cols Width of the Mat
     * @param squareSize Size of each square in the pattern
     * @return A Mat with checkerboard pattern
     */
    public static Mat createCheckerboardMat(int rows, int cols, int squareSize) {
        Mat mat = createSafeMat(rows, cols, CV_8UC1);
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                boolean isWhite = ((i / squareSize) + (j / squareSize)) % 2 == 0;
                mat.ptr(i, j).put((byte)(isWhite ? 255 : 0));
            }
        }
        
        return mat;
    }
    
    /**
     * Creates a Mat with random noise for testing.
     * 
     * @param rows Height of the Mat
     * @param cols Width of the Mat
     * @param type OpenCV type
     * @return A Mat filled with random values
     */
    public static Mat createNoiseMat(int rows, int cols, int type) {
        Mat mat = createSafeMat(rows, cols, type);
        Mat mean = new Mat(1, 1, type, new Scalar(128, 128, 128, 0));
        Mat stddev = new Mat(1, 1, type, new Scalar(50, 50, 50, 0));
        randn(mat, mean, stddev);
        safeRelease(mean);
        safeRelease(stddev);
        return mat;
    }
    
    /**
     * Creates a Mat with a gradient pattern.
     * 
     * @param rows Height of the Mat
     * @param cols Width of the Mat
     * @param horizontal true for horizontal gradient, false for vertical
     * @return A grayscale Mat with gradient
     */
    public static Mat createGradientMat(int rows, int cols, boolean horizontal) {
        Mat mat = createSafeMat(rows, cols, CV_8UC1);
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int value = horizontal ? (j * 255 / cols) : (i * 255 / rows);
                mat.ptr(i, j).put((byte)value);
            }
        }
        
        return mat;
    }
    
    /**
     * Creates a Mat with a simple geometric shape for testing.
     * 
     * @param rows Height of the Mat
     * @param cols Width of the Mat
     * @param shapeType 0=rectangle, 1=circle, 2=line
     * @return A Mat with the specified shape drawn
     */
    public static Mat createShapeMat(int rows, int cols, int shapeType) {
        Mat mat = createSafeMat(rows, cols, CV_8UC3);
        mat.setTo(new Mat(new Scalar(0, 0, 0, 0))); // Black background
        
        switch (shapeType) {
            case 0: // Rectangle
                rectangle(mat, 
                    new org.bytedeco.opencv.opencv_core.Point(cols/4, rows/4),
                    new org.bytedeco.opencv.opencv_core.Point(3*cols/4, 3*rows/4),
                    new Scalar(255, 255, 255, 0), -1, 8, 0);
                break;
            case 1: // Circle
                circle(mat,
                    new org.bytedeco.opencv.opencv_core.Point(cols/2, rows/2),
                    Math.min(cols, rows) / 4,
                    new Scalar(255, 255, 255, 0), -1, 8, 0);
                break;
            case 2: // Line
                line(mat,
                    new org.bytedeco.opencv.opencv_core.Point(0, 0),
                    new org.bytedeco.opencv.opencv_core.Point(cols, rows),
                    new Scalar(255, 255, 255, 0), 3, 8, 0);
                break;
        }
        
        return mat;
    }
    
    /**
     * Creates a safe MatVector with the specified number of Mats.
     * All Mats will have the same dimensions and type.
     * 
     * @param count Number of Mats to create
     * @param rows Height of each Mat
     * @param cols Width of each Mat
     * @param type OpenCV type
     * @return A MatVector containing valid Mats
     */
    public static MatVector createSafeMatVector(int count, int rows, int cols, int type) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive: " + count);
        }
        
        Mat[] mats = new Mat[count];
        for (int i = 0; i < count; i++) {
            mats[i] = createSafeMat(rows, cols, type);
        }
        
        return new MatVector(mats);
    }
    
    /**
     * Creates a MatVector with gradually changing images for motion detection tests.
     * 
     * @param count Number of frames
     * @param rows Height of each frame
     * @param cols Width of each frame
     * @param changeAmount Amount of change between frames (0-255)
     * @return A MatVector with gradually changing images
     */
    public static MatVector createMotionMatVector(int count, int rows, int cols, int changeAmount) {
        Mat[] mats = new Mat[count];
        
        for (int i = 0; i < count; i++) {
            int grayValue = Math.min(255, i * changeAmount);
            mats[i] = createGrayMat(rows, cols, grayValue);
        }
        
        return new MatVector(mats);
    }
    
    /**
     * Creates a MatVector where one frame has a changed region.
     * Useful for testing motion/change detection.
     * 
     * @param rows Height of each frame
     * @param cols Width of each frame
     * @param changeFrameIndex Which frame should have the change
     * @param changeX X position of change
     * @param changeY Y position of change
     * @param changeSize Size of changed region
     * @return A MatVector with one changed frame
     */
    public static MatVector createMatVectorWithChange(int rows, int cols, 
                                                      int changeFrameIndex,
                                                      int changeX, int changeY, 
                                                      int changeSize) {
        Mat[] mats = new Mat[3];
        
        for (int i = 0; i < 3; i++) {
            mats[i] = createColorMat(rows, cols, 100, 100, 100);
            
            if (i == changeFrameIndex) {
                // Add a changed region
                rectangle(mats[i],
                    new org.bytedeco.opencv.opencv_core.Point(changeX, changeY),
                    new org.bytedeco.opencv.opencv_core.Point(changeX + changeSize, changeY + changeSize),
                    new Scalar(200, 200, 200, 0), -1, 8, 0);
            }
        }
        
        return new MatVector(mats);
    }
    
    /**
     * Validates that a Mat is safe to use.
     * Checks for null, empty, and released states.
     * 
     * @param mat The Mat to validate
     * @param context Context string for error messages
     * @return true if the Mat is valid
     * @throws IllegalStateException if the Mat is invalid
     */
    public static boolean validateMat(Mat mat, String context) {
        if (mat == null) {
            throw new IllegalStateException(context + ": Mat is null");
        }
        if (mat.isNull()) {
            throw new IllegalStateException(context + ": Mat.isNull() is true");
        }
        if (mat.empty()) {
            throw new IllegalStateException(context + ": Mat is empty");
        }
        if (mat.rows() <= 0 || mat.cols() <= 0) {
            throw new IllegalStateException(context + ": Mat has invalid dimensions: " + 
                mat.rows() + "x" + mat.cols());
        }
        return true;
    }
    
    /**
     * Safely releases a Mat if it's not null or already released.
     * 
     * @param mat The Mat to release
     */
    public static void safeRelease(Mat mat) {
        if (mat != null && !mat.isNull()) {
            try {
                mat.release();
            } catch (Exception e) {
                // Ignore - Mat was already released
            }
        }
    }
    
    /**
     * Safely releases multiple Mats.
     * 
     * @param mats The Mats to release
     */
    public static void safeReleaseAll(Mat... mats) {
        for (Mat mat : mats) {
            safeRelease(mat);
        }
    }
    
    /**
     * Safely releases all Mats in a MatVector.
     * 
     * @param matVector The MatVector to release
     */
    public static void safeRelease(MatVector matVector) {
        if (matVector != null) {
            for (long i = 0; i < matVector.size(); i++) {
                safeRelease(matVector.get(i));
            }
        }
    }
    
    /**
     * Creates a deep copy of a Mat to prevent shared data issues.
     * 
     * @param source The Mat to copy
     * @return A new Mat with copied data
     */
    public static Mat safeCopy(Mat source) {
        validateMat(source, "safeCopy source");
        Mat copy = new Mat();
        source.copyTo(copy);
        validateMat(copy, "safeCopy result");
        return copy;
    }
    
    /**
     * Compares two Mats for equality within a tolerance.
     * Useful for testing image processing results.
     * 
     * @param mat1 First Mat
     * @param mat2 Second Mat
     * @param tolerance Maximum difference allowed per pixel
     * @return true if Mats are similar within tolerance
     */
    public static boolean areMatsEqual(Mat mat1, Mat mat2, double tolerance) {
        if (mat1.rows() != mat2.rows() || mat1.cols() != mat2.cols()) {
            return false;
        }
        if (mat1.type() != mat2.type()) {
            return false;
        }
        
        Mat diff = new Mat();
        absdiff(mat1, mat2, diff);
        
        Scalar sum = sumElems(diff);
        double totalDiff = sum.get(0);
        if (mat1.channels() > 1) {
            totalDiff += sum.get(1) + sum.get(2);
        }
        
        safeRelease(diff);
        
        double avgDiff = totalDiff / (mat1.rows() * mat1.cols() * mat1.channels());
        return avgDiff <= tolerance;
    }
    
    /**
     * Prints Mat info for debugging (without printing actual data).
     * 
     * @param mat The Mat to describe
     * @param name Name to identify the Mat
     * @return A string description of the Mat
     */
    public static String describeMat(Mat mat, String name) {
        if (mat == null) {
            return name + ": null";
        }
        if (mat.isNull()) {
            return name + ": isNull";
        }
        if (mat.empty()) {
            return name + ": empty";
        }
        
        return String.format("%s: %dx%d, type=%d, channels=%d", 
            name, mat.rows(), mat.cols(), mat.type(), mat.channels());
    }
}