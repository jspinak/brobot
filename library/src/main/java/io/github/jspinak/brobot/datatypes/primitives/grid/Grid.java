package io.github.jspinak.brobot.datatypes.primitives.grid;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Grid is either defined by columns and row, or by cell width and cell height.
 * When defined by cell width and cell height, the cells may not cover the region
 * exactly. If this occurs, the remainder areas of the region are discarded and the
 * region is redefined.
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
        Report.println("region = "+region);
        for (int r=0; r<rows; r++) {
            for (int c=0; c<cols; c++) {
                Region reg = gridRegions.get(cols*r+c);
                Report.format("%d.%d_%d.%d ", reg.x(), reg.y(), reg.w(), reg.h());
            }
            Report.println();
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
