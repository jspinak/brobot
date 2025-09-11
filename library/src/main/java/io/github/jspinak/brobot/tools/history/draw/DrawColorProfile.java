package io.github.jspinak.brobot.tools.history.draw;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.model.analysis.color.ColorInfo;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.history.HistoryFileNamer;
import io.github.jspinak.brobot.util.image.recognition.ImageLoader;

/**
 * Visual representation generator for color profiles of StateImage objects.
 *
 * <p>DrawColorProfile creates visual comparisons between original images and their computed color
 * profiles. It generates a grid layout where each row represents a StateImage, showing the original
 * image(s) alongside a color swatch representing the average color statistics.
 *
 * <p><b>Visual Output Structure:</b>
 *
 * <ul>
 *   <li>Each row displays one StateImage's visual data
 *   <li>Left side: Original image(s) resized to 40x40 pixels
 *   <li>Right side: 40x40 color swatch showing the mean color from ColorCluster
 *   <li>Rows are separated by 2-pixel spacing for clarity
 * </ul>
 *
 * <p><b>Configuration Parameters:</b>
 *
 * <ul>
 *   <li>Image dimensions: 40x40 pixels (imgsWH)
 *   <li>Spacing between elements: 2 pixels (spaceBetween)
 *   <li>Color space: BGR format for all visualizations
 *   <li>Background: Black with transparency (0,0,0,0)
 * </ul>
 *
 * <p><b>Use Cases:</b>
 *
 * <ul>
 *   <li>Debugging color-based pattern matching by visualizing color profiles
 *   <li>Comparing multiple StateImages' color characteristics side-by-side
 *   <li>Verifying color cluster calculations are producing expected results
 *   <li>Creating visual documentation of color analysis results
 * </ul>
 *
 * <p><b>Relationships:</b>
 *
 * <ul>
 *   <li>Uses {@link ColorCluster} to retrieve mean color statistics
 *   <li>Integrates with {@link HistoryFileNamer} for standardized file naming
 *   <li>Works with {@link StateImage} objects containing color profile data
 *   <li>Utilizes {@link ImageLoader} for image loading operations
 * </ul>
 *
 * @see ColorCluster
 * @see ColorInfo
 * @see StateImage
 * @see DrawClassesLegend
 * @see DrawHistogram
 */
@Component
public class DrawColorProfile {
    private final ImageLoader getImage;
    private final HistoryFileNamer illustrationFilename;

    private int imgsWH = 40;
    private int averageColorX = 42;
    private int spaceBetween = 2;

    public DrawColorProfile(ImageLoader getImage, HistoryFileNamer illustrationFilename) {
        this.getImage = getImage;
        this.illustrationFilename = illustrationFilename;
    }

    /**
     * Generates a visual matrix containing images and their color profiles.
     *
     * <p>Creates a composite visualization where each StateImage is represented as a row. The
     * matrix width dynamically adjusts based on the maximum number of pattern images in any
     * StateImage, ensuring all content fits properly.
     *
     * <p><b>Layout Algorithm:</b>
     *
     * <ol>
     *   <li>Calculate frame width based on max pattern count across all StateImages
     *   <li>Create black transparent background matrix of appropriate dimensions
     *   <li>For each StateImage, draw pattern images on the left
     *   <li>Draw corresponding color profile swatch on the right
     * </ol>
     *
     * @param imgs list of StateImage objects containing patterns and color profiles to visualize
     * @return Mat containing the complete visualization with all images and color swatches
     */
    public Mat getImagesAndProfileMat(List<StateImage> imgs) {
        int maxFiles = getMaxFilenames(imgs);
        averageColorX = maxFiles * (imgsWH + spaceBetween);
        int frameWidth = averageColorX + imgsWH + spaceBetween;
        int frameHeight = (imgsWH + spaceBetween) * imgs.size();
        Mat frame = new Mat(frameHeight, frameWidth, 16, new Scalar(0, 0, 0, 0));
        int y = 0;
        for (StateImage img : imgs) {
            drawImage(frame, img, y);
            drawProfiles(frame, img, y);
            y += imgsWH + spaceBetween;
        }
        return frame;
    }

