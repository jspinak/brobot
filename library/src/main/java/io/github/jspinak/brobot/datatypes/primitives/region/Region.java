package io.github.jspinak.brobot.datatypes.primitives.region;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.actions.BrobotEnvironment;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Rect;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a rectangular area on the screen in the Brobot model-based GUI automation framework.
 * 
 * <p>A Region is a fundamental data type that defines a rectangular area using x,y coordinates 
 * for the top-left corner and width,height dimensions. It serves as the spatial foundation for 
 * GUI element location and interaction in the model-based approach.</p>
 * 
 * <p>In the context of model-based GUI automation, Regions are used to:
 * <ul>
 *   <li>Define search areas for finding GUI elements (images, text, patterns)</li>
 *   <li>Represent the boundaries of matched GUI elements</li>
 *   <li>Specify areas for mouse and keyboard interactions</li>
 *   <li>Create spatial relationships between GUI elements in States</li>
 * </ul>
 * </p>
 * 
 * <p>This class extends SikuliX Region functionality with additional features for the 
 * model-based approach, including grid operations, region arithmetic, and integration 
 * with other Brobot data types like Match and Location.</p>
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
     * Creates a Region representing the full screen with default or environment-specified dimensions.
     * 
     * <p>The screen dimensions can be overridden using SCREEN_WIDTH and SCREEN_HEIGHT 
     * environment variables. Defaults to 1920x1080 if not specified.</p>
     */
    public Region() {
        int defaultWidth = Integer.parseInt(System.getenv().getOrDefault("SCREEN_WIDTH", "1920"));
        int defaultHeight = Integer.parseInt(System.getenv().getOrDefault("SCREEN_HEIGHT", "1080"));
        setXYWH(0, 0, defaultWidth, defaultHeight);
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
        setXYWH(x,y,w,h);
    }

    /**
     * Creates a Region from a Match object.
     * 
     * <p>This constructor is commonly used after finding an image to create a Region 
     * representing the matched area for further operations.</p>
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
     * <p>The resulting Region will be the smallest rectangle that contains both locations,
     * useful for defining areas between GUI elements.</p>
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

    public Region(org.sikuli.script.Region region) { setTo(region); }

    public Region(Rect rect) {
        setXYWH(rect.x(), rect.y(), rect.width(), rect.height());
    }

    /**
     * Converts this Region to a SikuliX Region for compatibility with Sikuli operations.
     * 
     * <p>This method enables interoperability with the underlying SikuliX library.
     * Returns null if SikuliX operations should be skipped (e.g., in headless mode).</p>
     * 
     * @return a SikuliX Region object, or null if SikuliX is disabled
     */
    @JsonIgnore
    public org.sikuli.script.Region sikuli() {
        BrobotEnvironment env = BrobotEnvironment.getInstance();
        
        if (env.shouldSkipSikuliX()) {
            // Return null when SikuliX operations should be skipped
            return null;
        }
        return new org.sikuli.script.Region(x, y, w, h);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int w() {
        return w;
    }

    public int h() {
        return h;
    }

    public int x2() {
        return RegionUtils.x2(this);
    }

    public int y2() {
        return RegionUtils.y2(this);
    }

    public void adjustX2(int x2) {
        RegionUtils.adjustX2(this, x2);
    }

    public void adjustY2(int y2) {
        RegionUtils.adjustY2(this, y2);
    }

    public void adjustX(int newX) {
        RegionUtils.adjustX(this, newX);
    }

    public void adjustY(int newY) {
        RegionUtils.adjustY(this, newY);
    }

    @Override
    public int compareTo(Region comparesTo) {
        return RegionUtils.compareRegions(this, comparesTo);
    }

    public void adjust(int xAdjust, int yAdjust, int wAdjust, int hAdjust) {
        setXYWH(x - xAdjust, y - yAdjust, wAdjust, hAdjust);
    }

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
     * <p>The NULL state is a special state in the model-based approach that represents
     * GUI elements not associated with any specific state. This is useful for defining
     * regions that exist across multiple states.</p>
     * 
     * @return a StateRegion owned by the NULL state
     */
    @JsonIgnore
    public StateRegion inNullState() {
        return new StateRegion.Builder()
                .setOwnerStateName("null")
                .setSearchRegion(this)
                .build();
    }

    /**
     * Checks if this Region has valid dimensions.
     * 
     * <p>A Region is considered defined if it has positive width and height.
     * This is important for validating search areas and interaction targets.</p>
     * 
     * @return true if the Region has positive width and height, false otherwise
     */
    @JsonIgnore
    public boolean isDefined() {
        return RegionUtils.isDefined(this);
    }

    // Instance Methods
    public boolean overlaps(Region r) {
        return RegionUtils.overlaps(this, r);
    }

    public boolean contains(Region r) {
        return RegionUtils.contains(this, r);
    }

    public boolean contains(Rect rect) {
        return RegionUtils.contains(this, new Region(rect));
    }

    public boolean contains(Location l) {
        return RegionUtils.containsX(this, l) && RegionUtils.containsY(this, l);
    }

    public int size() {
        return RegionUtils.size(this);
    }

    @JsonIgnore
    public Optional<Integer> getGridNumber(Location location) {
        return RegionUtils.getGridNumber(this, location);
    }

    @JsonIgnore
    public Region getGridRegion(int gridNumber) {
        if (gridNumber == -1) return this;
        org.sikuli.script.Region sikuliRegion = sikuli();
        if (sikuliRegion == null) {
            // In headless mode, calculate grid region manually
            int rows = 3; // default grid size
            int cols = 3;
            int cellWidth = w / cols;
            int cellHeight = h / rows;
            int row = RegionUtils.toRow(this, gridNumber);
            int col = RegionUtils.toCol(this, gridNumber);
            return new Region(x + col * cellWidth, y + row * cellHeight, cellWidth, cellHeight);
        }
        return new Region(sikuliRegion.getCell(RegionUtils.toRow(this, gridNumber), RegionUtils.toCol(this, gridNumber)));
    }

    @JsonIgnore
    public Optional<Region> getGridRegion(Location location) {
        Optional<Integer> gridNumber = getGridNumber(location);
        return gridNumber.map(this::getGridRegion);
    }

    @Override
    public String toString() {
        return "R[" + x + "." + y + "." + w + "." + h + "]";
    }

    public boolean overlaps(Rect rect) {
        return RegionUtils.overlaps(this, rect);
    }

    public boolean contains(Match m) {
        return contains(new Region(m));
    }

    @JsonIgnore
    public boolean contains(org.sikuli.script.Match m) {
        return contains(new Region(m));
    }

    @JsonIgnore
    public boolean contains(org.sikuli.script.Location l) {
        return new Rectangle(x, y, w, h).contains(l.x, l.y);
    }

    @JsonIgnore
    public boolean containsX(org.sikuli.script.Location l) {
        return l.getX() >= x() && l.getX() <= x() + w();
    }

    @JsonIgnore
    public boolean containsY(org.sikuli.script.Location l) {
        return l.getY() >= y() && l.getY() <= y() + h();
    }

    @JsonIgnore
    public Match toMatch() {
        return new Match(this);
    }

    /**
     * Divides this Region into a grid and returns all grid cells as separate Regions.
     * 
     * <p>This method is useful for systematic searching or interaction with GUI elements
     * arranged in a grid pattern. The cells are returned in reading order (left to right,
     * top to bottom).</p>
     * 
     * <p>Note: Uses SikuliX raster with minimum cell size of 5x5 pixels. For finer
     * control, use the Brobot Grid classes.</p>
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

    public void print() {
        System.out.println("region: XYWH="+x()+"."+y()+"."+w()+"."+h()+"| ");
    }

    @JsonIgnore
    public ObjectCollection asObjectCollection() {
        return new ObjectCollection.Builder()
                .withRegions(this)
                .build();
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
     * <p>This method is useful in the model-based approach for defining search areas
     * that exclude certain GUI elements, or for finding free space around objects.</p>
     * 
     * <p>Only the non-overlapping areas of this Region are returned; non-overlapping
     * areas of the parameter Region are not included.</p>
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