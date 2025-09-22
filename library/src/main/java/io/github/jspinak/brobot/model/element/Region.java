package io.github.jspinak.brobot.model.element;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.bytedeco.opencv.opencv_core.Rect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.capture.ScreenDimensions;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.util.coordinates.CoordinateScaler;
import io.github.jspinak.brobot.util.region.RegionUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a rectangular area on the screen in the Brobot model-based GUI automation framework.
 *
 * <p>A Region is a fundamental data type that defines a rectangular area using x,y coordinates for
 * the top-left corner and width,height dimensions. It serves as the spatial foundation for GUI
 * element location and interaction in the model-based approach.
 *
 * <p>In the context of model-based GUI automation, Regions are used to:
 *
 * <ul>
 *   <li>Define search areas for finding GUI elements (images, text, patterns)
 *   <li>Represent the boundaries of matched GUI elements
 *   <li>Specify areas for mouse and keyboard interactions
 *   <li>Create spatial relationships between GUI elements in States
 * </ul>
 *
 * <p>This class extends SikuliX Region functionality with additional features for the model-based
 * approach, including grid operations, region arithmetic, and integration with other Brobot data
 * types like Match and Location.
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>{@code
 * // Create regions
 * Region fullScreen = new Region();  // Full screen
 * Region area = new Region(100, 100, 200, 150);  // x, y, width, height
 * Region fromMatch = match.getRegion();  // From a match
 *
 * // Use builder for fluent creation
 * Region searchArea = Region.builder()
 *     .withPosition(Positions.Name.TOPRIGHT)
 *     .withSize(300, 200)
 *     .build();
 *
 * // Perform actions on regions
 * action.click(area);  // Click center
 * action.highlight(area);  // Highlight for debugging
 *
 * // Restrict search to region
 * PatternFindOptions options = new PatternFindOptions.Builder()
 *     .setSearchRegions(new SearchRegions(area))
 *     .build();
 * ActionResult result = action.perform(options, targetImage.asObjectCollection());
 *
 * // Region operations
 * Region expanded = area.grow(10);  // Expand by 10 pixels
 * Region shrunken = area.shrink(5);  // Shrink by 5 pixels
 * Region moved = area.move(50, 0);  // Move 50 pixels right
 *
 * // Check containment
 * Location point = new Location(150, 150);
 * if (area.contains(point)) {
 *     System.out.println("Point is inside region");
 * }
 *
 * // Grid operations
 * List<Region> grid = area.getGrid(2, 2);  // 2x2 grid
 * for (Region cell : grid) {
 *     action.highlight(cell);  // Highlight each cell
 * }
 * }</pre>
 *
 * @since 1.0
 * @see Location
 * @see Match
 * @see StateRegion
 * @see RegionUtils
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Region implements Comparable<Region> {

    private int x;
    private int y;
    private int w;
    private int h;

    /**
     * Creates a Region representing the full screen with dynamically detected dimensions.
     *
     * <p>The screen dimensions are detected from the primary screen. Falls back to environment
     * variables SCREEN_WIDTH and SCREEN_HEIGHT if screen detection fails. If all else fails,
     * defaults to 1920x1080.
     */
    public Region() {
        // Use the statically initialized screen dimensions
        // These are set once during startup based on the capture provider
        int width = ScreenDimensions.getWidth();
        int height = ScreenDimensions.getHeight();
        setXYWH(0, 0, width, height);
    }

    /**
     * Creates a RegionBuilder for fluent region creation with screen-size awareness.
     *
     * @return a new RegionBuilder instance
     */
    public static RegionBuilder builder() {
        return new RegionBuilder();
    }

    /**
     * Creates a Region with specified coordinates and dimensions.
     *
     * @param x the x-coordinate of the top-left corner
     * @param y the y-coordinate of the top-left corner
     * @param w the width of the region
     * @param h the height of the region
     */
    public Region(int x, int y, int w, int h) {
        setXYWH(x, y, w, h);
    }

    /**
     * Creates a Region from a Match object.
     *
     * <p>This constructor is commonly used after finding an image to create a Region representing
     * the matched area for further operations.
     *
     * @param match the Match object to convert to a Region
     */
    public Region(Match match) {
        setTo(RegionUtils.convertMatchToRegion(match));
    }

    public Region(org.sikuli.script.Match match) {
        if (match == null) return;
        setXYWH(RegionUtils.extractRegionFromSikuli(match));
    }

    /**
     * Creates a Region as the bounding box containing two Location points.
     *
     * <p>The resulting Region will be the smallest rectangle that contains both locations, useful
     * for defining areas between GUI elements.
     *
     * @param location1 the first location point
     * @param location2 the second location point
     */
    public Region(Location location1, Location location2) {
        setXYWH(RegionUtils.calculateBoundingBox(location1, location2));
    }

    public Region(Region r) {
        setXYWH(r.x, r.y, r.w, r.h);
    }

    public Region(org.sikuli.script.Region region) {
        setTo(region);
    }

    public Region(Rect rect) {
        setXYWH(rect.x(), rect.y(), rect.width(), rect.height());
    }

    /**
     * Converts this Region to a SikuliX Region for compatibility with Sikuli operations.
     *
     * <p>This method enables interoperability with the underlying SikuliX library. Returns null if
     * SikuliX operations should be skipped (e.g., in headless mode).
     *
     * @return a SikuliX Region object, or null if SikuliX is disabled
     */
    @JsonIgnore
    public org.sikuli.script.Region sikuli() {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();

        if (env.shouldSkipSikuliX()) {
            // Return null when SikuliX operations should be skipped
            return null;
        }

        try {
            return new org.sikuli.script.Region(x, y, w, h);
        } catch (org.sikuli.script.SikuliXception e) {
            // Handle headless environment gracefully
            // This happens when tests run in CI/CD without display
            return null;
        }
    }

    /**
     * Converts this Region to a SikuliX Region with coordinate scaling.
     *
     * <p>When captures are done at physical resolution (e.g., 1920x1080 with FFmpeg) but SikuliX
     * operations work in logical resolution (e.g., 1536x864 with 125% DPI), regions need to be
     * scaled appropriately.
     *
     * @param coordinateScaler The scaler to use for converting coordinates
     * @return A SikuliX Region with scaled coordinates, or null if SikuliX operations should be
     *     skipped or an error occurs
     * @since 1.1
     */
    @JsonIgnore
    public org.sikuli.script.Region sikuliScaled(CoordinateScaler coordinateScaler) {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();

        if (env.shouldSkipSikuliX()) {
            // Return null when SikuliX operations should be skipped
            return null;
        }

        try {
            if (coordinateScaler != null && coordinateScaler.isScalingNeeded()) {
                // Scale the region from capture coordinates to logical coordinates
                Region scaledRegion = coordinateScaler.scaleRegionToLogical(this);
                return new org.sikuli.script.Region(
                        scaledRegion.x(), scaledRegion.y(), scaledRegion.w(), scaledRegion.h());
            } else {
                // No scaling needed, return regular sikuli region
                return new org.sikuli.script.Region(x, y, w, h);
            }
        } catch (org.sikuli.script.SikuliXception e) {
            // Handle headless environment gracefully
            // This happens when tests run in CI/CD without display
            return null;
        }
    }

    /**
     * Returns the x-coordinate of the region's top-left corner.
     *
     * @return the x-coordinate
     */
    public int x() {
        return x;
    }

    /**
     * Returns the y-coordinate of the region's top-left corner.
     *
     * @return the y-coordinate
     */
    public int y() {
        return y;
    }

    /**
     * Returns the width of the region.
     *
     * @return the width in pixels
     */
    public int w() {
        return w;
    }

    /**
     * Returns the height of the region.
     *
     * @return the height in pixels
     */
    public int h() {
        return h;
    }

    /**
     * Returns the x-coordinate of the region's bottom-right corner.
     *
     * @return the x-coordinate of the right edge (x + width)
     */
    public int x2() {
        return RegionUtils.x2(this);
    }

    /**
     * Returns the y-coordinate of the region's bottom-right corner.
     *
     * @return the y-coordinate of the bottom edge (y + height)
     */
    public int y2() {
        return RegionUtils.y2(this);
    }

    /**
     * Adjusts the width of the region to set the right edge at the specified x-coordinate.
     *
     * @param x2 the new x-coordinate for the right edge
     */
    public void adjustX2(int x2) {
        RegionUtils.adjustX2(this, x2);
    }

    /**
     * Adjusts the height of the region to set the bottom edge at the specified y-coordinate.
     *
     * @param y2 the new y-coordinate for the bottom edge
     */
    public void adjustY2(int y2) {
        RegionUtils.adjustY2(this, y2);
    }

    /**
     * Adjusts the x-coordinate of the region while maintaining its width.
     *
     * @param newX the new x-coordinate for the top-left corner
     */
    public void adjustX(int newX) {
        RegionUtils.adjustX(this, newX);
    }

    /**
     * Adjusts the y-coordinate of the region while maintaining its height.
     *
     * @param newY the new y-coordinate for the top-left corner
     */
    public void adjustY(int newY) {
        RegionUtils.adjustY(this, newY);
    }

    /**
     * Compares this region to another region for ordering. Regions are compared first by
     * y-coordinate, then by x-coordinate.
     *
     * @param comparesTo the region to compare with
     * @return negative if this region comes before, positive if after, zero if equal
     */
    @Override
    public int compareTo(Region comparesTo) {
        return RegionUtils.compareRegions(this, comparesTo);
    }

    /**
     * Adjusts the region's position and dimensions.
     *
     * @param xAdjust amount to subtract from x-coordinate
     * @param yAdjust amount to subtract from y-coordinate
     * @param wAdjust new width
     * @param hAdjust new height
     */
    public void adjust(int xAdjust, int yAdjust, int wAdjust, int hAdjust) {
        setXYWH(x - xAdjust, y - yAdjust, wAdjust, hAdjust);
    }

    /**
     * Sets all region coordinates and dimensions at once.
     *
     * @param x the x-coordinate of the top-left corner
     * @param y the y-coordinate of the top-left corner
     * @param w the width
     * @param h the height
     */
    public void setXYWH(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    private void setXYWH(int[] ints) {
        if (ints == null || ints.length != 4) return;
        setXYWH(ints[0], ints[1], ints[2], ints[3]);
    }

    @JsonIgnore
    public void setTo(Match match) {
        if (match == null) return;
        setXYWH(match.x(), match.y(), match.w(), match.h());
    }

    @JsonIgnore
    public void setTo(org.sikuli.script.Region region) {
        if (region == null) return;
        setXYWH(region.x, region.y, region.w, region.h);
    }

    @JsonIgnore
    public void setTo(Region region) {
        if (region == null) return;
        setXYWH(region.x, region.y, region.w, region.h);
    }

    /**
     * Creates a StateRegion associated with the NULL state.
     *
     * <p>The NULL state is a special state in the model-based approach that represents GUI elements
     * not associated with any specific state. This is useful for defining regions that exist across
     * multiple states.
     *
     * @return a StateRegion owned by the NULL state
     */
    @JsonIgnore
    public StateRegion inNullState() {
        return new StateRegion.Builder().setOwnerStateName("null").setSearchRegion(this).build();
    }

    /**
     * Checks if this Region has valid dimensions.
     *
     * <p>A Region is considered defined if it has positive width and height. This is important for
     * validating search areas and interaction targets.
     *
     * @return true if the Region has positive width and height, false otherwise
     */
    @JsonIgnore
    public boolean isDefined() {
        return RegionUtils.isDefined(this);
    }

    /**
     * Checks if this region overlaps with another region.
     *
     * @param r the region to check for overlap
     * @return true if the regions overlap, false otherwise
     */
    public boolean overlaps(Region r) {
        return RegionUtils.overlaps(this, r);
    }

    /**
     * Checks if this region completely contains another region.
     *
     * @param r the region to check containment for
     * @return true if this region contains the entire other region
     */
    public boolean contains(Region r) {
        return RegionUtils.contains(this, r);
    }

    /**
     * Checks if this region contains an OpenCV Rect.
     *
     * @param rect the OpenCV rectangle to check
     * @return true if this region contains the entire rectangle
     */
    public boolean contains(Rect rect) {
        return RegionUtils.contains(this, new Region(rect));
    }

    /**
     * Checks if this region contains a specific location.
     *
     * @param l the location to check
     * @return true if the location is within this region's boundaries
     */
    public boolean contains(Location l) {
        return RegionUtils.containsX(this, l) && RegionUtils.containsY(this, l);
    }

    /**
     * Calculates the area of this region.
     *
     * @return the area in pixels (width * height)
     */
    public int size() {
        return RegionUtils.size(this);
    }

    /**
     * Gets the grid cell number for a given location within this region. The region is divided into
     * a grid based on configured rows and columns.
     *
     * @param location the location to find the grid cell for
     * @return Optional containing the grid cell number, or empty if location is outside region
     */
    @JsonIgnore
    public Optional<Integer> getGridNumber(Location location) {
        return RegionUtils.getGridNumber(this, location);
    }

    /**
     * Returns a sub-region corresponding to a specific grid cell. The region is divided into a grid
     * based on configured rows and columns.
     *
     * @param gridNumber the grid cell number (0-based)
     * @return the Region representing the specified grid cell, or this region if gridNumber is -1
     */
    @JsonIgnore
    public Region getGridRegion(int gridNumber) {
        if (gridNumber == -1) return this;
        org.sikuli.script.Region sikuliRegion = sikuli();
        if (sikuliRegion == null) {
            // In headless mode, calculate grid region manually using configured dimensions
            int rows =
                    io.github.jspinak.brobot.tools.testing.mock.grid.MockGridConfig
                            .getDefaultRows();
            int cols =
                    io.github.jspinak.brobot.tools.testing.mock.grid.MockGridConfig
                            .getDefaultCols();
            int cellWidth = w / cols;
            int cellHeight = h / rows;
            int row = RegionUtils.toRow(this, gridNumber);
            int col = RegionUtils.toCol(this, gridNumber);
            return new Region(x + col * cellWidth, y + row * cellHeight, cellWidth, cellHeight);
        }
        return new Region(
                sikuliRegion.getCell(
                        RegionUtils.toRow(this, gridNumber), RegionUtils.toCol(this, gridNumber)));
    }

    /**
     * Gets the grid cell region that contains the specified location.
     *
     * @param location the location to find the containing grid cell for
     * @return Optional containing the grid cell Region, or empty if location is outside region
     */
    @JsonIgnore
    public Optional<Region> getGridRegion(Location location) {
        Optional<Integer> gridNumber = getGridNumber(location);
        return gridNumber.map(this::getGridRegion);
    }

    /**
     * Returns a string representation of this region.
     *
     * @return string in format "R[x.y.w.h]"
     */
    @Override
    public String toString() {
        return "R[" + x + "." + y + "." + w + "." + h + "]";
    }

    /**
     * Checks if this region overlaps with an OpenCV Rect.
     *
     * @param rect the OpenCV rectangle to check for overlap
     * @return true if there is any overlap between this region and the rectangle
     */
    public boolean overlaps(Rect rect) {
        return RegionUtils.overlaps(this, rect);
    }

    /**
     * Checks if this region completely contains a match.
     *
     * @param m the match to check containment for
     * @return true if this region contains the entire match region
     */
    public boolean contains(Match m) {
        return contains(new Region(m));
    }

    /**
     * Checks if this region contains a SikuliX match.
     *
     * @param m the SikuliX match to check
     * @return true if this region contains the entire match region
     */
    @JsonIgnore
    public boolean contains(org.sikuli.script.Match m) {
        return contains(new Region(m));
    }

    /**
     * Checks if this region contains a SikuliX location.
     *
     * @param l the SikuliX location to check
     * @return true if the location is within this region's boundaries
     */
    @JsonIgnore
    public boolean contains(org.sikuli.script.Location l) {
        return new Rectangle(x, y, w, h).contains(l.x, l.y);
    }

    /**
     * Checks if a SikuliX location's x-coordinate is within this region's horizontal bounds.
     *
     * @param l the SikuliX location to check
     * @return true if the location's x-coordinate is within the region's width
     */
    @JsonIgnore
    public boolean containsX(org.sikuli.script.Location l) {
        return l.getX() >= x() && l.getX() <= x() + w();
    }

    /**
     * Checks if a SikuliX location's y-coordinate is within this region's vertical bounds.
     *
     * @param l the SikuliX location to check
     * @return true if the location's y-coordinate is within the region's height
     */
    @JsonIgnore
    public boolean containsY(org.sikuli.script.Location l) {
        return l.getY() >= y() && l.getY() <= y() + h();
    }

    /**
     * Converts this region to a Match object.
     *
     * @return a new Match with this region's boundaries
     */
    @JsonIgnore
    public Match toMatch() {
        return new Match(this);
    }

    /**
     * Divides this Region into a grid and returns all grid cells as separate Regions.
     *
     * <p>This method is useful for systematic searching or interaction with GUI elements arranged
     * in a grid pattern. The cells are returned in reading order (left to right, top to bottom).
     *
     * <p>Note: Uses SikuliX raster with minimum cell size of 5x5 pixels. For finer control, use the
     * Brobot Grid classes.
     *
     * @param rows the number of rows (y-partitions) in the grid
     * @param columns the number of columns (x-partitions) in the grid
     * @return a list of Regions representing each grid cell
     */
    @JsonIgnore
    public List<Region> getGridRegions(int rows, int columns) {
        return RegionUtils.getGridRegions(this, rows, columns);
    }

    @JsonIgnore
    public Optional<Integer> getGridNumber(org.sikuli.script.Location location) {
        return RegionUtils.getGridNumber(this, location);
    }

    @JsonIgnore
    public Optional<Region> getGridRegion(org.sikuli.script.Location location) {
        Optional<Integer> gridNumber = getGridNumber(location);
        return gridNumber.map(this::getGridRegion);
    }

    @JsonIgnore
    public Location getRandomLocation() {
        return RegionUtils.getRandomLocation(this);
    }

    @JsonIgnore
    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder().withRegions(this).build();
    }

    public boolean equals(Region r) {
        return RegionUtils.equals(this, r);
    }

    @JsonIgnore
    public Rect getJavaCVRect() {
        return RegionUtils.getJavaCVRect(this);
    }

    @JsonIgnore
    public Optional<Rect> getOverlappingRect(Rect rect) {
        return RegionUtils.getOverlappingRect(getJavaCVRect(), rect);
    }

    @JsonIgnore
    public Optional<Region> getOverlappingRegion(Region r) {
        return RegionUtils.getOverlappingRegion(this, r);
    }

    @JsonIgnore
    public Region getUnion(Region r) {
        return RegionUtils.getUnion(this, r);
    }

    @JsonIgnore
    public void setAsUnion(Region r) {
        Region union = getUnion(r);
        x = union.x();
        y = union.y();
        w = union.w();
        h = union.h();
    }

    @JsonIgnore
    public Location getLocation() {
        return RegionUtils.getLocation(this);
    }

    /**
     * Calculates the areas of this Region that do not overlap with another Region.
     *
     * <p>This method is useful in the model-based approach for defining search areas that exclude
     * certain GUI elements, or for finding free space around objects.
     *
     * <p>Only the non-overlapping areas of this Region are returned; non-overlapping areas of the
     * parameter Region are not included.
     *
     * @param b the Region that may overlap with this Region
     * @return a list of Regions representing the non-overlapping areas of this Region
     */
    @JsonIgnore
    public List<Region> minus(Region b) {
        return RegionUtils.minus(this, b);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Region region = (Region) obj;
        return x == region.x && y == region.y && w == region.w && h == region.h;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, w, h);
    }
}
