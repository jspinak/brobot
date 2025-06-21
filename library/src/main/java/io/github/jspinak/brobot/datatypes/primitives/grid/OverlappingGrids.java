package io.github.jspinak.brobot.datatypes.primitives.grid;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.report.Report;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Creates two offset grids that overlap to provide comprehensive coverage of grid-like interfaces.
 * 
 * <p>OverlappingGrids addresses a common challenge in grid-based GUI automation: elements 
 * that span cell boundaries or are positioned at cell intersections. By creating two grids 
 * offset by half a cell width and height, this class ensures that any element in a 
 * grid-based layout falls clearly within at least one cell, improving targeting accuracy.</p>
 * 
 * <p>Grid configuration:
 * <ul>
 *   <li><b>Primary Grid</b>: Original grid aligned with the region</li>
 *   <li><b>Inner Grid</b>: Offset by half cell dimensions in both directions</li>
 *   <li>Same cell dimensions for both grids</li>
 *   <li>Inner grid is smaller due to half-cell borders</li>
 * </ul>
 * </p>
 * 
 * <p>Problem scenarios solved:
 * <ul>
 *   <li>Icons positioned at cell boundaries</li>
 *   <li>UI elements that span multiple cells</li>
 *   <li>Click targets at grid intersections</li>
 *   <li>Partially visible elements at grid edges</li>
 *   <li>Misaligned content in imperfect grids</li>
 * </ul>
 * </p>
 * 
 * <p>Common applications:
 * <ul>
 *   <li>Desktop icon grids with large icons</li>
 *   <li>Game boards where pieces sit on intersections</li>
 *   <li>Tile-based maps with overlapping elements</li>
 *   <li>Menu grids with hover effects extending beyond cells</li>
 *   <li>Calendar interfaces with multi-day events</li>
 * </ul>
 * </p>
 * 
 * <p>Coverage guarantee:
 * <ul>
 *   <li>Any point in the original region is covered by at least one cell</li>
 *   <li>Most points are covered by cells from both grids</li>
 *   <li>Elements at boundaries are fully contained in at least one cell</li>
 *   <li>Reduces click accuracy issues near cell edges</li>
 * </ul>
 * </p>
 * 
 * <p>Example - Desktop icons at cell boundaries:
 * <pre>
 * // Original grid might split an icon between cells
 * Grid desktop = new Grid.Builder()
 *     .setRegion(desktopRegion)
 *     .setColumns(8)
 *     .setRows(6)
 *     .build();
 * 
 * // Overlapping grids ensure each icon falls within a cell
 * OverlappingGrids overlap = new OverlappingGrids(desktop);
 * List<Region> allCells = overlap.getAllRegions();
 * </pre>
 * </p>
 * 
 * <p>Implementation details:
 * <ul>
 *   <li>Inner grid starts at (x + cellWidth/2, y + cellHeight/2)</li>
 *   <li>Inner grid dimensions reduced by one cell width/height</li>
 *   <li>Combined regions sorted by Y coordinate for top-to-bottom processing</li>
 *   <li>Typically doubles the number of searchable regions</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, OverlappingGrids provides robust handling of 
 * grid-based interfaces where perfect alignment cannot be assumed. This technique 
 * is essential for reliable automation of real-world applications where visual 
 * elements don't always conform to rigid grid boundaries.</p>
 * 
 * @since 1.0
 * @see Grid
 * @see Region
 */
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
