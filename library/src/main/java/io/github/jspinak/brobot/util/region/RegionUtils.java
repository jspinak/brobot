package io.github.jspinak.brobot.util.region;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.RegionBuilder;
import io.github.jspinak.brobot.model.match.Match;

import org.bytedeco.opencv.opencv_core.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Comprehensive utility methods for Region manipulation and analysis in Brobot.
 * 
 * <p>RegionUtils provides a rich set of static methods for working with Region objects, 
 * including spatial calculations, transformations, comparisons, and grid operations. 
 * These utilities form the foundation for complex region-based operations throughout 
 * the framework, enabling sophisticated spatial reasoning about GUI elements.</p>
 * 
 * <p>Operation categories:
 * <ul>
 *   <li><b>Conversions</b>: Transform between Region, Match, Location, and Rect types</li>
 *   <li><b>Spatial Analysis</b>: Overlap detection, containment checks, bounding boxes</li>
 *   <li><b>Boundary Operations</b>: Adjust edges, calculate endpoints, union regions</li>
 *   <li><b>Grid Operations</b>: Divide regions into grids, map locations to cells</li>
 *   <li><b>Set Operations</b>: Subtract regions, merge adjacent areas, find intersections</li>
 * </ul>
 * </p>
 * 
 * <p>Key spatial methods:
 * <ul>
 *   <li>{@code overlaps()}: Detect if two regions share any area</li>
 *   <li>{@code contains()}: Check if one region fully contains another</li>
 *   <li>{@code getOverlappingRegion()}: Find the intersection of two regions</li>
 *   <li>{@code getUnion()}: Create smallest region containing both inputs</li>
 *   <li>{@code minus()}: Subtract one region from another</li>
 * </ul>
 * </p>
 * 
 * <p>Grid functionality:
 * <ul>
 *   <li>Divide regions into row/column grids for systematic processing</li>
 *   <li>Map locations to grid cell numbers</li>
 *   <li>Convert between grid coordinates and cell indices</li>
 *   <li>Support for SikuliX raster operations (minimum 5x5 cells)</li>
 * </ul>
 * </p>
 * 
 * <p>Advanced operations:
 * <ul>
 *   <li><b>Region Merging</b>: Combine adjacent regions to simplify collections</li>
 *   <li><b>Boundary Points</b>: Extract significant x/y coordinates for subdivision</li>
 *   <li><b>Random Locations</b>: Generate random points within regions for testing</li>
 *   <li><b>Definition Checking</b>: Validate regions have meaningful dimensions</li>
 * </ul>
 * </p>
 * 
 * <p>Integration notes:
 * <ul>
 *   <li>Works with both Brobot and SikuliX region representations</li>
 *   <li>Supports JavaCV Rect for OpenCV operations</li>
 *   <li>Handles edge cases like zero-sized or screen-sized regions</li>
 *   <li>Thread-safe through stateless design</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, RegionUtils enables sophisticated spatial reasoning 
 * about GUI elements. These utilities are essential for implementing features like 
 * smart search areas, collision detection, layout analysis, and dynamic region 
 * adjustment based on screen content.</p>
 * 
 * @since 1.0
 * @see Region
 * @see Location
 * @see Match
 * @see SearchRegions
 */
public class RegionUtils {

    public static Region convertMatchToRegion(Match match) {
        return new Region(match.x(), match.y(), match.w(), match.h());
    }

    public static int[] extractRegionFromSikuli(org.sikuli.script.Match match) {
        return new int[] { match.x, match.y, match.w, match.h };
    }

    public static int[] calculateBoundingBox(Location loc1, Location loc2) {
        int x = Math.min(loc1.getCalculatedX(), loc2.getCalculatedX());
        int y = Math.min(loc1.getCalculatedY(), loc2.getCalculatedY());
        int x2 = Math.max(loc1.getCalculatedX(), loc2.getCalculatedX());
        int y2 = Math.max(loc1.getCalculatedY(), loc2.getCalculatedY());
        return new int[] { x, y, x2 - x, y2 - y };
    }

