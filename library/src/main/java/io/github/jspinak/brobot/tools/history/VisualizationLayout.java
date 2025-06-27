package io.github.jspinak.brobot.tools.history;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.util.image.visualization.MatBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides matrix operations for arranging images in column-based layouts.
 * <p>
 * This utility class handles the complex task of organizing multiple OpenCV Mat images
 * into structured column layouts. It supports both single-column and multi-column
 * arrangements with configurable spacing, making it essential for creating organized
 * visual reports and sidebars in the Brobot history visualization system.
 * <p>
 * Key features:
 * <ul>
 * <li>Multi-column layout generation from lists of images</li>
 * <li>Configurable spacing between entries</li>
 * <li>Automatic distribution of images across columns</li>
 * <li>Support for both vertical and horizontal merging</li>
 * <li>Flexible column extraction for partial layouts</li>
 * </ul>
 * <p>
 * Layout structure:
 * <pre>
 * Column 1    Column 2    Column 3
 * ┌─────┐     ┌─────┐     ┌─────┐
 * │ Img1│     │ Img4│     │ Img7│
 * └─────┘     └─────┘     └─────┘
 *   |||         |||         |||
 * ┌─────┐     ┌─────┐     ┌─────┐
 * │ Img2│     │ Img5│     │ Img8│
 * └─────┘     └─────┘     └─────┘
 *   |||         |||         |||
 * ┌─────┐     ┌─────┐     ┌─────┐
 * │ Img3│     │ Img6│     │ Img9│
 * └─────┘     └─────┘     └─────┘
 * </pre>
 * <p>
 * Common use cases:
 * <ul>
 * <li>Creating sidebar visualizations for match results</li>
 * <li>Organizing classification results in grid layouts</li>
 * <li>Building comparison views of multiple images</li>
 * <li>Generating thumbnail galleries for reports</li>
 * </ul>
 *
 * @see MatBuilder
 * @see AnalysisSidebar
 * @see StateIllustration
 */
@Component
public class VisualizationLayout {

    /**
     * Creates a multi-column layout matrix from a list of images.
     * <p>
     * This method organizes images into a grid layout with the specified number of
     * columns and rows. Images are distributed column-by-column (top to bottom,
     * then left to right). Each column is first created separately, then all
     * columns are merged horizontally.
     * <p>
     * Distribution example with 9 images, 3 columns, 3 per column:
     * <ul>
     * <li>Column 1: images 0, 1, 2</li>
     * <li>Column 2: images 3, 4, 5</li>
     * <li>Column 3: images 6, 7, 8</li>
     * </ul>
     *
     * @param imagesMats List of OpenCV Mat images to arrange. Can be any size.
     * @param matchesPerColumn Maximum number of images per column.
     * @param columns Number of columns in the final layout.
     * @param spacesBetweenEntries Pixel spacing between images (both horizontal and vertical).
     * @return A single Mat containing all images arranged in the specified column layout.
     */
    public Mat getColumnMat(List<Mat> imagesMats, int matchesPerColumn, int columns, int spacesBetweenEntries) {
        List<Mat> columnMats = new ArrayList<>();
        for (int i=0; i<columns; i++) {
            List<Mat> columnMatsEntries = getColumnEntries(imagesMats, i, matchesPerColumn);
            Mat columnMat = mergeColumnMats(columnMatsEntries, spacesBetweenEntries);
            columnMats.add(columnMat);
        }
        return mergeColumnMats(columnMats, spacesBetweenEntries);
    }

    /**
     * Merges multiple column matrices horizontally into a single matrix.
     * <p>
     * This method takes pre-built column matrices and combines them side-by-side
     * with the specified spacing. It's used both for final layout assembly and
     * for merging individual columns.
     *
     * @param columnMats List of column matrices to merge horizontally.
     * @param spacesBetweenEntries Pixel spacing between columns.
     * @return A single Mat with all columns merged horizontally.
     */
    public Mat mergeColumnMats(List<Mat> columnMats, int spacesBetweenEntries) {
        return new MatBuilder()
                .setName("sidebar")
                .setSpaceBetween(spacesBetweenEntries)
                .addHorizontalSubmats(columnMats)
                .build();
    }

    /**
     * Extracts entries for a specific column from the full list of images.
     * <p>
     * This method calculates which images belong to the specified column based on
     * the column index and matches per column. It handles cases where the last
     * column may have fewer entries than the maximum.
     * <p>
     * Index calculation: startIndex = column * matchesPerColumn
     *
     * @param entries Complete list of Mat images to distribute.
     * @param column Zero-based column index to extract.
     * @param matchesPerColumn Maximum number of images per column.
     * @return List of Mat images belonging to the specified column.
     */
    public List<Mat> getColumnEntries(List<Mat> entries, int column, int matchesPerColumn) {
        List<Mat> sidebarEntries = new ArrayList<>();
        for (int i = 0; i < matchesPerColumn; i++) {
            int matchIndex = column * matchesPerColumn + i;
            if (matchIndex == entries.size()) break;
            sidebarEntries.add(entries.get(matchIndex));
        }
        return sidebarEntries;
    }

    /**
     * Creates a single column matrix from a subset of entries.
     * <p>
     * This convenience method combines entry extraction and column building.
     * It first extracts the appropriate entries for the specified column,
     * then builds them into a vertical column layout.
     *
     * @param entries Complete list of Mat images.
     * @param column Zero-based column index to create.
     * @param matchesPerColumn Maximum number of images per column.
     * @return A Mat containing the specified column with its entries.
     */
    public Mat getColumnMat(List<Mat> entries, int column, int matchesPerColumn) {
        List<Mat> columnEntries = getColumnEntries(entries, column, matchesPerColumn);
        return getColumnMat(columnEntries);
    }

    /**
     * Creates a single column matrix from a list of images.
     * <p>
     * This method arranges the provided images vertically with a fixed 4-pixel
     * spacing between them. It's the core building block for creating column
     * layouts, used by other methods to build individual columns before merging.
     *
     * @param entriesInColumn List of Mat images to arrange vertically.
     * @return A Mat containing all images arranged in a single column.
     */
    public Mat getColumnMat(List<Mat> entriesInColumn) {
        Mat sidebar = new MatBuilder()
                .setName("sidebar")
                .setSpaceBetween(4)
                .addVerticalSubmats(entriesInColumn)
                .build();
        return sidebar;
    }
}
