package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Divides a screen region into a matrix of cells for systematic interaction.
 * 
 * <p>Grid provides a structured way to interact with regularly arranged GUI elements 
 * such as icon grids, table cells, calendar dates, or tile-based interfaces. By 
 * dividing a region into rows and columns, Grid enables precise targeting of 
 * individual elements within repetitive layouts without defining each element 
 * separately.</p>
 * 
 * <p>Definition modes:
 * <ul>
 *   <li><b>By Dimensions</b>: Specify number of rows and columns</li>
 *   <li><b>By Cell Size</b>: Specify cell width and height</li>
 *   <li>Hybrid approach with intelligent remainder handling</li>
 * </ul>
 * </p>
 * 
 * <p>Remainder handling strategies:
 * <ul>
 *   <li><b>Adjust Region</b>: Shrink region to fit exact grid (adjustRegionToGrids=true)</li>
 *   <li><b>Expand Last Cells</b>: Rightmost and bottom cells absorb extra space</li>
 *   <li><b>Smart Expansion</b>: Add extra row/column if remainder > half cell size</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Desktop icon grids and application launchers</li>
 *   <li>Calendar date selection (7 columns × n rows)</li>
 *   <li>Spreadsheet or table cell navigation</li>
 *   <li>Game boards (chess, tic-tac-toe, etc.)</li>
 *   <li>Photo galleries and thumbnail grids</li>
 *   <li>Virtual keyboards and keypads</li>
 * </ul>
 * </p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Flexible definition by count or size</li>
 *   <li>Intelligent edge handling for imperfect fits</li>
 *   <li>Direct access to individual cell regions</li>
 *   <li>Maintains spatial relationships between cells</li>
 *   <li>Debugging support with visual printing</li>
 * </ul>
 * </p>
 * 
 * <p>Example - Click on calendar date:
 * <pre>
 * Grid calendar = new Grid.Builder()
 *     .setRegion(calendarRegion)
 *     .setColumns(7)  // Days of week
 *     .setRows(5)     // Weeks shown
 *     .build();
 * Region dateCell = calendar.getGridRegions().get(dayIndex);
 * </pre>
 * </p>
 * 
 * <p>Edge case handling:
 * <ul>
 *   <li>Non-uniform cell sizes at edges accommodate imperfect divisions</li>
 *   <li>Rightmost column may be wider to include remainder pixels</li>
 *   <li>Bottom row may be taller for the same reason</li>
 *   <li>Ensures complete coverage of original region</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Grid abstracts repetitive GUI layouts into 
 * manageable structures. This enables efficient interaction with matrix-based 
 * interfaces without manually defining dozens of individual regions, while 
 * maintaining the precision needed for reliable automation.</p>
 * 
 * @since 1.0
 * @see Region
 * @see OverlappingGrids
 */
@Getter
public class Grid {

    private Region region;
    private int cellWidth;
    private int cellHeight;
    private int rows;
    private int cols;
    private List<Region> gridRegions = new ArrayList<>();

    public void print() {
        ConsoleReporter.println("region = "+region);
        for (int r=0; r<rows; r++) {
            for (int c=0; c<cols; c++) {
                Region reg = gridRegions.get(cols*r+c);
                ConsoleReporter.format("%d.%d_%d.%d ", reg.x(), reg.y(), reg.w(), reg.h());
            }
            ConsoleReporter.println();
        }
    }

    public static class Builder {
        private Region region;
        private int cellWidth = 0;
        private int cellHeight = 0;
        private int rows = 0;
        private int cols = 0;
        private boolean adjustRegionToGrids = false; // when true, the region will be cut to fit the grids; otherwise, end grids may be smaller than others
        private List<Region> gridRegions = new ArrayList<>();

        public Builder setRegion(Region region) {
            this.region = region;
            return this;
        }

        public Builder setCellWidth(int cellWidth) {
            this.cellWidth = cellWidth;
            return this;
        }

        public Builder setCellHeight(int cellHeight) {
            this.cellHeight = cellHeight;
            return this;
        }

        public Builder setRows(int rows) {
            this.rows = rows;
            return this;
        }

        public Builder setColumns(int cols) {
            this.cols = cols;
            return this;
        }

        private void setGridRegions() {
            if (region == null) region = new Region();
            if (cellHeight > 0 && cellWidth > 0) { // width and height are defined
                cols = region.w() / cellWidth; // there could be a small region at the edges
                rows = region.h() / cellHeight;
            } else { // use rows and columns
                cellWidth = region.w() / cols;
                cellHeight = region.h() / rows;
            }
            cols = Math.max(cols, 1);
            rows = Math.max(rows, 1);
            // when selected, shrink the region to fit a grid with the cell width and height
            if (adjustRegionToGrids)
                region = new Region(region.x(), region.y(), cols * cellWidth, rows * cellHeight);
            int wRemainder = region.w() - cols * cellWidth;
            int hRemainder = region.h() - rows * cellHeight;
            // if the remainder is larger than half a cell, create a new cell; otherwise, add to the last cell
            if (wRemainder > cellWidth / 2) cols++;
            if (hRemainder > cellHeight / 2) rows++;
            int rightmostCellWidth = region.w() - (cols-1) * cellWidth;
            int bottommostCellHeight = region.h() - (rows-1) * cellHeight;
            int x = region.x(), y = region.y();
            for (int r=0; r<rows-1; r++) {
                for (int c=0; c<cols-1; c++) {
                    gridRegions.add(new Region(x+c*cellWidth, y+r*cellHeight, cellWidth, cellHeight));
                }
                // add the last region on the right, which might be bigger
                gridRegions.add(new Region(x+(cols-1)*cellWidth,y+r*cellHeight,
                        rightmostCellWidth, cellHeight));
            }
            // add the bottow row
            for (int c=0; c<cols-1; c++) gridRegions.add(
                    new Region(x+c*cellWidth, y+(rows-1)*cellHeight, cellWidth, bottommostCellHeight));
            gridRegions.add(new Region(x+(cols-1)*cellWidth, y+(rows-1)*cellHeight,
                    rightmostCellWidth, bottommostCellHeight));
        }

        public Grid build() {
            setGridRegions();
            Grid grid = new Grid();
            grid.region = region;
            grid.cellHeight = cellHeight;
            grid.cellWidth = cellWidth;
            grid.rows = rows;
            grid.cols = cols;
            grid.gridRegions = gridRegions;
            return grid;
        }
    }

}