    public static int x2(Region region) {
        return region.getX() + region.getW();
    }

    public static int y2(Region region) {
        return region.getY() + region.getH();
    }

    /**
     * Sets x2 with respect to x and not to w (adjusts the width)
     * @param x2 the right boundary of the region
     */
    public static void adjustX2(Region region, int x2) {
        region.setW(x2 - region.getX());
    }

    /**
     * Sets y2 with respect to y and not to h (adjusts the height)
     * @param y2 the lower boundary of the region
     */
    public static void adjustY2(Region region, int y2) {
        region.setH(y2 - region.getY());
    }

    public static void adjustX(Region region, int newX) {
        int xDiff = newX - region.getX();
        region.setX(newX);
        region.setW(region.getW() - xDiff);
    }

    public static void adjustY(Region region, int newY) {
        int yDiff = newY - region.getY();
        region.setY(newY);
        region.setH(region.getH() - yDiff);
    }

    public static boolean isDefined(Region region) {
        // A region with no area (width or height is zero or less) is not defined.
        if (region.getW() <= 0 || region.getH() <= 0) {
            return false;
        }

        // A region matching the full screen dimensions is the default, and thus not "defined"
        // in the sense of being a specific, user-set region.
        int defaultWidth = Integer.parseInt(System.getenv().getOrDefault("SCREEN_WIDTH", "1920"));
        int defaultHeight = Integer.parseInt(System.getenv().getOrDefault("SCREEN_HEIGHT", "1080"));
        if (region.getX() == 0 && region.getY() == 0 &&
                region.getW() == defaultWidth && region.getH() == defaultHeight) {
            return false;
        }

        // Otherwise, the region is considered to be defined.
        return true;
    }

    public static int size(Region region) {
        return region.getW() * region.getH();
    }

    public static int compareRegions(Region r1, Region r2) {
        int yDiff = r1.getY() - r2.getY();
        int xDiff = r1.getX() - r2.getX();
        return (yDiff != 0) ? yDiff : xDiff;
    }

    // Spatial & Comparison Methods
    public static boolean overlaps(Region r1, Region r2) {
        return getOverlappingRegion(r1, r2).isPresent();
    }

    public static boolean overlaps(Region region, Rect rect) {
        return getOverlappingRect(region.getJavaCVRect(), rect).isPresent();
    }

    public static Optional<Region> getOverlappingRegion(Region r1, Region r2) {
        Optional<Rect> rect = getOverlappingRect(r1.getJavaCVRect(), r2.getJavaCVRect());
        return rect.map(Region::new);
    }

    public static Optional<Rect> getOverlappingRect(Rect rect1, Rect rect2) {
        int x = Math.max(rect1.x(), rect2.x());
        int y = Math.max(rect1.y(), rect2.y());
        int x2 = Math.min(rect1.x() + rect1.width(), rect2.x() + rect2.width());
        int y2 = Math.min(rect1.y() + rect1.height(), rect2.y() + rect2.height());
        if (x2 <= x || y2 <= y) return Optional.empty();
        return Optional.of(new Rect(x, y, x2 - x, y2 - y));
    }

    public static boolean contains(Region outer, Region inner) {
        return contains(outer, new Location(inner.getX(), inner.getY())) &&
               contains(outer, new Location(inner.getX() + inner.getW(), inner.getY())) &&
               contains(outer, new Location(inner.getX(), inner.getY() + inner.getH())) &&
               contains(outer, new Location(inner.getX() + inner.getW(), inner.getY() + inner.getH()));
    }

    public static boolean contains(Rect rect1, Rect rect2) {
        return contains(new Region(rect1), new Region(rect2));
    }

    public static boolean contains(Region region, Location loc) {
        return containsX(region, loc) && containsY(region, loc);
    }

    public static boolean containsX(Region region, Location loc) {
        return loc.getCalculatedX() >= region.getX() && loc.getCalculatedX() <= region.getX() + region.getW();
    }

    public static boolean containsY(Region region, Location loc) {
        return loc.getCalculatedY() >= region.getY() && loc.getCalculatedY() <= region.getY() + region.getH();
    }

