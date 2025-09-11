package io.github.jspinak.brobot.model.element;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

import lombok.Getter;

/**
 * Creates two offset grids that overlap to provide comprehensive coverage of grid-like interfaces.
 *
 * <p>OverlappingGrids addresses a common challenge in grid-based GUI automation: elements that span
 * cell boundaries or are positioned at cell intersections. By creating two grids offset by half a
 * cell width and height, this class ensures that any element in a grid-based layout falls clearly
 * within at least one cell, improving targeting accuracy.
 *
 * <p>Grid configuration:
 *
 * <ul>
 *   <li><b>Primary Grid</b>: Original grid aligned with the region
 *   <li><b>Inner Grid</b>: Offset by half cell dimensions in both directions
 *   <li>Same cell dimensions for both grids
 *   <li>Inner grid is smaller due to half-cell borders
 * </ul>
 *
 * <p>Problem scenarios solved:
 *
 * <ul>
 *   <li>Icons positioned at cell boundaries
 *   <li>UI elements that span multiple cells
 *   <li>Click targets at grid intersections
 *   <li>Partially visible elements at grid edges
 *   <li>Misaligned content in imperfect grids
 * </ul>
 *
 * <p>Common applications:
 *
 * <ul>
 *   <li>Desktop icon grids with large icons
 *   <li>Game boards where pieces sit on intersections
 *   <li>Tile-based maps with overlapping elements
 *   <li>Menu grids with hover effects extending beyond cells
 *   <li>Calendar interfaces with multi-day events
 * </ul>
 *
 * <p>Coverage guarantee:
 *
 * <ul>
 *   <li>Any point in the original region is covered by at least one cell
 *   <li>Most points are covered by cells from both grids
 *   <li>Elements at boundaries are fully contained in at least one cell
 *   <li>Reduces click accuracy issues near cell edges
 * </ul>
 *
 * <p>Example - Desktop icons at cell boundaries:
 *
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
 *
 * <p>Implementation details:
 *
 * <ul>
 *   <li>Inner grid starts at (x + cellWidth/2, y + cellHeight/2)
 *   <li>Inner grid dimensions reduced by one cell width/height
 *   <li>Combined regions sorted by Y coordinate for top-to-bottom processing
 *   <li>Typically doubles the number of searchable regions
 * </ul>
 *
 * <p>In the model-based approach, OverlappingGrids provides robust handling of grid-based
 * interfaces where perfect alignment cannot be assumed. This technique is essential for reliable
 * automation of real-world applications where visual elements don't always conform to rigid grid
 * boundaries.
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
        Region overlapReg =
                new Region(
                        grid.getRegion().x() + grid.getCellWidth() / 2,
                        grid.getRegion().y() + grid.getCellHeight() / 2,
                        grid.getRegion().w() - grid.getCellWidth(),
                        grid.getRegion().h() - grid.getCellHeight());
        innerGrid =
                new Grid.Builder()
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
        ConsoleReporter.formatln("cell w.h = %d.%d", grid.getCellWidth(), grid.getCellHeight());
        ConsoleReporter.formatln("main grid x.y.w.h = %d.%d.%d.%d", g.x(), g.y(), g.w(), g.h());
        ConsoleReporter.formatln("inner grid x.y.w.h = %d.%d.%d.%d", i.x(), i.y(), i.w(), i.h());
    }
}
