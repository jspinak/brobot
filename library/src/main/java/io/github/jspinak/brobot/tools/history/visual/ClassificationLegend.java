package io.github.jspinak.brobot.tools.history.visual;

import static org.bytedeco.opencv.global.opencv_imgproc.FONT_HERSHEY_SIMPLEX;
import static org.bytedeco.opencv.global.opencv_imgproc.putText;

import java.util.List;

import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.ColorInfo;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Creates color-coded legends for classification visualization results.
 *
 * <p>This component generates visual legends that map colors to their corresponding state images in
 * classification analyses. The legend appears as a sidebar with colored squares representing each
 * classified element, enabling viewers to understand what each color represents in the main
 * classification visualization.
 *
 * <p>Legend structure:
 *
 * <ul>
 *   <li>Grid layout with configurable entry size (default 50x50 pixels)
 *   <li>Multi-column display when entries exceed screen height
 *   <li>Each entry shows the mean color of its corresponding class
 *   <li>White background for clear visibility
 * </ul>
 *
 * <p>Layout algorithm:
 *
 * <ul>
 *   <li>Entries arranged vertically first, then in additional columns
 *   <li>Column width determined by entry size
 *   <li>Number of rows calculated based on screen height
 *   <li>Automatic sidebar width adjustment for all entries
 * </ul>
 *
 * <p>Visual characteristics:
 *
 * <ul>
 *   <li>BGR color space for consistency with OpenCV
 *   <li>Mean color representation from color clusters
 *   <li>1-pixel padding around each color square
 *   <li>Support for future text labels (method exists but unused)
 * </ul>
 *
 * <p>Integration notes:
 *
 * <ul>
 *   <li>Works with {@link SceneAnalysis} for classification data
 *   <li>Uses {@link ColorCluster} for color statistics
 *   <li>Designed to be merged with classification visualizations
 *   <li>Complements {@link DrawClassifications} and {@link DrawClassesLegend}
 * </ul>
 *
 * @see SceneAnalysis
 * @see StateImage
 * @see ColorCluster
 * @see DrawClassesLegend
 */
@Component
public class ClassificationLegend {

    /** Width of each legend entry in pixels. */
    private int sidebarEntryW = 50;

    /** Height of each legend entry in pixels. */
    private int sidebarEntryH = 50;

    /**
     * Number of entries that fit vertically in one column. Calculated based on screen height during
     * initialization.
     */
    int labelsPerColumn;

    /**
     * Initializes a white sidebar matrix sized to accommodate all legend entries.
     *
     * <p>Calculates the required dimensions based on:
     *
     * <ul>
     *   <li>Screen height to determine entries per column
     *   <li>Total number of entries to determine column count
     *   <li>Entry dimensions for spacing calculations
     * </ul>
     *
     * The sidebar width includes an extra column for padding/overflow.
     *
     * @param screen reference matrix for height and type information
     * @param matchesSize number of legend entries to display
     * @return white background Mat sized for all entries
     */
    private Mat initSidebar(Mat screen, int matchesSize) {
        labelsPerColumn = screen.rows() / sidebarEntryH;
        int sidebarW = (matchesSize / labelsPerColumn + 1) * sidebarEntryW;
        return new Mat(screen.rows(), sidebarW, screen.type(), new Scalar(255, 255, 255, 255));
    }

    /**
     * Creates a complete legend showing color mappings for all classified images.
     *
     * <p>Generates a grid of colored squares where each square represents a state image's mean
     * color from its color cluster. The layout fills vertically first, then moves to the next
     * column.
     *
     * <p>Processing steps:
     *
     * <ol>
     *   <li>Extract state images from scene analysis
     *   <li>Initialize sidebar with calculated dimensions
     *   <li>For each image, calculate grid position
     *   <li>Extract mean color from the image's color cluster
     *   <li>Copy color square to the appropriate position
     * </ol>
     *
     * <p>Grid positioning formula:
     *
     * <ul>
     *   <li>Column: index / labelsPerColumn
     *   <li>Row: index % labelsPerColumn
     *   <li>1-pixel offset for padding between entries
     * </ul>
     *
     * @param screen reference matrix for sizing the legend height
     * @param sceneAnalysis contains state images with color cluster data
     * @return completed legend Mat with all color entries
     */
    public Mat draw(Mat screen, SceneAnalysis sceneAnalysis) {
        List<StateImage> imgs = sceneAnalysis.getStateImageObjects();
        Mat sidebar = initSidebar(screen, imgs.size());
        int i = 0;
        int x, y;
        for (StateImage img : imgs) {
            x = (i / labelsPerColumn) * sidebarEntryW + 1;
            y = (i % labelsPerColumn) * sidebarEntryH + 1;
            int labelWidth = sidebarEntryW - 2;
            int labelHeight = sidebarEntryH - 2;
            Size sidebarEntrySize = new Size(labelWidth, labelHeight);
            Rect sidebarEntry = new Rect(x, y, labelWidth, labelHeight);
            Mat targetInSidebar = sidebar.apply(sidebarEntry);
            Mat entryBGR =
                    img.getColorCluster()
                            .getMat(
                                    ColorCluster.ColorSchemaName.BGR,
                                    ColorInfo.ColorStat.MEAN,
                                    sidebarEntrySize);
            entryBGR.copyTo(targetInSidebar);
            i++;
        }
        return sidebar;
    }

    /**
     * Adds a text label to a legend entry (currently unused).
     *
     * <p>This method is intended for future enhancement where text labels could be overlaid on
     * color squares to show image names. Current implementation has hardcoded values and would need
     * adjustment for dynamic positioning.
     *
     * <p>Known issues:
     *
     * <ul>
     *   <li>Fixed position doesn't adapt to entry location
     *   <li>Displays "text" instead of actual image name
     *   <li>White text may not be visible on light colors
     * </ul>
     *
     * @param img state image whose name should be displayed
     * @param mat target matrix for the text (legend entry)
     */
    private void addLabel(StateImage img, Mat mat) {
        String text = img.getName();
        Point position = new Point(170, 280);
        Scalar color = new Scalar(255);
        int font = FONT_HERSHEY_SIMPLEX;
        int scale = 1;
        int thickness = 3;
        putText(mat, "text", position, font, scale, color);
    }
}
