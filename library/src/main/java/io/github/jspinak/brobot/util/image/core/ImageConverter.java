package io.github.jspinak.brobot.util.image.core;

import java.awt.image.BufferedImage;
import java.util.Optional;

import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.Image;

/**
 * Static utility methods for image format conversions and operations.
 *
 * <p>This class provides convenient static methods for converting between different image
 * representations used in Brobot: Sikuli Image objects, OpenCV Mat objects, BufferedImages, and
 * byte arrays. It serves as a facade over lower-level conversion utilities, providing a simplified
 * API.
 *
 * <p>Supported conversions:
 *
 * <ul>
 *   <li>Image ↔ byte array (for serialization)
 *   <li>Mat → Image (for OpenCV to Sikuli conversion)
 *   <li>Image/BufferedImage → Mat (BGR format)
 *   <li>Image/BufferedImage → Mat (HSV format)
 * </ul>
 *
 * <p>Color space notes:
 *
 * <ul>
 *   <li>BGR: OpenCV's default color format (Blue-Green-Red)
 *   <li>HSV: Hue-Saturation-Value, useful for color-based detection
 *   <li>Conversions handle format changes automatically
 * </ul>
 *
 * <p>Error handling:
 *
 * <ul>
 *   <li>Conversion failures return empty Mat objects
 *   <li>Null inputs are handled gracefully
 *   <li>No exceptions thrown from public methods
 * </ul>
 *
 * <p>Thread safety: All methods are stateless and thread-safe.
 *
 * @see BufferedImageUtilities
 * @see MatrixUtilities
 * @see Image
 * @see Mat
 */
public class ImageConverter {

    /**
     * Converts a Sikuli Image to a byte array.
     *
     * <p>Useful for serialization, network transmission, or storage. The image is encoded using the
     * default format (typically PNG).
     *
     * @param image Sikuli Image to convert
     * @return byte array representation of the image
     */
    public static byte[] getBytes(Image image) {
        return BufferedImageUtilities.toByteArray(image.get());
    }

    /**
     * Creates a Sikuli Image from a byte array.
     *
     * <p>Reconstructs an Image from its byte representation, typically used for deserialization or
     * loading from storage.
     *
     * @param bytes byte array containing encoded image data
     * @return Sikuli Image object
     */
    public static Image getImage(byte[] bytes) {
        return new Image(BufferedImageUtilities.fromByteArray(bytes));
    }

    /**
     * Converts an OpenCV Mat to a Sikuli Image.
     *
     * <p>Enables use of OpenCV-processed images in Sikuli operations. The Mat is expected to be in
     * BGR format.
     *
     * @param mat OpenCV Mat to convert
     * @return Sikuli Image object
     */
    public static Image getImage(Mat mat) {
        return new Image(BufferedImageUtilities.fromMat(mat));
    }

    /**
     * Converts a Sikuli Image to OpenCV Mat in BGR format.
     *
     * <p>BGR (Blue-Green-Red) is OpenCV's default color format. This conversion enables OpenCV
     * operations on Sikuli images.
     *
     * <p>Error handling: Returns an empty Mat if conversion fails, allowing operations to continue
     * without null checks.
     *
     * @param image Sikuli Image to convert
     * @return Mat in BGR format, or empty Mat on conversion failure
     */
    public static Mat getMatBGR(Image image) {
        return getMatBGR(image.get());
    }

    /**
     * Converts a BufferedImage to OpenCV Mat in BGR format.
     *
     * <p>Direct conversion from Java's BufferedImage to OpenCV's Mat. Handles the conversion
     * through MatOps utility.
     *
     * @param bufferedImage Java BufferedImage to convert
     * @return Mat in BGR format, or empty Mat on conversion failure
     */
    public static Mat getMatBGR(BufferedImage bufferedImage) {
        Optional<Mat> matOptional = MatrixUtilities.bufferedImageToMat(bufferedImage);
        return matOptional.orElseGet(Mat::new);
    }

    /**
     * Converts a Sikuli Image to OpenCV Mat in HSV format.
     *
     * <p>HSV (Hue-Saturation-Value) format is useful for color-based image processing as it
     * separates color information (hue) from intensity (value), making color detection more robust
     * to lighting changes.
     *
     * <p>The conversion path: Image → BufferedImage → Mat (BGR) → Mat (HSV)
     *
     * @param image Sikuli Image to convert
     * @return Mat in HSV format, or empty Mat on conversion failure
     */
    public static Mat getMatHSV(Image image) {
        return getMatHSV(image.get());
    }

    /**
     * Converts a BufferedImage to OpenCV Mat in HSV format.
     *
     * <p>Two-step conversion process:
     *
     * <ol>
     *   <li>BufferedImage → Mat (BGR)
     *   <li>Mat (BGR) → Mat (HSV)
     * </ol>
     *
     * @param bufferedImage Java BufferedImage to convert
     * @return Mat in HSV format, or empty Mat on conversion failure
     */
    public static Mat getMatHSV(BufferedImage bufferedImage) {
        Optional<Mat> matOptional = MatrixUtilities.bufferedImageToMat(bufferedImage);
        Mat HSVmat = new Mat();
        if (matOptional.isPresent()) HSVmat = MatrixUtilities.BGRtoHSV(matOptional.get());
        return HSVmat;
    }

    /**
     * Checks if a Sikuli Image contains no image data.
     *
     * <p>An Image is considered empty if its underlying BufferedImage is null. This can occur with
     * uninitialized or failed image loads.
     *
     * @param image Sikuli Image to check
     * @return true if the image contains no data, false otherwise
     */
    public static boolean isEmpty(Image image) {
        return image.get() == null;
    }
}
