package io.github.jspinak.brobot.datatypes.primitives.grid;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GridTest {

    @Test
    void builder_withRowsAndCols_shouldCreateGrid() {
        Region region = new Region(0, 0, 100, 60);
        Grid grid = new Grid.Builder()
                .setRegion(region)
                .setRows(3)
                .setColumns(5)
                .build();

        assertThat(grid.getRows()).isEqualTo(3);
        assertThat(grid.getCols()).isEqualTo(5);
        assertThat(grid.getCellWidth()).isEqualTo(20); // 100 / 5
        assertThat(grid.getCellHeight()).isEqualTo(20); // 60 / 3
        assertThat(grid.getGridRegions()).hasSize(15);
        // Check the last region
        assertThat(grid.getGridRegions().get(14))
                .isEqualTo(new Region(80, 40, 20, 20));
    }

    @Test
    void builder_withCellSize_shouldCreateGrid() {
        Region region = new Region(0, 0, 105, 65); // Dimensions not perfectly divisible
        Grid grid = new Grid.Builder()
                .setRegion(region)
                .setCellWidth(20)
                .setCellHeight(30)
                .build();

        assertThat(grid.getRows()).isEqualTo(2); // 65 / 30 = 2
        assertThat(grid.getCols()).isEqualTo(5); // 105 / 20 = 5
        assertThat(grid.getGridRegions()).hasSize(10);
        // Check the bottom-right region, which should contain the remainder
        assertThat(grid.getGridRegions().get(9))
                .isEqualTo(new Region(80, 30, 25, 35));
    }
}