    /**
     * Creates and saves a color profile visualization to disk.
     *
     * <p>Convenience method that generates the visualization matrix and saves it using the
     * standardized naming convention for color profile illustrations. The output file will be saved
     * with the "colorProfiles" suffix in the illustration directory.
     *
     * @param imgs list of StateImage objects to visualize
     * @see #getImagesAndProfileMat(List)
     */
    public void drawImagesAndProfiles(List<StateImage> imgs) {
        Mat frame = getImagesAndProfileMat(imgs);
        String savePath = illustrationFilename.getFilename(ActionType.CLASSIFY, "colorProfiles");
        imwrite(savePath, frame);
    }

    /**
     * Determines the maximum number of pattern images across all StateImages.
     *
     * <p>Used to calculate the required frame width to accommodate all patterns in the widest row.
     * Ensures at least 1 is returned even for empty lists.
     *
     * @param imgs list of StateImage objects to analyze
     * @return maximum pattern count, minimum value of 1
     */
    private int getMaxFilenames(List<StateImage> imgs) {
        int maxFiles = 1;
        for (StateImage img : imgs) maxFiles = Math.max(maxFiles, img.getPatterns().size());
        return maxFiles;
    }

    /**
     * Draws pattern images for a single StateImage into the visualization frame.
     *
     * <p>Processes each pattern image associated with the StateImage:
     *
     * <ol>
     *   <li>Loads the image from its bundle path
     *   <li>Resizes to fit within 40x40 pixel bounds while maintaining aspect ratio
     *   <li>Places the resized image at the appropriate horizontal position
     * </ol>
     *
     * <p>Images are arranged horizontally with 2-pixel spacing between them. Aspect ratio is
     * preserved during resizing to prevent distortion.
     *
     * @param frame the target matrix to draw into
     * @param img StateImage containing patterns to visualize
     * @param y vertical position (row) where images should be drawn
     */
    private void drawImage(Mat frame, StateImage img, int y) {
        int amountOfFiles = img.getPatterns().size();
        for (int i = 0; i < amountOfFiles; i++) {
            String filename = img.getPatterns().get(i).getImgpath();
            Mat imgFile = getImage.getMatFromBundlePath(filename, BGR);
            Mat resizedDown = new Mat();
            double scaleDown = (double) imgsWH / Math.max(imgFile.cols(), imgFile.rows());
            int newWidth = (int) (imgFile.cols() * scaleDown);
            int newHeight = (int) (imgFile.rows() * scaleDown);
            Size newSize = new Size(newWidth, newHeight);
            resize(imgFile, resizedDown, newSize);
            Rect rect = new Rect((imgsWH + spaceBetween) * i, y, newWidth, newHeight);
            Mat placeHere = frame.apply(rect);
            resizedDown.copyTo(placeHere);
        }
    }

    /**
     * Draws the color profile swatch for a StateImage.
     *
     * <p>Creates a 40x40 pixel color swatch representing the mean color values from the
     * StateImage's ColorCluster. The swatch is positioned to the right of all pattern images,
     * providing a visual summary of the dominant color.
     *
     * <p>The color is extracted using BGR color space and MEAN statistics, creating a solid color
     * block that represents the average color across all pixels in the original image(s).
     *
     * @param frame the target matrix to draw into
     * @param img StateImage containing the color cluster data
     * @param y vertical position (row) where the color swatch should be drawn
     */
    private void drawProfiles(Mat frame, StateImage img, int y) {
        Rect rect = new Rect(averageColorX, y, imgsWH, imgsWH);
        Mat imgMat = frame.apply(rect);
        img.getColorCluster().getMat(BGR, ColorInfo.ColorStat.MEAN, rect.size()).copyTo(imgMat);
    }
}
