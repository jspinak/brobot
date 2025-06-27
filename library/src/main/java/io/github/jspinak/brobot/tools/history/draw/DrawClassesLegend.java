package io.github.jspinak.brobot.tools.history.draw;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.analysis.color.profiles.ProfileMatrixBuilder;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.history.VisualizationLayout;
import io.github.jspinak.brobot.tools.history.visual.Visualization;
import io.github.jspinak.brobot.util.image.visualization.MatBuilder;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates visual legends for image classification results and color analysis.
 * 
 * <p>DrawClassesLegend generates a multi-column legend that displays StateImage objects
 * alongside their computed k-means color centers. This provides a visual reference for
 * understanding how images are classified and what color profiles they contain.</p>
 * 
 * <p><b>Visual Output Structure:</b></p>
 * <ul>
 *   <li>Organized in paired columns: images on left, k-means centers on right</li>
 *   <li>Each entry is 40x40 pixels with 4-pixel spacing between entries</li>
 *   <li>Multiple column pairs are created as needed to fit all images</li>
 *   <li>Columns are dynamically sized based on scene height</li>
 * </ul>
 * 
 * <p><b>Configuration Parameters:</b></p>
 * <ul>
 *   <li>Entry dimensions: 40x40 pixels (entryW x entryH)</li>
 *   <li>Spacing between entries: 4 pixels</li>
 *   <li>Spacing between column pairs: 8 pixels (2x entry spacing)</li>
 *   <li>Automatic column calculation based on scene dimensions</li>
 * </ul>
 * 
 * <p><b>Use Cases:</b></p>
 * <ul>
 *   <li>Creating visual documentation of classification results</li>
 *   <li>Debugging image classification by showing input images with their color profiles</li>
 *   <li>Generating legends for automated testing reports</li>
 *   <li>Providing visual context for pattern matching operations</li>
 * </ul>
 * 
 * <p><b>Relationships:</b></p>
 * <ul>
 *   <li>Works with {@link Visualization} to manage the complete visualization</li>
 *   <li>Uses {@link ProfileMatrixBuilder} to generate image and profile matrices</li>
 *   <li>Integrates with {@link VisualizationLayout} for multi-column layout management</li>
 *   <li>Complements {@link DrawColorProfile} by showing k-means centers instead of mean colors</li>
 * </ul>
 * 
 * @see DrawColorProfile
 * @see Visualization
 * @see ProfileMatrixBuilder
 * @see VisualizationLayout
 * @see StateImage
 */
@Component
public class DrawClassesLegend {
    private ProfileMatrixBuilder profileMatrixBuilder;
    private VisualizationLayout columnMatOps;

    private int spacesBetweenEntries = 4;

    private int entryW = 40, entryH = 40;

    int matchesPerColumn;
    int columns;

    public DrawClassesLegend(ProfileMatrixBuilder profileMatrixBuilder, VisualizationLayout columnMatOps) {
        this.profileMatrixBuilder = profileMatrixBuilder;
        this.columnMatOps = columnMatOps;
    }

    /**
     * Initializes sidebar dimensions based on scene size and image count.
     * 
     * <p>Calculates how many image entries can fit vertically in the scene
     * and determines the required number of columns to display all images.
     * This dynamic sizing ensures the legend scales appropriately with
     * different scene dimensions.</p>
     * 
     * @param scene the main scene matrix used to determine available height
     * @param numberOfImages total count of images to display in the legend
     */
    public void initSidebar(Mat scene, int numberOfImages) {
        matchesPerColumn = scene.rows() / (entryH + spacesBetweenEntries);
        columns = (int) Math.ceil((double) numberOfImages / matchesPerColumn);
    }

    /**
     * Generates and attaches a complete legend to the illustrations object.
     * 
     * <p>Creates a visual legend showing:</p>
     * <ol>
     *   <li>Original images from each StateImage's patterns</li>
     *   <li>Corresponding k-means color centers for each StateImage</li>
     * </ol>
     * 
     * <p>The legend is arranged in paired columns where each StateImage's
     * patterns appear alongside their computed color centers. This provides
     * immediate visual correlation between source images and their color analysis.</p>
     * 
     * @param visualization container for all visualization components
     * @param imgs list of StateImage objects to include in the legend
     * @param actionOptions configuration options affecting color profile display
     */
    public void drawLegend(Visualization visualization, List<StateImage> imgs, ActionOptions actionOptions) {
        if (visualization.getScene() == null) return;
        initSidebar(visualization.getScene(), imgs.size());
        List<Mat> imgEntries = getAllImageEntries(imgs);
        List<Mat> kmeansEntries = getAllColorEntries(imgs, actionOptions);
        Mat legend = getImagesAndKmeansCentersColumns(imgEntries, kmeansEntries);
        visualization.setLegend(legend);
    }

