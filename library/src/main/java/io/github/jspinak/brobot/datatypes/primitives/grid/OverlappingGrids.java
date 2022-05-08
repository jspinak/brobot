package io.github.jspinak.brobot.datatypes.primitives.grid;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class OverlappingGrids {

    private Grid grid;
    private Grid innerGrid; // overlaps grid

    public OverlappingGrids(Grid grid) {
        this.grid = grid;
        Region overlapReg = new Region(
                grid.getRegion().x + grid.getCellWidth()/2,
                grid.getRegion().y + grid.getCellHeight()/2,
                grid.getRegion().w - grid.getCellWidth(),
                grid.getRegion().h - grid.getCellHeight());
        innerGrid = new Grid.Builder()
                .setRegion(overlapReg)
                .setCellWidth(grid.getRegion().w)
                .setCellHeight(grid.getRegion().h)
                .build();
    }

    public List<Region> getAllRegions() {
        List<Region> regions = new ArrayList<>(grid.getGridRegions());
        regions.addAll(innerGrid.getGridRegions());
        regions.sort(Comparator.comparing(Region::getY));
        return regions;
    }
}
