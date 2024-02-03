package io.github.jspinak.brobot.datatypes.primitives.grid;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class OverlappingGrids {

    private final Grid grid;
    private final Grid innerGrid; // overlaps grid

    public OverlappingGrids(Grid grid) {
        this.grid = grid;
        Region overlapReg = new Region(
                grid.getRegion().x() + grid.getCellWidth()/2,
                grid.getRegion().y() + grid.getCellHeight()/2,
                grid.getRegion().w() - grid.getCellWidth(),
                grid.getRegion().h() - grid.getCellHeight());
        innerGrid = new Grid.Builder()
                .setRegion(overlapReg)
                .setCellWidth(grid.getCellWidth())
                .setCellHeight(grid.getCellHeight())
                .build();
    }

    public List<Region> getAllRegions() {
        List<Region> regions = new ArrayList<>(grid.getGridRegions());
        regions.addAll(innerGrid.getGridRegions());
        regions.sort(Comparator.comparing(Region::y));
        return regions;
    }

    public void print() {
        Region g = grid.getRegion();
        Region i = innerGrid.getRegion();
        Report.formatln("cell w.h = %d.%d", grid.getCellWidth(), grid.getCellHeight());
        Report.formatln("main grid x.y.w.h = %d.%d.%d.%d", g.x(), g.y(), g.w(), g.h());
        Report.formatln("inner grid x.y.w.h = %d.%d.%d.%d", i.x(), i.y(), i.w(), i.h());
    }
}
