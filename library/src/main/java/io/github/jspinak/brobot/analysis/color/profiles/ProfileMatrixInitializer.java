package io.github.jspinak.brobot.analysis.color.profiles;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.recognition.ImageLoader;

/**
 * Initializes one-column color matrices for state images to enable unified color analysis.
 *
 * <p>This class handles the preprocessing of state images by converting multiple image files of
 * potentially different sizes into standardized one-column matrices. This transformation is
 * essential for performing consistent color analysis across all images associated with a state,
 * regardless of their individual dimensions.
 *
 * <p>The one-column format is particularly important because:
 *
 * <ul>
 *   <li>State images can contain multiple files of different sizes
 *   <li>Color analysis algorithms require uniform data structures
 *   <li>Statistical operations are more efficient on continuous data
 *   <li>K-means clustering needs all pixels in a single structure
 * </ul>
 *
 * <p>Both BGR and HSV color space representations are created to support different types of color
 * analysis and matching strategies.
 *
 * @see StateImage
 * @see ColorMatrixUtilities
 * @see ImageLoader
 */
@Component
public class ProfileMatrixInitializer {

    private final ImageLoader getImage;
    private final ColorMatrixUtilities matOps3d;

    /**
     * Constructs an ProfileMatrixInitializer instance with required dependencies.
     *
     * @param getImage Service for retrieving image matrices from state images
     * @param matOps3d Utility for 3D matrix operations on multi-channel images
     */
    public ProfileMatrixInitializer(ImageLoader getImage, ColorMatrixUtilities matOps3d) {
        this.getImage = getImage;
        this.matOps3d = matOps3d;
    }

    /**
     * Creates and sets one-column color matrices for a state image in both BGR and HSV color
     * spaces.
     *
     * <p>This method performs the following operations:
     *
     * <ol>
     *   <li>Retrieves all image files associated with the state image
     *   <li>Concatenates them vertically into a single-column BGR matrix
     *   <li>Converts the BGR matrix to HSV color space
     *   <li>Stores both matrices in the state image for future analysis
     * </ol>
     *
     * <p>The one-column format allows uniform processing of multiple images regardless of their
     * original dimensions. Each pixel from all images is represented as a single row in the
     * resulting matrix, with three columns for the color channels.
     *
     * @param stateImage The state image to process. This object is modified by setting its
     *     oneColumnBGRMat and oneColumnHSVMat fields.
     */
    public void setOneColumnMats(StateImage stateImage) {
        // Skip processing if no patterns are defined
        if (stateImage.getPatterns() == null || stateImage.getPatterns().isEmpty()) {
            stateImage.setOneColumnBGRMat(new Mat());
            stateImage.setOneColumnHSVMat(new Mat());
            return;
        }

        List<Mat> imgMatsBGR = getImage.getMats(stateImage, BGR);

        // Skip if no images were loaded (e.g., files don't exist yet)
        if (imgMatsBGR == null || imgMatsBGR.isEmpty()) {
            stateImage.setOneColumnBGRMat(new Mat());
            stateImage.setOneColumnHSVMat(new Mat());
            return;
        }

        Mat oneColumnBGRMat = matOps3d.vConcatToSingleColumnPerChannel(imgMatsBGR);

        // Check if the BGR mat is empty before processing
        if (oneColumnBGRMat == null || oneColumnBGRMat.empty()) {
            // Set empty mats for both BGR and HSV
            stateImage.setOneColumnBGRMat(new Mat());
            stateImage.setOneColumnHSVMat(new Mat());
            return;
        }

        stateImage.setOneColumnBGRMat(oneColumnBGRMat);
        Mat oneColumnHSVMat = new Mat();
        cvtColor(stateImage.getOneColumnBGRMat(), oneColumnHSVMat, COLOR_BGR2HSV);
        stateImage.setOneColumnHSVMat(oneColumnHSVMat);
    }
}