    /**
     * Combines the classification results with the legend into a single visualization.
     * 
     * <p>Merges the matches-on-classes visualization with the generated legend,
     * creating a complete visual report. The legend appears to the right of
     * the classification results, providing context for interpreting the matches.</p>
     * 
     * @param visualization container holding both classification results and legend
     */
    public void mergeClassesAndLegend(Visualization visualization) {
        if (visualization.getMatchesOnClasses() == null) return;
        Mat classesAndLegend = new MatBuilder()
                .addHorizontalSubmats(visualization.getMatchesOnClasses(), visualization.getLegend())
                .setSpaceBetween(spacesBetweenEntries)
                .build();
        visualization.setClassesWithMatchesAndLegend(classesAndLegend);
    }

    /**
     * Arranges images and k-means centers into paired column layout.
     * 
     * <p>Creates a multi-column visualization where:</p>
     * <ul>
     *   <li>Each column pair shows images on the left, k-means centers on the right</li>
     *   <li>Columns are filled vertically before starting new column pairs</li>
     *   <li>Double spacing (8 pixels) separates column pairs for clarity</li>
     * </ul>
     * 
     * @param imgs list of image matrices to display
     * @param kmeansCenters corresponding k-means center matrices
     * @return combined matrix with all column pairs arranged horizontally
     */
    private Mat getImagesAndKmeansCentersColumns(List<Mat> imgs, List<Mat> kmeansCenters) {
        List<Mat> imgKmeansColumns = new ArrayList<>();
        for (int i=0; i<columns; i++) {
            Mat imgColumn = columnMatOps.getColumnMat(imgs, i, matchesPerColumn);
            Mat kmeansColumn = columnMatOps.getColumnMat(kmeansCenters, i, matchesPerColumn);
            Mat imgAndKmeans = new MatBuilder()
                    .addHorizontalSubmats(imgColumn, kmeansColumn)
                    .setSpaceBetween(spacesBetweenEntries)
                    .build();
            imgKmeansColumns.add(imgAndKmeans);
        }
        return columnMatOps.mergeColumnMats(imgKmeansColumns, spacesBetweenEntries * 2);
    }

    /**
     * Extracts and prepares all image entries for the legend.
     * 
     * <p>Processes each StateImage to create a horizontal arrangement of its
     * pattern images. Multiple patterns within a StateImage are displayed
     * side-by-side with standard spacing.</p>
     * 
     * @param imgs list of StateImage objects to process
     * @return list of matrices, each containing one StateImage's patterns
     */
    private List<Mat> getAllImageEntries(List<StateImage> imgs) {
        List<Mat> sidebarEntries = new ArrayList<>();
        for (StateImage img : imgs) {
            Mat entry = getImagesEntryForClassesLegend(img);
            sidebarEntries.add(entry);
        }
        return sidebarEntries;
    }

    /**
     * Creates a single legend entry showing all patterns from a StateImage.
     * 
     * <p>Arranges multiple pattern images horizontally if the StateImage
     * contains more than one pattern. This provides a complete view of
     * all images that contribute to the StateImage's classification.</p>
     * 
     * @param img StateImage containing patterns to visualize
     * @return matrix with horizontally arranged pattern images
     */
    private Mat getImagesEntryForClassesLegend(StateImage img) {
        return new MatBuilder()
                .setName("classes legend")
                .addHorizontalSubmats(profileMatrixBuilder.getImagesMat(img))
                .setSpaceBetween(spacesBetweenEntries)
                .build();
    }

    /**
     * Extracts and prepares all k-means color center entries.
     * 
     * <p>Processes each StateImage to create visual representations of its
     * computed k-means color centers. These centers represent the dominant
     * colors found through clustering analysis.</p>
     * 
     * @param imgs list of StateImage objects to process
     * @param actionOptions configuration affecting color profile generation
     * @return list of matrices, each containing one StateImage's color centers
     */
    private List<Mat> getAllColorEntries(List<StateImage> imgs, ActionOptions actionOptions) {
        List<Mat> sidebarEntries = new ArrayList<>();
        for (StateImage img : imgs) {
            Mat entry = getKmeansCenterEntry(img, actionOptions);
            sidebarEntries.add(entry);
        }
        return sidebarEntries;
    }

    /**
     * Creates a single legend entry showing k-means color centers.
     * 
     * <p>Generates visual swatches representing the k-means cluster centers
     * computed from the StateImage's color analysis. Multiple centers are
     * arranged horizontally to show the color distribution.</p>
     * 
     * @param img StateImage containing color cluster data
     * @param actionOptions configuration for profile visualization
     * @return matrix with horizontally arranged color center swatches
     */
    private Mat getKmeansCenterEntry(StateImage img, ActionOptions actionOptions) {
        return new MatBuilder()
                .setName("classes legend")
                .addHorizontalSubmats(profileMatrixBuilder.getProfilesMat(img, actionOptions))
                .setSpaceBetween(spacesBetweenEntries)
                .build();
    }
}
