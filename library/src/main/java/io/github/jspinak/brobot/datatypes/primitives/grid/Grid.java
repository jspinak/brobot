package io.github.jspinak.brobot.datatypes.primitives.grid;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
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

    public static class Builder {
        private Region region;
        private int cellWidth;
        private int cellHeight;
        private int rows;
        private int cols;
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

        private void setGrid() {
            if (region == null) return;
            if (cols > 0 && rows > 0) {
                gridRegions = region.getGridRegions(rows, cols);
                cellWidth = gridRegions.get(0).w;
                cellHeight = gridRegions.get(0).h;
                return;
            }
            if (cellHeight > 0 && cellWidth > 0) {
                cols = region.w / cellWidth;
                rows = region.h / cellHeight;
                // shrink the region to fit a grid with the cell width and height
                region = new Region(region.x, region.y, cols * cellWidth, rows * cellHeight);
                gridRegions = region.getGridRegions(rows, cols);
            }
        }

        public Grid build() {
            setGrid();
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