    // Grid & Raster Functions
    public static Optional<Integer> getGridNumber(Region region, Location location) {
        if (!contains(region, location)) return Optional.empty();
        if (!region.sikuli().isRasterValid()) return Optional.of(-1);
        Region firstCell = new Region(region.sikuli().getCell(0, 0));
        int row = gridNumber(firstCell.getH(), firstCell.getY(), location.getCalculatedY());
        int col = gridNumber(firstCell.getW(), firstCell.getX(), location.getCalculatedX());
        return Optional.of(toGridNumber(region, row, col));
    }

    public static Optional<Integer> getGridNumber(Region region, org.sikuli.script.Location location) {
        if (!region.contains(location)) return Optional.empty();
        if (!region.sikuli().isRasterValid()) return Optional.of(-1);
        Region firstCell = new Region(region.sikuli().getCell(0, 0));
        int row = gridNumber(firstCell.getH(), firstCell.getY(), location.getY());
        int col = gridNumber(firstCell.getW(), firstCell.getX(), location.getX());
        return Optional.of(toGridNumber(region, row, col));
    }

    public static int gridNumber(int oneGridSize, int regionStart, int locationValue) {
        int distFromEdge = locationValue - regionStart;
        return distFromEdge / oneGridSize;
    }

    public static int toGridNumber(Region region, int row, int col) {
        return row * region.sikuli().getCols() + col;
    }

    public static int toRow(Region region, int gridNumber) {
        return gridNumber / region.sikuli().getCols();
    }

    public static int toCol(Region region, int gridNumber) {
        return gridNumber % region.sikuli().getCols();
    }

    // Spatial Transformations
    public static List<Integer> xPoints(Region a, Region b) {
        // Using a TreeSet to keep the points sorted and unique
        java.util.Set<Integer> points = new java.util.TreeSet<>();
        points.add(a.getX());
        points.add(a.x2());
        // Add the start-point of b if it's inside a
        if (b.getX() > a.getX() && b.getX() < a.x2()) {
            points.add(b.getX());
        }
        // Add the end-point of b if it's inside a
        if (b.x2() > a.getX() && b.x2() < a.x2()) {
            points.add(b.x2());
        }
        return new java.util.ArrayList<>(points);
    }

    public static List<Integer> yPoints(Region a, Region b) {
        // Using a TreeSet to keep the points sorted and unique
        java.util.Set<Integer> points = new java.util.TreeSet<>();
        points.add(a.getY());
        points.add(a.y2());
        // Add the start-point of b if it's inside a
        if (b.getY() > a.getY() && b.getY() < a.y2()) {
            points.add(b.getY());
        }
        // Add the end-point of b if it's inside a
        if (b.y2() > a.getY() && b.y2() < a.y2()) {
            points.add(b.y2());
        }
        return new java.util.ArrayList<>(points);
    }

    // Methods moved from Region class
    public static Location getRandomLocation(Region region) {
        Random randW = new Random();
        Random randH = new Random();
        return new Location(
                region.getX() + randW.nextInt(region.getW()),
                region.getY() + randH.nextInt(region.getH()));
    }

    public static boolean equals(Region r1, Region r2) {
        return r1.getX() == r2.getX() && r1.getY() == r2.getY() &&
                r1.getW() == r2.getW() && r1.getH() == r2.getH();
    }

    public static Rect getJavaCVRect(Region region) {
        return new Rect(region.getX(), region.getY(), region.getW(), region.getH());
    }

    public static Region getUnion(Region r1, Region r2) {
        int x = Math.min(r1.getX(), r2.getX());
        int y = Math.min(r1.getY(), r2.getY());
        int x2 = Math.max(r1.x2(), r2.x2());
        int y2 = Math.max(r1.y2(), r2.y2());
        return new Region(x, y, x2 - x, y2 - y);
    }

    public static Location getLocation(Region region) {
        return new Location(region.sikuli().getTarget().x, region.sikuli().getTarget().y);
    }

