package io.github.jspinak.brobot.util.image.recognition;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.sikuli.script.Match;
import org.sikuli.script.Region;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * OpenCV-based template matching implementation for image recognition.
 * <p>
 * This component provides template matching functionality using OpenCV's matchTemplate
 * function, converting results to Sikuli Match objects for integration with Brobot's
 * automation framework. It uses normalized correlation coefficient matching for
 * robust pattern recognition.
 * <p>
 * Key features:
 * <ul>
 * <li>Direct OpenCV Mat-based template matching</li>
 * <li>Configurable similarity threshold</li>
 * <li>Automatic conversion to Sikuli Match objects</li>
 * <li>Size validation to prevent invalid operations</li>
 * <li>Optional return pattern for no-match scenarios</li>
 * </ul>
 * <p>
 * Matching algorithm:
 * <ul>
 * <li>Method: TM_CCOEFF_NORMED (Normalized Correlation Coefficient)</li>
 * <li>Range: 0.0 to 1.0 (1.0 = perfect match)</li>
 * <li>Returns location of best match if above threshold</li>
 * <li>Single best match only (no multi-match support)</li>
 * </ul>
 * <p>
 * Use cases:
 * <ul>
 * <li>Custom template matching outside standard Find operations</li>
 * <li>Performance-critical matching scenarios</li>
 * <li>Integration with custom image processing pipelines</li>
 * <li>Testing and debugging template matching algorithms</li>
 * </ul>
 * <p>
 * Limitations:
 * <ul>
 * <li>Finds only the single best match</li>
 * <li>No rotation or scale invariance</li>
 * <li>Template must be smaller than search image</li>
 * <li>No sub-pixel accuracy</li>
 * </ul>
 *
 * @see Match
 * @see Mat
 */
@Component
public class MatImageRecognition {

    /**
     * Finds the best match of a template within a search image.
     * <p>
     * Performs normalized cross-correlation template matching to locate
     * the template pattern within the search image. Returns the location
     * with the highest correlation score if it exceeds the threshold.
     * <p>
     * Algorithm steps:
     * <ol>
     * <li>Validate input images are non-empty</li>
     * <li>Check template fits within search image</li>
     * <li>Apply matchTemplate with TM_CCOEFF_NORMED</li>
     * <li>Find location of maximum correlation</li>
     * <li>Return Match if score exceeds threshold</li>
     * </ol>
     * <p>
     * The correlation score ranges from 0.0 to 1.0, where:
     * <ul>
     * <li>1.0 = Perfect match (rare in practice)</li>
     * <li>0.9+ = Excellent match</li>
     * <li>0.8+ = Good match</li>
     * <li>0.7+ = Fair match (typical minimum threshold)</li>
     * </ul>
     *
     * @param template the pattern to search for; must be smaller than searchImage
     * @param searchImage the image to search within; must be larger than template
     * @param threshold minimum correlation score (0.0-1.0) to consider a match
     * @return Optional containing Match if found above threshold, empty otherwise
     * @throws RuntimeException if either image is empty/null
     */
    public Optional<Match> findTemplateMatch(Mat template, Mat searchImage, double threshold) {
        // Ensure the loaded images are valid
        if (template.empty() || searchImage.empty()) {
            throw new RuntimeException("Error loading images.");
        }
        if (template.rows() > searchImage.rows() || template.cols() > searchImage.cols())
            return Optional.empty();

        // Convert images to compatible format if needed
        Mat processedTemplate = ensureCompatibleFormat(template);
        Mat processedSearchImage = ensureCompatibleFormat(searchImage);
        
        // Ensure both images have the same number of channels
        if (processedTemplate.channels() != processedSearchImage.channels()) {
            // Convert both to 3-channel BGR if they differ
            if (processedTemplate.channels() == 1) {
                Mat temp = new Mat();
                cvtColor(processedTemplate, temp, COLOR_GRAY2BGR);
                if (processedTemplate != template) {
                    processedTemplate.release();
                }
                processedTemplate = temp;
            }
            if (processedSearchImage.channels() == 1) {
                Mat temp = new Mat();
                cvtColor(processedSearchImage, temp, COLOR_GRAY2BGR);
                if (processedSearchImage != searchImage) {
                    processedSearchImage.release();
                }
                processedSearchImage = temp;
            }
        }

        // Create a result Mat to store the correlation scores
        // Result dimensions: (searchImage.width - template.width + 1) x (searchImage.height - template.height + 1)
        Mat result = new Mat();

        // Perform template matching using normalized correlation coefficient
        // Each pixel in result contains the match score for template placed at that position
        matchTemplate(processedSearchImage, processedTemplate, result, TM_CCOEFF_NORMED);

        // Find the location of the best match (highest correlation)
        double[] minVal = new double[1];  // Minimum correlation (ignored for TM_CCOEFF_NORMED)
        double[] maxVal = new double[1];  // Maximum correlation (best match score)
        Point minLoc = new Point();       // Location of minimum (ignored)
        Point maxLoc = new Point();       // Location of maximum (best match position)
        minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, null);

        // Clean up temporary Mats if created
        if (processedTemplate != template && !processedTemplate.isNull()) {
            processedTemplate.release();
        }
        if (processedSearchImage != searchImage && !processedSearchImage.isNull()) {
            processedSearchImage.release();
        }
        result.release();

        // Check if the best match exceeds the threshold
        if (maxVal[0] >= threshold) {
            // Create a Match object at the found location
            // maxLoc contains the top-left corner of the best match
            Match match = new Match(new Region((int) maxLoc.x(), (int) maxLoc.y(), template.cols(), template.rows()), maxVal[0]);

            // Return the Match object wrapped in an Optional
            return Optional.of(match);
        } else {
            // No match found above threshold
            return Optional.empty();
        }
    }
    
    /**
     * Ensures the image is in a compatible format for template matching.
     * Converts 4-channel images (BGRA) to 3-channel (BGR).
     * 
     * @param image The image to process
     * @return The processed image (may be the same object if no conversion needed)
     */
    private Mat ensureCompatibleFormat(Mat image) {
        if (image.channels() == 4) {
            // Convert BGRA to BGR
            Mat converted = new Mat();
            cvtColor(image, converted, COLOR_BGRA2BGR);
            return converted;
        }
        // Return as-is for 1-channel (grayscale) or 3-channel (BGR) images
        return image;
    }

}
