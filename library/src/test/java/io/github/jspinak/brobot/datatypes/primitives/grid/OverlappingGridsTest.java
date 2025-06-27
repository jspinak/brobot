package io.github.jspinak.brobot.datatypes.primitives.grid;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Grid;
import io.github.jspinak.brobot.model.element.OverlappingGrids;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;


class OverlappingGridsTest {

    @Test
    void constructor_shouldCreateCorrectlyOverlappedGrids() {
        Region region = new Region(0, 0, 100, 100);
        Grid mainGrid = new Grid.Builder()
                .setRegion(region)
                .setCellWidth(20)
                .setCellHeight(20)
                .build();

        OverlappingGrids overlappingGrids = new OverlappingGrids(mainGrid);
        Grid innerGrid = overlappingGrids.getInnerGrid();

        // Inner grid should be offset by half a cell and smaller by a full cell
        Region expectedInnerRegion = new Region(10, 10, 80, 80);

        assertThat(innerGrid.getRegion()).isEqualTo(expectedInnerRegion);
        assertThat(innerGrid.getGridRegions()).hasSize(16); // 80x80 grid with 20x20 cells
    }

    @Test
    void getAllRegions_shouldReturnCombinedAndSortedRegions() {
        Region region = new Region(0, 0, 40, 40);
        Grid mainGrid = new Grid.Builder().setRegion(region).setCellWidth(20).setCellHeight(20).build();
        OverlappingGrids overlappingGrids = new OverlappingGrids(mainGrid);

        List<Region> allRegions = overlappingGrids.getAllRegions();

        // main grid: 4 regions. inner grid: 1 region. Total: 5 regions.
        assertThat(allRegions).hasSize(5);

        // Check if sorted by 'y' coordinate
        assertThat(allRegions).isSortedAccordingTo((r1, r2) -> Integer.compare(r1.y(), r2.y()));
    }
}