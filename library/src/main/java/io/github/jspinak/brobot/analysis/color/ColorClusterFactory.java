package io.github.jspinak.brobot.analysis.color;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.ColorSchema;
import io.github.jspinak.brobot.model.analysis.color.ColorSchemaBGR;
import io.github.jspinak.brobot.model.analysis.color.ColorSchemaHSV;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

/**
 * Creates color profiles from image data for pattern matching.
 * 
 * <p>ColorClusterFactory analyzes pixel data to generate statistical color profiles
 * in both BGR and HSV color spaces. These profiles capture the color characteristics
 * of images, enabling robust color-based pattern matching throughout the Brobot
 * framework.</p>
 * 
 * <p>Key operations:</p>
 * <ul>
 *   <li>Converts image pixels to single-column format for analysis</li>
 *   <li>Calculates statistical measures (min, max, mean, stddev) per channel</li>
 *   <li>Generates profiles for both BGR and HSV color spaces</li>
 *   <li>Supports masked analysis for selective pixel inclusion</li>
 * </ul>
 * 
 * <p>The generated {@link ColorCluster} objects are used throughout the color
 * matching pipeline for:</p>
 * <ul>
 *   <li>K-means clustering analysis</li>
 *   <li>Color-based region detection</li>
 *   <li>State image color profiling</li>
 *   <li>Dynamic color tolerance matching</li>
 * </ul>
 * 
 * @see ColorCluster
 * @see ColorSchema
 * @see ColorMatrixUtilities
 */
@Component
public class ColorClusterFactory {

    private ColorMatrixUtilities matOps3d;

    public ColorClusterFactory(ColorMatrixUtilities matOps3d) {
        this.matOps3d = matOps3d;
    }

    /**
     * Creates a color profile from BGR image data.
     * 
     * <p>Analyzes all pixels in the provided single-column BGR matrix to generate
     * comprehensive color statistics. Uses a full mask (all pixels included) for
     * the analysis.</p>
     * 
     * @param oneColumnBGRMat single-column BGR matrix containing all pixels to analyze
     * @return ColorCluster containing BGR and HSV color profiles
     */
    public ColorCluster getColorProfile(Mat oneColumnBGRMat) {
        return getColorProfile(oneColumnBGRMat, new Mat(oneColumnBGRMat.size(), oneColumnBGRMat.type(), new Scalar(255, 255, 255, 0))); // mask needs 3 channels
    }

    /**
     * Creates a color profile from BGR image data with masking.
     * 
     * <p>Generates both BGR and HSV color profiles from the input data. The mask
     * parameter allows selective pixel analysis, useful for k-means clustering
     * where different pixels belong to different clusters.</p>
     * 
     * <p>The method automatically converts BGR data to HSV for dual color space
     * analysis, providing more robust matching capabilities.</p>
     * 
     * @param oneColumnBGRMat single-column BGR matrix containing all pixels to analyze
     * @param mask 3-channel mask indicating which pixels to include (255 = include, 0 = exclude)
     * @return ColorCluster containing both BGR and HSV color profiles
     */
    public ColorCluster getColorProfile(Mat oneColumnBGRMat, Mat mask) {
        ColorCluster colorCluster = new ColorCluster();
        ColorSchema colorSchemaBGR = getColorSchema(oneColumnBGRMat, mask, ColorCluster.ColorSchemaName.BGR);
        colorCluster.setSchema(ColorCluster.ColorSchemaName.BGR, colorSchemaBGR);
        Mat oneColumnHSVMat = new Mat();
        cvtColor(oneColumnBGRMat, oneColumnHSVMat, COLOR_BGR2HSV);
        ColorSchema colorSchemaHSV = getColorSchema(oneColumnHSVMat, mask, ColorCluster.ColorSchemaName.HSV);
        colorCluster.setSchema(ColorCluster.ColorSchemaName.HSV, colorSchemaHSV);
        return colorCluster;
    }

    /**
     * Creates a color schema with statistical analysis for the specified color space.
     * 
     * <p>Calculates comprehensive statistics for each color channel:</p>
     * <ul>
     *   <li>Minimum and maximum values</li>
     *   <li>Mean (average) values</li>
     *   <li>Standard deviation</li>
     * </ul>
     * 
     * <p>The mask parameter is crucial for k-means clustering applications where
     * different pixels belong to different clusters and must be analyzed separately.</p>
     * 
     * @param oneCol3ChanMat single-column 3-channel matrix in the specified color space
     * @param masks mask indicating which pixels to include in statistics
     * @param schema color space type (BGR or HSV) for proper schema creation
     * @return ColorSchema containing statistical data for all three channels
     */
    public ColorSchema getColorSchema(Mat oneCol3ChanMat, Mat masks, ColorCluster.ColorSchemaName schema) {
        // get the mean and stddev for each channel
        MatVector meanStddev = matOps3d.mEanStdDev(oneCol3ChanMat, masks);
        Mat means = meanStddev.get(0);
        Mat stddevs = meanStddev.get(1);
        // get the min and max for each channel
        DoublePointer min = new DoublePointer(3);
        DoublePointer max = new DoublePointer(3);
        matOps3d.minMax(oneCol3ChanMat, min, max, masks);
        ColorSchema colorSchema;
        if (schema == ColorCluster.ColorSchemaName.BGR) colorSchema = new ColorSchemaBGR();
        else colorSchema = new ColorSchemaHSV();
        for (int i=0; i<3; i++) {
            colorSchema.setValues(i, min.get(i), max.get(i),
                    means.createIndexer().getDouble(0,0,i),
                    stddevs.createIndexer().getDouble(0,0,i));
        }
        return colorSchema;
    }

}