    /**
     * Region uses the SikuliX raster (setRaster), which has a min cell size of 5x5.
     * For smaller cell sizes, or more in-depth work with grids, use the
     * Brobot Grid classes.
     * @param rows the y-partitions
     * @param columns the x-partitions
     * @return a list of regions (cells of the grid), added in order of left to right and then up to down
     */
    public static List<Region> getGridRegions(Region region, int rows, int columns) {
        region.sikuli().setRaster(rows, columns); // SikuliX raster (setRaster) has a min cell size of 5x5
        List<Region> regions = new ArrayList<>();
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                regions.add(new Region(region.sikuli().getCell(i,j)));
            }
        }
        return regions;
    }

    /**
     * Finds the areas of region that do not overlap with the parameter region.
     * Non-overlapping areas of the parameter region are not considered.
     * @param a the main region
     * @param b the region that may overlap
     * @return a collection of Region objects comprising the non-overlapping areas.
     */
    public static List<Region> minus(Region a, Region b) {
        Optional<Region> overlapOpt = getOverlappingRegion(a, b);
        if (overlapOpt.isEmpty()) return List.of(a); // no overlap
        Region overlap = overlapOpt.get();
        List<Integer> xPoints = xPoints(a, b); // between 2 and 4 points
        List<Integer> yPoints = yPoints(a, b); // between 2 and 4 points
        List<Region> subRegions = getSubRegions(xPoints, yPoints);
        List<Region> uniqueRegions = removeRegion(subRegions, overlap);
        return mergeAdjacent(uniqueRegions);
    }

    /**
     * Return a list of all subregions created by the x and y points.
     * @param xPoints points along the x-axis
     * @param yPoints points along the y-axis
     * @return all regions created with these points as edges
     */
    public static List<Region> getSubRegions(List<Integer> xPoints, List<Integer> yPoints) {
        if (xPoints.size() < 2 || yPoints.size() < 2) return new ArrayList<>();
        List<Region> subRegions = new ArrayList<>();
        for (int i=0; i<xPoints.size()-1; i++) {
            for (int j=0; j<yPoints.size()-1; j++) {
                int x1 = xPoints.get(i);
                int x2 = xPoints.get(i+1);
                int y1 = yPoints.get(j);
                int y2 = yPoints.get(j+1);
                subRegions.add(new Region(x1, y1, x2-x1, y2-y1));
            }
        }
        return subRegions;
    }

    public static List<Region> removeRegion(List<Region> regions, Region toRemove) {
        List<Region> newRegions = new ArrayList<>();
        regions.forEach(region -> {
            if (!equals(region, toRemove)) newRegions.add(region);
        });
        return newRegions;
    }

    public static List<Region> mergeAdjacent(List<Region> regions) {
        if (regions.size() <= 1) return regions;
        boolean listIsSmaller = true;
        List<Region> merged = regions;
        while (listIsSmaller) {
            List<Region> newMerge = mergeIteration(merged);
            listIsSmaller = newMerge.size() < merged.size();
            merged = newMerge;
        }
        return merged;
    }

    private static List<Region> mergeIteration(List<Region> regions) {
        if (regions.size() <= 1) return regions;
        List<Region> merged = new ArrayList<>();
        Region m = new Region(regions.getFirst());
        for (int i=1; i<=regions.size(); i++) {
            if (i==regions.size()) { // we've checked all regions
                merged.add(m);
                break;
            }
            Region r = regions.get(i);
            boolean horizontalMatch = m.x2() == r.x() && m.y() == r.y() && m.y2() == r.y2();
            boolean verticalMatch = m.y2() == r.y() && m.x() == r.x() && m.x2() == r.x2();
            if (horizontalMatch) m.adjustX2(r.x2());
            else if (verticalMatch) m.adjustY2(r.y2());
            else { // no match
                merged.add(m);
                m = new Region(r);
            }
        }
        return merged;
    }
    
    /**
     * Creates a region scaled from a base screen size to the current screen size.
     * 
     * @param baseRegion the region designed for the base screen
     * @param baseScreenWidth the width of the base screen
     * @param baseScreenHeight the height of the base screen
     * @param currentScreenWidth the width of the current screen
     * @param currentScreenHeight the height of the current screen
     * @return a new region scaled to the current screen
     */
    public static Region scaleToScreen(Region baseRegion, 
                                       int baseScreenWidth, int baseScreenHeight,
                                       int currentScreenWidth, int currentScreenHeight) {
        if (baseRegion == null) return null;
        
        double xScale = (double) currentScreenWidth / baseScreenWidth;
        double yScale = (double) currentScreenHeight / baseScreenHeight;
        
        int newX = (int) Math.round(baseRegion.getX() * xScale);
        int newY = (int) Math.round(baseRegion.getY() * yScale);
        int newWidth = (int) Math.round(baseRegion.getW() * xScale);
        int newHeight = (int) Math.round(baseRegion.getH() * yScale);
        
        return new Region(newX, newY, newWidth, newHeight);
    }
    
    /**
     * Creates a region as a percentage of the screen size.
     * 
     * @param xPercent x position as percentage (0.0 to 1.0)
     * @param yPercent y position as percentage (0.0 to 1.0)
     * @param widthPercent width as percentage (0.0 to 1.0)
     * @param heightPercent height as percentage (0.0 to 1.0)
     * @param screenWidth the screen width
     * @param screenHeight the screen height
     * @return a new region based on screen percentages
     */
    public static Region fromScreenPercentage(double xPercent, double yPercent,
                                             double widthPercent, double heightPercent,
                                             int screenWidth, int screenHeight) {
        int x = (int) Math.round(screenWidth * xPercent);
        int y = (int) Math.round(screenHeight * yPercent);
        int width = (int) Math.round(screenWidth * widthPercent);
        int height = (int) Math.round(screenHeight * heightPercent);
        
        return new Region(x, y, width, height);
    }
    
    /**
     * Creates a centered region with the specified size.
     * 
     * @param width the region width
     * @param height the region height
     * @param screenWidth the screen width
     * @param screenHeight the screen height
     * @return a new region centered on the screen
     */
    public static Region centerOnScreen(int width, int height,
                                       int screenWidth, int screenHeight) {
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;
        return new Region(x, y, width, height);
    }
    
    /**
     * Adjusts a region by the specified offsets.
     * 
     * @param region the region to adjust
     * @param xOffset offset to add to x
     * @param yOffset offset to add to y
     * @param widthOffset offset to add to width
     * @param heightOffset offset to add to height
     * @return a new adjusted region
     */
    public static Region adjust(Region region, int xOffset, int yOffset,
                               int widthOffset, int heightOffset) {
        if (region == null) return null;
        
        return new Region(
            region.getX() + xOffset,
            region.getY() + yOffset,
            Math.max(1, region.getW() + widthOffset),
            Math.max(1, region.getH() + heightOffset)
        );
    }
    
    /**
     * Expands or contracts a region by the specified amount on all sides.
     * 
     * @param region the region to expand
     * @param pixels pixels to expand (negative to contract)
     * @return a new expanded/contracted region
     */
    public static Region expand(Region region, int pixels) {
        return adjust(region, -pixels, -pixels, pixels * 2, pixels * 2);
    }
    
    /**
     * Constrains a region to fit within screen bounds.
     * 
     * @param region the region to constrain
     * @param screenWidth the screen width
     * @param screenHeight the screen height
     * @return a new region constrained to screen bounds
     */
    public static Region constrainToScreen(Region region, int screenWidth, int screenHeight) {
        if (region == null) return null;
        
        int x = Math.max(0, Math.min(region.getX(), screenWidth - region.getW()));
        int y = Math.max(0, Math.min(region.getY(), screenHeight - region.getH()));
        int width = Math.min(region.getW(), screenWidth - x);
        int height = Math.min(region.getH(), screenHeight - y);
        
        return new Region(x, y, Math.max(1, width), Math.max(1, height));
    }
    
    /**
     * Creates a RegionBuilder for fluent region creation.
     * 
     * @return a new RegionBuilder instance
     */
    public static RegionBuilder builder() {
        return new RegionBuilder();
    }
}